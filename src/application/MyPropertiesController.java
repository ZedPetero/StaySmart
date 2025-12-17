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

    @FXML private GridPane propertiesGrid;
    @FXML private VBox emptyStateBox;

    @FXML
    public void initialize() {
        loadPropertiesFromDatabase();
    }

    public void loadPropertiesFromDatabase() {
        propertiesGrid.getChildren().clear();

        // --- UPDATED SQL: Filter by landlord_id ---
        String query = "SELECT * FROM properties WHERE landlord_id = ?";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // --- GET CURRENT USER ID ---
            pstmt.setInt(1, LoginController.getCurrentUser().getId());

            ResultSet rs = pstmt.executeQuery();

            int column = 0;
            int row = 1;
            boolean hasProperties = false;

            while (rs.next()) {
                hasProperties = true;
                Property p = new Property(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getDouble("price"),
                        rs.getString("type"),
                        rs.getString("floors"),
                        rs.getString("image_path"),
                        rs.getString("amenities")
                );

                FXMLLoader loader = new FXMLLoader(getClass().getResource("PropertyCard.fxml"));
                VBox card = loader.load();

                PropertyCardController controller = loader.getController();
                controller.setData(p, this);

                propertiesGrid.add(card, column, row);

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

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

    public void handleAddProperty() {
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

            // Refresh grid after closing dialog
            loadPropertiesFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Detail View Logic ---
    public void openPropertyDetails(Property property) {
        try {
            List<Floor> floors = fetchFloorsAndRooms(property.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HouseView.fxml"));
            Parent root = loader.load();

            HouseViewController houseController = loader.getController();
            houseController.setupPropertyData(property, floors);

            Stage stage = new Stage();
            stage.setTitle(property.getName() + " - Visual View");
            stage.setScene(new Scene(root, 900, 700));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Floor> fetchFloorsAndRooms(int propertyId) {
        Map<Integer, Floor> floorMap = new HashMap<>();
        // 1. Updated SQL to include 'id' and 'property_id'
        String query = "SELECT id, property_id, room_number, status, price, image_path, facilities, payment_status " +
                "FROM rooms WHERE property_id = ? ORDER BY floor_level ASC, room_number ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int level = rs.getInt("floor_level");
                Floor floor = floorMap.computeIfAbsent(level, k -> new Floor(k));

                // 2. Pass exactly 8 arguments to match your updated Room model
                Room room = new Room(
                        rs.getInt("id"),             // Argument 1: int id
                        rs.getInt("property_id"),    // Argument 2: int propertyId
                        rs.getString("room_number"), // Argument 3: String
                        rs.getString("status"),      // Argument 4: String
                        rs.getDouble("price"),       // Argument 5: Double
                        rs.getString("facilities"),  // Argument 6: String (Note: Check order with your Room.java)
                        rs.getString("payment_status"), // Argument 7: String
                        rs.getString("image_path")   // Argument 8: String
                );
                floor.addRoom(room);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(floorMap.values());
    }

    public static void reloadHouseView(HouseViewController controller, Property prop) {
        MyPropertiesController temp = new MyPropertiesController();
        List<Floor> floors = temp.fetchFloorsAndRooms(prop.getId());
        controller.setupPropertyData(prop, floors);
    }
    // Inside MyPropertiesController.java

    public void deleteProperty(Property property) {
        // 1. Confirm with user
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Property");
        alert.setHeaderText("Are you sure you want to delete " + property.getName() + "?");
        alert.setContentText("This will also remove all associated rooms and floors.");

        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            String query = "DELETE FROM properties WHERE id = ?"; // Tables 'rooms' and 'property_floors' will cascade

            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, property.getId());
                pstmt.executeUpdate();

                // 2. Refresh the UI
                loadPropertiesFromDatabase();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openEditPropertyDialog(Property property) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddPropertyDialog.fxml"));
            Parent root = loader.load();

            AddPropertyController controller = loader.getController();
            controller.setExistingPropertyData(property);

            Stage stage = new Stage();
            stage.setTitle("Edit Property: " + property.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // --- FIX: Use showAndWait() to trigger refresh after closing ---
            stage.showAndWait();

            // Refresh the grid to show updated amenities/data
            loadPropertiesFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}