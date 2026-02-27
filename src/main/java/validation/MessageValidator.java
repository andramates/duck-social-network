package validation;

import domain.Message;
import exceptions.ValidationException;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message m) {
        if (m == null) throw new ValidationException("Message is null");
        if (m.getId() == null) throw new ValidationException("Message id is null");
        if (m.getSender() == null) throw new ValidationException("Sender cannot be null");
        if (m.getReceivers() == null || m.getReceivers().isEmpty())
            throw new ValidationException("Message must have at least one receiver");
        if (m.getContent() == null || m.getContent().isBlank())
            throw new ValidationException("Content cannot be empty");
        if (m.getTimestamp() == null)
            throw new ValidationException("Timestamp cannot be null");
        if (m.getReply() != null && m.getReply().getId().equals(m.getId()))
            throw new ValidationException("Cannot reply to itself");
    }
}
