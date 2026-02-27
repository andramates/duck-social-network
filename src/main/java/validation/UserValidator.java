package validation;

import domain.user.User;
import exceptions.ValidationException;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User user) {
        if (user == null) throw new ValidationException("User is null");
        if (user.getId() == null) throw new ValidationException("User id is null");
        if (user.getEmail() == null || !user.getEmail().contains("@")) throw new ValidationException("User email invalid");
        if (user.getPassword() == null) throw new ValidationException("User password is null");
    }
}
