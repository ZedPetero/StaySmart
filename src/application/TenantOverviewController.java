package application;

import application.model.Application;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class TenantOverviewController {

    @FXML
    private Label lblWelcomeName;
    @FXML
    private Label lblSavedCount;
    @FXML
    private Label lblAppCount;
    @FXML
    private Label lblTourCount;
    @FXML
    private Label lblViewedCount;
    @FXML
    private VBox toursContainer;
    @FXML
    private VBox emptyToursBox;

    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - hh:mm a");

    @FXML
    public void initialize() {
        if (LoginController.getCurrentUser() != null) {
            String fullName = LoginController.getCurrentUser().getFullname();
            lblWelcomeName.setText("Hello, " + fullName + "!");

            loadStats();
            loadUpcomingTours();
        }
    }

    private void loadStats() {
        int userId = LoginController.getCurrentUser().getId();
        Map<String, Integer> stats = DatabaseHandler.getDashboardStats(userId, "tenant");

        lblSavedCount.setText(String.valueOf(stats.getOrDefault("savedProperties", 0)));
        lblAppCount.setText(String.valueOf(stats.getOrDefault("activeApps", 0)));
        lblTourCount.setText(String.valueOf(stats.getOrDefault("activeTours", 0)));
        lblViewedCount.setText(String.valueOf(stats.getOrDefault("viewedProperties", 0)));
    }

    private void loadUpcomingTours() {
        int userId = LoginController.getCurrentUser().getId();
        List<Application> tours = DatabaseHandler.getUpcomingTours(userId);

        toursContainer.getChildren().clear();

        if (tours.isEmpty()) {
            emptyToursBox.setVisible(true);
            emptyToursBox.setManaged(true);
        } else {
            emptyToursBox.setVisible(false);
            emptyToursBox.setManaged(false);

            for (Application tour : tours) {
                toursContainer.getChildren().add(createTourItem(tour));
            }
        }
    }

    private HBox createTourItem(Application tour) {
        HBox item = new HBox(15);
        item.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(5);
        Label title = new Label(tour.getPropertyName() + " - Room " + tour.getRoomNumber());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label date = new Label("Scheduled: " + sdf.format(tour.getApplyDate())); // Using applyDate as schedule for now
        date.setStyle("-fx-text-fill: #666;");

        info.getChildren().addAll(title, date);
        item.getChildren().add(info);

        return item;
    }
}