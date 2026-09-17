package application;

import application.model.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ApplicationCardController {

    @FXML
    private Label lblPropertyName, lblApplyDate, lblStatus, lblPayment, lblMessage, lblType;
    @FXML
    private HBox boxPayment;
    @FXML
    private ImageView imgPaymentIcon;
    @FXML
    private Button btnMessage;

    private Application currentApp;

    public void setApplication(Application app) {
        this.currentApp = app;
        lblPropertyName.setText(app.getPropertyName());
        lblApplyDate.setText("Applied on: " + (app.getApplyDate() != null ? app.getApplyDate().toString() : "N/A"));
        String status = app.getStatus() != null ? app.getStatus() : "Pending";
        lblStatus.setText(status);
        lblMessage.setText(app.getMessage() != null && !app.getMessage().isEmpty() ? app.getMessage() : "No message provided.");
        lblType.setText(app.getType());

        if ("Tour".equalsIgnoreCase(app.getType())) {
            boxPayment.setVisible(false);
            boxPayment.setManaged(false);
            lblType.setStyle(
                    "-fx-background-color: #E3F2FD; -fx-text-fill: #1976D2; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblType.setText("PROPERTY TOUR");
        } else {
            boxPayment.setVisible(true);
            boxPayment.setManaged(true);
            lblPayment.setText(
                    "Payment Method: " + (app.getPaymentMethod() != null ? app.getPaymentMethod() : "Not specified"));
            lblType.setStyle(
                    "-fx-background-color: #F3E5F5; -fx-text-fill: #7B1FA2; -fx-padding: 2 8; -fx-background-radius: 5; -fx-font-size: 11px; -fx-font-weight: bold;");
            lblType.setText("ROOM BOOKING");
        }

        updateStatusStyle(status);
    }

    @FXML
    private void handleMessage() {
        if (currentApp == null)
            return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ChatDialog.fxml"));
            javafx.scene.Parent root = loader.load();
            ChatDialogController controller = loader.getController();

            // Pass context: Application ID, Landlord ID (receiver), Landlord Name
            controller.setContext(currentApp.getId(), currentApp.getLandlordId(),
                    currentApp.getLandlordName() != null ? currentApp.getLandlordName() : "Landlord");

            AppWindow.show(new javafx.stage.Stage(), root, "Chat - " + currentApp.getPropertyName());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateStatusStyle(String status) {
        lblStatus.getStyleClass().removeAll("badge-pending", "badge-approved", "badge-rejected");
        if (status == null || status.equalsIgnoreCase("Pending")) {
            lblStatus.getStyleClass().add("badge-pending");
        } else if (status.equalsIgnoreCase("Approved")) {
            lblStatus.getStyleClass().add("badge-approved");
        } else {
            lblStatus.getStyleClass().add("badge-rejected");
        }
    }
}