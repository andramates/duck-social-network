package ui.gui;

import domain.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.CommunityService;

import java.util.List;

public class CommunityController {

    @FXML private Label labelNumCommunities;

    @FXML private TableView<User> tableCommunity;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colEmail;

    private CommunityService communityService;

    private ObservableList<User> model = FXCollections.observableArrayList();


    public void setService(CommunityService communityService) {
        this.communityService = communityService;
        setupTable();
    }


    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tableCommunity.setItems(model);
    }


    @FXML
    private void showNumCommunities() {
        try {
            int nr = communityService.numberOfCommunities();
            labelNumCommunities.setText("= " + nr);

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    @FXML
    private void showMostSociable() {
        try {
            List<User> list = communityService.mostSociableCommunity();
            model.setAll(list);

            if (list.isEmpty()) {
                showInfo("No community found.");
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }


    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
