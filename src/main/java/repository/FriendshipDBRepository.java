package repository;

import db.DatabaseManager;
import domain.Friendship;
import domain.user.User;
import repository.Repository;
import exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDBRepository implements Repository<Long, Friendship> {

    private final Repository<Long, User> userRepo;

    public FriendshipDBRepository(Repository<Long, User> userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Friendship save(Friendship f) {
        String sql = """
                INSERT INTO friendships(id, user1_id, user2_id, since)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, f.getId());
            ps.setLong(2, f.getUser1().getId());
            ps.setLong(3, f.getUser2().getId());
            ps.setTimestamp(4, Timestamp.valueOf(f.getSince()));

            ps.executeUpdate();
            return f;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public Friendship findById(Long id) {
        String sql = "SELECT * FROM friendships WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new RepositoryException("Friendship not found: " + id);

            User u1 = userRepo.findById(rs.getLong("user1_id"));
            User u2 = userRepo.findById(rs.getLong("user2_id"));
            LocalDateTime since = rs.getTimestamp("since").toLocalDateTime();

            return new Friendship(id, u1, u2, since);

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public List<Friendship> findAll() {
        List<Friendship> list = new ArrayList<>();
        String sql = "SELECT * FROM friendships";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("id");
                User u1 = userRepo.findById(rs.getLong("user1_id"));
                User u2 = userRepo.findById(rs.getLong("user2_id"));
                LocalDateTime since = rs.getTimestamp("since").toLocalDateTime();

                list.add(new Friendship(id, u1, u2, since));
            }

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }

        return list;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM friendships WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM friendships WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    @Override
    public Friendship update(Friendship f) {
        deleteById(f.getId());
        return save(f);
    }
}
