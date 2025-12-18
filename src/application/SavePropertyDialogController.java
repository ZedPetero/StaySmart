package application;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javafx.scene.control.CheckBox; // THIS IS THE MISSING LINE
import java.util.ArrayList;
import java.util.List;
public class SavePropertyDialogController {
    @FXML private CheckBox checkCampus, checkPremium, checkBudget;
    private int propertyId;
    private boolean isSaved = false;

    public void setPropertyId(int id) { this.propertyId = id; }
    public boolean isSaved() { return isSaved; }

    @FXML
    private void handleSave() {
        List<String> categories = new ArrayList<>();
        if (checkCampus.isSelected()) categories.add("Near Campus");
        if (checkPremium.isSelected()) categories.add("Premium Options");
        if (checkBudget.isSelected()) categories.add("Budget Picks");

        if (categories.isEmpty()) return;

        String sql = "INSERT IGNORE INTO saved_properties (tenant_id, property_id, category) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false); // Use transaction for multiple inserts
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (String cat : categories) {
                    pstmt.setInt(1, LoginController.getCurrentUser().getId());
                    pstmt.setInt(2, propertyId);
                    pstmt.setString(3, cat);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                conn.commit();
                isSaved = true;
                handleCancel();
            } catch (Exception e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCancel() {
        ((Stage) checkCampus.getScene().getWindow()).close();
    }
}