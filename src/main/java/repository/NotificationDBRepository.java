package repository;

import db.DatabaseManager;
import domain.Notification;
import exceptions.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDBRepository {

    public Notification save(Notification n) {

        String sql = """
            INSERT INTO user_notifications(user_id, event_id, message, created_at)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, n.getUserId());
            ps.setLong(2, n.getEventId()); // 🔥 OBLIGATORIU
            ps.setString(3, n.getMessage());
            ps.setTimestamp(4, Timestamp.valueOf(n.getCreatedAt()));

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return new Notification(
                        rs.getLong(1),
                        n.getUserId(),
                        n.getEventId(),
                        n.getMessage(),
                        n.getCreatedAt()
                );
            }

            return n;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }

    public List<Notification> findByUser(Long userId) {

        String sql = """
            SELECT * FROM user_notifications
            WHERE user_id = ?
            ORDER BY created_at
        """;

        List<Notification> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Notification(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getLong("event_id"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }

            return list;

        } catch (SQLException e) {
            throw new RepositoryException(e.getMessage());
        }
    }
}
