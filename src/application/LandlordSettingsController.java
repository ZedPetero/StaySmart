package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class LandlordSettingsController {

    @FXML private HBox btnProfile;
    @FXML private HBox btnAccount;
    @FXML private HBox btnPayment;
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load Profile by default when page opens
        loadView("SettingsProfile.fxml");
    }

    @FXML
    private void handleShowProfile() {
        setActiveState(btnProfile);
        loadView("SettingsProfile.fxml");
    }

    @FXML
    private void handleShowAccount() {
        setActiveState(btnAccount);
        loadView("SettingsAccount.fxml");
    }

    @FXML
    private void handleShowPayment() {
        setActiveState(btnPayment);
        loadView("SettingsPayment.fxml");
    }

    private void loadView(String fxmlFileName) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlFileName));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveState(HBox activeBox) {
        // Remove 'menu-active' class from all buttons
        btnProfile.getStyleClass().remove("menu-active");
        btnAccount.getStyleClass().remove("menu-active");
        btnPayment.getStyleClass().remove("menu-active");

        // Add 'menu-active' to the clicked button
        activeBox.getStyleClass().add("menu-active");
    }
}