package service;

import domain.FriendRequest;
import domain.FriendRequest.Status;
import domain.Friendship;
import domain.user.User;
import repository.FriendRequestDBRepository;
import repository.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FriendRequestService {

    private final FriendRequestDBRepository requestRepo;
    private final Repository<Long, User> userRepo;
    private final FriendshipService friendshipService;

    public FriendRequestService(FriendRequestDBRepository requestRepo,
                                Repository<Long, User> userRepo,
                                FriendshipService friendshipService) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.friendshipService = friendshipService;
    }

    public void sendRequest(User from, User to) {
        if (friendshipService.areFriends(from.getId(), to.getId())) {
            throw new RuntimeException("Users are already friends!");
        }

        List<FriendRequest> all = requestRepo.findAll();
        for (FriendRequest fr : all) {
            if (fr.getFrom().getId().equals(from.getId()) &&
                    fr.getTo().getId().equals(to.getId()) &&
                    fr.getStatus() == Status.PENDING) {

                throw new RuntimeException("Request already sent!");
            }
        }

        FriendRequest req = new FriendRequest(
                System.nanoTime(),
                from,
                to,
                Status.PENDING,
                LocalDateTime.now()
        );

        requestRepo.save(req);
    }

    public void acceptRequest(FriendRequest req) {
        req.setStatus(Status.APPROVED);
        requestRepo.update(req);

        // Create friendship
        friendshipService.addFriendship(req.getFrom().getId(), req.getTo().getId());
    }

    public void rejectRequest(FriendRequest req) {
        req.setStatus(Status.REJECTED);
        requestRepo.update(req);
    }

    public void deleteRequest(FriendRequest req) {
        requestRepo.deleteById(req.getId());
    }

    public List<FriendRequest> getReceivedRequests(User user) {
        return requestRepo.findAll()
                .stream()
                .filter(r -> r.getTo().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }


    public List<FriendRequest> getSentRequests(User user) {
        return requestRepo.findAll()
                .stream()
                .filter(r -> r.getFrom().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }


    public List<FriendRequest> getPendingRequests(User user) {
        return requestRepo.findAll()
                .stream()
                .filter(r -> r.getTo().getId().equals(user.getId()))
                .filter(r -> r.getStatus() == Status.PENDING)
                .collect(Collectors.toList());
    }


    public boolean requestExists(User from, User to) {
        return requestRepo.findAll()
                .stream()
                .anyMatch(r ->
                        r.getFrom().getId().equals(from.getId()) &&
                                r.getTo().getId().equals(to.getId()) &&
                                r.getStatus() == Status.PENDING
                );
    }
}
