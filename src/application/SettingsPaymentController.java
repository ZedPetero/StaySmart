package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class SettingsPaymentController {

    @FXML
    private TextField txtProvider;

    @FXML
    private TextField txtMobileNumber;

    @FXML
    private void handleSaveEWallet() {
        String provider = txtProvider.getText();
        String mobile = txtMobileNumber.getText();

        if (provider.isEmpty() || mobile.isEmpty()) {
            showAlert("Error", "Please fill in the E-Wallet provider and mobile number.");
            return;
        }

        // TODO: Save E-Wallet logic
        System.out.println("Saving E-Wallet: " + provider + " | " + mobile);
        showAlert("Success", "E-Wallet details saved successfully!");
    }

    @FXML
    private void handleGenerateQR() {
        // TODO: Logic to generate or show QR code overlay
        System.out.println("Generating QR Code...");
        showAlert("QR Code", "QR Code generation initiated (Stub).");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}