package domain;

import domain.user.User;

import java.time.LocalDateTime;

public class Friendship extends Entity<Long> {
    private User user1;
    private User user2;
    private LocalDateTime since;

    public Friendship(Long id,User user1, User user2) {
        super(id);
        this.user1 = user1;
        this.user2 = user2;
        this.since = LocalDateTime.now();
    }

    public Friendship(Long id, User user1, User user2, LocalDateTime since) {
        super(id);
        this.user1 = user1;
        this.user2 = user2;
        this.since = since;
    }



    public User getUser1() {
        return user1;
    }
    public User getUser2() {
        return user2;
    }
    public LocalDateTime getSince() {
        return since;
    }

    @Override
    public String toString() {
        return String.format("FRIENDSHIP: id: " + id + " " + user1.getUsername()
                + " (id: " + user1.getId() + ")"
                + " and " +
                user2.getUsername()  + " (id: " + user2.getId() + ")"
                + " are friends since: " + since.toLocalDate());
    }
}
