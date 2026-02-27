package service;

import domain.Flock;
import domain.Friendship;
import domain.user.Duck;
import domain.user.User;
import exceptions.RepositoryException;
import repository.Repository;
import validation.Validator;

import java.util.ArrayList;
import java.util.List;

public class FlockService {
    private final Repository<Long, Flock> flockRepository;
    private final Validator<Flock> flockValidator;

    public FlockService(Repository<Long, Flock> flockRepository, Validator<Flock> flockValidator) {
        this.flockRepository = flockRepository;
        this.flockValidator = flockValidator;
    }

    public void addFlock(String name) {
        long id = nextId();
        Flock flock = new Flock(id, name);
        flockValidator.validate(flock);
        flockRepository.save(flock);
    }

    public void removeFlock(Long id) {
        flockRepository.deleteById(id);
    }

    public Flock findById(Long id) {
        return flockRepository.findById(id);
    }

    public List<Flock> findAll() {
        return flockRepository.findAll();
    }

    public List<Duck> listMembers(Long flockId) {
        Flock flock = flockRepository.findById(flockId);
        return new ArrayList<Duck>(flock.getMembers());
    }

    public Flock update(Flock flock) {
        flockValidator.validate(flock);
        return flockRepository.update(flock);
    }

    private long nextId() {
        long max = 0;
        for (Flock f : flockRepository.findAll()) {
            Long id = f.getId();
            if (id != null && id > max) max = id;
        }
        return max + 1;
    }

    public Flock.Performance getAveragePerformance(long flockId) {
        Flock f = flockRepository.findById(flockId);
        return f.getAvgPerformance();
    }

}
