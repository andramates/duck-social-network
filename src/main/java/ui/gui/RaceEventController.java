package ui.gui;


import domain.event.Lane;
import domain.event.RaceEvent;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import service.EventService;
import service.NetworkService;
import service.NotificationService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RaceEventController {

    @FXML
    private TableView<RaceEvent> table;
    @FXML private Button btnSubscribe;
    @FXML private Button btnUnsubscribe;
    @FXML private Button btnStartRace;

    @FXML private TableColumn<RaceEvent, Long> colId;
    @FXML private TableColumn<RaceEvent, String> colTitle;
    @FXML private TableColumn<RaceEvent, Integer> colDucks;


    private EventService eventService;
    private NotificationService notificationService;
    private NetworkService networkService;
    private User loggedUser;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();



    public void init(EventService es,
                     NotificationService ns,
                     NetworkService net,
                     User user) {

        this.eventService = es;
        this.notificationService = ns;
        this.networkService = net;
        this.loggedUser = user;

        loadEvents();
        startMonitoring();
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colDucks.setCellValueFactory(new PropertyValueFactory<>("numberOfDucks"));

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, old, ev) -> onEventSelected());
    }

    private void startMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {

            String currentState = computeStateSignature();

            Platform.runLater(() -> {
                RaceEvent selected = table.getSelectionModel().getSelectedItem();
                Long selectedId = selected != null ? selected.getId() : null;

                loadEvents();

                if (selectedId != null) {
                    table.getItems().stream()
                            .filter(e -> e.getId().equals(selectedId))
                            .findFirst()
                            .ifPresent(e -> table.getSelectionModel().select(e));
                }

                onEventSelected();
            });

        }, 0, 800, TimeUnit.MILLISECONDS);
    }


    private String computeStateSignature() {
        return eventService.findAll().stream()
                .filter(e -> e instanceof RaceEvent)
                .map(e -> {
                    RaceEvent r = (RaceEvent) e;
                    return r.getId() + ":" + r.isStarted();
                })
                .sorted()
                .reduce("", (a, b) -> a + "|" + b);
    }

    public void stopMonitoring() {
        scheduler.shutdown();
    }


    private void loadEvents() {
        table.getItems().setAll(
                eventService.findAll().stream()
                        .filter(e -> e instanceof RaceEvent)
                        .map(e -> (RaceEvent) e)
                        .toList()
        );
    }

    @FXML
    private void onEventSelected() {
        RaceEvent ev = table.getSelectionModel().getSelectedItem();
        if (ev == null) {
            btnSubscribe.setDisable(true);
            btnUnsubscribe.setDisable(true);
            btnStartRace.setDisable(true);
            return;
        }

        boolean subscribed = ev.getObservers().contains(loggedUser);

        btnSubscribe.setDisable(subscribed);
        btnUnsubscribe.setDisable(!subscribed);

        btnSubscribe.setVisible(!subscribed);
        btnUnsubscribe.setVisible(subscribed);

        btnStartRace.setDisable(ev.isStarted());
    }


    @FXML
    private void onSubscribe() {
        RaceEvent ev = table.getSelectionModel().getSelectedItem();
        if (ev == null) return;

        eventService.subscribeUser(ev.getId(), loggedUser);

        reloadAndReselect(ev.getId());
    }


    @FXML
    private void onUnsubscribe() {
        RaceEvent ev = table.getSelectionModel().getSelectedItem();
        if (ev == null) return;

        eventService.unsubscribeUser(ev.getId(), loggedUser);

        reloadAndReselect(ev.getId());
    }

    private void reloadAndReselect(Long eventId) {
        loadEvents();

        RaceEvent refreshed = table.getItems().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElse(null);

        if (refreshed != null) {
            table.getSelectionModel().select(refreshed);
            onEventSelected();
        }
    }


    @FXML
    private void onStartRace() {

        RaceEvent ev = table.getSelectionModel().getSelectedItem();
        if (ev == null) return;

        if (ev.isStarted()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Race already started!").show();
            return;
        }

        eventService.startRace(ev);

        var result = networkService.calcTimpMinim(ev);

        loadEvents();

        Long eventId = ev.getId();
        ev = table.getItems().stream()
                .filter(e -> e.getId().equals(eventId))
                .findFirst()
                .orElseThrow();

        for (var o : ev.getObservers()) {
            if (o instanceof User u) {
                notificationService.notifyAsync(
                        u,
                        ev,
                        "🏁 Race \"" + ev.getTitle() + "\" started!\n\n" + result
                );
            }
        }

        btnStartRace.setDisable(true);
        onEventSelected();
    }



    @FXML
    private void onAddRaceEvent() {

        Dialog<RaceEvent> dialog = new Dialog<>();
        dialog.setTitle("Add Race Event");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        TextField tfTitle = new TextField();
        TextField tfDescription = new TextField();
        TextField tfNrDucks = new TextField();

        tfNrDucks.setPromptText("Number of ducks");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.addRow(0, new Label("Title:"), tfTitle);
        grid.addRow(1, new Label("Description:"), tfDescription);
        grid.addRow(2, new Label("Nr Ducks:"), tfNrDucks);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                try {
                    int nr = Integer.parseInt(tfNrDucks.getText());

                    return eventService.addRaceEvent(
                            tfTitle.getText(),
                            tfDescription.getText(),
                            nr,
                            networkService.getUserService().findAllSwimmingDucks(),
                            List.of(new Lane(1, 50),
                                    new Lane(2, 100),
                                    new Lane(3, 150)) // lanes vor fi adăugate implicit
                    );

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                }
            }
            return null;
        });

        dialog.showAndWait();
        loadEvents();
    }

}

