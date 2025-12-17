package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplorePropertiesController {

    @FXML private GridPane propertiesGrid;
    @FXML private TextField txtSearch;
    @FXML private VBox emptyStateBox;

    @FXML
    public void initialize() {
        loadProperties(""); // Load all on start
    }

    @FXML
    private void handleSearch() {
        loadProperties(txtSearch.getText());
    }

    private void loadProperties(String searchTerm) {
        propertiesGrid.getChildren().clear();

        // 1. SELECT * to show properties from ALL landlords
        String sql = "SELECT * FROM properties";
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql += " WHERE name LIKE ? OR location LIKE ?";
        }

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            int column = 0;
            int row = 1;
            boolean hasProperties = false;

            while (rs.next()) {
                hasProperties = true;

                // Construct Property Object matching your Constructor
                Property p = new Property(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getDouble("price"), // Database has price column
                        rs.getString("type"),
                        rs.getString("floors"), // Usually "total_floors" in DB, check your column name
                        rs.getString("image_path")
                );

                // Create the Card UI (Mimicking Landlord View)
                VBox card = createPropertyCard(p);
                propertiesGrid.add(card, column, row);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            if (!hasProperties) {
                emptyStateBox.setVisible(true);
                emptyStateBox.setManaged(true);
            } else {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Card Generation (Matches Landlord Style) ---
    private VBox createPropertyCard(Property p) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 0 0 15 0;");

        // Drop Shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        card.setEffect(shadow);

        // 1. Image Area
        StackPane imgContainer = new StackPane();
        ImageView iv = new ImageView();
        iv.setFitWidth(280); // Adjust width to fit grid
        iv.setFitHeight(160);
        iv.setPreserveRatio(false);

        // Load Image
        if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
            try {
                File file = new File(p.getImagePath());
                if(file.exists()) iv.setImage(new Image(file.toURI().toString()));
            } catch (Exception e) {}
        }

        // Clip Image to match card corners
        Rectangle clip = new Rectangle(280, 160);
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        iv.setClip(clip);
        imgContainer.getChildren().add(iv);

        // 2. Text Details
        VBox details = new VBox(5);
        details.setPadding(new Insets(10, 15, 0, 15));

        Label typeLbl = new Label(p.getType() != null ? p.getType().toUpperCase() : "PROPERTY");
        typeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #4da6ff; -fx-font-weight: bold;");

        Label nameLbl = new Label(p.getName());
        nameLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #333; -fx-font-family: 'Outfit Bold'; -fx-font-weight: bold;");

        Label priceLbl = new Label("₱ " + String.format("%.2f", p.getPrice()) + " / mo");
        priceLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #113970;");

        details.getChildren().addAll(typeLbl, nameLbl, priceLbl);

        // 3. Button
        Button btnCheck = new Button("Check Details");
        btnCheck.setMaxWidth(Double.MAX_VALUE); // Full width
        btnCheck.setStyle("-fx-background-color: #e6f0ff; -fx-text-fill: #0066cc; -fx-background-radius: 5; -fx-cursor: hand;");
        btnCheck.setOnAction(e -> openPropertyDetails(p));

        VBox btnContainer = new VBox(btnCheck);
        btnContainer.setPadding(new Insets(10, 15, 0, 15));

        card.getChildren().addAll(imgContainer, details, btnContainer);
        return card;
    }

    // In ExplorePropertiesController.java

    private void openPropertyDetails(Property property) {
        try {
            List<Floor> floors = fetchFloorsAndRooms(property.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HouseView.fxml"));
            Parent root = loader.load();

            HouseViewController houseController = loader.getController();

            // 1. Set Data
            houseController.setupPropertyData(property, floors);

            // 2. ENABLE TENANT MODE (This hides the "+" buttons and enables applying)
            houseController.setTenantMode(true);

            Stage stage = new Stage();
            stage.setTitle(property.getName());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Helper to fetch Room Data (Copy of logic from MyPropertiesController) ---
    private List<Floor> fetchFloorsAndRooms(int propertyId) {
        Map<Integer, Floor> floorMap = new HashMap<>();
        String query = "SELECT * FROM rooms WHERE property_id = ? ORDER BY floor_level ASC, room_number ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int level = rs.getInt("floor_level");
                // Create floor if not exists
                Floor floor = floorMap.computeIfAbsent(level, k -> new Floor(k));

                // Create room
                Room room = new Room(
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        rs.getString("image_path"),
                        rs.getString("facilities"),
                        rs.getString("payment_status")
                );
                floor.addRoom(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(floorMap.values());
    }
}