package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MyPropertiesController {

    // Matches the fx:id="propertiesGrid" in your FXML
    @FXML private GridPane propertiesGrid;

    // Matches the fx:id="emptyStateBox"
    @FXML private VBox emptyStateBox;

    @FXML
    public void initialize() {
        loadPropertiesFromDatabase();
    }

    public void loadPropertiesFromDatabase() {
        // Clear previous items
        propertiesGrid.getChildren().clear();

        String query = "SELECT * FROM properties";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            int column = 0;
            int row = 1;

            boolean hasProperties = false;

            while (rs.next()) {
                hasProperties = true;

                // Create Property Object
                Property p = new Property(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getDouble("price"),
                        rs.getString("type"),
                        rs.getString("floors"),
                        rs.getString("image_path")
                );

                // Load Card
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PropertyCard.fxml"));
                VBox card = loader.load();

                // Pass Data AND 'this' controller so the card can call back
                PropertyCardController controller = loader.getController();
                controller.setData(p, this);

                // Add to Grid (column, row)
                propertiesGrid.add(card, column, row);

                // Grid Logic (3 columns max)
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            // Toggle Empty State Visibility
            if (hasProperties) {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
                propertiesGrid.setVisible(true);
            } else {
                emptyStateBox.setVisible(true);
                emptyStateBox.setManaged(true);
                propertiesGrid.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProperty() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddPropertyDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Property");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/homeicon.png")));
            } catch (Exception ignored) {}

            stage.showAndWait();

            // Refresh after closing dialog
            loadPropertiesFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ========================================================================
    // NEW CODE: CALL THIS FROM YOUR PropertyCardController
    // ========================================================================
    // 1. Update openPropertyDetails
    public void openPropertyDetails(Property property) {
        try {
            List<Floor> floors = fetchFloorsAndRooms(property.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("HouseView.fxml"));
            Parent root = loader.load();

            HouseViewController houseController = loader.getController();
            // CHANGED: passing 'property' object, not just name
            houseController.setupPropertyData(property, floors);

            Stage stage = new Stage();
            stage.setTitle(property.getName() + " - Visual View");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to organize DB rows into Floor/Room objects
    private List<Floor> fetchFloorsAndRooms(int propertyId) {
        Map<Integer, Floor> floorMap = new HashMap<>();
        String query = "SELECT * FROM rooms WHERE property_id = ? ORDER BY floor_level ASC, room_number ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int level = rs.getInt("floor_level");

                // Get or Create Floor
                Floor floor = floorMap.computeIfAbsent(level, k -> new Floor(k));

                // Create Room
                Room room = new Room(
                        rs.getString("room_number"),
                        rs.getString("status"), // 'Occupied' or 'Available'
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

        // Return list of floors
        return new ArrayList<>(floorMap.values());
    }
    // 2. Add this Static Helper for refreshing (Call this from HouseViewController)
    public static void reloadHouseView(HouseViewController controller, Property prop) {
        // Re-fetch data
        MyPropertiesController temp = new MyPropertiesController(); // Just to access the non-static fetch method
        List<Floor> floors = temp.fetchFloorsAndRooms(prop.getId());

        // Update the controller
        controller.setupPropertyData(prop, floors);
    }
}