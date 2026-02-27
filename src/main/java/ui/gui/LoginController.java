package ui.gui;

import domain.user.User;
import exceptions.ValidationException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import service.*;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private UserService userService;
    private FriendshipService friendshipService;
    private CommunityService communityService;
    private MessageService messageService;
    private FriendRequestService friendRequestService;
    private EventService eventService;
    private NetworkService networkService;
    private NotificationService notificationService;

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

    @FXML
    private void onLogin() {
        errorLabel.setVisible(false);

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            User loggedUser = userService.authenticate(username, password);
            openMainMenu(loggedUser);

        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private void openMainMenu(User loggedUser) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/mainMenu.fxml"));
            Scene scene = new Scene(loader.load(), 800, 500);

            MainMenuController ctrl = loader.getController();
            ctrl.setServices(userService, friendshipService, communityService, messageService,friendRequestService,
                    eventService, networkService, notificationService);
            ctrl.setLoggedUser(loggedUser);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (Exception e) {
            throw new RuntimeException("Cannot load main menu: " + e.getMessage());
        }
    }
}
