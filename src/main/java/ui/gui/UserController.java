package ui.gui;

import domain.user.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import service.UserService;

public class UserController {

    @FXML private ComboBox<String> comboType;
    @FXML private GridPane formGrid;

    @FXML private TextField tfDeleteId;

    private UserService userService;

    // form fields
    private TextField tfUsername, tfEmail, tfPassword;
    private TextField tfLastName, tfFirstName, tfDob, tfOccupation, tfEmpathy;
    private ComboBox<String> cbDuckType;
    private TextField tfSpeed, tfEndurance;




    public void setService(UserService userService) {
        this.userService = userService;
        loadTypes();
        setupPersonForm(); // default
    }

    @FXML
    public void initialize() {
        comboType.setOnAction(e -> switchForm());
    }

    private void loadTypes() {
        comboType.getItems().addAll("PERSON", "DUCK");
        comboType.getSelectionModel().select("PERSON");
    }

    private void switchForm() {
        if (comboType.getValue().equals("PERSON"))
            setupPersonForm();
        else
            setupDuckForm();
    }



    private void setupPersonForm() {
        formGrid.getChildren().clear();

        tfUsername = new TextField();
        tfEmail = new TextField();
        tfPassword = new TextField();
        tfLastName = new TextField();
        tfFirstName = new TextField();
        tfDob = new TextField();
        tfOccupation = new TextField();
        tfEmpathy = new TextField();

        addField("Username:", tfUsername, 0);
        addField("Email:", tfEmail, 1);
        addField("Password:", tfPassword, 2);
        addField("Last name:", tfLastName, 3);
        addField("First name:", tfFirstName, 4);
        addField("Date of birth (yyyy-mm-dd):", tfDob, 5);
        addField("Occupation:", tfOccupation, 6);
        addField("Empathy:", tfEmpathy, 7);
    }

    private void setupDuckForm() {
        formGrid.getChildren().clear();

        tfUsername = new TextField();
        tfEmail = new TextField();
        tfPassword = new TextField();
        cbDuckType = new ComboBox<>();
        cbDuckType.getItems().addAll("SWIMMING", "FLYING", "FLYING_AND_SWIMMING");
        cbDuckType.getSelectionModel().select("SWIMMING");
        tfSpeed = new TextField();
        tfEndurance = new TextField();

        addField("Username:", tfUsername, 0);
        addField("Email:", tfEmail, 1);
        addField("Password:", tfPassword, 2);
        addField("Duck type:", cbDuckType, 3);
        addField("Speed:", tfSpeed, 4);
        addField("Endurance:", tfEndurance, 5);
    }

    private void addField(String label, Control field, int row) {
        formGrid.add(new Label(label), 0, row);
        formGrid.add(field, 1, row);
    }



    @FXML
    private void addUser() {
        try {
            if (comboType.getValue().equals("PERSON")) {
                userService.addPerson(
                        tfUsername.getText(),
                        tfEmail.getText(),
                        tfPassword.getText(),
                        tfLastName.getText(),
                        tfFirstName.getText(),
                        tfDob.getText(),
                        tfOccupation.getText(),
                        Double.parseDouble(tfEmpathy.getText())
                );
            } else {
                userService.addDuck(
                        tfUsername.getText(),
                        tfEmail.getText(),
                        tfPassword.getText(),
                        cbDuckType.getValue(),
                        Double.parseDouble(tfSpeed.getText()),
                        Double.parseDouble(tfEndurance.getText())
                );
            }

            showInfo("User added successfully!");

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }



    @FXML
    private void deleteUser() {
        try {
            long id = Long.parseLong(tfDeleteId.getText());
            userService.removeUser(id);
            showInfo("User deleted!");
        } catch (Exception e) {
            showError("Invalid ID or user does not exist.");
        }
    }



    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }
}
