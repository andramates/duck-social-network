package repository;

import db.DatabaseManager;
import domain.user.*;
import exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserDBRepository implements Repository<Long, User> {

    @Override
    public User save(User user) {

        String sql = """
                INSERT INTO users(id, type, username, email, password,
                                  last_name, first_name, date_of_birth,
                                  occupation, empathy_level,
                                  duck_type, speed, endurance)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, user.getId());
            ps.setString(2, getUserType(user));
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPassword());

            if (user instanceof Person p) {
                ps.setString(6, p.getLastName());
                ps.setString(7, p.getFirstName());
                ps.setObject(8, p.getDateOfBirth());
                ps.setString(9, p.getOccupation());
                ps.setDouble(10, p.getEmpathyLevel());

                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.DOUBLE);
                ps.setNull(13, Types.DOUBLE);

            } else if (user instanceof Duck d) {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.DATE);
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.DOUBLE);

                ps.setString(11, d.getType().name());
                ps.setDouble(12, d.getSpeed());
                ps.setDouble(13, d.getEndurance());
            }

            ps.executeUpdate();
            return user;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public User findById(Long id) {

        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new RepositoryException("User not found: " + id);

            User user = readUser(rs);

            loadNotifications(conn, user);

            return user;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public List<User> findAll() {

        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User u = readUser(rs);
                loadNotifications(conn, u);
                list.add(u);
            }

            return list;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }


    @Override
    public void deleteById(Long id) {
        try (Connection conn = DatabaseManager.getConnection()) {

            PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM users WHERE id=?");
            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public boolean existsById(Long id) {

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE id=?")) {

            ps.setLong(1, id);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public User update(User user) {
        deleteById(user.getId());
        return save(user);
    }

    private String getUserType(User u) {
        if (u instanceof Person) return "PERSON";
        if (u instanceof Duck d) return d.getType().name();
        throw new RepositoryException("Unknown user type");
    }

    private User readUser(ResultSet rs) throws SQLException {

        String type = rs.getString("type");

        if (type.equals("PERSON")) {
            return new Person(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("last_name"),
                    rs.getString("first_name"),
                    rs.getObject("date_of_birth", LocalDate.class),
                    rs.getString("occupation"),
                    rs.getDouble("empathy_level")
            );
        }

        DuckType dt = DuckType.valueOf(type);

        return switch (dt) {
            case SWIMMING -> new SwimmingDuck(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getDouble("speed"),
                    rs.getDouble("endurance"));
            case FLYING -> new FlyingDuck(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getDouble("speed"),
                    rs.getDouble("endurance"));
            case FLYING_AND_SWIMMING -> new FlyingAndSwimmingDuck(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getDouble("speed"),
                    rs.getDouble("endurance"));
        };
    }

    private void loadNotifications(Connection conn, User user) throws SQLException {
        String sql = """
                SELECT message FROM user_notifications
                WHERE user_id = ?
                ORDER BY created_at
                """;

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, user.getId());

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            user.getEventsLog().add(rs.getString("message"));
        }
    }

    @Override
    public List<User> findPage(int page, int size) {
        String sql = "SELECT * FROM users  WHERE type IN ('SWIMMING', 'FLYING', 'FLYING_AND_SWIMMING') ORDER BY id LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, page * size);

            ResultSet rs = ps.executeQuery();

            List<User> list = new ArrayList<>();
            while (rs.next()) {
                User u = readUser(rs);
                list.add(u);
            }
            return list;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public int count() {
        String sql = "SELECT COUNT(*) FROM users  WHERE type IN ('SWIMMING', 'FLYING', 'FLYING_AND_SWIMMING')";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }


}
