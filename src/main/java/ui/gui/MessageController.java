package ui.gui;

import domain.Message;
import domain.ReplyMessage;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import service.MessageService;
import service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageController {

    @FXML private ListView<User> usersListView;
    @FXML private ListView<String> messagesListView;
    @FXML private TextArea messageBox;
    @FXML private Label replyLabel;
    @FXML private Button cancelReplyBtn;

    private MessageService messageService;
    private UserService userService;
    private User loggedUser;
    private User selectedUser;
    private Message replyTarget;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();


    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm dd/MM");

    public void init(MessageService messageService, UserService userService, User loggedUser) {
        this.messageService = messageService;
        this.userService = userService;
        this.loggedUser = loggedUser;

        loadUsers();
    }

    private void loadUsers() {
        List<User> all = userService.findAll();
        all.removeIf(u -> u.getId().equals(loggedUser.getId()));
        usersListView.getItems().setAll(all);
    }

    @FXML
    private void onUserSelected(MouseEvent e) {
        selectedUser = usersListView.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            loadConversation();
            startMonitoring();
        }
    }



    private void loadConversation() {
        if (selectedUser == null) return;

        messagesListView.getItems().clear();

        List<Message> msgs = messageService.getConversation(loggedUser, selectedUser);

        for (Message m : msgs) {
            String entry;

            if (m instanceof ReplyMessage r && r.getReply() != null) {
                entry =
                        m.getSender().getUsername() + " (" + m.getTimestamp().format(formatter) + ")\n" +
                                "-> reply to: \"" + r.getReply().getContent() + "\"\n" +
                                m.getContent();
            } else {
                entry =
                        m.getSender().getUsername() + " (" + m.getTimestamp().format(formatter) + ")\n" +
                                m.getContent();
            }

            messagesListView.getItems().add(entry);
        }
    }

    @FXML
    private void onSend() {
        if (selectedUser == null) return;
        if (messageBox.getText().isBlank()) return;

        String text = messageBox.getText().trim();

        if (replyTarget == null)
            messageService.sendMessage(loggedUser, List.of(selectedUser), text);
        else {
            messageService.replyMessage(loggedUser, List.of(selectedUser), text, replyTarget);
            clearReply();
        }

        messageBox.clear();
        loadConversation();
    }

    @FXML
    private void onMessageSelected(MouseEvent e) {
        if (selectedUser == null) return;

        List<Message> msgs = messageService.getConversation(loggedUser, selectedUser);
        int index = messagesListView.getSelectionModel().getSelectedIndex();

        if (index >= 0 && index < msgs.size()) {
            replyTarget = msgs.get(index);
            replyLabel.setText("Replying to: \"" + replyTarget.getContent() + "\"");
            replyLabel.setVisible(true);
            cancelReplyBtn.setVisible(true);
        }
    }

    @FXML
    private void onCancelReply() {
        clearReply();
    }

    private void clearReply() {
        replyTarget = null;
        replyLabel.setVisible(false);
        cancelReplyBtn.setVisible(false);
    }

    private void startMonitoring() {

        scheduler.scheduleAtFixedRate(() -> {

            if (selectedUser == null) return;

            List<Message> msgs =
                    messageService.getConversation(loggedUser, selectedUser);

            Platform.runLater(() -> {
                messagesListView.getItems().clear();

                for (Message m : msgs) {
                    String entry;

                    if (m instanceof ReplyMessage r && r.getReply() != null) {
                        entry =
                                m.getSender().getUsername() + " (" +
                                        m.getTimestamp().format(formatter) + ")\n" +
                                        "-> reply to: \"" + r.getReply().getContent() + "\"\n" +
                                        m.getContent();
                    } else {
                        entry =
                                m.getSender().getUsername() + " (" +
                                        m.getTimestamp().format(formatter) + ")\n" +
                                        m.getContent();
                    }

                    messagesListView.getItems().add(entry);
                }
            });

        }, 0, 800, TimeUnit.MILLISECONDS);
    }


    private void stopMonitoring() {
        scheduler.shutdown();
    }

    @FXML
    public void onClose() {
        stopMonitoring();
    }

    @FXML private BorderPane root;
    public BorderPane getRoot() { return root; }

}
