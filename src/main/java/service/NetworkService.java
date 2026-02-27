package service;

import domain.Flock;
import domain.Friendship;
import domain.event.Assignment;
import domain.event.Lane;
import domain.event.RaceEvent;
import domain.event.RaceResult;
import domain.user.Duck;
import domain.user.User;
import exceptions.RepositoryException;
import repository.EventDBRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkService {
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CommunityService communityService;
    private final FlockService flockService;
    private final EventService eventService;

    private static final double EPSILON = 1e-6;
    private static final double NR_ITER = 100;

    private final ExecutorService raceExecutor =
            Executors.newFixedThreadPool(2);

    public NetworkService(UserService userService, FriendshipService friendshipService, CommunityService communityService, FlockService flockService, EventService eventService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.communityService = communityService;
        this.flockService = flockService;
        this.eventService = eventService;
    }

    public void removeUserAndAllFriendships(Long userId) {
        for (User user: userService.findAll()) {
            Friendship found = friendshipService.getFriendship(userId, user.getId());
            if (found != null) {
                friendshipService.removeFriendship(userId, user.getId());
            }
        }
        userService.removeUser(userId);
    }

    public void addDuckToFlock(Long flockId, Long duckId) {
        Flock flock = flockService.findById(flockId);
        Duck duck = userService.findDuckById(duckId);
        if (flock.getMembers().contains(duck)) throw new RepositoryException("Duck is already in this flock");
        if (duck.getFlock() != null) throw new RepositoryException("Duck already exists in a flock: " + duck.getFlock().getName());
        flock.getMembers().add(duck);
        duck.setFlock(flock);
        flockService.update(flock);
//        userService.update(duck);
    }

    public void removeDuckFromFlock(Long flockId, Long duckId) {
        Flock flock = flockService.findById(flockId);
        Duck duck = userService.findDuckById(duckId);
        if(!flock.getMembers().contains(duck)) throw new RepositoryException("Duck is not in this flock");
        flock.getMembers().remove(duck);
        duck.setFlock(null);
        flockService.update(flock);
//        userService.update(duck);
    }

    public void removeFlock(Long flockId) {
        Flock flock = flockService.findById(flockId);
        if (flock.getMembers() == null) return;
        for( Duck duck: flock.getMembers()) {
            duck.setFlock(null);
//            userService.update(duck);
        }

        flockService.removeFlock(flockId);
    }

    public RaceEvent addRaceEvent(String title, String description, int numberOfDucks, List<Lane> lanes) {
        List<Duck> ducks = userService.findAllSwimmingDucks();
        if (numberOfDucks > ducks.size()) throw new RepositoryException("Not enough ducks!");
        return eventService.addRaceEvent(title, description, numberOfDucks, ducks, lanes);
    }

    public RaceResult calcTimpMinim(RaceEvent race) {
        Duck[] ducks = userService.findAllSwimmingDucks().toArray(new Duck[0]);
        Lane[] lanes = race.getLanes().toArray(new Lane[0]);

        sortLanes(lanes);
        sortDucks(ducks);

        // cea mai mica viteza
        double vmin = ducks[0].getSpeed();
        for (int i = 1; i < ducks.length; i++) {
            if (ducks[i].getSpeed() < vmin) {
                vmin = ducks[i].getSpeed();
            }
        }

        // cea mai mare distanta
        double dmax = lanes[lanes.length - 1].getDistanta();

        double lowest = 0.0, highest = (2.0 * dmax) / vmin;
        for (int it = 0; it < NR_ITER && highest - lowest > EPSILON; it ++) {
            double mid = (lowest + highest) / 2;
            if (fezabil(mid, ducks, lanes)) highest = mid;
            else lowest = mid;
        }

        Assignment[] assignments = buildAssignments(highest, ducks, lanes);
        return new RaceResult(highest, assignments);
    }

    private static Duck[] copyDucks(Duck[] ducks) {
        Duck[] d = new Duck[ducks.length];
        System.arraycopy(ducks, 0, d, 0, ducks.length);
        return d;
    }

    private static Lane[] copyLanes(Lane[] lanes) {
        Lane[] l = new Lane[lanes.length];
        System.arraycopy(lanes, 0, l, 0, lanes.length);
        return l;
    }

    /// sorteaza culoarele crescator dupa distanta
    private static void sortLanes(Lane[]  lanes) {
        for (int i = 1; i < lanes.length; i++) {
            Lane l = lanes[i];
            int j = i - 1;
            while (j >= 0 && lanes[j].getDistanta() > l.getDistanta()) {
                lanes[j + 1] = lanes[j];
                j--;
            }
            lanes[j + 1] = l;
        }
    }

    /// sorteaza ratele crescator dupa rezistenta si apoi crescator dupa viteza
    private static void sortDucks(Duck[] ducks) {
        for (int i = 1; i < ducks.length; i++) {
            Duck d = ducks[i];
            int j = i - 1;
            while (j >= 0 && (ducks[j].getEndurance() > d.getEndurance() ||
                    ducks[j].getEndurance() == d.getEndurance() && ducks[j].getSpeed() > d.getSpeed())) {
                ducks[j + 1] = ducks[j];
                j--;
            }
            ducks[j + 1] = d;
        }
    }

    /// verifica daca cursa se poate termina in cel mult T secunde
    private static boolean fezabil (double T, Duck[] ducks, Lane[] lanes) {
        int i = 0;
        for (int j = 0; j < lanes.length; j++) {
            double vitezaMin = (2.0 * lanes[j].getDistanta()) / T; // viteza minima pentru culoarul j
            while (i < ducks.length && ducks[i].getSpeed() + EPSILON < vitezaMin) i++;
            if (i == ducks.length) return false; // nu mai sunt rate
            i++; // am gasit rata pentru culoar j
        }
        return true;
    }

    private static Assignment[] buildAssignments(double T, Duck[] ducks, Lane[] lanes) {
        Assignment[] a = new Assignment[lanes.length];
        int i = 0;
        for (int j = 0; j < lanes.length; j++) {
            double vitezaMin = (2.0 * lanes[j].getDistanta()) / T;
            while (ducks[i].getSpeed() + EPSILON < vitezaMin) i++;
            double t = ducks[i].getTimp(lanes[j].getDistanta()); // timp dus intors pe culoarul j
            a[j] = new Assignment(ducks[i], lanes[j], t);
            i++;
        }
        return a;
    }

    public CompletableFuture<RaceResult> startRaceAsync(RaceEvent ev) {
        return CompletableFuture.supplyAsync(() -> {
            ev.start();
            ((EventDBRepository)eventService.getRepo()).markRaceStarted(ev.getId());
            return calcTimpMinim(ev);
        }, raceExecutor);
    }

    public UserService getUserService() {
        return userService;
    }
    public FriendshipService getFriendshipService() {
        return friendshipService;
    }
    public CommunityService getCommunityService() {
        return communityService;
    }

    public FlockService getFlockService() {
        return flockService;
    }

    public EventService getEventService() {
        return eventService;
    }
}
