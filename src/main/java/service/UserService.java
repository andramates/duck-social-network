package service;

import domain.user.*;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import repository.Repository;
import utils.PasswordEncoder;
import validation.Validator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static domain.user.DuckType.SWIMMING;

public class UserService {
    private final Repository<Long, User> userRepo;
    private final Validator<User> userValidator;

    public UserService(Repository<Long, User> userRepo, Validator<User> userValidator) {
        this.userRepo = userRepo;
        this.userValidator = userValidator;
    }

    public Person addPerson(String username, String email, String password,
                            String lastName, String firstName,
                            String dateOfBirth, String occupation, Double empathyLevel) {
        long id = nextId();
        LocalDate dob = LocalDate.parse(dateOfBirth);
        String hashed = PasswordEncoder.hashPassword(password);
        Person p = new Person(id, username, email, hashed,
                lastName, firstName, dob, occupation, empathyLevel);

        userValidator.validate(p);
        return (Person) userRepo.save(p);
    }


    public Duck addDuck(String username, String email, String password,
                        String duckType, Double speed, Double endurance) {
        long id = nextId();
        DuckType dt = DuckType.valueOf(duckType.trim().toUpperCase());
        String hashed = PasswordEncoder.hashPassword(password);
        Duck d = switch (dt) {
            case SWIMMING -> new SwimmingDuck(id, username, email, hashed, speed, endurance);
            case FLYING -> new FlyingDuck(id, username, email, password, speed, endurance);
            case FLYING_AND_SWIMMING -> new FlyingAndSwimmingDuck(id, username, email, password, speed, endurance);
        };

        userValidator.validate(d);
        return (Duck) userRepo.save(d);
    }

    public void removeUser(long id) {
        userRepo.deleteById(id);
    }

    public User findById(long id) {
        return userRepo.findById(id);
    }

    public Duck findDuckById(long id) {
        User user = userRepo.findById(id);
        if (!(user instanceof  Duck)) throw new RepositoryException("This is not a duck");
        return (Duck) user;
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User update(User user) {
        userValidator.validate(user);
        return userRepo.update(user);
    }

    private long nextId() {
        long max = 0;
        for (User u : userRepo.findAll()) {
            Long id = u.getId();
            if (id != null && id > max) max = id;
        }
        return max + 1;
    }

    public List<Duck> findAllDucks() {
        ArrayList<Duck> ducks = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            if (u instanceof Duck) {
                ducks.add((Duck) u);
            }
        }
        return ducks;
    }

    public List<Duck> findAllSwimmingDucks() {
        ArrayList<Duck> ducks = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            if (u instanceof SwimmingDuck) {
                ducks.add((Duck) u);
            }
        }
        return ducks;
    }

    public List<Person> findAllPersons() {
        ArrayList<Person> persons = new ArrayList<>();
        for (User u : userRepo.findAll()) {
            if (u instanceof Person) {
                persons.add((Person) u);
            }
        }
        return persons;
    }

    public List<Duck> findDucksPage(int page, int size) {
        List<User> all = userRepo.findPage(page, size);
        ArrayList<Duck> ducks = new ArrayList<>();
        for (User u : all) {
            if (u instanceof Duck) {
                ducks.add((Duck) u);
            }
        }
        return ducks;
    }

    public int countDucks() {
        return userRepo.count();
    }

    public User authenticate(String username, String password) {

        if (username == null || username.isBlank())
            throw new ValidationException("Username cannot be empty");

        if (password == null || password.isBlank())
            throw new ValidationException("Password cannot be empty");

        String hashed = PasswordEncoder.hashPassword(password);

        List<User> users = userRepo.findAll();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                if (u.getPassword().equals(hashed)) {
                    return u;
                } else {
                    throw new ValidationException("Wrong password!");
                }
            }
        }

        throw new ValidationException("User not found!");
    }



}
