package service;

import domain.Message;
import domain.ReplyMessage;
import domain.user.User;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import javafx.application.Platform;
import repository.MessageDBRepository;
import validation.Validator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MessageService {

    private final MessageDBRepository messageRepo;
    private final Validator<Message> messageValidator;
    private final AtomicLong idGenerator;

    public MessageService(MessageDBRepository messageRepo, Validator<Message> validator) {
        this.messageRepo = messageRepo;
        this.messageValidator = validator;

        long maxId = 0;
        for (Message m : messageRepo.findAll()) {
            if (m.getId() > maxId) maxId = m.getId();
        }
        this.idGenerator = new AtomicLong(maxId);
    }

    private long nextId() {
        return idGenerator.incrementAndGet();
    }

    public Message sendMessage(User from, List<User> to, String text) {
        if (from == null) throw new ValidationException("Sender cannot be null");
        if (to == null || to.isEmpty()) throw new ValidationException("Receiver list cannot be empty");
        if (text == null || text.isBlank()) throw new ValidationException("Message cannot be empty");

        Message m = new Message(
                nextId(),
                from,
                to,
                text,
                LocalDateTime.now(),
                null
        );

        messageValidator.validate(m);
        return messageRepo.save(m);
    }


    public ReplyMessage replyMessage(User from, List<User> to, String text, Message replyTo) {
        if (replyTo == null) throw new ValidationException("Reply target cannot be null");

        ReplyMessage rm = new ReplyMessage(
                nextId(),
                from,
                to,
                text,
                LocalDateTime.now(),
                replyTo
        );

        messageValidator.validate(rm);
        return (ReplyMessage) messageRepo.save(rm);
    }


    public List<Message> getConversation(User a, User b) {
        return messageRepo.loadConversation(a.getId(), b.getId());
    }


    public List<Message> getAllMessages() {
        return messageRepo.findAll();
    }


    public Message findMessage(Long id) {
        return messageRepo.findById(id);
    }

    public List<Message> getMessagesForUser(User user) {
        return messageRepo.findAll().stream()
                .filter(m ->
                        m.getSender().getId().equals(user.getId()) ||
                                m.getReceivers().stream().anyMatch(u -> u.getId().equals(user.getId()))
                )
                .toList();
    }


}
