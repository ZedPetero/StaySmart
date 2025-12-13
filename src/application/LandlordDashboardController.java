package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.IOException;

public class LandlordDashboardController {

    @FXML private HBox overviewMenuItem;
    @FXML private HBox myPropertiesMenuItem;
    @FXML private HBox applicationsMenuItem;
    @FXML private HBox tenantsMenuItem;
    @FXML private HBox messageMenuItem;
    @FXML private HBox settingsMenuItem;
    
    @FXML private BorderPane mainBorderPane;
    @FXML private VBox dashboardContent;
    @FXML private VBox propertiesContent;
    @FXML private VBox applicationsContent;
    @FXML private VBox tenantsContent;
    @FXML private VBox messageContent;
    @FXML private VBox settingsContent;

    @FXML
    private void initialize() {
        // Initialize with dashboard content visible and all other content hidden
        if (propertiesContent != null) {
            propertiesContent.setVisible(false);
            propertiesContent.setManaged(false);
        }
        if (applicationsContent != null) {
            applicationsContent.setVisible(false);
            applicationsContent.setManaged(false);
        }
        if (tenantsContent != null) {
            tenantsContent.setVisible(false);
            tenantsContent.setManaged(false);
        }
        if (messageContent != null) {
            messageContent.setVisible(false);
            messageContent.setManaged(false);
        }
        if (settingsContent != null) {
            settingsContent.setVisible(false);
            settingsContent.setManaged(false);
        }
    }

    @FXML
    private void onOverviewClicked(MouseEvent event) {
        setActiveMenuItem(overviewMenuItem);
        showDashboardContent();
    }

    @FXML
    private void onMyPropertiesClicked(MouseEvent event) {
        setActiveMenuItem(myPropertiesMenuItem);
        showPropertiesContent();
    }

    @FXML
    private void onApplicationsClicked(MouseEvent event) {
        setActiveMenuItem(applicationsMenuItem);
        showApplicationsContent();
    }

    @FXML
    private void onTenantsClicked(MouseEvent event) {
        setActiveMenuItem(tenantsMenuItem);
        showTenantsContent();
    }

    @FXML
    private void onMessageClicked(MouseEvent event) {
        setActiveMenuItem(messageMenuItem);
        showMessageContent();
    }

    @FXML
    private void onSettingsClicked(MouseEvent event) {
        setActiveMenuItem(settingsMenuItem);
        showSettingsContent();
    }

    private void setActiveMenuItem(HBox activeItem) {
        // Remove active style from all menu items
        if (overviewMenuItem != null) {
            overviewMenuItem.getStyleClass().remove("menu-item-active");
            overviewMenuItem.getStyleClass().add("menu-item");
            ((Label) overviewMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) overviewMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }
        if (myPropertiesMenuItem != null) {
            myPropertiesMenuItem.getStyleClass().remove("menu-item-active");
            myPropertiesMenuItem.getStyleClass().add("menu-item");
            ((Label) myPropertiesMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) myPropertiesMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }
        if (applicationsMenuItem != null) {
            applicationsMenuItem.getStyleClass().remove("menu-item-active");
            applicationsMenuItem.getStyleClass().add("menu-item");
            ((Label) applicationsMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) applicationsMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }
        if (tenantsMenuItem != null) {
            tenantsMenuItem.getStyleClass().remove("menu-item-active");
            tenantsMenuItem.getStyleClass().add("menu-item");
            ((Label) tenantsMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) tenantsMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }
        if (messageMenuItem != null) {
            messageMenuItem.getStyleClass().remove("menu-item-active");
            messageMenuItem.getStyleClass().add("menu-item");
            ((Label) messageMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) messageMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }
        if (settingsMenuItem != null) {
            settingsMenuItem.getStyleClass().remove("menu-item-active");
            settingsMenuItem.getStyleClass().add("menu-item");
            ((Label) settingsMenuItem.getChildren().get(1)).getStyleClass().remove("menu-text-active");
            ((Label) settingsMenuItem.getChildren().get(1)).getStyleClass().add("menu-text");
        }

        // Add active style to selected menu item
        if (activeItem != null) {
            activeItem.getStyleClass().remove("menu-item");
            activeItem.getStyleClass().add("menu-item-active");
            ((Label) activeItem.getChildren().get(1)).getStyleClass().remove("menu-text");
            ((Label) activeItem.getChildren().get(1)).getStyleClass().add("menu-text-active");
        }
    }

    private void showDashboardContent() {
        if (dashboardContent != null) {
            dashboardContent.setVisible(true);
            dashboardContent.setManaged(true);
        }
        if (propertiesContent != null) {
            propertiesContent.setVisible(false);
            propertiesContent.setManaged(false);
        }
    }

    private void showPropertiesContent() {
        if (dashboardContent != null) {
            dashboardContent.setVisible(false);
            dashboardContent.setManaged(false);
        }
        if (propertiesContent != null) {
            propertiesContent.setVisible(true);
            propertiesContent.setManaged(true);
        }
    }

    private void hideAllContent() {
        if (dashboardContent != null) {
            dashboardContent.setVisible(false);
            dashboardContent.setManaged(false);
        }
        if (propertiesContent != null) {
            propertiesContent.setVisible(false);
            propertiesContent.setManaged(false);
        }
        if (applicationsContent != null) {
            applicationsContent.setVisible(false);
            applicationsContent.setManaged(false);
        }
        if (tenantsContent != null) {
            tenantsContent.setVisible(false);
            tenantsContent.setManaged(false);
        }
        if (messageContent != null) {
            messageContent.setVisible(false);
            messageContent.setManaged(false);
        }
        if (settingsContent != null) {
            settingsContent.setVisible(false);
            settingsContent.setManaged(false);
        }
    }

    private void showApplicationsContent() {
        hideAllContent();
        if (applicationsContent != null) {
            applicationsContent.setVisible(true);
            applicationsContent.setManaged(true);
        }
    }

    private void showTenantsContent() {
        hideAllContent();
        if (tenantsContent != null) {
            tenantsContent.setVisible(true);
            tenantsContent.setManaged(true);
        }
    }

    private void showMessageContent() {
        hideAllContent();
        if (messageContent != null) {
            messageContent.setVisible(true);
            messageContent.setManaged(true);
        }
    }

    private void showSettingsContent() {
        hideAllContent();
        if (settingsContent != null) {
            settingsContent.setVisible(true);
            settingsContent.setManaged(true);
        }
    }
}
