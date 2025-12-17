package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.event.ActionEvent;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;

import java.util.regex.Pattern;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheck;
    @FXML private Button loginButton;
    @FXML private Circle bgCircle;

    // --- STORE USER SESSION HERE ---
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
                    new Stop(1.0, Color.web("#125A8B", 0.0))
            );
            bgCircle.setFill(gradient);
            bgCircle.setRadius(640);
            BoxBlur noise = new BoxBlur();
            noise.setWidth(2); noise.setHeight(2); noise.setIterations(1);
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

        User user = AuthService.authenticate(emailInput, password);

        if (user != null) {
            currentUser = user; // SAVE THE USER
            System.out.println("DEBUG: User role is: '" + user.getRole() + "'");
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
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
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

            Stage signupStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            Scene scene = new Scene(root);
            signupStage.setTitle("Sign Up");
            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                signupStage.getIcons().add(icon);
            } catch (Exception e) {}
            signupStage.setScene(scene);
            signupStage.setResizable(false);
            signupStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load signup page: " + e.getMessage());
        }
    }

    private void loadMainApplication(ActionEvent event) {
        try {
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            try {
                Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Regular.ttf"), 10);
                Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Medium.ttf"), 10);
                Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-SemiBold.ttf"), 10);
                Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Bold.ttf"), 10);
            } catch (Exception ignored) {}

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
            Scene scene = new Scene(root);
            Stage mainStage = new Stage();
            mainStage.setTitle(windowTitle);
            mainStage.setResizable(true);
            mainStage.setMaximized(true);

            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                mainStage.getIcons().add(icon);
            } catch (Exception e) {}

            mainStage.setScene(scene);
            mainStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load application: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.initOwner(loginButton.getScene().getWindow());
            alert.showAndWait();
        });
    }
}