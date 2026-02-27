package domain.event;

import domain.user.Duck;

public class Assignment {
    private final Duck duck;
    private final Lane lane;
    private final double timp;

    public Assignment(Duck duck, Lane lane, double timp) {
        this.duck = duck;
        this.lane = lane;
        this.timp = timp;
    }

    public Duck getDuck() {
        return duck;
    }

    public Lane getLane() {
        return lane;
    }

    public double getTimp() {
        return timp;
    }

    @Override
    public String toString() {
        return "Duck " + duck.getId() + " on lane " + lane.getIndex() + ": t=" + String.format("%.3f", timp) + " secunde";
    }
}
