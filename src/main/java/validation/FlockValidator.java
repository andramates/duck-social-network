package validation;

import domain.Flock;
import domain.user.Duck;
import exceptions.ValidationException;

public class FlockValidator implements Validator<Flock> {
    private final DuckValidator duckValidator = new DuckValidator();

    @Override
    public void validate(Flock flock) {
        if (flock.getName() == null || flock.getName().isEmpty()) throw new ValidationException("Name is null or empty");
        for (Duck d: flock.getMembers()) {
            duckValidator.validate(d);
        }
    }

}
