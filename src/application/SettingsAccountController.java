package application;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SettingsAccountController {

    @FXML
    private PasswordField txtCurrentPass;

    @FXML
    private PasswordField txtNewPass;

    @FXML
    private PasswordField txtConfirmPass;

    @FXML
    private void handleUpdatePassword() {
        String current = txtCurrentPass.getText();
        String newVal = txtNewPass.getText();
        String confirm = txtConfirmPass.getText();

        if (current.isEmpty() || newVal.isEmpty() || confirm.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        if (!newVal.equals(confirm)) {
            showAlert("Error", "New passwords do not match.");
            return;
        }

        // TODO: Add your database update logic here

        showAlert("Success", "Password updated successfully!");
        clearFields();
    }

    private void clearFields() {
        txtCurrentPass.clear();
        txtNewPass.clear();
        txtConfirmPass.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}