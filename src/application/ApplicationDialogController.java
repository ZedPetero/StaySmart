package application;

import application.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ApplicationDialogController {

    @FXML
    private Label lblRoomTitle;
    @FXML
    private TextArea txtAppMessage;
    @FXML
    private ComboBox<String> comboPayment;
    @FXML
    private TextField txtContact;
    @FXML
    private TextArea txtTourMessage;

    private Room currentRoom;

    @FXML
    public void initialize() {
        comboPayment.getItems().addAll("Cash / Physical Payment", "GCash / E-Wallet", "Bank Transfer");
    }

    public void setRoomData(Room room) {
        this.currentRoom = room;
        lblRoomTitle.setText("Applying for Room " + room.getRoomNumber());
    }

    @FXML
    private void handleSubmitApplication() {
        if (comboPayment.getValue() == null || txtAppMessage.getText().isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }
        saveToDatabase("Booking", txtAppMessage.getText(), comboPayment.getValue(), null);
    }

    @FXML
    private void handleRequestTour() {
        if (txtContact.getText().isEmpty() || txtTourMessage.getText().isEmpty()) {
            showAlert("Error", "Please provide contact info and a message.");
            return;
        }
        saveToDatabase("Tour", txtTourMessage.getText(), null, txtContact.getText());
    }

    private void saveToDatabase(String type, String message, String payment, String contact) {
        if (LoginController.getCurrentUser() == null) {
            showAlert("Error", "You must be logged in to apply.");
            return;
        }

        boolean success = DatabaseHandler.saveApplication(
                currentRoom.getId(),
                LoginController.getCurrentUser().getId(),
                currentRoom.getPropertyId(),
                type,
                message,
                payment,
                contact);

        if (success) {
            showAlert("Success", "Request sent successfully!");
            ((Stage) lblRoomTitle.getScene().getWindow()).close();
        } else {
            showAlert("Error", "Failed to send request. Check database connection.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}