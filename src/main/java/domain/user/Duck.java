package domain.user;

import domain.event.Event;
import domain.Flock;
import domain.Message;

import java.time.LocalDateTime;

public abstract class Duck extends User {
    protected final DuckType type;
    protected double speed;
    protected double endurance;
    protected Flock flock;

    protected Duck(Long id, String username, String email, String password,
                DuckType type, double speed, double endurance, Flock flock) {
        super(id, username, email, password);
        this.type = type;
        this.speed = speed;
        this.endurance = endurance;
        this.flock = flock;
    }

    public DuckType getType() {
        return type;
    }
    public double getSpeed() {
        return speed;
    }
    public double getEndurance() {
        return endurance;
    }
    public Flock getFlock() {
        return flock;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setEndurance(double endurance) {
        this.endurance = endurance;
    }

    public void setFlock(Flock flock) {
        this.flock = flock;
    }

//    public Message autoMessage(User user) {
//        return new Message(
//                System.currentTimeMillis(), this, user,
//                "Quack!",
//                LocalDateTime.now());
//    }

    public boolean canSwim() {
        return type ==  DuckType.SWIMMING || type == DuckType.FLYING_AND_SWIMMING;
    }
    public boolean canFly() {
        return type == DuckType.FLYING || type == DuckType.FLYING_AND_SWIMMING;
    }

    public void observeEvent(Event event) {
        //TODO
    }

    @Override
    public String toString() {
//        return String.format("DUCK: id: " + id + ", username: " + username +
//                ", type: " + type + ", speed: " + speed + ", endurance: " + endurance +
//                ", flock: " + (flock == null ? "no flock" : flock.getName()));
        return String.format("DUCK: id: " + id + ", username: " + username +
                ", type: " + type + ", speed: " + speed + ", endurance: " + endurance);
    }

    public double getTimp(double distanta) {
        return 2.0 * distanta / speed; // dus intors
    }
}

