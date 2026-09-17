package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomepageController {

    @FXML
    private Label welcomeText;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Circle bgCircle;

    @FXML
    private ImageView heroImage;

    @FXML
    private Circle decorativeImage;

    @FXML
    private ImageView logoImage;

    @FXML
    private ImageView homeIconBg;

    @FXML
    private ImageView homeIcon;

    @FXML
    private AnchorPane rootPane;

    @FXML
    private HBox logoGroup;

    @FXML
    private Label homeLabel;

    @FXML
    private Label pricingLabel;

    @FXML
    private Label contactLabel;

    @FXML
    private Label aboutLabel;

    @FXML
    private VBox heroTextSection;

    @FXML
    private Text bodyText;

    @FXML
    private Button getStartedButton;

    @FXML
    private VBox mostViewedSection;

    @FXML
    private void initialize() {
        // Setup background circles with blur and noise effect
        if (bgCircle != null) {
            // Top left circle: reduced size, center positioned top and left of corner
            // Apply radial gradient that fades from center to edges
            RadialGradient gradient = new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#125A8B")),  // Color at center
                new Stop(1.0, Color.web("#125A8B", 0.0))  // Transparent at edges
            );
            bgCircle.setFill(gradient);
            bgCircle.setRadius(260); // Bigger radius
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

        try {
            heroImage.setImage(new Image(getClass().getResource("/application/images/hero.png").toExternalForm()));
        } catch (Exception e) {
            // Image not found, will be invisible
        }

        // Bottom right circle
        if (decorativeImage != null) {
            // Bottom right circle: 620W x 511H, center at bottom-right edge
            decorativeImage.setFill(Color.web("#125A8B"));
            decorativeImage.setOpacity(0.15); // 15% opacity
            decorativeImage.setRadius(330); // Slightly bigger radius
            // Combine blur and noise effect - apply BoxBlur first for noise, then GaussianBlur
            BoxBlur noise2 = new BoxBlur();
            noise2.setWidth(2);
            noise2.setHeight(2);
            noise2.setIterations(1);
            GaussianBlur blur2 = new GaussianBlur();
            blur2.setRadius(80); // Intense blur
            blur2.setInput(noise2); // Chain noise into blur
            decorativeImage.setEffect(blur2);
        }

        // Logo image - using homeicon
        try {
            logoImage.setImage(new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm()));
        } catch (Exception e) {
            // Image not found, will be invisible
        }

        // Make layout responsive - scale positions based on window size
        setupResponsiveLayout();
    }

    private void setupResponsiveLayout() {
        if (rootPane == null) return;

        // Base dimensions from the original design (1400x750)
        double baseWidth = 1400.0;
        double baseHeight = 750.0;

        // Bind element positions to scale with window size
        rootPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                double scaleX = newVal.doubleValue() / baseWidth;
                double scaleY = rootPane.getHeight() > 0 ? rootPane.getHeight() / baseHeight : 1.0;
                updateElementPositions(scaleX, scaleY);
            }
        });

        rootPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                double scaleY = newVal.doubleValue() / baseHeight;
                double scaleX = rootPane.getWidth() > 0 ? rootPane.getWidth() / baseWidth : 1.0;
                updateElementPositions(scaleX, scaleY);
            }
        });

        // Initial scaling - wait for scene to be set
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null && rootPane.getWidth() > 0 && rootPane.getHeight() > 0) {
                        updateElementPositions(rootPane.getWidth() / baseWidth, rootPane.getHeight() / baseHeight);
                    }
                });
                if (newScene.getWindow() != null && rootPane.getWidth() > 0 && rootPane.getHeight() > 0) {
                    updateElementPositions(rootPane.getWidth() / baseWidth, rootPane.getHeight() / baseHeight);
                }
            }
        });
    }

    private void updateElementPositions(double scaleX, double scaleY) {
        // Original positions (from base 1400x750 design)

        // Background circle (top left) - center positioned at top-left corner (0, 0)
        if (bgCircle != null) {
            // Center positioned exactly at top-left corner of screen
            bgCircle.setCenterX(0); // At left edge
            bgCircle.setCenterY(0);  // At top edge
            bgCircle.setRadius(260 * scaleX); // Bigger radius, scaled
        }

        // Hero image
        if (heroImage != null) {
            heroImage.setLayoutX(720 * scaleX);
            heroImage.setLayoutY(75 * scaleY);
            heroImage.setFitWidth(657 * scaleX);
            heroImage.setFitHeight(657 * scaleY);
        }

        // Decorative circle (bottom right) - positioned at bottom-right corner
        if (decorativeImage != null && rootPane != null) {
            // Position circle at bottom-right corner
            double paneWidth = rootPane.getWidth() > 0 ? rootPane.getWidth() : 1400 * scaleX;
            double paneHeight = rootPane.getHeight() > 0 ? rootPane.getHeight() : 750 * scaleY;
            double radius = 330 * scaleX;
            // Center at bottom-right corner
            decorativeImage.setCenterX(paneWidth);  // At right edge
            decorativeImage.setCenterY(paneHeight); // At bottom edge
            decorativeImage.setRadius(radius);
        }

        // Logo group - aligned with navigation (Y position 57)
        if (logoGroup != null) {
            logoGroup.setLayoutX(188 * scaleX);
            logoGroup.setLayoutY(57 * scaleY);  // Aligned with navigation
            logoGroup.setPrefWidth(291 * scaleX);
            logoGroup.setPrefHeight(52 * scaleY);
        }

        if (logoImage != null) {
            logoImage.setFitWidth(45 * scaleX);  // Bigger icon
            logoImage.setFitHeight(45 * scaleY);
        }

        // Navigation labels - all aligned to same Y position (57)
        double navY = 57 * scaleY;
        if (homeLabel != null) {
            homeLabel.setLayoutX(556 * scaleX);
            homeLabel.setLayoutY(navY);
        }
        if (pricingLabel != null) {
            pricingLabel.setLayoutX(683 * scaleX);
            pricingLabel.setLayoutY(navY);
        }
        if (contactLabel != null) {
            contactLabel.setLayoutX(799 * scaleX);
            contactLabel.setLayoutY(navY);
        }
        if (aboutLabel != null) {
            aboutLabel.setLayoutX(912 * scaleX);
            aboutLabel.setLayoutY(navY);
        }

        // Buttons - aligned to same Y position as navigation (57)
        if (loginButton != null) {
            loginButton.setLayoutX(1029 * scaleX);
            loginButton.setLayoutY(navY - 11);  // Slightly adjusted for button padding
        }
        if (signupButton != null) {
            signupButton.setLayoutX(1183 * scaleX);
            signupButton.setLayoutY(navY - 11);  // Slightly adjusted for button padding
        }

        // Hero text section
        if (heroTextSection != null) {
            heroTextSection.setLayoutX(191 * scaleX);
            heroTextSection.setLayoutY(231 * scaleY);
            heroTextSection.setPrefWidth(601 * scaleX);
            heroTextSection.setPrefHeight(167 * scaleY);
        }

        // Body text
        if (bodyText != null) {
            bodyText.setLayoutX(425 * scaleX);
            bodyText.setLayoutY(1095 * scaleY);
            bodyText.setWrappingWidth(595 * scaleX);
        }

        // Get started button - moved up a bit (from 524 to 480)
        if (getStartedButton != null) {
            getStartedButton.setLayoutX(191 * scaleX);
            getStartedButton.setLayoutY(480 * scaleY);  // Moved up
        }

        // Most viewed section
        if (mostViewedSection != null) {
            mostViewedSection.setLayoutX(593 * scaleX);
            mostViewedSection.setLayoutY(1011 * scaleY);
            mostViewedSection.setPrefWidth(279 * scaleX);
            mostViewedSection.setPrefHeight(84 * scaleY);
        }
    }

    @FXML
    private void onLoginButtonClick() {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            currentStage.close();

            Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
            AppWindow.showFixed(new Stage(), root, "Login System", AppWindow.LOGIN_WIDTH, AppWindow.LOGIN_HEIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSignupButtonClick() {
        try {
            Stage currentStage = (Stage) signupButton.getScene().getWindow();
            currentStage.close();

            Parent root = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            AppWindow.showFixed(new Stage(), root, "Sign Up", AppWindow.SIGNUP_WIDTH, AppWindow.SIGNUP_HEIGHT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setWelcomeText(String text) {
        welcomeText.setText(text);
    }
}
