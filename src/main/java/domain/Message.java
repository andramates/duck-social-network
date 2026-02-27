package domain;

import domain.user.User;
import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long> {

    private final User sender;
    private final List<User> receivers;
    private final String content;
    private final LocalDateTime timestamp;
    private final Message reply;          // null daca nu este reply

    public Message(Long id,
                   User sender,
                   List<User> receivers,
                   String content,
                   LocalDateTime timestamp,
                   Message reply) {

        super(id);
        this.sender = sender;
        this.receivers = receivers;
        this.content = content;
        this.timestamp = timestamp == null ? LocalDateTime.now() : timestamp;
        this.reply = reply;
    }

    public User getSender() {
        return sender;
    }

    public List<User> getReceivers() {
        return receivers;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Message getReply() {
        return reply;
    }

    public boolean isReply() {
        return reply != null;
    }

    @Override
    public String toString() {
        if (reply != null)
            return String.format("[%s] %s → %s (reply to %d): %s",
                    timestamp, sender.getUsername(),
                    receivers, reply.getId(),
                    content);

        return String.format("[%s] %s → %s: %s",
                timestamp, sender.getUsername(),
                receivers, content);
    }
}
