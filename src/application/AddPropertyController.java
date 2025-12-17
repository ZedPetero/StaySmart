package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.Arrays;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AddPropertyController {

    @FXML private TextField txtName;
    @FXML private TextField txtLocation;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtFloors;
    @FXML private VBox dynamicRoomContainer;
    @FXML private Label lblImageName;
    // NEW CONTAINER FOR CHECKBOXES
    @FXML private FlowPane amenitiesContainer;
    private File selectedImageFile = null;
    private boolean saveClicked = false;
    private List<TextField> roomInputs = new ArrayList<>();
    // List to track the checkboxes so we can read them later
    private List<CheckBox> amenityCheckBoxes = new ArrayList<>();
    private Property existingProperty = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        cmbType.getItems().addAll("Urban", "Rural");
        cmbType.getSelectionModel().selectFirst();

        txtFloors.textProperty().addListener((observable, oldValue, newValue) -> {
            generateRoomInputs(newValue);
        });

        // --- NEW: Generate Amenity Checkboxes ---
        String[] commonAmenities = {
                "Toilet & bath",
                "Kitchen / cooking area",
                "Refrigerator",
                "Laundry area",
                "Washing machine",
                "CCTV cameras",
                "Fire extinguisher",
                "Emergency exits",
                "Secure main entrance / gate"
        };

        if (amenitiesContainer != null) {
            amenitiesContainer.setHgap(10);
            amenitiesContainer.setVgap(10);

            for (String amenity : commonAmenities) {
                CheckBox cb = new CheckBox(amenity);
                cb.setStyle("-fx-text-fill: #333; -fx-font-size: 12px;");
                amenityCheckBoxes.add(cb);
                amenitiesContainer.getChildren().add(cb);
            }
        }
    }

    private void generateRoomInputs(String floorCountStr) {
        dynamicRoomContainer.getChildren().clear();
        roomInputs.clear();
        try {
            int floors = Integer.parseInt(floorCountStr);
            if (floors > 20) return;

            for (int i = 1; i <= floors; i++) {
                VBox row = new VBox(5);
                Label lbl = new Label("How many rooms in Floor " + i + "?");
                lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
                TextField input = new TextField();
                input.setPromptText("e.g. 3");
                roomInputs.add(input);
                row.getChildren().addAll(lbl, input);
                dynamicRoomContainer.getChildren().add(row);
            }
        } catch (NumberFormatException e) { }
    }

    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Property Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) txtName.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImageFile = file;
            lblImageName.setText(file.getName());
        }
    }

    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtLocation.getText().isEmpty() || txtPrice.getText().isEmpty()) return;

        // --- 1. COLLECT AMENITIES ---
        StringBuilder amenitiesBuilder = new StringBuilder();
        for (CheckBox cb : amenityCheckBoxes) {
            if (cb.isSelected()) {
                if (amenitiesBuilder.length() > 0) amenitiesBuilder.append(", ");
                amenitiesBuilder.append(cb.getText());
            }
        }
        String amenitiesString = amenitiesBuilder.toString();

        // DEBUG: Check this in your console when you click save
        System.out.println("DEBUG: Saving Amenities -> [" + amenitiesString + "]");

        try (Connection conn = DatabaseHandler.getConnection()) {
            if (isEditMode) {
                // SQL INDEXES: 1:name, 2:loc, 3:price, 4:type, 5:floors, 6:path, 7:amenities, 8:id
                String sql = "UPDATE properties SET name=?, location=?, price=?, type=?, floors=?, image_path=?, amenities=? WHERE id=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtLocation.getText());
                pstmt.setDouble(3, Double.parseDouble(txtPrice.getText().replace(",", "")));
                pstmt.setString(4, cmbType.getValue());
                pstmt.setString(5, txtFloors.getText());

                // Handle image path correctly
                String path = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : existingProperty.getImagePath();
                pstmt.setString(6, path);

                pstmt.setString(7, amenitiesString); // Parameter 7
                pstmt.setInt(8, existingProperty.getId()); // Parameter 8

                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Rows updated: " + rowsAffected);

                // Re-sync floors/rooms if floor count changed
                if (!existingProperty.getFloors().equals(txtFloors.getText())) {
                    deleteOldRoomsAndFloors(existingProperty.getId());
                    saveFloors(existingProperty.getId());
                    createInitialRooms(existingProperty.getId());
                }
            } else {
                // INSERT LOGIC (Remains the same as your working version)
                String sql = "INSERT INTO properties (name, location, price, type, floors, image_path, landlord_id, amenities) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, txtName.getText());
                pstmt.setString(2, txtLocation.getText());
                pstmt.setDouble(3, Double.parseDouble(txtPrice.getText().replace(",", "")));
                pstmt.setString(4, cmbType.getValue());
                pstmt.setString(5, txtFloors.getText());
                pstmt.setString(6, selectedImageFile != null ? selectedImageFile.getAbsolutePath() : null);
                pstmt.setInt(7, LoginController.getCurrentUser().getId());
                pstmt.setString(8, amenitiesString);

                pstmt.executeUpdate();
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    saveFloors(newId);
                    createInitialRooms(newId);
                }
            }

            saveClicked = true;
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFloors(int propertyId) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql = "INSERT INTO property_floors (property_id, floor_number, room_count) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < roomInputs.size(); i++) {
                TextField input = roomInputs.get(i);
                int rooms = 0;
                try { rooms = Integer.parseInt(input.getText()); } catch(Exception e){}

                pstmt.setInt(1, propertyId);
                pstmt.setInt(2, i + 1);
                pstmt.setInt(3, rooms);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createInitialRooms(int propertyId) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < roomInputs.size(); i++) {
                int floorLevel = i + 1;
                int roomCount = 0;
                try { roomCount = Integer.parseInt(roomInputs.get(i).getText()); } catch(Exception e){}

                for (int r = 1; r <= roomCount; r++) {
                    String roomNum = String.format("%d%02d", floorLevel, r);

                    pstmt.setInt(1, propertyId);
                    pstmt.setInt(2, floorLevel);
                    pstmt.setString(3, roomNum);
                    pstmt.setDouble(4, 0.0);         // Default Price
                    pstmt.setString(5, "Available");   // Default Status
                    pstmt.setString(6, "Pending");     // Payment Status

                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }
    // Fix for setExistingPropertyData to ensure checkboxes are matched correctly
    public void setExistingPropertyData(Property property) {
        this.existingProperty = property;
        this.isEditMode = true;

        txtName.setText(property.getName());
        txtLocation.setText(property.getLocation());
        txtPrice.setText(String.format("%.0f", property.getPrice()));
        txtFloors.setText(property.getFloors());
        cmbType.setValue(property.getType());

        // Reset all checkboxes first
        for (CheckBox cb : amenityCheckBoxes) cb.setSelected(false);

        // Select the checkboxes based on the string from DB
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            String[] savedAmenities = property.getAmenities().split(", ");
            List<String> savedList = Arrays.asList(savedAmenities);

            for (CheckBox cb : amenityCheckBoxes) {
                if (savedList.contains(cb.getText())) {
                    cb.setSelected(true);
                }
            }
        }

        if (property.getImagePath() != null) {
            selectedImageFile = new File(property.getImagePath());
            lblImageName.setText(selectedImageFile.getName());
        }

        generateRoomInputs(property.getFloors());
    }
    // Helper for Edit Mode
    private void deleteOldRoomsAndFloors(int propertyId) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql1 = "DELETE FROM rooms WHERE property_id = ?";
            String sql2 = "DELETE FROM property_floors WHERE property_id = ?";
            PreparedStatement p1 = conn.prepareStatement(sql1);
            PreparedStatement p2 = conn.prepareStatement(sql2);
            p1.setInt(1, propertyId);
            p2.setInt(1, propertyId);
            p1.executeUpdate();
            p2.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    public boolean isSaveClicked() { return saveClicked; }
}