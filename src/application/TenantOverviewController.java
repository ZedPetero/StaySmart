package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.sql.*;

public class TenantOverviewController {

    @FXML private Label lblWelcomeName, lblSavedCount, lblAppCount, lblTourCount, lblViewedCount;
    @FXML private VBox toursContainer, emptyToursBox;

    @FXML
    public void initialize() {
        User user = LoginController.getCurrentUser();
        if (user != null) {
            // Greets using the full name defined in your User class
            lblWelcomeName.setText("Hello, " + user.getFullname() + "!");
            loadStatistics(user.getId());
            loadUpcomingTours(user.getId());
        }
    }

    private void loadStatistics(int userId) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            // 1. Saved Properties (Unique count)
            lblSavedCount.setText(String.valueOf(getCount(conn,
                    "SELECT COUNT(DISTINCT property_id) FROM saved_properties WHERE tenant_id = ?", userId)));

            // 2. Active Applications (Booking type)
            lblAppCount.setText(String.valueOf(getCount(conn,
                    "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND application_type = 'Booking' AND status = 'Pending'", userId)));

            // 3. Scheduled Tours (Pending tours)
            lblTourCount.setText(String.valueOf(getCount(conn,
                    "SELECT COUNT(*) FROM applications WHERE tenant_id = ? AND application_type = 'Tour' AND status = 'Pending'", userId)));

            // 4. Viewed Properties (Unique properties ever applied for or saved)
            lblViewedCount.setText(String.valueOf(getCount(conn,
                    "SELECT COUNT(DISTINCT property_id) FROM applications WHERE tenant_id = ?", userId)));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadUpcomingTours(int userId) {
        toursContainer.getChildren().clear();

        // JOIN query to get Property info and Landlord's FULL name
        String sql = "SELECT a.*, p.name as prop_name, p.location, u.fullname as host_name " +
                "FROM applications a " +
                "JOIN properties p ON a.property_id = p.id " +
                "JOIN users u ON p.landlord_id = u.id " +
                "WHERE a.tenant_id = ? AND a.application_type = 'Tour' " +
                "ORDER BY a.apply_date DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            boolean hasTours = false;
            while (rs.next()) {
                hasTours = true;
                addTourItem(
                        rs.getString("prop_name"),
                        rs.getString("apply_date"),
                        rs.getString("location"),
                        rs.getString("host_name")
                );
            }

            // Logic for "If no upcoming property tours, just say there are no upcoming ones"
            emptyToursBox.setVisible(!hasTours);
            emptyToursBox.setManaged(!hasTours);
            toursContainer.setVisible(hasTours);
            toursContainer.setManaged(hasTours);

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addTourItem(String name, String date, String loc, String host) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("OverviewTourItem.fxml"));
            HBox item = loader.load();

            // Mapping database results to the FXML labels
            ((Label) item.lookup("#lblPropertyName")).setText(name);
            ((Label) item.lookup("#lblDate")).setText(date);
            ((Label) item.lookup("#lblLocation")).setText(loc);
            ((Label) item.lookup("#lblHost")).setText("Host: " + host);

            toursContainer.getChildren().add(item);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private int getCount(Connection conn, String sql, int userId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}