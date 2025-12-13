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

public class LandlordLayoutController {

    @FXML private BorderPane mainBorderPane;

    // Inject the buttons from FXML
    @FXML private HBox btnOverview;
    @FXML private HBox btnProperties;
    @FXML private HBox btnApplications;
    @FXML private HBox btnTenants;
    @FXML private HBox btnMessages;
    @FXML private HBox btnSettings;

    @FXML
    public void initialize() {
        // 1. Load the default page
        loadPage("/application/Overview.fxml");

        // 2. MANUALLY set the button to active so it highlights immediately
        setActiveButton(btnOverview);
    }

    @FXML private void handleShowOverview() {
        loadPage("/application/Overview.fxml");
        setActiveButton(btnOverview);
    }

    @FXML private void handleShowProperties() {
        loadPage("/application/My Properties.fxml");
        setActiveButton(btnProperties);
    }

    @FXML private void handleShowApplications() {
        loadPage("/application/Applications.fxml");
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

    private void setActiveButton(HBox activeButton) {
        // 1. Reset ALL buttons (Style + Icon Color)
        resetButtonStyle(btnOverview);
        resetButtonStyle(btnProperties);
        resetButtonStyle(btnApplications);
        resetButtonStyle(btnTenants);
        resetButtonStyle(btnMessages);
        resetButtonStyle(btnSettings);

        // 2. Set Active Style
        activeButton.getStyleClass().removeAll("menu-item");
        activeButton.getStyleClass().add("menu-item-active");

        // 3. Apply White Color Effect to the Icon
        // (Assumes the ImageView is the first child, index 0)
        if (activeButton.getChildren().get(0) instanceof ImageView) {
            ImageView icon = (ImageView) activeButton.getChildren().get(0);
            ColorAdjust whiteEffect = new ColorAdjust();
            whiteEffect.setBrightness(1.0); // Make it 100% bright (White)
            icon.setEffect(whiteEffect);
        }
    }

    private void resetButtonStyle(HBox button) {
        // Remove active class
        button.getStyleClass().removeAll("menu-item-active");
        if (!button.getStyleClass().contains("menu-item")) {
            button.getStyleClass().add("menu-item");
        }

        // Remove Icon Effect (Return to original color)
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
            mainBorderPane.setCenter(newPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}