package application;

import application.model.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
        applicationsListContainer.getChildren().clear();

        int userId = LoginController.getCurrentUser().getId();
        int total = 0, pending = 0, approved = 0, rejected = 0;

        String sql = "SELECT a.*, p.name as property_name, r.room_number, p.landlord_id, u.fullname as landlord_name " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN rooms r ON a.room_id = r.id " +
                "LEFT JOIN users u ON p.landlord_id = u.id " +
                "WHERE a.tenant_id = ? " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                total++;
                String status = rs.getString("status") != null ? rs.getString("status") : "Pending";

                if (status.equalsIgnoreCase("Pending")) pending++;
                else if (status.equalsIgnoreCase("Approved")) approved++;
                else if (status.equalsIgnoreCase("Rejected")) rejected++;

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/ApplicationCard.fxml"));
                    VBox card = loader.load();

                    ApplicationCardController cardController = loader.getController();

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
                            rs.getTimestamp("apply_date")
                    );
                    app.setPropertyName(rs.getString("property_name"));
                    app.setRoomNumber(rs.getString("room_number"));
                    app.setLandlordId(rs.getInt("landlord_id"));
                    app.setLandlordName(rs.getString("landlord_name"));

                    cardController.setApplication(app);

                    applicationsListContainer.getChildren().add(card);
                } catch (Exception e) {
                    System.err.println("Error loading application card: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            lblTotal.setText(String.valueOf(total));
            lblPending.setText(String.valueOf(pending));
            lblApproved.setText(String.valueOf(approved));
            lblRejected.setText(String.valueOf(rejected));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}