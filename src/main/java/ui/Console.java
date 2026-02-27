package ui;

import domain.Flock;
import domain.Friendship;
import domain.event.Event;
import domain.event.Lane;
import domain.event.RaceEvent;
import domain.event.RaceResult;
import domain.user.*;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import observer.Observer;
import repository.EventDBRepository;
import repository.InMemoryRepository;
import repository.Repository;
import service.FriendshipService;
import service.NetworkService;
import service.UserService;
import validation.FriendshipValidator;
import validation.UserValidator;
import validation.Validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Console {

    private final Scanner in = new Scanner(System.in);
    private final NetworkService networkService;

    public Console(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void run() {
        while (true) {
            printMenu();
            System.out.print("> ");
            String cmd = in.nextLine().trim();
            try {
                switch (cmd) {
                    case "1" -> addUser();
                    case "2" -> removeUser();
                    case "3" -> listUsers();
                    case "4" -> addFriendship();
                    case "5" -> removeFriendship();
                    case "6" -> listFriendships();
                    case "7" -> numOfCommunities();
                    case "8" -> mostSociableCommunity();
                    case "9" -> listCommunities();
                    case "10" -> populateNetwork();
                    case "11" -> addFlock();
                    case "12" -> removeFlock();
                    case "13" -> listFlocks();
                    case "14" -> addDuckToFlock();
                    case "15" -> removeDuckFromFlock();
                    case "16" -> addRaceEvent();
                    case "17" -> listEvents();
                    case "18" -> subscribeUserToEvent();
                    case "19" -> unsubscribeUserFromEvent();
                    case "20" -> showFlockAverage();
                    case "21" -> listUserSubscriptions();
                    case "22" -> showUserNotifications();
                    case "23" -> sendEventNotification();
                    case "24" -> startRaceEvent();
                    case "0" -> { System.out.println("done"); return; }
                    default -> System.out.println("invalid command");
                }
            } catch (ValidationException e) {
                System.out.println("VALIDATION ERROR: " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("REPOSITORY ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("""
                1) Add user (PERSON / DUCK)
                2) Remove user
                3) List users
                4) Add friendship
                5) Remove friendship
                6) List friendships
                7) Number of communities
                8) Most sociable community
                9) List communities
                10) Populate network
                11) Add flock
                12) Remove flock
                13) List flocks
                14) Add duck to flock
                15) Remove duck from flock
                16) Add event
                17) List events
                18) Subscribe user to event
                19) Unsubscribe user from event
                20) Show flock performance
                21) List user subscriptions
                22) Show user notifications
                23) Send event notifications
                24) Start race event
                0) Exit
                """);
    }


    private void addUser() {
        String type = askType();                 // PERSON sau DUCK
        String username = askNonEmpty("username");
        String email    = askNonEmpty("email");
        String password = askNonEmpty("password");

        if ("PERSON".equals(type)) {
            String lastName   = askNonEmpty("lastName");
            String firstName  = askNonEmpty("firstName");
            LocalDate dob     = askDate("dateOfBirth (yyyy-MM-dd)");
            String occupation = askNonEmpty("occupation");
            double empathy    = askPositiveDouble("empathyLevel");

            Person p = networkService.getUserService().addPerson(
                    username, email, password,
                    lastName, firstName, dob.toString(), occupation, empathy
            );
            System.out.println("PERSON added: id=" + p.getId() + ", username=" + p.getUsername());

        } else { // DUCK
            DuckType typeEnum = askDuckType("duckType [FLYING|SWIMMING|FLYING_AND_SWIMMING]");
            double speed      = askPositiveDouble("speed (>0)");
            double endurance  = askNonNegativeDouble("endurance (>=0)");

            Duck d = networkService.getUserService().addDuck(
                    username, email, password,
                    typeEnum.name(), speed, endurance
            );
            System.out.println("DUCK added: id=" + d.getId() + ", username=" + d.getUsername());
        }
    }

    private void removeUser() {
        long id = askLong("id");
        networkService.removeUserAndAllFriendships(id);
        System.out.println("user removed: id=" + id);
    }

    private void listUsers() {
        List<User> users = networkService.getUserService().findAll();
        if (users.isEmpty()) {
            System.out.println("(no users)");
            return;
        }
        users.stream()
                .sorted(Comparator.comparing(User::getId))
                .forEach(System.out::println);
    }

    private void addFriendship() {
        long user1Id = askLong("user 1 id");
        long user2Id = askLong("user 2 id");
        networkService.getFriendshipService().addFriendship(user1Id, user2Id);
        System.out.println("FRIENDSHIP added: user1 id=" + user1Id + ", user2=" + user2Id);
    }

    private void removeFriendship() {
        long user1Id = askLong("user 1 id");
        long user2Id = askLong("user 2 id");
        networkService.getFriendshipService().removeFriendship(user1Id, user2Id);
        System.out.println("FRIENDSHIP removed: user1 id=" + user1Id + ", user2 id=" + user2Id);
    }

    private void listFriendships() {
        List<Friendship> friendships = networkService.getFriendshipService().findAll();
        if (friendships.isEmpty()) {
            System.out.println("(no friendships)");
            return;
        }
        friendships.stream()
                .sorted(Comparator.comparing(Friendship::getId))
                .forEach(System.out::println);
    }

    private void numOfCommunities() {
        System.out.println("NUMBER OF COMMUNITIES: " + networkService.getCommunityService().numberOfCommunities());
    }

    private void mostSociableCommunity() {
        List<User> users = networkService.getCommunityService().mostSociableCommunity();
        if (users.isEmpty()) {
            System.out.println("(no most sociable community)");
            return;
        }
        users.forEach(System.out::println);
    }

    private void listCommunities() {
        List<List<User>> communities = networkService.getCommunityService().findAllCommunities();
        if (communities.isEmpty()) {
            System.out.println("(no communities)");
            return;
        }
        int index = 1;
        for (List<User> community : communities) {
            System.out.println("COMMUNITY " + index + " (" + community.size() + " members):");
            for (User u : community) {
                if (u instanceof Person p) {
                    System.out.println(p);
                } else if (u instanceof Duck d) {
                    System.out.println(d);
                }
            }
            System.out.println();
            index++;
        }
    }


    private void addFlock() {
        String name = askNonEmpty("Flock name");
        networkService.getFlockService().addFlock(name);
        System.out.println("FLOCK added: name=" + name);
    }


    private void removeFlock() {
        long id = askLong("Flock ID to remove");
        networkService.removeFlock(id);
        System.out.println("FLOCK removed: id=" + id);

    }


    private void listFlocks() {
        List<Flock> flocks = networkService.getFlockService().findAll();
        if (flocks.isEmpty()) {
            System.out.println("(no flocks)");
            return;
        }

        for (Flock f : flocks) {
            System.out.println("FLOCK: " + f.getId() + ": " + f.getName());
            if (f.getMembers().isEmpty()) {
                System.out.println("   (no ducks)");
            } else {
                for (Duck d : f.getMembers()) {
                    System.out.println("   DUCK " + d.getUsername() + " [" + d.getType() + "]");
                }
            }
            System.out.println();
        }
    }

    private void addDuckToFlock() {
        long flockId = askLong("Flock ID");
        long duckId = askLong("Duck ID to add");

        networkService.addDuckToFlock(flockId, duckId);
        System.out.println("DUCK added to FLOCK: flockId=" + flockId + ", duckId=" + duckId);
    }


    private void removeDuckFromFlock() {
        long flockId = askLong("Flock ID");
        long duckId = askLong("Duck ID to remove");

        networkService.removeDuckFromFlock(flockId, duckId);
        System.out.println("DUCK removed from FLOCK: " + flockId + ", duckId=" + duckId);
    }


    private void addRaceEvent() {
        String title = askNonEmpty("Title");
        String desc  = askNonEmpty("Description");
        int m        = askPositiveInt("Number of swimming ducks (M)");

        List<Lane> lanes = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            double dist = askPositiveDouble("Distance for lane " + (i+1));
            lanes.add(new Lane(i + 1, dist));
        }

        RaceEvent ev = networkService.addRaceEvent(title, desc, m, lanes);
        System.out.println("RACE EVENT added: " + "id=" + ev.getId() + ", title=" + ev.getTitle());

        if (ev.getParticipants() != null && !ev.getParticipants().isEmpty()) {
            System.out.println("Participants:");
            ev.getParticipants().forEach(d ->
                    System.out.println("  - Duck " + d.getId() + " (" + d.getUsername() + "), v=" + d.getSpeed() + ", r=" + d.getEndurance()));
        }


    }

    private void listEvents() {
        List<Event> events = networkService.getEventService().findAll();
        if (events.isEmpty()) {
            System.out.println("(no events)");
            return;
        }

        for (Event ev : events) {
            System.out.println("EVENT: " + ev.getId() + ", title: " + ev.getTitle() + ", description: " + ev.getDescription() +
                    ", created at: " + ev.getCreatedAt());

//            if (ev instanceof domain.event.RaceEvent r) {
//                RaceEvent rv = (RaceEvent) ev;
//                rv.selectParticipants(networkService.getUserService().findAllDucks());
//                if (rv.getParticipants() != null && !rv.getParticipants().isEmpty()) {
//                    System.out.println("Participants:");
//                    rv.getParticipants().forEach(d ->
//                            System.out.println("  - Duck " + d.getId() + " (" + d.getUsername() + "), v=" + d.getSpeed() + ", r=" + d.getEndurance()));
//                }
//            }
        }
    }


    private void subscribeUserToEvent() {
        long eventId = askLong("event id");
        long userId  = askLong("user id");
        User user = networkService.getUserService().findById(userId);
        Event event = networkService.getEventService().findById(eventId);
        networkService.getEventService().subscribeUser(eventId, user);
        System.out.println("User " + user.getUsername() + " subscribed to '" + event.getTitle() + "'.");
    }

    private void unsubscribeUserFromEvent() {
        long eventId = askLong("event id");
        long userId  = askLong("user id");
        User user = networkService.getUserService().findById(userId);
        Event event = networkService.getEventService().findById(eventId);
        networkService.getEventService().unsubscribeUser(eventId, user);
        System.out.println("User " + user.getUsername() + " unsubscribed from '" + event.getTitle() + "'.");
    }

    private void showFlockAverage() {
        long flockId = askLong("flock id");
        var perf = networkService.getFlockService().getAveragePerformance(flockId);
        System.out.printf("PERFORMANCE: avg speed=%.2f, avg endurance=%.2f%n",
                    perf.avgSpeed(), perf.avgEndurance());
    }

    private void listUserSubscriptions() {
        long userId = askLong("user id");
        var user = networkService.getUserService().findById(userId);
        var events = networkService.getEventService().findAll();

        System.out.println("User " + user.getUsername() + " is subscribed to:");

        boolean found = false;
        for (var e : events) {
            if (e.getObservers().contains(user)) {
                System.out.println(" - [" + e.getId() + "] " + e.getTitle());
                found = true;
            }
        }

        if (!found) System.out.println(" (no subscriptions found)");
    }

    private void showUserNotifications() {
        long userId = askLong("user id");
        User realUser = null;

        for (Event ev : networkService.getEventService().findAll()) {
            for (Observer o : ev.getObservers()) {
                User u = (User) o;
                if (u.getId().equals(userId)) {
                    realUser = u;
                    break;
                }
            }
            if (realUser != null) break;
        }

        if (realUser == null) {
            realUser = networkService.getUserService().findById(userId);
        }

        System.out.println("Notifications for user " + realUser.getUsername() + ":");
        if (realUser.getEventsLog().isEmpty()) {
            System.out.println(" (no notifications received)");
            return;
        }

        for (String log : realUser.getEventsLog()) {
            System.out.println(" - " + log);
        }
    }

    private void sendEventNotification() {
        long eventId = askLong("event id");
        String msg = askNonEmpty("notification message");

        Event event = networkService.getEventService().findById(eventId);

        event.notifyObservers(msg);


        var repo = (EventDBRepository) networkService.getEventService().getRepo();
        for (Observer o : event.getObservers()) {
            User u = (User) o;
            repo.saveNotification(u.getId(), event.getId(),
                    "[EVENT][" + event.getTitle() + "] " + msg);
        }

        System.out.println("Notification sent to all subscribers of '" + event.getTitle() + "'.");
    }




    private void startRaceEvent() {
        long eventId = askLong("event id");
        RaceEvent race = (RaceEvent) networkService.getEventService().findById(eventId);

        RaceResult res = networkService.calcTimpMinim(race);
        System.out.println(res);

        race.notifyObservers("Race event started!");


        var repo = (EventDBRepository) networkService.getEventService().getRepo();
        for (Observer o : race.getObservers()) {
            User u = (User) o;
            repo.saveNotification(u.getId(), race.getId(),
                    "[EVENT][" + race.getTitle() + "] Race event started!");
        }
    }




    /// functii pentru input

    private String askType() {
        while (true) {
            System.out.print("type [PERSON|DUCK]: ");
            String t = in.nextLine().trim().toUpperCase();
            if ("PERSON".equals(t) || "DUCK".equals(t)) return t;
            System.out.println("invalid type");
        }
    }

    private String askNonEmpty(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            if (!s.isBlank()) return s;
            System.out.println("can't be blank");
        }
    }

    private long askLong(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try { return Long.parseLong(s); }
            catch (NumberFormatException e) { System.out.println("invalid number"); }
        }
    }

    private LocalDate askDate(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try { return LocalDate.parse(s); }
            catch (DateTimeParseException e) { System.out.println("invalid format. (example: 2000-01-31)"); }
        }
    }

    private double askPositiveDouble(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v > 0) return v;
                System.out.println("should be a positive number");
            } catch (NumberFormatException e) { System.out.println("invalid number"); }
        }
    }

    private double askNonNegativeDouble(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                double v = Double.parseDouble(s);
                if (v >= 0) return v;
                System.out.println("should be a positive number");
            } catch (NumberFormatException e) { System.out.println("invalid number"); }
        }
    }


    private DuckType askDuckType(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim().toUpperCase();
            try { return DuckType.valueOf(s); }
            catch (IllegalArgumentException e) { System.out.println("Values: FLYING, SWIMMING, FLYING_AND_SWIMMING."); }
        }
    }

    private int askPositiveInt(String label) {
        while (true) {
            System.out.print(label + ": ");
            String s = in.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v > 0) return v;
                System.out.println("should be a positive integer");
            } catch (NumberFormatException e) { System.out.println("invalid number"); }
        }
    }


    private void populateNetwork() {
        Person p1 = networkService.getUserService().addPerson("alice", "alice@mail.com", "pass",
                "Popescu", "Alice", LocalDate.of(2000, 1, 1).toString(),
                "Student", 0.8);

        Person p2 = networkService.getUserService().addPerson("bob", "bob@mail.com", "pass",
                "Ionescu", "Bob", LocalDate.of(1999, 2, 10).toString(),
                "Inginer", 0.6);

        Person p3 = networkService.getUserService().addPerson("carla", "carla@mail.com", "pass",
                "Vasilescu", "Carla", LocalDate.of(1998, 3, 5).toString(),
                "Doctor", 0.9);

        Person p4 = networkService.getUserService().addPerson("david", "david@mail.com", "pass",
                "Pop", "David", LocalDate.of(1995, 5, 15).toString(),
                "Designer", 0.7);

        Person p5 = networkService.getUserService().addPerson("elena", "elena@mail.com", "pass",
                "Marin", "Elena", LocalDate.of(1997, 8, 20).toString(),
                "Profesor", 0.5);

        Duck d6 = networkService.getUserService().addDuck("duck1", "duck1@mail.com", "pass",
                "SWIMMING", 3.0, 5.0);
        Duck d7 = networkService.getUserService().addDuck("duck2", "duck2@mail.com", "pass",
                "FLYING", 4.0, 4.5);
        Duck d8 = networkService.getUserService().addDuck("duck3", "duck3@mail.com", "pass",
                "FLYING_AND_SWIMMING", 5.0, 6.0);
        Duck d9 = networkService.getUserService().addDuck("duck4", "duck4@mail.com", "pass",
                "SWIMMING", 3.5, 5.5);
        Duck d10 = networkService.getUserService().addDuck("alexandramoga", "alexandramoga@gmail.com", "pass",
                "FLYING", 2.8, 3.0);


        // Comunitatea 1: p1 - p2 - p3
        networkService.getFriendshipService().addFriendship(p1.getId(), p2.getId());
        networkService.getFriendshipService().addFriendship(p2.getId(), p3.getId());

        // Comunitatea 2: p4 - p5 - d6 - d7
        networkService.getFriendshipService().addFriendship(p4.getId(), p5.getId());
        networkService.getFriendshipService().addFriendship(p5.getId(), d6.getId());
        networkService.getFriendshipService().addFriendship(d6.getId(), d7.getId());

        // Comunitatea 3: d8 - d9 - d10
        networkService.getFriendshipService().addFriendship(d8.getId(), d9.getId());
        networkService.getFriendshipService().addFriendship(d9.getId(), d10.getId());
    }
}
