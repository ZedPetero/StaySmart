package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
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

        if (!LoginController.isValidPhoneNumber(contact)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Format", "Contact number must contain digits only (at least 7).");
            return;
        }
        if (!DatabaseHandler.isDatabaseReachable()) {
            showAlert(Alert.AlertType.ERROR, "Database Unavailable", DatabaseHandler.DB_UNREACHABLE_MESSAGE);
            return;
        }
        if (SignupService.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Email in use", "An account with this email already exists. Please log in instead.");
            return;
        }

        String role = type.equals("Owner") ? "owner" : "tenant";

        boolean success = SignupService.registerUser(fullname, email, pass, role, contact);

        if (success) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Account created successfully! Returning to Homepage.");

            onBack(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Registration failed. Please check the database connection and try again.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    @FXML
    private void onLoginClick(ActionEvent event) {
        loadPage(event, "Main.fxml", "Login System", AppWindow.LOGIN_WIDTH, AppWindow.LOGIN_HEIGHT);
    }

    @FXML
    private void onBack(ActionEvent event) {
        loadPage(event, "Homepage.fxml", "StaySmart", AppWindow.HOMEPAGE_WIDTH, AppWindow.HOMEPAGE_HEIGHT);
    }

    private void loadPage(ActionEvent event, String fxml, String title, double width, double height) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            // Fixed design size: without it the homepage's off-canvas elements inflate the window
            // past the screen edge and the title bar ends up unreachable.
            AppWindow.showFixed(new Stage(), root, title, width, height);
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