package ui.gui;

import domain.user.Duck;
import domain.user.DuckType;
import domain.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.UserService;

import java.util.List;

public class DuckController {

    @FXML private TableView<Duck> table;

    @FXML private TableColumn<Duck, Long> colId;
    @FXML private TableColumn<Duck, String> colUsername;
    @FXML private TableColumn<Duck, String> colEmail;
    @FXML private TableColumn<Duck, String> colPassword;
    @FXML private TableColumn<Duck, String> colTip;
    @FXML private TableColumn<Duck, Double> colViteza;
    @FXML private TableColumn<Duck, Double> colRezistenta;

    @FXML private ComboBox<String> comboTip;

    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Label lblPage;

    private UserService userService;
    private ObservableList<Duck> model = FXCollections.observableArrayList();

    private int page = 0;
    private final int PAGE_SIZE = 5;


    public void setService(UserService userService) {
        this.userService = userService;
        loadCombo();
        loadPage();
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colTip.setCellValueFactory(new PropertyValueFactory<>("type"));
        colViteza.setCellValueFactory(new PropertyValueFactory<>("speed"));
        colRezistenta.setCellValueFactory(new PropertyValueFactory<>("endurance"));

        table.setItems(model);

        comboTip.setOnAction(event -> {
            page = 0;
            loadPage();
        });
    }

    private void loadCombo() {
        comboTip.getItems().add("ALL");
        for (DuckType dt : DuckType.values()) {
            comboTip.getItems().add(dt.name());
        }
        comboTip.getSelectionModel().select("ALL");
    }

    private void loadPage() {

        String filter = comboTip.getSelectionModel().getSelectedItem();

//        List<Duck> all = userService.findAllDucks();

        List<Duck> pageDucks = userService.findDucksPage(page, PAGE_SIZE);

        if(filter != null && !filter.equals("ALL")) {
            DuckType dt = DuckType.valueOf(filter);
            pageDucks = userService.findAllDucks().stream()
                    .filter(d -> d.getType() == dt)
                    .skip(page * PAGE_SIZE)
                    .limit(PAGE_SIZE)
                    .toList();
//            pageDucks = pageDucks.stream().filter(d -> d.getType() == dt).toList();
        }

        model.setAll(pageDucks);
//        int total = all.size();
//        int from = page * PAGE_SIZE;
//        int to = Math.min(from + PAGE_SIZE, total);

        int total = userService.countDucks();

        if (!filter.equals("ALL")) {
            total = (int)  userService.findAllDucks().stream().filter(u -> u instanceof Duck)
                    .map(u -> (Duck) u)
                    .filter(d-> d.getType() == DuckType.valueOf(filter))
                    .count();
        }

        int maxPage = total == 0 ? 1 : ((total - 1) / PAGE_SIZE) + 1;

        btnPrev.setDisable(page == 0);
        btnNext.setDisable(page >= maxPage - 1);

        lblPage.setText("Page " + (page + 1) + " / " + (maxPage));
    }


    @FXML
    private void nextPage() {
        page++;
        loadPage();
    }

    @FXML
    private void prevPage() {
        if (page > 0) {
            page--;
        }
        loadPage();
    }
}
