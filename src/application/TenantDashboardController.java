package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.net.URL;

public class TenantDashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    private HBox btnDashboard;
    @FXML
    private HBox btnExplore;
    @FXML
    private HBox btnSaved;
    @FXML
    private HBox btnApplications;
    @FXML
    private HBox btnMessages;
    @FXML
    private HBox btnProfile;
    @FXML
    private HBox btnSettings;
    @FXML
    private HBox btnLogout;

    @FXML
    private void handleLogout() {
        LoginController.setCurrentUser(null);
        UserSession.cleanUserSession();
        try {
            // Close the maximized dashboard and open the fixed-size login window fresh,
            // the same way the homepage does, instead of squeezing the login form into a maximized stage.
            javafx.stage.Stage current = (javafx.stage.Stage) btnLogout.getScene().getWindow();
            current.close();

            Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
            AppWindow.showFixed(new javafx.stage.Stage(), root, "Login System", AppWindow.LOGIN_WIDTH, AppWindow.LOGIN_HEIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Start with Dashboard
        handleShowDashboard();
    }

    @FXML
    private void handleShowDashboard() {
        loadPage("TenantOverview.fxml");
        highlightButton(btnDashboard);
    }

    @FXML
    private void handleShowExplore() {
        loadPage("ExploreProperties.fxml");
        highlightButton(btnExplore);
    }

    @FXML
    private void handleShowSaved() {
        loadPage("TenantSavedProperties.fxml");
        highlightButton(btnSaved);
    }

    @FXML
    private void handleShowApplications() {
        loadPage("TenantApplications.fxml");
        highlightButton(btnApplications);
    }

    @FXML
    private void handleShowMessages() {
        loadPage("TenantMessages.fxml");
        highlightButton(btnMessages);
    }

    @FXML
    private void handleShowProfile() {
        loadPage("TenantProfile.fxml");
        highlightButton(btnProfile);
    }

    @FXML
    private void handleShowSettings() {
        loadPage("TenantSettings.fxml");
        highlightButton(btnSettings);
    }

    private void loadPage(String fxmlFile) {
        try {
            URL fileUrl = getClass().getResource(fxmlFile);
            if (fileUrl == null) {
                System.out.println("FXML file not found: " + fxmlFile);
                return;
            }
            Parent view = FXMLLoader.load(fileUrl);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void highlightButton(HBox activeBox) {
        // 1. Reset ALL buttons to Inactive State
        resetButtonVisuals(btnDashboard);
        resetButtonVisuals(btnExplore);
        resetButtonVisuals(btnSaved);
        resetButtonVisuals(btnApplications);
        resetButtonVisuals(btnMessages);
        resetButtonVisuals(btnProfile);
        resetButtonVisuals(btnSettings);
        resetButtonVisuals(btnLogout); // never "active", but its icon must be white on the dark sidebar too

        // 2. Set the Clicked Button to Active State
        if (!activeBox.getStyleClass().contains("active")) {
            activeBox.getStyleClass().add("active");
        }

        // 3. Make the Icon Dark Blue (Original Color)
        // Assuming your PNGs are naturally black/dark blue.
        ImageView icon = (ImageView) activeBox.getChildren().get(0);
        icon.setEffect(null); // Remove any white filter
    }

    private void resetButtonVisuals(HBox box) {
        box.getStyleClass().remove("active");

        // Apply "White" filter to icon so it shows up on the dark background
        ImageView icon = (ImageView) box.getChildren().get(0);
        ColorAdjust whiteEffect = new ColorAdjust();
        whiteEffect.setBrightness(1.0); // 1.0 = All White
        icon.setEffect(whiteEffect);
    }
}