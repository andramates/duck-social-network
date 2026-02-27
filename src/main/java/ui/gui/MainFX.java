package ui.gui;

import domain.Flock;
import domain.Friendship;
import domain.event.Event;
import domain.Message;
import domain.user.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import repository.*;
import service.*;
import validation.*;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Repository<Long, User> userRepo = new UserDBRepository();
        Validator<User> userValidator = new UserValidator();
        UserService userService = new UserService(userRepo, userValidator);

        Repository<Long, Friendship> friendshipRepo = new FriendshipDBRepository(userRepo);
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        FriendshipService friendshipService = new FriendshipService(friendshipRepo, userRepo, friendshipValidator);

        CommunityService communityService = new CommunityService(userRepo, friendshipRepo);

        Repository<Long, Flock> flockRepo = new FlockDBRepository(userRepo);
        Validator<Flock> flockValidator = new FlockValidator();
        FlockService flockService = new FlockService(flockRepo, flockValidator);

        Repository<Long, Event> eventRepo = new EventDBRepository(userRepo);
        Validator<Event> eventValidator = new EventValidator();
        EventService eventService = new EventService(eventRepo, eventValidator);

        Validator<Message> messageValidator = new MessageValidator();
        MessageDBRepository messageRepo = new MessageDBRepository(userRepo);
        MessageService messageService = new MessageService(messageRepo, messageValidator);

        FriendRequestDBRepository friendRequestRepo = new FriendRequestDBRepository(userRepo);
        FriendRequestService friendRequestService = new FriendRequestService(friendRequestRepo, userRepo, friendshipService);

        NotificationDBRepository notificationRepo = new NotificationDBRepository();
        NotificationService notificationService = new NotificationService(notificationRepo);

        NetworkService networkService = new NetworkService(userService, friendshipService, communityService, flockService, eventService);

        FXMLLoader loader =
                new FXMLLoader(MainFX.class.getResource("/views/loginView.fxml"));
        Scene scene = new Scene(loader.load(), 500, 350);

        LoginController loginCtrl = loader.getController();
        loginCtrl.setServices(
                userService,
                friendshipService,
                communityService,
                messageService,
                friendRequestService,
                eventService,
                networkService,
                notificationService
        );

        stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
