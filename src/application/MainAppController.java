package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class MainAppController implements Initializable {

    @FXML private Label roleTab;
    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private TextArea adminContent;
    @FXML private TextArea userContent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = LoginController.getCurrentUser();
        
        if (currentUser != null) {
            // Set role tab
            if (currentUser.isAdmin()) {
                roleTab.setText("ADMIN");
                roleTab.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 3;");
                adminContent.setVisible(true);
                adminContent.setText("Welcome Administrator!\n\nYou have access to:\n• User Management\n• System Settings\n• Database Administration\n• Reports and Analytics");
            } else {
                roleTab.setText("USER");
                roleTab.setStyle("-fx-background-color: #4C9AFF; -fx-text-fill: white; -fx-padding: 5 10 5 10; -fx-background-radius: 3;");
                userContent.setVisible(true);
                userContent.setText("Welcome User!\n\nYou have access to:\n• View Profile\n• Basic Features\n• Personal Settings");
            }
            
            // Set welcome message
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
            userInfoLabel.setText("Logged in as: " + currentUser.getUsername() + " | Role: " + currentUser.getRole());
        }
    }

    @FXML
    private void onExit(ActionEvent event) {
        Stage stage = (Stage) roleTab.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("JavaFX Application");
        alert.setContentText("Role-based login system with JavaFX and MySQL");
        alert.showAndWait();
    }
}