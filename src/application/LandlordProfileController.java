package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LandlordProfileController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBusinessName;
    @FXML private ComboBox<String> cmbBusinessType;
    @FXML private ImageView profileImage;

    private int currentUserId;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        // --- FIX: Get the ACTUAL logged-in user ID ---
        User user = LoginController.getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getId();
            loadUserProfile();
        } else {
            System.err.println("No user logged in!");
            // Optional: Redirect to login or show error
        }
    }

    private void loadUserProfile() {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Populate fields
                setTextField(txtFullName, rs.getString("fullname"));
                setTextField(txtEmail, rs.getString("email"));
                setTextField(txtPhone, rs.getString("contact_number"));
                setTextField(txtAddress, rs.getString("address"));
                setTextField(txtBusinessName, rs.getString("business_name"));

                String bType = rs.getString("business_type");
                if (bType != null && !bType.isEmpty()) {
                    cmbBusinessType.setValue(bType);
                } else {
                    cmbBusinessType.getSelectionModel().selectFirst();
                }

                // Load Image
                String imgPath = rs.getString("profile_image_path");
                if (imgPath != null && !imgPath.isEmpty()) {
                    File imgFile = new File(imgPath);
                    if (imgFile.exists()) {
                        profileImage.setImage(new Image(imgFile.toURI().toString()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load profile data.");
        }
    }

    // Helper to prevent null pointer exceptions if DB field is null
    private void setTextField(TextField field, String value) {
        field.setText(value != null ? value : "");
    }

    @FXML
    private void handleSave() {
        // Validate Inputs
        if (!txtPhone.getText().matches("^\\+?[0-9]+$")) {
            showAlert("Validation Error", "Phone number must contain only numbers.");
            return;
        }

        // Simple Email Regex
        if (!txtEmail.getText().matches("^[^@]+@[^@]+\\.[^@]+$")) {
            showAlert("Validation Error", "Invalid email format.");
            return;
        }

        String sql = "UPDATE users SET fullname = ?, email = ?, contact_number = ?, address = ?, business_name = ?, business_type = ?, profile_image_path = ? WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtFullName.getText());
            pstmt.setString(2, txtEmail.getText());
            pstmt.setString(3, txtPhone.getText());
            pstmt.setString(4, txtAddress.getText());
            pstmt.setString(5, txtBusinessName.getText());
            pstmt.setString(6, cmbBusinessType.getValue());

            String path = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : getCurrentDbImagePath();
            pstmt.setString(7, path);

            pstmt.setInt(8, currentUserId);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // UPDATE SESSION: Update the global User object so the Dashboard name updates immediately if needed
                User currentUser = LoginController.getCurrentUser();
                if (currentUser != null) {
                    currentUser.setFullname(txtFullName.getText());
                    currentUser.setContactNumber(txtPhone.getText());
                    currentUser.setEmail(txtEmail.getText());
                }

                showAlert("Success", "Profile updated successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not save changes.");
        }
    }

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(txtFullName.getScene().getWindow());

        if (file != null) {
            selectedImageFile = file;
            profileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleCancel() {
        loadUserProfile();
    }

    private String getCurrentDbImagePath() {
        String sql = "SELECT profile_image_path FROM users WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("profile_image_path");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}