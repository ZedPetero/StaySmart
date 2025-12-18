package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyCardController {

    @FXML private VBox cardContainer;
    @FXML private ImageView propertyImage;
    @FXML private Label nameLabel, locationLabel, priceLabel, typeLabel, floorsLabel;
    @FXML private FlowPane amenitiesContainer;
    @FXML private Button deleteBtn, editBtn, btnHeart;
    @FXML private HBox actionBox;

    private Property property;
    private MyPropertiesController parentController;

    public void setData(Property property, MyPropertiesController parentController) {
        this.property = property;
        this.parentController = parentController;

        // Populate basic text
        nameLabel.setText(property.getName());
        locationLabel.setText(property.getLocation());
        priceLabel.setText("₱ " + String.format("%,.0f", property.getPrice()));
        typeLabel.setText(property.getType());
        floorsLabel.setText(property.getFloors() + (property.getFloors().equals("1") ? " Floor" : " Floors"));

        // Load and center image
        if (property.getImagePath() != null && !property.getImagePath().isEmpty()) {
            File file = new File(property.getImagePath());
            if (file.exists()) {
                propertyImage.setImage(new Image(file.toURI().toString()));
                centerImage(propertyImage);
            }
        }

        populateAmenities(property.getAmenities());

        // ROLE-BASED VISIBILITY & LOGIC
        if (parentController != null) {
            // Landlord Mode
            deleteBtn.setVisible(true);
            editBtn.setVisible(true);
            btnHeart.setVisible(true);
            btnHeart.setManaged(true);
            btnHeart.setStyle("-fx-background-color: white; -fx-text-fill: #e0e0e0; -fx-background-radius: 50;");
            deleteBtn.setManaged(true);

            deleteBtn.setOnAction(event -> {
                event.consume();
                parentController.deleteProperty(property);
            });

            editBtn.setOnAction(event -> {
                event.consume();
                parentController.openEditPropertyDialog(property);
            });

            cardContainer.setOnMouseClicked(e -> parentController.openPropertyDetails(property));
        } else {
            // Tenant Mode
            deleteBtn.setVisible(false);
            editBtn.setVisible(false);
            btnHeart.setVisible(true);
            deleteBtn.setManaged(false);
            btnHeart.setManaged(true);

            // Opens details with setTenantMode(true)
            cardContainer.setOnMouseClicked(e -> openHouseView(true));

            // Initial check to see if already saved (optional polish)
            checkIfAlreadySaved();
        }
    }

    private void openHouseView(boolean isTenant) {
        try {
            List<Floor> floors = fetchFloorsAndRooms(property.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HouseView.fxml"));
            Parent root = loader.load();

            HouseViewController houseController = loader.getController();
            houseController.setupPropertyData(property, floors);
            houseController.setTenantMode(isTenant);

            Stage stage = new Stage();
            stage.setTitle(property.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Floor> fetchFloorsAndRooms(int propertyId) {
        Map<Integer, Floor> floorMap = new HashMap<>();
        String query = "SELECT * FROM rooms WHERE property_id = ? ORDER BY floor_level ASC, room_number ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int level = rs.getInt("floor_level");
                Floor floor = floorMap.computeIfAbsent(level, k -> new Floor(k));
                Room room = new Room(
                        rs.getInt("id"),
                        rs.getInt("property_id"),
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        rs.getString("facilities"),
                        rs.getString("payment_status"),
                        rs.getString("image_path")
                );
                floor.addRoom(room);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ArrayList<>(floorMap.values());
    }

    @FXML
    private void handleHeartClick(javafx.event.ActionEvent event) {
        event.consume(); // Don't trigger card click
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("SavePropertyDialog.fxml"));
            Parent root = loader.load();

            SavePropertyDialogController controller = loader.getController();
            controller.setPropertyId(property.getId());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                btnHeart.setStyle("-fx-text-fill: #e74c3c;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIfAlreadySaved() {
        String sql = "SELECT COUNT(*) FROM saved_properties WHERE tenant_id = ? AND property_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, LoginController.getCurrentUser().getId());
            pstmt.setInt(2, property.getId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // If saved in ANY category, make it red
                btnHeart.setStyle("-fx-background-color: white; -fx-text-fill: #e74c3c; -fx-background-radius: 50;");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- Original Helpers ---
    private void populateAmenities(String amenitiesString) {
        amenitiesContainer.getChildren().clear();
        if (amenitiesString == null || amenitiesString.trim().isEmpty()) return;
        String[] items = amenitiesString.split(",");
        for (int i = 0; i < Math.min(items.length, 4); i++) {
            Label tag = new Label(items[i].trim());
            tag.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 4;");
            amenitiesContainer.getChildren().add(tag);
        }
    }

    private void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = img.getWidth();
            double h = img.getHeight();
            double targetRatio = 300.0 / 160.0;
            double sourceRatio = w / h;
            double viewW, viewH, viewX, viewY;
            if (sourceRatio > targetRatio) {
                viewH = h; viewW = h * targetRatio;
                viewX = (w - viewW) / 2; viewY = 0;
            } else {
                viewW = w; viewH = w / targetRatio;
                viewX = 0; viewY = (h - viewH) / 2;
            }
            imageView.setViewport(new Rectangle2D(viewX, viewY, viewW, viewH));
        }
    }
}