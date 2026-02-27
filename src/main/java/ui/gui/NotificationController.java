package ui.gui;

import domain.Notification;
import domain.user.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import service.NotificationService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationController {

    @FXML
    private ListView<Notification> list;

    private final ObservableList<Notification> model =
            FXCollections.observableArrayList();


    private NotificationService service;
    private User loggedUser;


    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();



    public void init(NotificationService service, User user) {
        this.service = service;
        this.loggedUser = user;
//        load();
        list.setItems(model);
        startMonitoring();
    }

//    private void load() {
//        list.getItems().setAll(
//                service.getNotifications(loggedUser)
//                        .stream()
//                        .map(Notification::toString)
//                        .toList()
//        );
//    }

//    private void startMonitoring() {
//        running = true;
//
//        monitor = new Thread(() -> {
//            int last = service.getNotifications(loggedUser).size();
//
//            while (running) {
//                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
//
//                int now = service.getNotifications(loggedUser).size();
//                if (now != last) {
//                    last = now;
//                    Platform.runLater(this::load);
//                }
//            }
//        });
//
//        monitor.setDaemon(true);
//        monitor.start();
//    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {

            service.getNotificationsAsync(loggedUser)
                    .thenAccept(list ->
                            Platform.runLater(() ->
                                    model.setAll(list)
                            ));

        }, 0, 1, TimeUnit.SECONDS);
    }

    public void onClose() {
        scheduler.shutdown();
    }
}

