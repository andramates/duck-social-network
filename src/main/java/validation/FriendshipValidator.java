package validation;

import domain.Friendship;
import domain.user.User;
import exceptions.ValidationException;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship friendship) {
        if (friendship == null) throw new ValidationException("Friendship is null");
        if (friendship.getId() == null) throw new ValidationException("Friendship id is null");
        Validator<User> userValidator = new UserValidator();
        userValidator.validate(friendship.getUser1());
        userValidator.validate(friendship.getUser2());
        if (friendship.getUser1().getId().equals(friendship.getUser2().getId())) {
            throw new ValidationException("Friendship cannot be between a single user");
        }
    }
}
