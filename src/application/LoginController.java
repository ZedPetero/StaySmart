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

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheck;
    @FXML private Button loginButton;

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    @FXML
    private Circle bgCircle;

    @FXML
    private void initialize() {
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        // Setup top left circle with blur and noise effect (like homepage)
        if (bgCircle != null) {
            // Apply radial gradient that fades from center to edges
            RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#125A8B")),  // Color at center
                new Stop(1.0, Color.web("#125A8B", 0.0))  // Transparent at edges
            );
            bgCircle.setFill(gradient);
            bgCircle.setRadius(640);
            // Combine blur and noise effect - apply BoxBlur first for noise, then GaussianBlur
            BoxBlur noise = new BoxBlur();
            noise.setWidth(2);
            noise.setHeight(2);
            noise.setIterations(1);
            GaussianBlur blur = new GaussianBlur();
            blur.setRadius(80); // Intense blur
            blur.setInput(noise); // Chain noise into blur
            bgCircle.setEffect(blur);
        }
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter both username and password.");
            return;
        }

        // Authenticate using database
        User user = AuthService.authenticate(username, password);

        if (user != null) {
            currentUser = user;
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", 
                     "Welcome, " + username + "! Role: " + user.getRole());
            
            // Load main application window
            loadMainApplication(event);
            
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
        }
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
            // Set application icon
            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                signupStage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon not found, continue without it
            }
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
            // Close login window
            Stage currentStage = (Stage)((Node)event.getSource()).getScene().getWindow();
            currentStage.close();

            // Load fonts first
            Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Medium.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-SemiBold.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/Outfit-Bold.ttf"), 10);

            // Then load FXML
            Parent root = FXMLLoader.load(getClass().getResource("MainLandlordLayout.fxml"));
            Scene scene = new Scene(root);

            Stage mainStage = new Stage();
            mainStage.setTitle("Landlord Dashboard");
            mainStage.setMaximized(true);
            mainStage.setFullScreen(false);

            // Set application icon
            try {
                Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
                mainStage.getIcons().add(icon);
            } catch (Exception e) {
                // Icon not found, continue
            }

            mainStage.setScene(scene);
            mainStage.setResizable(true);
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