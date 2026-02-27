package validation;

import domain.event.Event;
import exceptions.ValidationException;

public class EventValidator implements Validator<Event> {
    @Override
    public void validate(Event event) {
        if (event == null) throw new ValidationException("Event is null");
        if (event.getId() == null) throw new ValidationException("Event id is null");
        if (event.getTitle() == null || event.getTitle().isBlank()) throw new ValidationException("Title is invalid");
        if (event.getDescription() == null || event.getDescription().isBlank()) throw new ValidationException("Description is invalid");
        if (event.getCreatedAt() == null) throw new ValidationException("CreatedAt is invalid");

    }
}
