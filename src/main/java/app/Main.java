package app;

import domain.Flock;
import domain.Friendship;
import domain.event.Event;
import domain.user.User;
import repository.*;
import service.*;
import ui.Console;
import validation.*;

public class Main {

    public static void main(String[] args) {

//        Repository<Long, User> userRepo = new UserFileRepository("src/main/resources/data/users.txt");
        Repository<Long, User> userRepo = new UserDBRepository();
        Validator<User> userValidator = new UserValidator();
        UserService userService = new UserService(userRepo, userValidator);

//        Repository<Long, Friendship> friendshipRepo= new FriendshipFileRepository("src/main/resources/data/friendships.txt", userRepo);
        Repository<Long, Friendship> friendshipRepo = new FriendshipDBRepository(userRepo);
        Validator<Friendship> friendshipValidator = new FriendshipValidator();
        FriendshipService friendshipService = new FriendshipService(friendshipRepo, userRepo, friendshipValidator);

        CommunityService communityService = new CommunityService(userRepo, friendshipRepo);

//        Repository<Long, Flock> flockRepo = new FlockFileRepository("src/main/resources/data/flocks.txt", userRepo);
        Repository<Long, Flock> flockRepo = new FlockDBRepository(userRepo);
        Validator<Flock> flockValidator = new FlockValidator();
        FlockService flockService = new FlockService(flockRepo, flockValidator);

//        Repository<Long, Event> eventRepo = new EventFileRepository("src/main/resources/data/events.txt");
        Repository<Long, Event> eventRepo = new EventDBRepository(userRepo);
        Validator<Event> eventValidator = new EventValidator();
        EventService eventService = new EventService(eventRepo, eventValidator);

        NetworkService networkService = new NetworkService(
                userService, friendshipService, communityService, flockService, eventService
        );

        new Console(networkService).run();
    }
}
