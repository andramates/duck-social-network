package domain.event;

import domain.Entity;
import domain.Message;
import domain.user.User;

import java.time.LocalDateTime;
import java.util.*;
import observer.Subject;
import observer.Observer;

public class Event extends Entity<Long> implements Subject {
    protected final String title;
    protected final String description;
    protected final LocalDateTime createdAt;

    protected final List<Observer> observers = new ArrayList<>();

    public Event(Long id, String title, String description, LocalDateTime createdAt) {
        super(id);
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<Observer> getObservers() {
        return observers;
    }

    @Override
    public void subscribe(Observer o) {
        if (o != null && !observers.contains(o)) observers.add(o);
    }

    @Override
    public void unsubscribe(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers(String message) {
        for (Observer o : observers) {
            o.update(title, message);
        }
    }
}

