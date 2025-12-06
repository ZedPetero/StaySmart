package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField contactField;
    @FXML private Button signupButton;
    @FXML private Button backButton;
    @FXML private ComboBox<String> ownershipCombo;

    @FXML
    private void initialize() {
        // Populate ComboBox with only Tenant and Owner
        ownershipCombo.getItems().addAll("Tenant", "Owner");

        // Enter key navigation
        if (usernameField != null && passwordField != null && contactField != null) {
            usernameField.setOnAction(e -> passwordField.requestFocus());
            passwordField.setOnAction(e -> contactField.requestFocus());
            contactField.setOnAction(e -> onSignup(null));
        }
    }

    @FXML
    private void onSignup(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        String contactNumber = contactField.getText() == null ? "" : contactField.getText().trim();

        // Get selected role based on ownership type
        // Owner = admin, Tenant = user
        String role = "user"; // default to tenant
        String ownershipType = ownershipCombo.getValue();
        if (ownershipType != null && ownershipType.equals("Owner")) {
            role = "admin";
        } else if (ownershipType != null && ownershipType.equals("Tenant")) {
            role = "user";
        }

        // Validation
        if (username.isEmpty() || password.isEmpty() || contactNumber.isEmpty() || ownershipType == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields including ownership type.");
            return;
        }

        if (username.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Username must be at least 3 characters long.");
            return;
        }

        if (password.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Password must be at least 3 characters long.");
            return;
        }

        // Register user
        boolean success = SignupService.registerUser(username, password, role, contactNumber);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Account created successfully! You can now log in.");

            redirectToHomepage(event);

        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed",
                    "Username already exists or registration failed. Please try a different username.");
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        redirectToHomepage(event);
    }

    private void redirectToHomepage(ActionEvent event) {
        try {
            if (event != null) {
                Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
                currentStage.close();
            }

            Stage homepageStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("Homepage.fxml"));
            Scene scene = new Scene(root, 1400, 750); // Explicit size to prevent deformation
            homepageStage.setTitle("StaySmart");
            // Set application icon
            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                homepageStage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon not found, continue without it
            }
            homepageStage.setScene(scene);
            homepageStage.setResizable(false);
            homepageStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load homepage: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            if (signupButton != null && signupButton.getScene() != null) {
                alert.initOwner(signupButton.getScene().getWindow());
            }
            alert.showAndWait();
        });
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        try {
            if (event != null) {
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();
            }

            Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Login");
            // Set application icon
            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                stage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon not found, continue without it
            }
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login page: " + e.getMessage());
        }
    }

}
