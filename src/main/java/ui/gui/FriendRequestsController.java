package ui.gui;

import domain.FriendRequest;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import service.FriendRequestService;
import service.UserService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FriendRequestsController {

    @FXML private ListView<FriendRequest> receivedList;
    @FXML private ListView<FriendRequest> sentList;
    @FXML private ListView<User> usersList;
    @FXML private TextField searchField;
    @FXML private BorderPane root;

    private FriendRequestService friendRequestService;
    private UserService userService;
    private User loggedUser;

    private List<User> allUsers;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private List<FriendRequest> lastReceived = List.of();
    private List<FriendRequest> lastSent = List.of();
    private List<User> lastUsers = List.of();



    public BorderPane getRoot() { return root; }

    public void init(FriendRequestService service, User loggedUser, UserService userService) {
        this.friendRequestService = service;
        this.loggedUser = loggedUser;
        this.userService = userService;

        loadRequests();
        loadUsers();
        startMonitoring();
    }

    private void loadRequests() {
        receivedList.getItems().setAll(friendRequestService.getReceivedRequests(loggedUser));
        sentList.getItems().setAll(friendRequestService.getSentRequests(loggedUser));

        receivedList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(FriendRequest fr, boolean empty) {
                super.updateItem(fr, empty);
                if (empty || fr == null) {
                    setText(null);
                    return;
                }
                setText("From: " + fr.getFrom().getUsername() + " (" + fr.getStatus().name() + ")");
            }
        });

        sentList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(FriendRequest fr, boolean empty) {
                super.updateItem(fr, empty);
                if (empty || fr == null) {
                    setText(null);
                    return;
                }
                setText("To: " + fr.getTo().getUsername() + " (" + fr.getStatus().name() + ")");
            }
        });
    }

    private void loadUsers() {
        allUsers = userService.findAll().stream()
                .filter(u -> !u.getId().equals(loggedUser.getId()))
                .filter(u ->
                        friendRequestService.getSentRequests(loggedUser).stream()
                                .noneMatch(r -> r.getTo().getId().equals(u.getId()))
                )
                .collect(Collectors.toList());

        usersList.getItems().setAll(allUsers);
    }

    private void refreshUI() {
        loadRequests();
        loadUsers();
    }

    @FXML
    private void onSearch() {
        String text = searchField.getText().toLowerCase().trim();

        List<User> filtered = allUsers.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(text))
                .collect(Collectors.toList());

        usersList.getItems().setAll(filtered);
    }

    @FXML
    private void onSendFriendRequest() {
        User selected = usersList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "No user selected!").show();
            return;
        }

        try {
            friendRequestService.sendRequest(loggedUser, selected);
            new Alert(Alert.AlertType.INFORMATION,
                    "Friend request sent to " + selected.getUsername()).show();

            refreshUI();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    @FXML
    private void onAccept() {
        FriendRequest req = receivedList.getSelectionModel().getSelectedItem();
        if (req == null) return;

        friendRequestService.acceptRequest(req);

        new Alert(Alert.AlertType.INFORMATION,
                "You are now friends with " + req.getFrom().getUsername()).show();

        refreshUI();
    }

    @FXML
    private void onReject() {
        FriendRequest req = receivedList.getSelectionModel().getSelectedItem();
        if (req == null) return;

        friendRequestService.rejectRequest(req);

        new Alert(Alert.AlertType.INFORMATION, "Request rejected.").show();

        refreshUI();
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {

            List<FriendRequest> received =
                    friendRequestService.getReceivedRequests(loggedUser);
            List<FriendRequest> sent =
                    friendRequestService.getSentRequests(loggedUser);
            List<User> users =
                    userService.findAll().stream()
                            .filter(u -> !u.getId().equals(loggedUser.getId()))
                            .filter(u ->
                                    sent.stream()
                                            .noneMatch(r -> r.getTo().getId().equals(u.getId()))
                            )
                            .toList();

            Platform.runLater(() ->
                    sentList.getItems().setAll(sent)
            );


            if (!received.equals(lastReceived)
                    || !sent.equals(lastSent)
                    || !users.equals(lastUsers)) {

                lastReceived = received;
                lastSent = sent;
                lastUsers = users;

                Platform.runLater(() -> {
                    receivedList.getItems().setAll(received);
                    sentList.getItems().setAll(sent);
                    usersList.getItems().setAll(users);
                });
            }

        }, 0, 800, TimeUnit.MILLISECONDS);

    }


    public void stopMonitoring() {
        scheduler.shutdown();
    }

    public void onClose() {
        stopMonitoring();
    }
}
