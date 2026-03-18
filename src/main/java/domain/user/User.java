package domain.user;

import domain.Entity;
import domain.Message;
import observer.Observer;

import java.util.*;

public abstract class User extends Entity<Long> implements Observer {
    protected final String username;
    protected final String email;
    protected final String password;

    protected final Set<Long> friends = new HashSet<>();
    protected final List<String> eventsLog = new ArrayList<>();

    protected User(Long id, String username, String email, String password) {
        super(id);
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }

    public Set<Long> getFriends() {
        return friends;
    }
    public List<String> getEventsLog() {
        return eventsLog;
    }

    public void login() {
        eventsLog.add("[LOGIN] " + username + " at ");
    }

    public void logout() {
        eventsLog.add("[LOGOUT] " + username + " at ");
    }

//    public void sendMessage(Message m) {
//        if (m == null) return;
//        eventsLog.add("[SEND] to " + m.getReceiver().getUsername() + " at " + m.getTimestamp());
//    }
//
//    public void receiveMessage(Message m) {
//        if (m == null) return;
//        eventsLog.add("[RECEIVE] " + m.getReceiver().getUsername() + " at " + m.getTimestamp());
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public void update(String eventTitle, String message) {
        eventsLog.add("[EVENT][" + eventTitle + "] " + message);
    }

    @Override
    public String toString() {
        return username;
    }
}

