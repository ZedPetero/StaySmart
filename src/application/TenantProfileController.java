package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TenantProfileController {

    // --- UI Components ---
    @FXML private ImageView profileImage;
    @FXML private Label lblSideName;

    // Tabs
    @FXML private Button btnTabPersonal;
    @FXML private Button btnTabEmergency;
    @FXML private VBox viewPersonal;
    @FXML private VBox viewEmergency;

    // Personal Info Fields
    @FXML private Button btnEdit;
    @FXML private TextField txtFullName, txtEmail, txtPhone, txtAddress;
    @FXML private TextArea txtAboutMe;

    // Emergency Contact Fields
    @FXML private Button btnEditEmergency;
    @FXML private TextField txtEmName, txtEmPhone, txtEmRelation;

    // State Variables
    private int currentUserId;
    private boolean isEditingPersonal = false;
    private boolean isEditingEmergency = false;
    private File selectedImageFile;

    @FXML
    public void initialize() {
        User user = LoginController.getCurrentUser();
        if (user != null) {
            this.currentUserId = user.getId();
            loadProfile();
        }
    }

    // --- Tab Switching Logic ---

    @FXML
    private void switchTabPersonal() {
        viewPersonal.setVisible(true);
        viewEmergency.setVisible(false);

        // Update Button Styles (Active/Inactive)
        btnTabEmergency.getStyleClass().remove("tab-active-right");
        if (!btnTabPersonal.getStyleClass().contains("tab-active-right")) {
            btnTabPersonal.getStyleClass().add("tab-active-right");
        }
    }

    @FXML
    private void switchTabEmergency() {
        viewPersonal.setVisible(false);
        viewEmergency.setVisible(true);

        btnTabPersonal.getStyleClass().remove("tab-active-right");
        if (!btnTabEmergency.getStyleClass().contains("tab-active-right")) {
            btnTabEmergency.getStyleClass().add("tab-active-right");
        }
    }

    // --- Data Loading ---

    private void loadProfile() {
        String sql = "SELECT fullname, email, contact_number, address, about_me, profile_image_path, " +
                "emergency_contact_name, emergency_contact_phone, emergency_contact_relation " +
                "FROM users WHERE id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Personal
                setTextSafe(txtFullName, rs.getString("fullname"));
                setTextSafe(txtEmail, rs.getString("email"));
                setTextSafe(txtPhone, rs.getString("contact_number"));
                setTextSafe(txtAddress, rs.getString("address"));
                setTextSafe(txtAboutMe, rs.getString("about_me"));
                String fullname = rs.getString("fullname");
                lblSideName.setText(fullname != null && !fullname.isEmpty() ? fullname : LoginController.getCurrentUser().getUsername());

                // Emergency
                setTextSafe(txtEmName, rs.getString("emergency_contact_name"));
                setTextSafe(txtEmPhone, rs.getString("emergency_contact_phone"));
                setTextSafe(txtEmRelation, rs.getString("emergency_contact_relation"));

                // Image
                String imgPath = rs.getString("profile_image_path");
                if (imgPath != null && !imgPath.isEmpty()) {
                    File file = new File(imgPath);
                    if (file.exists()) profileImage.setImage(new Image(file.toURI().toString()));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Personal Info Handling ---

    @FXML
    private void handleEditToggle() {
        if (isEditingPersonal) {
            savePersonalProfile();
        } else {
            setPersonalEditable(true);
            btnEdit.setText("Save Changes");
            isEditingPersonal = true;
        }
    }

    private void savePersonalProfile() {
        String sql = "UPDATE users SET fullname=?, email=?, contact_number=?, address=?, about_me=?, profile_image_path=? WHERE id=?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtFullName.getText());
            pstmt.setString(2, txtEmail.getText());
            pstmt.setString(3, txtPhone.getText());
            pstmt.setString(4, txtAddress.getText());
            pstmt.setString(5, txtAboutMe.getText());

            String path = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : getCurrentDbImagePath();
            pstmt.setString(6, path);
            pstmt.setInt(7, currentUserId);

            if (pstmt.executeUpdate() > 0) {
                User currentUser = LoginController.getCurrentUser();
                if (currentUser != null) {
                    currentUser.setFullname(txtFullName.getText());
                    currentUser.setEmail(txtEmail.getText());
                    currentUser.setContactNumber(txtPhone.getText());
                }
                showAlert("Success", "Personal info updated!");
                setPersonalEditable(false);
                btnEdit.setText("Edit Profile");
                isEditingPersonal = false;
                loadProfile(); // Refresh side label
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setPersonalEditable(boolean enable) {
        txtFullName.setEditable(enable); txtEmail.setEditable(enable);
        txtPhone.setEditable(enable); txtAddress.setEditable(enable);
        txtAboutMe.setEditable(enable);
        setStyle(enable, txtFullName, txtEmail, txtPhone, txtAddress, txtAboutMe);
    }

    // --- Emergency Contact Handling ---

    @FXML
    private void handleEditEmergencyToggle() {
        if (isEditingEmergency) {
            saveEmergencyContact();
        } else {
            setEmergencyEditable(true);
            btnEditEmergency.setText("Save");
            isEditingEmergency = true;
        }
    }

    private void saveEmergencyContact() {
        String sql = "UPDATE users SET emergency_contact_name=?, emergency_contact_phone=?, emergency_contact_relation=? WHERE id=?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtEmName.getText());
            pstmt.setString(2, txtEmPhone.getText());
            pstmt.setString(3, txtEmRelation.getText());
            pstmt.setInt(4, currentUserId);

            if (pstmt.executeUpdate() > 0) {
                showAlert("Success", "Emergency contact updated!");
                setEmergencyEditable(false);
                btnEditEmergency.setText("Edit");
                isEditingEmergency = false;
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setEmergencyEditable(boolean enable) {
        txtEmName.setEditable(enable);
        txtEmPhone.setEditable(enable);
        txtEmRelation.setEditable(enable);
        setStyle(enable, txtEmName, txtEmPhone, txtEmRelation);
    }

    // --- Helpers ---

    @FXML
    private void handleUploadPhoto() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg"));
        File file = fc.showOpenDialog(txtFullName.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            profileImage.setImage(new Image(file.toURI().toString()));
        }
    }

    private void setStyle(boolean enable, TextInputControl... controls) {
        String style = enable ? "-fx-background-color: white; -fx-border-color: #ddd;" : "";
        for (TextInputControl c : controls) c.setStyle(style);
    }

    private void setTextSafe(TextInputControl tf, String val) {
        tf.setText(val != null ? val : "");
    }

    private String getCurrentDbImagePath() {
        // (Same helper as before, fetching just the image path to prevent null overwrite)
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT profile_image_path FROM users WHERE id=?")) {
            pstmt.setInt(1, currentUserId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("profile_image_path");
        } catch (Exception e) {}
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