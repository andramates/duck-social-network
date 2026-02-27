package service;

import domain.event.Event;
import domain.event.Lane;
import domain.event.RaceEvent;
import domain.event.RaceResult;
import domain.user.Duck;
import domain.user.User;
import exceptions.RepositoryException;
import repository.EventDBRepository;
import repository.Repository;
import validation.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventService {
    private final Repository<Long, Event> eventRepo;
    private final Validator<Event> eventValidator;

    public EventService(Repository<Long, Event> eventRepo,  Validator<Event> eventValidator) {
        this.eventRepo = eventRepo;
        this.eventValidator = eventValidator;
    }

    public RaceEvent addRaceEvent(String title, String description, int numberOfDucks, List<Duck> allDucks, List<Lane> lanes) {
        long id = nextId();
        RaceEvent ev = new RaceEvent(id, title, description, LocalDateTime.now(), numberOfDucks, lanes, false);
        eventValidator.validate(ev);
        eventRepo.save(ev);
        return ev;
    }

    public Event addGenericEvent(String title, String description) {
        long id = nextId();
        Event ev = new Event(id, title, description, LocalDateTime.now());
        eventValidator.validate(ev);
        eventRepo.save(ev);
        return ev;
    }

    public void removeEvent(long id) { eventRepo.deleteById(id); }

    public List<Event> findAll() { return eventRepo.findAll(); }

    public Event findById(long id) { return eventRepo.findById(id); }

    private long nextId() {
        long max = 0;
        for (Event e : eventRepo.findAll()) {
            Long id = e.getId();
            if (id != null && id > max) max = id;
        }
        return max + 1;
    }

    public void subscribeUser(long eventId, User user) {
        Event e = eventRepo.findById(eventId);
        if (e.getObservers().contains(user)) throw new RepositoryException("User is already subscribed");
        e.subscribe(user);
        ((EventDBRepository) eventRepo).addSubscription(eventId, user.getId());
    }

    public void unsubscribeUser(long eventId, User user) {
        Event e = eventRepo.findById(eventId);
        if (!e.getObservers().contains(user)) throw new RepositoryException("User is not subscribed");
        e.unsubscribe(user);
        ((EventDBRepository) eventRepo).removeSubscription(eventId, user.getId());
    }

    public void startRace(RaceEvent ev) {
        ev.start();
        ((EventDBRepository)eventRepo).markRaceStarted(ev.getId());
    }


    public Repository<Long, Event> getRepo() {
        return eventRepo;
    }

    public long countSubscriptions(User user) {
        return eventRepo.findAll().stream()
                .filter(e -> e.getObservers().contains(user))
                .count();
    }
}
