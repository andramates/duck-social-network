package ui.gui;

import domain.user.Duck;
import domain.user.Person;
import domain.user.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import service.EventService;
import service.FriendshipService;

public class ProfilePageController {

    @FXML private Label lblUsername;
    @FXML private Label lblEmail;
    @FXML private Label lblFriends;
    @FXML private Label lblType;
    @FXML private ImageView profileImage;
    @FXML private Label lblSubscriptions;

    private User user;
    private FriendshipService friendshipService;
    private EventService eventService;

    public void init(User user, FriendshipService friendshipService, EventService eventService) {
        this.user = user;
        this.friendshipService = friendshipService;
        this.eventService = eventService;
        loadProfile();
    }

    private void loadProfile() {
        lblUsername.setText(user.getUsername());
        lblEmail.setText(user.getEmail());
        lblType.setText(user.getClass().getSimpleName());

        int friendsCount = friendshipService.countFriends(user.getId());
        lblFriends.setText(friendsCount + " friends");

        long subs = eventService.countSubscriptions(user);
        lblSubscriptions.setText(subs + " subscriptions");


        Image avatar = null;

        if (user instanceof Person) {
            avatar = new Image(
                    getClass().getResource("/images/person.png").toExternalForm()
            );
        }

        if (user instanceof Duck) {
            avatar = new Image(
                    getClass().getResource("/images/duck.png").toExternalForm()
            );
        }

        profileImage.setImage(avatar);


    }
}
