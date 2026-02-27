package validation;

import domain.user.Duck;
import exceptions.ValidationException;

public class DuckValidator implements Validator<Duck> {
    private final UserValidator baseValidator = new UserValidator();

    @Override
    public void validate(Duck duck) {
        baseValidator.validate(duck);
        if (duck.getType() == null)  throw new ValidationException("Duck type is null");
        if (duck.getSpeed() <= 0) throw new ValidationException("Duck speed is negative");
        if (duck.getEndurance() <= 0) throw new ValidationException("Duck endurance is negative");
    }
}
