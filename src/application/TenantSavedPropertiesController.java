package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantSavedPropertiesController {

    @FXML private Label lblTotalCount;
    @FXML private FlowPane savedPropertiesFlowPane;
    @FXML private Button btnAll, btnCampus, btnPremium, btnBudget;

    @FXML
    public void initialize() {
        updatePropertyCount();
        loadSavedProperties("All");
    }

    @FXML private void handleFilterAll() { updateTabStyling(btnAll); loadSavedProperties("All"); }
    @FXML private void handleFilterCampus() { updateTabStyling(btnCampus); loadSavedProperties("Near Campus"); }
    @FXML private void handleFilterPremium() { updateTabStyling(btnPremium); loadSavedProperties("Premium Options"); }
    @FXML private void handleFilterBudget() { updateTabStyling(btnBudget); loadSavedProperties("Budget Picks"); }

    private void loadSavedProperties(String category) {
        savedPropertiesFlowPane.getChildren().clear();
        int userId = LoginController.getCurrentUser().getId();
        int count = 0;

        // Query to join saved_properties with the actual property details
        String query;

        if (category.equals("All")) {
            // Get unique properties regardless of category
            query = "SELECT DISTINCT p.* FROM properties p " +
                    "JOIN saved_properties s ON p.id = s.property_id " +
                    "WHERE s.tenant_id = ?";
        } else {
            // Get properties specifically in this category
            query = "SELECT p.* FROM properties p " +
                    "JOIN saved_properties s ON p.id = s.property_id " +
                    "WHERE s.tenant_id = ? AND s.category = ?";
        }

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            if (!category.equals("All")) {
                pstmt.setString(2, category);
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                count++;
                Property p = new Property(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getDouble("price"),
                        rs.getString("type"),
                        rs.getString("floors"),
                        rs.getString("image_path"),
                        rs.getString("amenities")
                );

                // Use your existing PropertyCard.fxml (Tenant version)
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PropertyCard.fxml"));
                VBox card = loader.load();

                // Assuming you have a TenantPropertyCardController or similar
                PropertyCardController controller = loader.getController();
                controller.setData(p, null); // Link to details view

                savedPropertiesFlowPane.getChildren().add(card);
            }

            lblTotalCount.setText("You have " + count + " saved properties");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTabStyling(Button activeBtn) {
        btnAll.getStyleClass().remove("tab-active");
        btnCampus.getStyleClass().remove("tab-active");
        btnPremium.getStyleClass().remove("tab-active");
        btnBudget.getStyleClass().remove("tab-active");
        activeBtn.getStyleClass().add("tab-active");
    }

    private void updatePropertyCount() {
        // This counts UNIQUE properties, so 1 property in 3 categories still equals 1
        String sql = "SELECT COUNT(DISTINCT property_id) FROM saved_properties WHERE tenant_id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.getCurrentUser().getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int uniqueCount = rs.getInt(1);
                lblTotalCount.setText("You have " + uniqueCount + " saved properties");
                btnAll.setText("All Saved (" + uniqueCount + ")");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}