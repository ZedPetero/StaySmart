package application;

import application.model.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LandlordTenantsController {

    @FXML
    private VBox tenantsContainer;
    @FXML
    private Label lblTotalTenants;
    @FXML
    private Label lblPaidMonth;
    @FXML
    private Label lblPendingPayment;
    @FXML
    private Label lblOverdue;

    @FXML
    public void initialize() {
        if (LoginController.getCurrentUser() != null) {
            loadTenants();
        }
    }

    private void loadTenants() {
        int landlordId = LoginController.getCurrentUser().getId();
        java.util.List<Application> tenants = DatabaseHandler.getAllTenants(landlordId);

        lblTotalTenants.setText(String.valueOf(tenants.size()));

        int paid = 0;
        for (Application t : tenants) {
            if ("Paid".equalsIgnoreCase(t.getRoomPaymentStatus())) paid++;
        }
        lblPaidMonth.setText(String.valueOf(paid));
        lblPendingPayment.setText(String.valueOf(tenants.size() - paid));
        lblOverdue.setText("0");

        tenantsContainer.getChildren().clear();
        for (Application tenant : tenants) {
            tenantsContainer.getChildren().add(createTenantCard(tenant));
        }
    }

    private VBox createTenantCard(Application tenant) {
        // Main Card Container
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #e3eff6; -fx-background-radius: 15; -fx-padding: 20;");

        // Header: Profile Icon & Name/Email
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Profile Icon Placeholder
        StackPane icon = new StackPane();
        icon.setStyle(
                "-fx-background-color: white; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-color: black; -fx-border-width: 2;");
        icon.setPrefSize(50, 50);
        icon.getChildren().add(new Label("User"));

        VBox nameBox = new VBox(2);
        Label name = new Label(tenant.getTenantName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        String subtext = tenant.getTenantEmail() != null ? tenant.getTenantEmail()
                : (tenant.getTenantContact() != null ? tenant.getTenantContact() : "No contact info");
        Label email = new Label(subtext);
        email.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        nameBox.getChildren().addAll(name, email);

        header.getChildren().addAll(icon, nameBox);

        // Property Info Row
        HBox infoRow = new HBox(20);

        VBox propBox = new VBox(5);
        propBox.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10; -fx-padding: 10;");
        propBox.setPrefWidth(200);
        Label propLabel = new Label("Property");
        propLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        Label propValue = new Label(tenant.getPropertyName());
        propValue.setStyle("-fx-font-weight: bold;");
        propBox.getChildren().addAll(propLabel, propValue);

        VBox roomBox = new VBox(5);
        roomBox.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10; -fx-padding: 10;");
        roomBox.setPrefWidth(200);
        Label roomLabel = new Label("Room");
        roomLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 10px;");
        Label roomValue = new Label(tenant.getRoomNumber());
        roomValue.setStyle("-fx-font-weight: bold;");
        roomBox.getChildren().addAll(roomLabel, roomValue);

        infoRow.getChildren().addAll(propBox, roomBox);

        // Action Buttons
        HBox actions = new HBox(15);
        Button btnContact = new Button("Contact Tenant");
        btnContact.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnContact, Priority.ALWAYS);
        btnContact.setStyle(
                "-fx-background-color: white; -fx-text-fill: #113970; -fx-border-color: #113970; -fx-border-radius: 20; -fx-background-radius: 20; -fx-cursor: hand;");
        btnContact.setOnAction(e -> handleContactTenant(tenant));

        Button btnDetails = new Button("View Details");
        btnDetails.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnDetails, Priority.ALWAYS);
        btnDetails.setStyle(
                "-fx-background-color: #1a56db; -fx-text-fill: white; -fx-background-radius: 20; -fx-cursor: hand;");
        btnDetails.setOnAction(e -> handleViewDetails(tenant));

        actions.getChildren().addAll(btnContact, btnDetails);

        card.getChildren().addAll(header, infoRow, actions);
        return card;
    }

    private void handleContactTenant(Application tenant) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatDialog.fxml"));
            Parent root = loader.load();
            ChatDialogController controller = loader.getController();
            controller.setContext(tenant.getId(), tenant.getTenantId(),
                    tenant.getTenantName() != null ? tenant.getTenantName() : "Tenant");

            Stage stage = new Stage();
            stage.initOwner(tenantsContainer.getScene().getWindow());
            AppWindow.show(stage, root, "Chat - " + tenant.getTenantName() + " (" + tenant.getPropertyName() + ")");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleViewDetails(Application tenant) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Tenant Details");
        alert.setHeaderText(tenant.getTenantName());
        String details = "Email: " + (tenant.getTenantEmail() != null ? tenant.getTenantEmail() : "N/A") + "\n"
                + "Contact: " + (tenant.getTenantContact() != null ? tenant.getTenantContact() : "N/A") + "\n"
                + "Property: " + tenant.getPropertyName() + "\n"
                + "Room: " + tenant.getRoomNumber() + "\n"
                + "Payment method: " + (tenant.getPaymentMethod() != null ? tenant.getPaymentMethod() : "N/A") + "\n"
                + "Room payment status: " + (tenant.getRoomPaymentStatus() != null ? tenant.getRoomPaymentStatus() : "Pending") + "\n"
                + "Approved application since: " + (tenant.getApplyDate() != null ? tenant.getApplyDate().toString() : "N/A");
        alert.setContentText(details);
        alert.initOwner(tenantsContainer.getScene().getWindow());
        alert.showAndWait();
    }
}
