package repository;

import db.DatabaseManager;
import domain.event.*;
import domain.user.User;
import exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventDBRepository implements Repository<Long, Event> {

    private final Repository<Long, User> userRepo;

    public EventDBRepository(Repository<Long, User> userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public Event save(Event e) {
        String sql = """
                INSERT INTO events(id, type, title, description, created_at, number_of_ducks)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, e.getId());
            ps.setString(2, (e instanceof RaceEvent) ? "RACE" : "EVENT");
            ps.setString(3, e.getTitle());
            ps.setString(4, e.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(e.getCreatedAt()));

            if (e instanceof RaceEvent r)
                ps.setInt(6, r.getNumberOfDucks());
            else
                ps.setNull(6, Types.INTEGER);

            ps.executeUpdate();

            if (e instanceof RaceEvent r)
                saveLanes(conn, r);

            saveSubscriptions(conn, e);

            return e;

        } catch (SQLException ex) {
            throw new RepositoryException(ex.getMessage());
        }
    }

    private void saveLanes(Connection conn, RaceEvent r) throws SQLException {
        String sql = "INSERT INTO race_lanes(event_id, lane_index, distance) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        for (Lane lane : r.getLanes()) {
            ps.setLong(1, r.getId());
            ps.setInt(2, lane.getIndex());
            ps.setDouble(3, lane.getDistanta());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    private void saveSubscriptions(Connection conn, Event e) throws SQLException {
        String sql = "INSERT INTO event_subscriptions(event_id, user_id) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        for (var o : e.getObservers()) {
            User u = (User) o;
            ps.setLong(1, e.getId());
            ps.setLong(2, u.getId());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public void addSubscription(long eventId, long userId) {

        String sql = """
        INSERT INTO event_subscriptions(event_id, user_id)
        VALUES (?, ?)
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public void removeSubscription(long eventId, long userId) {

        String sql = """
        DELETE FROM event_subscriptions
        WHERE event_id = ? AND user_id = ?
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.setLong(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }


    public void saveNotification(long userId, long eventId, String message) {
        String sql = """
                INSERT INTO user_notifications(user_id, event_id, message, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setLong(2, eventId);
            ps.setString(3, message);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }


    @Override
    public Event findById(Long id) {

        String sql = "SELECT * FROM events WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new RepositoryException("Event not found: " + id);

            return readEvent(conn, rs);

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    private Event readEvent(Connection conn, ResultSet rs) throws SQLException {

        long id = rs.getLong("id");
        String type = rs.getString("type");

        String title = rs.getString("title");
        String desc = rs.getString("description");
        LocalDateTime created = rs.getTimestamp("created_at").toLocalDateTime();

        Event event;

        if (type.equals("RACE")) {
            int n = rs.getInt("number_of_ducks");
            List<Lane> lanes = loadLanes(conn, id);
            boolean started = rs.getBoolean("started");
            event = new RaceEvent(id, title, desc, created, n, lanes, started);
        } else {
            event = new Event(id, title, desc, created);
        }

        loadSubscriptions(conn, event);

        return event;
    }


    private List<Lane> loadLanes(Connection conn, long eventId) throws SQLException {

        List<Lane> lanes = new ArrayList<>();
        String sql = "SELECT lane_index, distance FROM race_lanes WHERE event_id=? ORDER BY lane_index";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, eventId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lanes.add(new Lane(
                    rs.getInt("lane_index"),
                    rs.getDouble("distance")
            ));
        }
        return lanes;
    }

    private void loadSubscriptions(Connection conn, Event e) throws SQLException {

        String sql = "SELECT user_id FROM event_subscriptions WHERE event_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, e.getId());

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            long userId = rs.getLong("user_id");
            User realUser = userRepo.findById(userId);
            e.subscribe(realUser);
        }
    }


    @Override
    public List<Event> findAll() {

        List<Event> list = new ArrayList<>();

        String sql = "SELECT * FROM events";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                list.add(readEvent(conn, rs));

            return list;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }


    @Override
    public void deleteById(Long id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM events WHERE id=?")) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM events WHERE id=?")) {

            ps.setLong(1, id);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public Event update(Event e) {
        deleteById(e.getId());
        return save(e);
    }

    public void markRaceStarted(Long eventId) {

        String sql = """
        UPDATE events
        SET started = TRUE
        WHERE id = ?
    """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, eventId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

}
