package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsAccountController {

    @FXML private PasswordField txtCurrentPass;
    @FXML private PasswordField txtNewPass;
    @FXML private PasswordField txtConfirmPass;

    @FXML
    private void handleUpdatePassword() {
        String currentInput = txtCurrentPass.getText();
        String newVal = txtNewPass.getText();
        String confirm = txtConfirmPass.getText();

        // 1. Basic Validation
        if (currentInput.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        if (!newVal.equals(confirm)) {
            showAlert(AlertType.WARNING, "Validation Error", "New passwords do not match.");
            return;
        }

        if (newVal.length() < 3) {
            showAlert(AlertType.WARNING, "Security", "New password must be at least 3 characters long.");
            return;
        }

        // 2. Database Logic
        User currentUser = LoginController.getCurrentUser();

        if (currentUser == null) {
            showAlert(AlertType.ERROR, "Error", "No user logged in.");
            return;
        }

        // Check if the current password provided matches the DB
        if (checkCurrentPassword(currentUser.getId(), currentInput)) {
            // Update to new password
            boolean success = updatePasswordInDB(currentUser.getId(), newVal);

            if (success) {
                showAlert(AlertType.INFORMATION, "Success", "Password updated successfully!");
                clearFields();
            } else {
                showAlert(AlertType.ERROR, "Database Error", "Failed to update password.");
            }
        } else {
            showAlert(AlertType.ERROR, "Authentication Failed", "The current password you entered is incorrect.");
        }
    }

    // --- HELPER METHODS ---

    private boolean checkCurrentPassword(int userId, String inputPassword) {
        String sql = "SELECT password FROM users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                // Compare input with DB password
                return dbPassword.equals(inputPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updatePasswordInDB(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void clearFields() {
        txtCurrentPass.clear();
        txtNewPass.clear();
        txtConfirmPass.clear();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}