package repository;

import db.DatabaseManager;
import domain.Flock;
import domain.user.Duck;
import domain.user.User;
import exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FlockDBRepository implements Repository<Long, Flock> {

    private final Repository<Long, User> userRepo;

    public FlockDBRepository(Repository<Long, User> userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Flock save(Flock flock) {
        String sql = "INSERT INTO flocks(id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, flock.getId());
            ps.setString(2, flock.getName());
            ps.executeUpdate();

            insertMembers(conn, flock);

            return flock;

        } catch (SQLException e) {
            throw new RepositoryException("DB insert flock error: " + e.getMessage());
        }
    }


    @Override
    public Flock findById(Long id) {
        String sql = "SELECT * FROM flocks WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) throw new RepositoryException("No flock found with id: " + id);

            Flock flock = new Flock(id, rs.getString("name"));

            loadMembers(conn, flock);

            return flock;

        } catch (SQLException e) {
            throw new RepositoryException("DB findById flock error: " + e.getMessage());
        }
    }


    @Override
    public List<Flock> findAll() {
        List<Flock> list = new ArrayList<>();
        String sql = "SELECT * FROM flocks ORDER BY id";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");

                Flock flock = new Flock(id, name);
                loadMembers(conn, flock);

                list.add(flock);
            }

            return list;

        } catch (SQLException e) {
            throw new RepositoryException("DB findAll flocks error: " + e.getMessage());
        }
    }


    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM flocks WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("DB delete flock error: " + e.getMessage());
        }
    }


    @Override
    public Flock update(Flock flock) {
        deleteById(flock.getId());
        return save(flock);
    }


    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM flocks WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RepositoryException("DB existsById flock error: " + e.getMessage());
        }
    }



    private void loadMembers(Connection conn, Flock flock) throws SQLException {
        String sql = "SELECT duck_id FROM flock_members WHERE flock_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, flock.getId());
            ResultSet rs = ps.executeQuery();

            List<Duck> members = new ArrayList<>();

            while (rs.next()) {
                Long duckId = rs.getLong("duck_id");
                User u = userRepo.findById(duckId);

                if (u instanceof Duck d) {
                    d.setFlock(flock);
                    members.add(d);
                }
            }

            flock.setMembers(members);
        }
    }


    private void insertMembers(Connection conn, Flock flock) throws SQLException {
        String sql = "INSERT INTO flock_members(flock_id, duck_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Duck d : flock.getMembers()) {
                ps.setLong(1, flock.getId());
                ps.setLong(2, d.getId());
                ps.executeUpdate();
            }
        }
    }
}
