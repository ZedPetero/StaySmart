package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TenantSettingsController {

    // --- Security Fields ---
    @FXML private VBox viewSecurity;
    @FXML private PasswordField txtCurrentPass;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;
    @FXML private Button btnTabSecurity;

    // --- Preferences Fields ---
    @FXML private VBox viewPreferences;
    @FXML private TextField txtLanguage;
    @FXML private TextField txtTimezone;
    @FXML private Button btnTabPreferences;

    private int currentUserId;

    @FXML
    public void initialize() {
        User user = LoginController.getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getId();
            loadPreferences();
        }
        // Force default state
        switchTabSecurity();
    }

    // --- Tab Switching Logic (Fixed) ---

    @FXML
    private void switchTabSecurity() {
        // 1. Manage Visibility
        viewSecurity.setVisible(true);
        viewPreferences.setVisible(false);

        // 2. Manage Button Styles (Clear both first to avoid sticking)
        btnTabSecurity.getStyleClass().remove("settings-tab-active");
        btnTabPreferences.getStyleClass().remove("settings-tab-active");

        // 3. Add active class to correct button
        btnTabSecurity.getStyleClass().add("settings-tab-active");
    }

    @FXML
    private void switchTabPreferences() {
        viewSecurity.setVisible(false);
        viewPreferences.setVisible(true);

        btnTabSecurity.getStyleClass().remove("settings-tab-active");
        btnTabPreferences.getStyleClass().remove("settings-tab-active");

        btnTabPreferences.getStyleClass().add("settings-tab-active");
    }

    // --- Preferences Logic ---

    private void loadPreferences() {
        String sql = "SELECT pref_language, pref_timezone FROM users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String lang = rs.getString("pref_language");
                String zone = rs.getString("pref_timezone");

                txtLanguage.setText((lang != null && !lang.isEmpty()) ? lang : "English");
                txtTimezone.setText((zone != null) ? zone : "");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSavePreferences() {
        String sql = "UPDATE users SET pref_language = ?, pref_timezone = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtLanguage.getText());
            pstmt.setString(2, txtTimezone.getText());
            pstmt.setInt(3, currentUserId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Preferences saved successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save preferences.");
        }
    }

    // --- Security Logic (Same as before) ---

    @FXML
    private void handleUpdatePassword() {
        String current = txtCurrentPass.getText();
        String newVal = txtNewPass.getText();
        String confirm = txtConfirmPass.getText();

        if (current.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill in all password fields.");
            return;
        }
        if (!newVal.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Mismatch", "New passwords do not match.");
            return;
        }
        if (newVal.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Security", "Password must be at least 3 characters.");
            return;
        }

        if (!checkCurrentPassword(current)) {
            showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Incorrect current password.");
            return;
        }

        if (updatePassword(newVal)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
            txtCurrentPass.clear(); txtNewPass.clear(); txtConfirmPass.clear();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password.");
        }
    }

    private boolean checkCurrentPassword(String inputPass) {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("password").equals(inputPass);
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean updatePassword(String newPass) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPass);
            pstmt.setInt(2, currentUserId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}