package domain.event;

import domain.user.Duck;
import domain.user.DuckType;
import exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RaceEvent extends Event {

    private final int numberOfDucks; // M
    private final List<Duck> participants = new ArrayList<>();
    private final List<Lane> lanes;

    private boolean started = false;

    public boolean isStarted() {
        return started;
    }

    public void start() {
        this.started = true;
    }


    public RaceEvent(Long id, String title, String description, LocalDateTime createdAt, int numberOfDucks, List<Lane> lanes,
                     boolean started) {
        super(id, title, description, createdAt);
        this.numberOfDucks = numberOfDucks;
        this.lanes = lanes;
        this.started = started;
    }

    public int getNumberOfDucks() {
        return numberOfDucks;
    }

    public List<Duck> getParticipants() {
        return participants;
    }

    public List<Lane> getLanes() {
        return lanes;
    }


    @Override
    public String toString() {
        return "RACE EVENT: " + id + " " + title;
    }
}
