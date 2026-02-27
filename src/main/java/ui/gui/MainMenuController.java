package ui.gui;

import domain.user.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import service.*;

public class MainMenuController {

    private UserService userService;
    private FriendshipService friendshipService;
    private CommunityService communityService;
    private MessageService messageService;
    private FriendRequestService friendRequestService;
    private EventService eventService;
    private NetworkService networkService;
    private NotificationService notificationService;

    private User loggedUser;

    @FXML
    private BorderPane root;

    public void setServices(UserService userService,
                            FriendshipService friendshipService,
                            CommunityService communityService,
                            MessageService messageService,
                            FriendRequestService friendRequestService,
                            EventService eventService,
                            NetworkService networkService,
                            NotificationService notificationService) {

        this.userService = userService;
        this.friendshipService = friendshipService;
        this.communityService = communityService;
        this.messageService = messageService;
        this.friendRequestService = friendRequestService;
        this.eventService = eventService;
        this.networkService = networkService;
        this.notificationService = notificationService;
    }

    public void setLoggedUser(User user) {
        this.loggedUser = user;

        var pending = friendRequestService.getPendingRequests(user);
        if (!pending.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "You have " + pending.size() + " pending friend requests!").show();
        }
    }

    /* ===================== GENERIC WINDOW OPENER ===================== */

    private void openWindow(String fxmlPath,
                            String title,
                            javafx.util.Callback<Object, Void> controllerSetup) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            Scene scene = new Scene(loader.load());

            Object controller = loader.getController();
            controllerSetup.call(controller);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================== EXISTING WINDOWS ===================== */

    @FXML
    private void openDucks() {
        openWindow("/views/ducksView.fxml", "Ducks View", controller -> {
            ((DuckController) controller).setService(userService);
            return null;
        });
    }

    @FXML
    private void openUsers() {
        openWindow("/views/usersView.fxml", "Users Manager", controller -> {
            ((UserController) controller).setService(userService);
            return null;
        });
    }

    @FXML
    private void openFriendships() {
        openWindow("/views/friendshipsView.fxml", "Friendships Manager", controller -> {
            ((FriendshipController) controller)
                    .setServices(userService, friendshipService);
            return null;
        });
    }

    @FXML
    private void openCommunities() {
        openWindow("/views/communitiesView.fxml", "Communities", controller -> {
            ((CommunityController) controller).setService(communityService);
            return null;
        });
    }

    @FXML
    private void openChat() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/messageView.fxml"));
            Scene scene = new Scene(loader.load());

            MessageController msgCtrl = loader.getController();
            msgCtrl.init(messageService, userService, loggedUser);

            Stage stage = new Stage();
            stage.setTitle("Chat");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> msgCtrl.onClose());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openFriendRequests() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/friendRequestsView.fxml"));
            Scene scene = new Scene(loader.load());

            FriendRequestsController frCtrl = loader.getController();
            frCtrl.init(friendRequestService, loggedUser, userService);

            Stage stage = new Stage();
            stage.setTitle("Friend Requests");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> frCtrl.stopMonitoring());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================== NEW WINDOWS ===================== */

    @FXML
    private void openRaceEvents() {

        try {
            FXMLLoader loader =  new FXMLLoader(getClass().getResource("/views/raceEventsView.fxml"));
            Scene scene = new Scene(loader.load());

            RaceEventController reCtrl = loader.getController();
            reCtrl.init(eventService, notificationService, networkService, loggedUser);

            Stage stage = new Stage();
            stage.setTitle("Race Events");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> reCtrl.stopMonitoring());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openNotifications() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/notificationsView.fxml"));
            Scene scene = new Scene(loader.load());

            NotificationController notifCtrl = loader.getController();
            notifCtrl.init(notificationService, loggedUser);

            Stage stage = new Stage();
            stage.setTitle("Notifications");
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> notifCtrl.onClose());
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================== LOGOUT ===================== */

    @FXML
    private void onLogout() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/loginView.fxml"));
            Scene loginScene = new Scene(loader.load(), 500, 350);

            LoginController loginController = loader.getController();
            loginController.setServices(
                    userService,
                    friendshipService,
                    communityService,
                    messageService,
                    friendRequestService,
                    eventService,
                    networkService,
                    notificationService
            );

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(loginScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/profileView.fxml"));
            Scene scene = new Scene(loader.load());

            ProfilePageController ctrl = loader.getController();
            ctrl.init(loggedUser, friendshipService, eventService);

            Stage stage = new Stage();
            stage.setTitle("My Profile");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
