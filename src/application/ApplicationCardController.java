package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView; // Add this import

public class ApplicationCardController {

    @FXML private Label lblPropertyName, lblApplyDate, lblStatus, lblPayment, lblMessage;
    @FXML private HBox boxPayment;
    @FXML private ImageView imgPaymentIcon; // Add this ID

    public void setApplicationData(String title, String date, String status, String payment, String message, String type) {
        lblPropertyName.setText(title);
        lblApplyDate.setText("Applied on: " + date);
        lblStatus.setText(status);
        lblMessage.setText(message);

        if ("Tour".equalsIgnoreCase(type)) {
            boxPayment.setVisible(false);
            boxPayment.setManaged(false);
        } else {
            boxPayment.setVisible(true);
            boxPayment.setManaged(true);
            lblPayment.setText("Payment Method: " + (payment != null ? payment : "Not specified"));
        }

        // Keep your existing badge styling logic here...
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