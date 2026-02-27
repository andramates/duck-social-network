package service;

import domain.Notification;
import domain.event.RaceEvent;
import domain.user.User;
import repository.NotificationDBRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class NotificationService {

    private final NotificationDBRepository repo;
    private final AtomicLong idGen = new AtomicLong(0);
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    public NotificationService(NotificationDBRepository repo) {
        this.repo = repo;
    }

    public CompletableFuture<List<Notification>> getNotificationsAsync(User user) {
        return CompletableFuture.supplyAsync(
                () -> repo.findByUser(user.getId()),
                executor
        );
    }

//    public void notify(User user, RaceEvent event, String message) {
//
//        Notification n = new Notification(
//                idGen.incrementAndGet(),
//                user.getId(),
//                event.getId(),
//                message,
//                LocalDateTime.now()
//        );
//
//        repo.save(n);
//    }

    public void notifyAsync(User user, RaceEvent event, String message) {
        executor.submit(() ->
                repo.save(new Notification(
                        idGen.incrementAndGet(),
                        user.getId(),
                        event.getId(),
                        message,
                        LocalDateTime.now()
                ))
        );
    }

    public void shutdown() {
        executor.shutdown();
    }

    public List<Notification> getNotifications(User user) {
        return repo.findByUser(user.getId());
    }
}
