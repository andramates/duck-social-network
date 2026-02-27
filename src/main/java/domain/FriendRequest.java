package domain;

import domain.user.User;
import java.time.LocalDateTime;

public class FriendRequest extends Entity<Long> {

    public enum Status { PENDING, APPROVED, REJECTED }

    private final User from;
    private final User to;
    private Status status;
    private final LocalDateTime date;

    public FriendRequest(Long id, User from, User to, Status status, LocalDateTime date) {
        super(id);
        this.from = from;
        this.to = to;
        this.status = status;
        this.date = date;
    }

    public User getFrom() { return from; }
    public User getTo() { return to; }
    public Status getStatus() { return status; }
    public LocalDateTime getDate() { return date; }

    public void setStatus(Status status) { this.status = status; }
}
