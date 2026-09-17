package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Lets a tenant file a property under one collection. The saved_properties table has a
 * UNIQUE (tenant_id, property_id) key, so a property lives in exactly one collection;
 * saving again simply moves it.
 */
public class SavePropertyDialogController {
    @FXML private RadioButton checkCampus, checkPremium, checkBudget;
    @FXML private ToggleGroup categoryGroup;
    @FXML private Button btnRemove;

    private int propertyId;
    private boolean isSaved = false;
    private boolean wasAlreadySaved = false;

    public void setPropertyId(int id) {
        this.propertyId = id;
        preselectExistingCategory();
    }

    /** True when the dialog changed the saved state (saved, moved or removed). */
    public boolean isSaved() { return isSaved; }

    /** True when the property is saved in any collection after the dialog closed. */
    public boolean isCurrentlySaved() { return wasAlreadySaved; }

    private void preselectExistingCategory() {
        if (LoginController.getCurrentUser() == null) return;
        String sql = "SELECT category FROM saved_properties WHERE tenant_id = ? AND property_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.getCurrentUser().getId());
            pstmt.setInt(2, propertyId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                wasAlreadySaved = true;
                String cat = rs.getString("category");
                if ("Near Campus".equals(cat)) checkCampus.setSelected(true);
                else if ("Premium Options".equals(cat)) checkPremium.setSelected(true);
                else if ("Budget Picks".equals(cat)) checkBudget.setSelected(true);
                btnRemove.setVisible(true);
                btnRemove.setManaged(true);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String selectedCategory() {
        if (checkCampus.isSelected()) return "Near Campus";
        if (checkPremium.isSelected()) return "Premium Options";
        if (checkBudget.isSelected()) return "Budget Picks";
        return null;
    }

    @FXML
    private void handleSave() {
        String category = selectedCategory();
        if (category == null || LoginController.getCurrentUser() == null) return;

        String sql = "INSERT INTO saved_properties (tenant_id, property_id, category) VALUES (?, ?, ?) " +
                "ON CONFLICT(tenant_id, property_id) DO UPDATE SET category = excluded.category";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.getCurrentUser().getId());
            pstmt.setInt(2, propertyId);
            pstmt.setString(3, category);
            pstmt.executeUpdate();
            isSaved = true;
            wasAlreadySaved = true;
            handleCancel();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRemove() {
        if (LoginController.getCurrentUser() == null) return;
        String sql = "DELETE FROM saved_properties WHERE tenant_id = ? AND property_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.getCurrentUser().getId());
            pstmt.setInt(2, propertyId);
            pstmt.executeUpdate();
            isSaved = true;
            wasAlreadySaved = false;
            handleCancel();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCancel() {
        ((Stage) checkCampus.getScene().getWindow()).close();
    }
}
