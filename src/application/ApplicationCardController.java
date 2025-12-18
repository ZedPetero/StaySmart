package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView; // Add this import

public class ApplicationCardController {

    @FXML private Label lblPropertyName, lblApplyDate, lblStatus, lblPayment, lblMessage;
    @FXML private HBox boxPayment;
    @FXML private ImageView imgPaymentIcon; // Add this ID
    @FXML private Label lblType; // Add this FXML link

    public void setApplicationData(String title, String date, String status, String payment, String message, String type) {
        lblPropertyName.setText(title);
        lblApplyDate.setText("Applied on: " + date);
        lblStatus.setText(status);
        lblMessage.setText(message);
        lblType.setText(type); // "Booking" or "Tour"

        if ("Tour".equalsIgnoreCase(type)) {
            boxPayment.setVisible(false);
            boxPayment.setManaged(false);
            // Style the Tour badge
            lblType.setStyle("-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblType.setText("PROPERTY TOUR");
        } else {
            boxPayment.setVisible(true);
            boxPayment.setManaged(true);
            lblPayment.setText("Payment Method: " + (payment != null ? payment : "Not specified"));
            // Style the Booking badge
            lblType.setStyle("-fx-background-color: #F3E5F5; -fx-text-fill: #7B1FA2; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblType.setText("ROOM BOOKING");
        }

        updateStatusStyle(status);
    }

    private void updateStatusStyle(String status) {
        lblStatus.getStyleClass().removeAll("badge-pending", "badge-approved", "badge-rejected");
        if (status.equalsIgnoreCase("Pending")) {
            lblStatus.getStyleClass().add("badge-pending");
        } else if (status.equalsIgnoreCase("Approved")) {
            lblStatus.getStyleClass().add("badge-approved");
        } else {
            lblStatus.getStyleClass().add("badge-rejected");
        }
    }
}