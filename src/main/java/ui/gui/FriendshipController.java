package ui.gui;
import domain.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import service.FriendshipService;
import service.UserService;

import java.util.List;


public class FriendshipController {
    @FXML private ComboBox<User> comboUserA;
    @FXML private ComboBox<User> comboUserB;

    @FXML private ComboBox<User> comboRemoveA;
    @FXML private ComboBox<User> comboRemoveB;

    private UserService userService;
    private FriendshipService friendshipService;



    public void setServices(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userService.findAll();

        comboUserA.getItems().setAll(users);
        comboUserB.getItems().setAll(users);

        comboRemoveA.getItems().setAll(users);
        comboRemoveB.getItems().setAll(users);
    }



    @FXML
    private void addFriendship() {
        User a = comboUserA.getValue();
        User b = comboUserB.getValue();

        try {
            if (a == null || b == null) {
                showError("Select two users!");
                return;
            }

            if (a.getId().equals(b.getId())) {
                showError("A user cannot befriend themselves!");
                return;
            }

            friendshipService.addFriendship(a.getId(), b.getId());
            showInfo("Friendship added!");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }




    @FXML
    private void removeFriendship() {
        User a = comboRemoveA.getValue();
        User b = comboRemoveB.getValue();

        try {
            if (a == null || b == null) {
                showError("Select two users!");
                return;
            }

            if (a.getId().equals(b.getId())) {
                showError("A user cannot unfriend themselves!");
                return;
            }

            friendshipService.removeFriendship(a.getId(), b.getId());
            showInfo("Friendship removed!");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}