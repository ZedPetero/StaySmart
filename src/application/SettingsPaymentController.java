package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SettingsPaymentController {

    @FXML private TextField txtProvider;
    @FXML private TextField txtMobileNumber;
    @FXML private ImageView qrPreview;

    private File selectedQRFile;
    private int currentUserId;

    @FXML
    public void initialize() {
        // 1. Get logged-in user
        User user = LoginController.getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getId();
            loadPaymentDetails();
        }
    }

    private void loadPaymentDetails() {
        String sql = "SELECT ewallet_provider, ewallet_number, qr_image_path FROM users WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Load Text Fields
                String provider = rs.getString("ewallet_provider");
                String number = rs.getString("ewallet_number");

                if (provider != null) txtProvider.setText(provider);
                if (number != null) txtMobileNumber.setText(number);

                // Load QR Image
                String qrPath = rs.getString("qr_image_path");
                if (qrPath != null && !qrPath.isEmpty()) {
                    File imgFile = new File(qrPath);
                    if (imgFile.exists()) {
                        qrPreview.setImage(new Image(imgFile.toURI().toString()));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUploadQR() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(txtProvider.getScene().getWindow());

        if (file != null) {
            selectedQRFile = file;
            qrPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSaveEWallet() {
        String provider = txtProvider.getText();
        String mobile = txtMobileNumber.getText();

        // Basic Validation
        if (provider.isEmpty() || mobile.isEmpty()) {
            showAlert("Error", "Please fill in the E-Wallet provider and mobile number.");
            return;
        }

        if (!mobile.matches("^\\+?[0-9]+$")) {
            showAlert("Error", "Mobile number must contain digits only.");
            return;
        }

        // Database Update
        String sql = "UPDATE users SET ewallet_provider = ?, ewallet_number = ?, qr_image_path = ? WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, provider);
            pstmt.setString(2, mobile);

            // Logic: If new file selected, save new path. If not, keep existing path from DB.
            String path = (selectedQRFile != null) ? selectedQRFile.getAbsolutePath() : getCurrentDbQrPath();
            pstmt.setString(3, path);

            pstmt.setInt(4, currentUserId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                showAlert("Success", "Payment details saved successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save details.");
        }
    }

    // Helper to get existing path so we don't overwrite it with null if user didn't upload a NEW image
    private String getCurrentDbQrPath() {
        String sql = "SELECT qr_image_path FROM users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("qr_image_path");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}