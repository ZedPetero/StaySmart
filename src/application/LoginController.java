package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;

import java.util.regex.Pattern;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberCheck;
    @FXML
    private Button loginButton;
    @FXML
    private Circle bgCircle;

    // ====== ( User Session ) =========
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    @FXML
    private void initialize() {
        usernameField.setOnAction(e -> passwordField.requestFocus());

        if (bgCircle != null) {
            RadialGradient gradient = new RadialGradient(
                    0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, Color.web("#125A8B")),
                    new Stop(1.0, Color.web("#125A8B", 0.0)));
            bgCircle.setFill(gradient);
            bgCircle.setRadius(640);
            BoxBlur noise = new BoxBlur();
            noise.setWidth(2);
            noise.setHeight(2);
            noise.setIterations(1);
            GaussianBlur blur = new GaussianBlur();
            blur.setRadius(80);
            blur.setInput(noise);
            bgCircle.setEffect(blur);
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String emailInput = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (emailInput.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter both email and password.");
            return;
        }

        if (!isValidEmail(emailInput)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Format", "Please enter a valid email address.");
            return;
        }

        if (!DatabaseHandler.isDatabaseReachable()) {
            showAlert(Alert.AlertType.ERROR, "Database Unavailable", DatabaseHandler.DB_UNREACHABLE_MESSAGE);
            return;
        }

        User user = AuthService.authenticate(emailInput, password);

        if (user != null) {
            currentUser = user; // SAVE THE USER
            UserSession.cleanUserSession();
            UserSession.getInstance(user.getId(), user.getUsername(), user.getRole());
            showAlert(Alert.AlertType.INFORMATION, "Login Successful",
                    "Welcome, " + user.getFullname() + "!");

            loadMainApplication(event);

        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\+?[0-9]+$") && phone.length() >= 7;
    }

    @FXML
    private void onCancel(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onForgot(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Forgot Password", "Please contact administrator.");
    }

    @FXML
    private void onSignupClick(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            Parent root = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            AppWindow.showFixed(new Stage(), root, "Sign Up", AppWindow.SIGNUP_WIDTH, AppWindow.SIGNUP_HEIGHT);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private void loadMainApplication(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            String fxmlPath;
            String windowTitle;

            if ("owner".equalsIgnoreCase(currentUser.getRole()) || "admin".equalsIgnoreCase(currentUser.getRole())) {
                fxmlPath = "MainLandlordLayout.fxml";
                windowTitle = "Landlord Dashboard";
            } else {
                fxmlPath = "TenantDashboard.fxml";
                windowTitle = "Tenant Dashboard";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage mainStage = new Stage();
            mainStage.setResizable(true);
            mainStage.setMaximized(true);
            AppWindow.show(mainStage, root, windowTitle);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load application: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        // Shown synchronously: the caller may close the login window right after this,
        // and a deferred alert would otherwise pop up owned by an already-closed window.
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (loginButton != null && loginButton.getScene() != null
                && loginButton.getScene().getWindow() != null
                && loginButton.getScene().getWindow().isShowing()) {
            alert.initOwner(loginButton.getScene().getWindow());
        }
        alert.showAndWait();
    }
}