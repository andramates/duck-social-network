package repository;

import db.DatabaseManager;
import domain.Message;
import domain.ReplyMessage;
import domain.user.User;
import exceptions.RepositoryException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDBRepository implements Repository<Long, Message> {

    private final Repository<Long, User> userRepo;

    public MessageDBRepository(Repository<Long, User> userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public Message save(Message message) {
        String sql = """
            INSERT INTO messages(id, sender_id, content, timestamp, reply_to)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, message.getId());
            ps.setLong(2, message.getSender().getId());
            ps.setString(3, message.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(message.getTimestamp()));

            if (message.getReply() != null)
                ps.setLong(5, message.getReply().getId());
            else
                ps.setNull(5, Types.BIGINT);

            ps.executeUpdate();

            saveReceivers(conn, message);

            return message;

        } catch (SQLException e) {
            throw new RepositoryException("DB Message save error: " + e.getMessage());
        }
    }

    private void saveReceivers(Connection conn, Message message) throws SQLException {
        String sql = "INSERT INTO message_receivers(message_id, receiver_id) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        for (User u : message.getReceivers()) {
            ps.setLong(1, message.getId());
            ps.setLong(2, u.getId());
            ps.addBatch();
        }
        ps.executeBatch();
    }

    @Override
    public Message findById(Long id) {
        String sql = "SELECT * FROM messages WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                throw new RepositoryException("Message not found: " + id);

            return readMessage(conn, rs);

        } catch (SQLException e) {
            throw new RepositoryException("DB Message find error: " + e.getMessage());
        }
    }

    private Message readMessage(Connection conn, ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long senderId = rs.getLong("sender_id");
        String content = rs.getString("content");
        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();

        Long replyTo = rs.getObject("reply_to", Long.class);

        User sender = userRepo.findById(senderId);
        List<User> receivers = loadReceivers(conn, id);

        Message reply = null;
        if (replyTo != null)
            reply = findById(replyTo);

        if (reply != null) {
            return new ReplyMessage(id, sender, receivers, content, timestamp, reply);
        }

        return new Message(id, sender, receivers, content, timestamp, null);
    }


    private List<User> loadReceivers(Connection conn, long messageId) throws SQLException {
        List<User> list = new ArrayList<>();

        String sql = "SELECT receiver_id FROM message_receivers WHERE message_id=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setLong(1, messageId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            long uid = rs.getLong("receiver_id");
            User u = userRepo.findById(uid);
            list.add(u);
        }

        return list;
    }

    @Override
    public List<Message> findAll() {
        List<Message> list = new ArrayList<>();

        String sql = "SELECT * FROM messages ORDER BY timestamp ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next())
                list.add(readMessage(conn, rs));

            return list;

        } catch (SQLException e) {
            throw new RepositoryException("DB Message findAll error: " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        try (Connection conn = DatabaseManager.getConnection()) {

            PreparedStatement ps1 = conn.prepareStatement("DELETE FROM message_receivers WHERE message_id=?");
            ps1.setLong(1, id);
            ps1.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM messages WHERE id=?");
            ps2.setLong(1, id);
            ps2.executeUpdate();

        } catch (SQLException e) {
            throw new RepositoryException("DB Message delete error: " + e.getMessage());
        }
    }

    @Override
    public boolean existsById(Long id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM messages WHERE id=?")) {

            ps.setLong(1, id);
            return ps.executeQuery().next();

        } catch (SQLException e) {
            throw new RepositoryException("DB Message exists error: " + e.getMessage());
        }
    }

    @Override
    public Message update(Message message) {
        deleteById(message.getId());
        return save(message);
    }


    public List<Message> loadConversation(long userA, long userB) {
        String sql = """
            SELECT DISTINCT m.*
            FROM messages m
            JOIN message_receivers r ON m.id = r.message_id
            WHERE (m.sender_id=? AND r.receiver_id=?)
               OR (m.sender_id=? AND r.receiver_id=?)
            ORDER BY m.timestamp ASC
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userA);
            ps.setLong(2, userB);
            ps.setLong(3, userB);
            ps.setLong(4, userA);

            ResultSet rs = ps.executeQuery();
            List<Message> list = new ArrayList<>();

            while (rs.next()) {
                list.add(readMessage(conn, rs));
            }

            return list;

        } catch (SQLException e) {
            throw new RepositoryException("DB loadConversation error: " + e.getMessage());
        }
    }
}
