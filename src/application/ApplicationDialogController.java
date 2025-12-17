package application;

import application.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ApplicationDialogController {

    @FXML private Label lblRoomTitle;
    @FXML private TextArea txtAppMessage;
    @FXML private ComboBox<String> comboPayment;
    @FXML private TextField txtContact;
    @FXML private TextArea txtTourMessage;

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
        String sql = "INSERT INTO applications (room_id, tenant_id, property_id, application_type, message, payment_method, contact_number) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentRoom.getId()); // Works now with updated Room.java
            pstmt.setInt(2, LoginController.getCurrentUser().getId());
            pstmt.setInt(3, currentRoom.getPropertyId());
            pstmt.setString(4, type);
            pstmt.setString(5, message);
            pstmt.setString(6, payment);
            pstmt.setString(7, contact);

            pstmt.executeUpdate();
            showAlert("Success", "Request sent successfully!");
            ((Stage) lblRoomTitle.getScene().getWindow()).close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}