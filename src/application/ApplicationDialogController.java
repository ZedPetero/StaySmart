package application;

import application.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ApplicationDialogController {

    @FXML private Label lblRoomTitle;

    // Tab 1: Application
    @FXML private TextArea txtAppMessage;
    @FXML private ComboBox<String> comboPayment;

    // Tab 2: Tour
    @FXML private TextField txtContact;
    @FXML private TextArea txtTourMessage;

    private Room currentRoom;

    @FXML
    public void initialize() {
        // Setup payment options
        comboPayment.getItems().addAll("Cash / Physical Payment", "GCash / E-Wallet", "Bank Transfer");
    }

    public void setRoomData(Room room) {
        this.currentRoom = room;
        lblRoomTitle.setText("Applying for Room " + room.getRoomNumber());
    }

    @FXML
    private void handleSubmitApplication() {
        String message = txtAppMessage.getText();
        String payment = comboPayment.getValue();

        if (payment == null || message.isEmpty()) {
            showAlert("Error", "Please fill in all fields and select a payment method.");
            return;
        }

        // TODO: DATABASE INSERTION HERE
        // INSERT INTO applications (room_id, tenant_id, message, payment_method, type) VALUES (...)
        System.out.println("Application Sent: " + message + " via " + payment);

        showAlert("Success", "Your application has been sent to the landlord!");
        closeDialog();
    }

    @FXML
    private void handleRequestTour() {
        String contact = txtContact.getText();
        String message = txtTourMessage.getText();

        if (contact.isEmpty() || message.isEmpty()) {
            showAlert("Error", "Please provide your contact number and a message.");
            return;
        }

        // TODO: DATABASE INSERTION HERE
        // INSERT INTO tour_requests (room_id, tenant_id, contact, message) VALUES (...)
        System.out.println("Tour Requested: " + contact);

        showAlert("Success", "Tour request sent! The landlord will contact you shortly.");
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) lblRoomTitle.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}