package repository;

import domain.FriendRequest;
import domain.FriendRequest.Status;
import domain.user.User;
import db.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDBRepository implements Repository<Long, FriendRequest> {

    private final Repository<Long, User> userRepo;

    public FriendRequestDBRepository(Repository<Long, User> userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    public FriendRequest save(FriendRequest fr) {
        String sql = "INSERT INTO friend_requests(id, from_user, to_user, status, date) VALUES (?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, fr.getId());
            ps.setLong(2, fr.getFrom().getId());
            ps.setLong(3, fr.getTo().getId());
            ps.setString(4, fr.getStatus().name());
            ps.setTimestamp(5, Timestamp.valueOf(fr.getDate()));

            ps.executeUpdate();
            return fr;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public FriendRequest findById(Long id) {
        String sql = "SELECT * FROM friend_requests WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            User from = userRepo.findById(rs.getLong("from_user"));
            User to = userRepo.findById(rs.getLong("to_user"));
            Status status = Status.valueOf(rs.getString("status"));
            LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();

            return new FriendRequest(id, from, to, status, date);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<FriendRequest> findAll() {
        String sql = "SELECT * FROM friend_requests";
        List<FriendRequest> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                User from = userRepo.findById(rs.getLong("from_user"));
                User to = userRepo.findById(rs.getLong("to_user"));
                Status status = Status.valueOf(rs.getString("status"));
                LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();

                list.add(new FriendRequest(id, from, to, status, date));
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM friend_requests WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean existsById(Long id) {
        return findById(id) != null;
    }


    @Override
    public FriendRequest update(FriendRequest fr) {
        String sql = "UPDATE friend_requests SET status=? WHERE id=?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, fr.getStatus().name());
            ps.setLong(2, fr.getId());
            ps.executeUpdate();

            return fr;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatus(Long id, Status status) {
        FriendRequest fr = findById(id);
        if (fr == null) return;

        fr.setStatus(status);
        update(fr);
    }
}
