package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class LandlordLayoutController {

    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane contentArea;

    // Navigation Buttons
    @FXML private HBox btnOverview;
    @FXML private HBox btnProperties;
    @FXML private HBox btnApplications;
    @FXML private HBox btnTenants;
    @FXML private HBox btnMessages;
    @FXML private HBox btnSettings;

    // TRACKING: We need to know which controller is currently active
    private Object currentController;

    @FXML
    public void initialize() {
        // 1. Load the default page
        loadPage("/application/Overview.fxml");

        // 2. Set Overview as active
        setActiveButton(btnOverview);
    }

    // =========================================================
    // GLOBAL HEADER ACTIONS
    // =========================================================

    @FXML
    private void handleGlobalAddProperty() {
        // 1. If we are NOT on the properties page, go there first
        if (!(currentController instanceof MyPropertiesController)) {
            handleShowProperties(); // This loads the page and updates 'currentController'
        }

        // 2. Now that we are definitely on the page, trigger the add dialog
        if (currentController instanceof MyPropertiesController) {
            ((MyPropertiesController) currentController).handleAddProperty();
        }
    }

    // =========================================================
    // NAVIGATION HANDLERS
    // =========================================================

    @FXML private void handleShowOverview() {
        loadPage("/application/Overview.fxml");
        setActiveButton(btnOverview);
    }

    @FXML private void handleShowProperties() {
        loadPage("/application/My Properties.fxml");
        setActiveButton(btnProperties);
    }

    @FXML private void handleShowApplications() {
        loadPage("/application/LandlordApplications.fxml");
        setActiveButton(btnApplications);
    }

    @FXML private void handleShowTenants() {
        loadPage("/application/Tenants.fxml");
        setActiveButton(btnTenants);
    }

    @FXML private void handleShowMessages() {
        loadPage("/application/Messages.fxml");
        setActiveButton(btnMessages);
    }

    @FXML private void handleShowSettings() {
        loadPage("/application/Settings.fxml");
        setActiveButton(btnSettings);
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private void setActiveButton(HBox activeButton) {
        resetButtonStyle(btnOverview);
        resetButtonStyle(btnProperties);
        resetButtonStyle(btnApplications);
        resetButtonStyle(btnTenants);
        resetButtonStyle(btnMessages);
        resetButtonStyle(btnSettings);

        activeButton.getStyleClass().removeAll("menu-item");
        activeButton.getStyleClass().add("menu-item-active");

        if (activeButton.getChildren().get(0) instanceof ImageView) {
            ImageView icon = (ImageView) activeButton.getChildren().get(0);
            ColorAdjust whiteEffect = new ColorAdjust();
            whiteEffect.setBrightness(1.0);
            icon.setEffect(whiteEffect);
        }
    }

    private void resetButtonStyle(HBox button) {
        button.getStyleClass().removeAll("menu-item-active");
        if (!button.getStyleClass().contains("menu-item")) {
            button.getStyleClass().add("menu-item");
        }

        if (button.getChildren().get(0) instanceof ImageView) {
            ImageView icon = (ImageView) button.getChildren().get(0);
            icon.setEffect(null);
        }
    }

    private void loadPage(String fxmlFileName) {
        try {
            URL fileUrl = getClass().getResource(fxmlFileName);
            if (fileUrl == null) {
                System.out.println("ERROR: Could not find file: " + fxmlFileName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fileUrl);
            Parent newPage = loader.load();

            // KEY CHANGE: Capture the controller of the loaded page
            this.currentController = loader.getController();

            contentArea.getChildren().setAll(newPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}