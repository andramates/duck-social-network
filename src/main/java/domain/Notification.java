package domain;

import java.time.LocalDateTime;

public class Notification {

    private final Long id;
    private final Long userId;
    private final Long eventId;
    private final String message;
    private final LocalDateTime createdAt;

    public Notification(Long id,
                        Long userId,
                        Long eventId,
                        String message,
                        LocalDateTime createdAt) {

        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.message = message;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getEventId() { return eventId; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return message;
    }
}
