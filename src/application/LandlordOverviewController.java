package application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.Map;

public class LandlordOverviewController {

    @FXML
    private Label lblTotalProperties;
    @FXML
    private Label lblActiveTenants;
    @FXML
    private Label lblMonthlyRevenue;
    @FXML
    private Label lblPendingApps;

    @FXML
    public void initialize() {
        if (LoginController.getCurrentUser() != null) {
            loadStats();
        }
    }

    private void loadStats() {
        int userId = LoginController.getCurrentUser().getId();
        Map<String, Integer> stats = DatabaseHandler.getDashboardStats(userId, "landlord");

        lblTotalProperties.setText(String.valueOf(stats.getOrDefault("totalProperties", 0)));
        lblActiveTenants.setText(String.valueOf(stats.getOrDefault("activeTenants", 0)));

        int revenue = stats.getOrDefault("monthlyRevenue", 0);
        lblMonthlyRevenue.setText("₱" + String.format("%,d", revenue));

        lblPendingApps.setText(String.valueOf(stats.getOrDefault("pendingApps", 0)));
    }
}
