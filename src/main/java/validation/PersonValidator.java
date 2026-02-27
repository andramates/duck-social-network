package validation;

import domain.user.Person;
import exceptions.ValidationException;

public class PersonValidator implements Validator<Person> {
    private final UserValidator baseValidator = new UserValidator();

    @Override
    public void validate(Person person) {
        baseValidator.validate(person);

        if (person.getLastName() == null || person.getLastName().isBlank()) throw new ValidationException("Last name is invalid");
        if (person.getFirstName() == null || person.getFirstName().isBlank()) throw new ValidationException("First name is invalid");
        if (person.getOccupation() == null || person.getOccupation().isBlank()) throw new ValidationException("Occupation is invalid");
        if (person.getDateOfBirth() == null) throw new ValidationException("Date of birth is invalid");
        if (person.getEmpathyLevel() < 0)  throw new ValidationException("Empathy level is invalid");
    }
}
