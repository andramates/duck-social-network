package domain;

import domain.user.User;
import java.time.LocalDateTime;
import java.util.List;

public class ReplyMessage extends Message {

    public ReplyMessage(Long id,
                        User sender,
                        List<User> receivers,
                        String content,
                        LocalDateTime timestamp,
                        Message replyTo) {

        super(id, sender, receivers, content, timestamp, replyTo);
    }

    public Message getReplyTo() {
        return getReply();
    }
}
