package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TenantApplicationsController {

    @FXML private Label lblTotal, lblPending, lblApproved, lblRejected;
    @FXML private VBox applicationsListContainer;

    @FXML
    public void initialize() {
        loadApplications();
    }

    public void loadApplications() {
        // 1. Clear existing cards
        applicationsListContainer.getChildren().clear();

        int userId = LoginController.getCurrentUser().getId();
        int total = 0, pending = 0, approved = 0, rejected = 0;

        // 2. Query joins applications with properties and rooms for full details
        String sql = "SELECT a.*, p.name as property_name, r.room_number " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "WHERE a.tenant_id = ? " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                total++;
                String status = rs.getString("status");

                // Update stats counters
                if (status.equalsIgnoreCase("Pending")) pending++;
                else if (status.equalsIgnoreCase("Approved")) approved++;
                else if (status.equalsIgnoreCase("Rejected")) rejected++;

                // 3. Load the Application Card FXML for each row
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/ApplicationCard.fxml"));
                    VBox card = loader.load();

                    // 4. Pass data to the card's controller
                    ApplicationCardController cardController = loader.getController();
                    cardController.setApplicationData(
                            rs.getString("property_name") + " - Room " + rs.getString("room_number"),
                            rs.getString("apply_date"),
                            status,
                            rs.getString("payment_method"),
                            rs.getString("message"),
                            rs.getString("application_type")
                    );

                    applicationsListContainer.getChildren().add(card);
                } catch (Exception e) {
                    System.err.println("Error loading application card: " + e.getMessage());
                }
            }

            // 5. Update the UI labels
            lblTotal.setText(String.valueOf(total));
            lblPending.setText(String.valueOf(pending));
            lblApproved.setText(String.valueOf(approved));
            lblRejected.setText(String.valueOf(rejected));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}