package application;

import application.model.Application;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LandlordApplicationsController {

    @FXML
    private VBox applicationsContainer;
    @FXML
    private Label lblPendingCount;
    @FXML
    private Label lblApprovedCount;
    @FXML
    private Label lblRejectedCount;

    @FXML
    public void initialize() {
        loadApplications();
    }

    private void loadApplications() {
        if (applicationsContainer == null)
            return;
        applicationsContainer.getChildren().clear();

        List<Application> apps = fetchLandlordApplications(LoginController.getCurrentUser().getId());

        int pending = 0;
        int approved = 0;
        int rejected = 0;

        for (Application app : apps) {
            String status = app.getStatus();
            if ("Pending".equalsIgnoreCase(status))
                pending++;
            else if ("Approved".equalsIgnoreCase(status))
                approved++;
            else if ("Rejected".equalsIgnoreCase(status))
                rejected++;

            applicationsContainer.getChildren().add(createApplicationCard(app));
        }

        if (lblPendingCount != null)
            lblPendingCount.setText(String.valueOf(pending));
        if (lblApprovedCount != null)
            lblApprovedCount.setText(String.valueOf(approved));
        if (lblRejectedCount != null)
            lblRejectedCount.setText(String.valueOf(rejected));
    }

    private List<Application> fetchLandlordApplications(int landlordId) {
        List<Application> list = new ArrayList<>();
        // Query to get applications for properties owned by this landlord
        String sql = "SELECT a.*, u.fullname, p.name as property_name, r.room_number " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "JOIN users u ON a.tenant_id = u.id " +
                "WHERE p.landlord_id = ? " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = DatabaseHandler.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, landlordId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Application app = new Application(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getInt("tenant_id"),
                        rs.getInt("property_id"),
                        rs.getString("application_type"),
                        rs.getString("message"),
                        rs.getString("payment_method"),
                        rs.getString("contact_number"),
                        rs.getString("status"),
                        rs.getTimestamp("apply_date"));
                app.setTenantName(rs.getString("fullname"));
                app.setPropertyName(rs.getString("property_name"));
                app.setRoomNumber(rs.getString("room_number"));
                list.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private VBox createApplicationCard(Application app) {
        VBox card = new VBox(15);
        card.getStyleClass().add("application-card");
        card.setPadding(new javafx.geometry.Insets(25, 30, 25, 30));

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView profileImg = new ImageView();
        try {
            profileImg.setImage(new Image(getClass().getResource("/application/images/profile.png").toExternalForm()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        profileImg.setFitWidth(50);
        profileImg.setFitHeight(50);
        profileImg.setPreserveRatio(true);
        profileImg.setClip(new Circle(25, 25, 25));

        VBox nameBox = new VBox(2);
        Label nameLbl = new Label(app.getTenantName());
        nameLbl.getStyleClass().add("applicant-name");
        Label contactLbl = new Label(app.getContactNumber() != null ? app.getContactNumber() : "No contact info");
        contactLbl.getStyleClass().add("applicant-email");
        nameBox.getChildren().addAll(nameLbl, contactLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLbl = new Label(app.getApplyDate().toString());
        dateLbl.getStyleClass().add("timestamp-text");

        header.getChildren().addAll(profileImg, nameBox, spacer, dateLbl);

        // Details
        HBox details = new HBox(5);
        details.setAlignment(Pos.BASELINE_LEFT);
        Label appliedLbl = new Label("Applied for: ");
        appliedLbl.getStyleClass().add("label-gray");
        Label propertyLbl = new Label(app.getPropertyName() + " - Room " + app.getRoomNumber());
        propertyLbl.getStyleClass().add("link-purple");
        details.getChildren().addAll(appliedLbl, propertyLbl);

        // Message
        VBox msgBox = new VBox(5);
        msgBox.getStyleClass().add("message-box");
        Label msgTitle = new Label("Message:");
        msgTitle.getStyleClass().add("label-gray");
        Label msgText = new Label(app.getMessage());
        msgText.getStyleClass().add("message-text");
        msgText.setWrapText(true);
        msgBox.getChildren().addAll(msgTitle, msgText);

        // Actions
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_LEFT);

        if ("Pending".equalsIgnoreCase(app.getStatus())) {
            Button btnApprove = new Button("Approve");
            btnApprove.getStyleClass().addAll("btn-action", "btn-approve");
            btnApprove.setPrefWidth(150);
            btnApprove.setOnAction(e -> handleStatusUpdate(app.getId(), "Approved"));

            Button btnReject = new Button("Reject");
            btnReject.getStyleClass().addAll("btn-action", "btn-reject");
            btnReject.setPrefWidth(150);
            btnReject.setOnAction(e -> handleStatusUpdate(app.getId(), "Rejected"));

            actions.getChildren().addAll(btnApprove, btnReject);
        } else {
            Label statusLbl = new Label("Status: " + app.getStatus());
            statusLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
            actions.getChildren().add(statusLbl);
        }

        card.getChildren().addAll(header, details, msgBox, actions);
        return card;
    }

    private void handleStatusUpdate(int appId, String newStatus) {
        boolean success = DatabaseHandler.updateApplicationStatus(appId, newStatus);
        if (success) {
            loadApplications(); // Refresh view
        }
    }
}
