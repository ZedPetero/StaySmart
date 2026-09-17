package application;

import application.model.Activity;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class LandlordOverviewController {

    @FXML private Label lblWelcome;
    @FXML private Label lblTotalProperties;
    @FXML private Label lblActiveTenants;
    @FXML private Label lblMonthlyRevenue;
    @FXML private Label lblPendingApps;
    @FXML private Label lblPropertiesChange;
    @FXML private Label lblOccupancy;
    @FXML private Label lblRevenueChange;
    @FXML private Label lblPendingChange;
    @FXML private VBox activityContainer;
    @FXML private VBox statusContainer;

    private static final int ACTIVITY_LIMIT = 5;
    private static final int STATUS_LIMIT = 5;

    @FXML
    public void initialize() {
        User user = LoginController.getCurrentUser();
        if (user == null) return;

        String name = user.getFullname();
        lblWelcome.setText("Hello " + (name != null && !name.isEmpty() ? name.split(" ")[0] : "Landlord") + "!");

        loadStats(user.getId());
        loadRecentActivity(user.getId());
        loadPropertyStatus(user.getId());
    }

    // ---------- Stat cards ----------

    private void loadStats(int userId) {
        Map<String, Integer> stats = DatabaseHandler.getDashboardStats(userId, "landlord");

        int totalProperties = stats.getOrDefault("totalProperties", 0);
        int addedThisMonth = stats.getOrDefault("propertiesThisMonth", 0);
        lblTotalProperties.setText(String.valueOf(totalProperties));
        lblPropertiesChange.setText(addedThisMonth > 0
                ? "+" + addedThisMonth + " this month"
                : (totalProperties == 0 ? "Add your first property" : "No new properties this month"));

        int totalRooms = stats.getOrDefault("totalRooms", 0);
        int occupiedRooms = stats.getOrDefault("occupiedRooms", 0);
        lblActiveTenants.setText(String.valueOf(stats.getOrDefault("activeTenants", 0)));
        lblOccupancy.setText(totalRooms > 0
                ? Math.round(occupiedRooms * 100.0 / totalRooms) + "% occupancy (" + occupiedRooms + "/" + totalRooms + " rooms)"
                : "No rooms yet");

        int revenue = stats.getOrDefault("monthlyRevenue", 0);
        int paidRooms = stats.getOrDefault("paidRooms", 0);
        lblMonthlyRevenue.setText("₱" + String.format("%,d", revenue));
        lblRevenueChange.setText(occupiedRooms > 0
                ? paidRooms + " of " + occupiedRooms + " occupied rooms paid"
                : "No occupied rooms");

        int pending = stats.getOrDefault("pendingApps", 0);
        int pendingToday = stats.getOrDefault("pendingToday", 0);
        lblPendingApps.setText(String.valueOf(pending));
        lblPendingChange.setText(pendingToday > 0
                ? pendingToday + " new today"
                : (pending > 0 ? "Awaiting your review" : "All caught up"));
    }

    // ---------- Recent activity ----------

    private void loadRecentActivity(int userId) {
        activityContainer.getChildren().clear();
        List<Activity> items = DatabaseHandler.getLandlordRecentActivity(userId, ACTIVITY_LIMIT);

        if (items.isEmpty()) {
            activityContainer.getChildren().add(emptyLabel("No activity yet. New applications and messages will show up here."));
            return;
        }
        for (Activity item : items) {
            activityContainer.getChildren().add(createActivityCard(item));
        }
    }

    private HBox createActivityCard(Activity item) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("activity-card");

        String iconBg;
        String iconFile;
        switch (item.getKind()) {
            case MESSAGE: iconBg = "icon-bg-green"; iconFile = "bubble-discussion.png"; break;
            case TOUR: iconBg = "icon-bg-orange"; iconFile = "icon-calendar.png"; break;
            default: iconBg = "icon-bg-blue"; iconFile = "document-signed.png";
        }
        card.getChildren().add(createIcon(iconBg, iconFile));

        VBox text = new VBox();
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label main = new Label(item.getTitle());
        main.getStyleClass().add("activity-main-text");
        Label sub = new Label(item.getSubtitle() != null ? item.getSubtitle() : "");
        sub.getStyleClass().add("activity-sub-text");
        sub.setMaxWidth(320);
        text.getChildren().addAll(main, sub);
        card.getChildren().add(text);

        Label time = new Label(timeAgo(item.getTime()));
        time.getStyleClass().add("activity-time-text");
        card.getChildren().add(time);
        return card;
    }

    // ---------- Property status ----------

    private void loadPropertyStatus(int userId) {
        statusContainer.getChildren().clear();
        List<Map<String, Object>> rows = DatabaseHandler.getLandlordRoomStatus(userId, STATUS_LIMIT);

        if (rows.isEmpty()) {
            statusContainer.getChildren().add(emptyLabel("No rooms yet. Add a property to see its rooms here."));
            return;
        }
        for (Map<String, Object> row : rows) {
            statusContainer.getChildren().add(createStatusCard(row));
        }
    }

    private HBox createStatusCard(Map<String, Object> row) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("status-card");

        String status = (String) row.get("status");
        String tenant = (String) row.get("tenantName");
        boolean occupied = "Occupied".equalsIgnoreCase(status);
        boolean maintenance = "Maintenance".equalsIgnoreCase(status);

        VBox text = new VBox();
        text.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label name = new Label(row.get("propertyName") + " - Room " + row.get("roomNumber"));
        name.getStyleClass().add("property-name");
        Label sub = new Label(occupied
                ? "Tenant: " + (tenant != null ? tenant : "Not recorded")
                : (maintenance ? "Under maintenance" : "Available for rent"));
        sub.getStyleClass().add(occupied ? "property-tenant" : "property-tenant-vacant");
        text.getChildren().addAll(name, sub);

        VBox right = new VBox(5);
        right.setAlignment(Pos.CENTER_RIGHT);
        double price = (Double) row.get("price");
        Label priceLbl = new Label(price > 0 ? "₱" + String.format("%,.0f", price) + "/mo" : "Price not set");
        priceLbl.getStyleClass().add("property-price");

        StackPane tag = new StackPane();
        tag.setPrefSize(80, 25);
        tag.getStyleClass().add(occupied ? "occupied-tag-container" : "vacant-tag-container");
        Label tagText = new Label(occupied ? "Occupied" : (maintenance ? "Maintenance" : "Vacant"));
        tagText.getStyleClass().add(occupied ? "occupied-tag-text" : "vacant-tag-text");
        tag.getChildren().add(tagText);
        right.getChildren().addAll(priceLbl, tag);

        card.getChildren().addAll(text, right);
        return card;
    }

    @FXML
    private void handleViewAll() {
        LandlordLayoutController layout = LandlordLayoutController.getInstance();
        if (layout != null) layout.showProperties();
    }

    // ---------- Helpers ----------

    private StackPane createIcon(String bgClass, String imageFile) {
        StackPane bg = new StackPane();
        bg.getStyleClass().add(bgClass);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResource("/application/images/" + imageFile).toExternalForm()));
            iv.setFitWidth(30);
            iv.setFitHeight(30);
            iv.setPreserveRatio(true);
            bg.getChildren().add(iv);
        } catch (Exception ignored) {
        }
        return bg;
    }

    private Label emptyLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("activity-sub-text");
        lbl.setWrapText(true);
        lbl.setStyle("-fx-padding: 10 0;");
        return lbl;
    }

    static String timeAgo(Timestamp time) {
        if (time == null) return "";
        long seconds = Math.max(0, (System.currentTimeMillis() - time.getTime()) / 1000);
        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 30) return days + (days == 1 ? " day ago" : " days ago");
        long months = days / 30;
        if (months < 12) return months + (months == 1 ? " month ago" : " months ago");
        long years = days / 365;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}
