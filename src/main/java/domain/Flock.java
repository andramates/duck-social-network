package domain;

import domain.user.Duck;

import java.util.*;

public class Flock extends Entity<Long> {
    private final String name;
    private List<Duck> members;

    public Flock(Long id, String name) {
        super(id);
        this.name = name;
        this.members = new ArrayList<>();
    }

    public String getName() {
        return name;
    }
    public List<Duck> getMembers() {
        return members;
    }

    public void setMembers(List<Duck> members) {
        this.members = members;
    }

    public static record Performance(double avgSpeed, double avgEndurance) {}
    public Performance getAvgPerformance() {
        if (members.isEmpty()) return new Performance(0, 0);
        double speed = members.stream().mapToDouble(Duck::getSpeed).average().orElse(0.0);
        double endurance = members.stream().mapToDouble(Duck::getEndurance).average().orElse(0.0);
        return new Performance(speed, endurance);
    }
}
