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
import java.util.regex.Pattern;

public class SignupController {

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> ownershipCombo;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button signupButton;

    @FXML
    private void initialize() {
        ownershipCombo.getItems().addAll("Tenant", "Owner");
        Platform.runLater(() -> fullNameField.requestFocus());
    }

    @FXML
    private void onSignup(ActionEvent event) {

        String fullname = fullNameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String type = ownershipCombo.getValue();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (fullname.isEmpty() || contact.isEmpty() || email.isEmpty() || type == null || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing Info", "Please fill in all fields.");
            return;
        }
        if (!pass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Password Error", "Passwords do not match.");
            return;
        }
        if (pass.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Security", "Password must be at least 3 characters.");
            return;
        }
        if (!isValidEmail(email)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Format", "Please enter a valid email address.");
            return;
        }

        String role = type.equals("Owner") ? "owner" : "tenant";

        boolean success = SignupService.registerUser(fullname, email, pass, role, contact);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully! Returning to Homepage.");

            onBack(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Registration failed. Email might already be in use.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        loadPage(event, "Main.fxml", "Login");
    }

    @FXML
    private void onBack(ActionEvent event) {
        loadPage(event, "Homepage.fxml", "StaySmart");
    }

    private void loadPage(ActionEvent event, String fxml, String title) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(title);
            try {
                stage.getIcons()
                        .add(new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm()));
            } catch (Exception ignored) {
            }
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}