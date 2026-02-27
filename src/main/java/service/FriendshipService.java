package service;

import domain.Friendship;
import domain.user.User;
import exceptions.RepositoryException;
import repository.Repository;
import validation.Validator;

import java.util.List;

public class FriendshipService {
    private final Repository<Long, Friendship> friendshipRepo;
    private final Repository<Long, User> userRepository;
    private final Validator<Friendship> friendshipValidator;

    public FriendshipService(Repository<Long, Friendship> friendshipRepo, Repository<Long, User> userRepository, Validator<Friendship> friendshipValidator) {
        this.friendshipRepo = friendshipRepo;
        this.userRepository = userRepository;
        this.friendshipValidator = friendshipValidator;
    }

    public void addFriendship(Long user1Id, Long user2Id) {
        Friendship found = getFriendship(user1Id, user2Id);
        if (found != null) {
            throw new RepositoryException("Friendship already exists!");
        }

        User user1 = userRepository.findById(user1Id);
        User user2 = userRepository.findById(user2Id);

        long id = nextId();
        Friendship friendship = new Friendship(id, user1, user2);
        friendshipValidator.validate(friendship);
        friendshipRepo.save(friendship);
    }

    public void removeFriendship(Long user1Id, Long user2Id) {
        Friendship found = getFriendship(user1Id, user2Id);
        if (found == null) {
            throw new RepositoryException("Friendship does not exist");
        }
        friendshipRepo.deleteById(found.getId());
    }

    public Friendship getFriendship(Long user1Id, Long user2Id) {
        for (Friendship friendship : friendshipRepo.findAll()) {
            Long id1 = friendship.getUser1().getId();
            Long id2 = friendship.getUser2().getId();
            if (id1.equals(user1Id) && id2.equals(user2Id) || id1.equals(user2Id) && id2.equals(user1Id)) {
                return friendship;
            }
        }
        return null;
    }

    public List<Friendship> findAll() {
        return friendshipRepo.findAll();
    }

    private long nextId() {
        long max = 0;
        for (Friendship f : friendshipRepo.findAll()) {
            Long id = f.getId();
            if (id != null && id > max) max = id;
        }
        return max + 1;
    }

    public boolean areFriends(Long user1Id, Long user2Id) {
        return getFriendship(user1Id, user2Id) != null;
    }

    public int countFriends(Long userId) {
        int count = 0;
        for(User user : userRepository.findAll()) {
            if(areFriends(user.getId(), userId)) {
                count++;
            }
        }
        return count;
    }

}
