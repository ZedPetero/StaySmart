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
            int floors = Integer.parseInt(floorCountStr == null ? "" : floorCountStr.trim());
            if (floors < 1 || floors > 20) return;

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

    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) errors.append("Property name is required.\n");
        if (txtLocation.getText() == null || txtLocation.getText().trim().isEmpty()) errors.append("Location is required.\n");

        String price = txtPrice.getText() == null ? "" : txtPrice.getText().trim().replace(",", "");
        if (price.isEmpty()) {
            errors.append("Price is required.\n");
        } else {
            try {
                if (Double.parseDouble(price) < 0) errors.append("Price cannot be negative.\n");
            } catch (NumberFormatException e) {
                errors.append("Price must be a number.\n");
            }
        }

        String floors = txtFloors.getText() == null ? "" : txtFloors.getText().trim();
        try {
            int f = Integer.parseInt(floors);
            if (f < 1 || f > 20) errors.append("Number of floors must be between 1 and 20.\n");
        } catch (NumberFormatException e) {
            errors.append("Number of floors must be a whole number.\n");
        }

        if (errors.length() == 0) return true;
        showAlert(Alert.AlertType.WARNING, "Missing or invalid information", errors.toString().trim());
        return false;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (txtName.getScene() != null) alert.initOwner(txtName.getScene().getWindow());
        alert.showAndWait();
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;
        String floorsText = txtFloors.getText().trim();
        double priceValue = Double.parseDouble(txtPrice.getText().trim().replace(",", ""));

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

                pstmt.setString(1, txtName.getText().trim());
                pstmt.setString(2, txtLocation.getText().trim());
                pstmt.setDouble(3, priceValue);
                pstmt.setString(4, cmbType.getValue());
                pstmt.setString(5, floorsText);

                // Handle image path correctly
                String path = (selectedImageFile != null) ? selectedImageFile.getAbsolutePath() : existingProperty.getImagePath();
                pstmt.setString(6, path);

                pstmt.setString(7, amenitiesString); // Parameter 7
                pstmt.setInt(8, existingProperty.getId()); // Parameter 8

                pstmt.executeUpdate();

                String oldFloors = existingProperty.getFloors() == null ? "" : existingProperty.getFloors().trim();
                if (!oldFloors.equals(floorsText)) {
                    // Floor count changed: rooms are rebuilt, which also drops their applications.
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Rebuild rooms?");
                    confirm.setHeaderText("The number of floors changed from " + oldFloors + " to " + floorsText + ".");
                    confirm.setContentText("All existing rooms of this property (and any applications for them) will be removed and recreated. Continue?");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        deleteOldRoomsAndFloors(existingProperty.getId());
                        saveFloors(existingProperty.getId());
                        createInitialRooms(existingProperty.getId());
                    } else {
                        // Keep the old floor count consistent with the rooms we kept
                        try (PreparedStatement revert = conn.prepareStatement("UPDATE properties SET floors = ? WHERE id = ?")) {
                            revert.setString(1, oldFloors.isEmpty() ? "1" : oldFloors);
                            revert.setInt(2, existingProperty.getId());
                            revert.executeUpdate();
                        }
                    }
                } else {
                    // Same floor count: just update the per-floor room counts and add missing rooms
                    updateFloorRoomCounts(existingProperty.getId());
                    addMissingRooms(existingProperty.getId());
                }
            } else {
                // INSERT LOGIC (Remains the same as your working version)
                String sql = "INSERT INTO properties (name, location, price, type, floors, image_path, landlord_id, amenities, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, txtName.getText().trim());
                pstmt.setString(2, txtLocation.getText().trim());
                pstmt.setDouble(3, priceValue);
                pstmt.setString(4, cmbType.getValue());
                pstmt.setString(5, floorsText);
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
            showAlert(Alert.AlertType.ERROR, "Save failed", "Could not save the property: " + e.getMessage());
        }
    }

    // Edit mode, same floor count: keep property_floors.room_count in step with the inputs
    private void updateFloorRoomCounts(int propertyId) {
        String update = "UPDATE property_floors SET room_count = ? WHERE property_id = ? AND floor_number = ?";
        String insert = "INSERT INTO property_floors (property_id, floor_number, room_count) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement up = conn.prepareStatement(update);
             PreparedStatement ins = conn.prepareStatement(insert)) {
            for (int i = 0; i < roomInputs.size(); i++) {
                int rooms = parseRoomCount(roomInputs.get(i));
                up.setInt(1, rooms);
                up.setInt(2, propertyId);
                up.setInt(3, i + 1);
                if (up.executeUpdate() == 0) {
                    ins.setInt(1, propertyId);
                    ins.setInt(2, i + 1);
                    ins.setInt(3, rooms);
                    ins.executeUpdate();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Edit mode, same floor count: create rooms only where a floor now has more rooms than before
    private void addMissingRooms(int propertyId) {
        String countSql = "SELECT COUNT(*) FROM rooms WHERE property_id = ? AND floor_level = ?";
        String insertSql = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement count = conn.prepareStatement(countSql);
             PreparedStatement insert = conn.prepareStatement(insertSql)) {
            for (int i = 0; i < roomInputs.size(); i++) {
                int floorLevel = i + 1;
                int wanted = parseRoomCount(roomInputs.get(i));

                count.setInt(1, propertyId);
                count.setInt(2, floorLevel);
                ResultSet rs = count.executeQuery();
                int existing = rs.next() ? rs.getInt(1) : 0;

                for (int r = existing + 1; r <= wanted; r++) {
                    insert.setInt(1, propertyId);
                    insert.setInt(2, floorLevel);
                    insert.setString(3, String.format("%d%02d", floorLevel, r));
                    insert.setDouble(4, 0.0);
                    insert.setString(5, "Available");
                    insert.setString(6, "Pending");
                    insert.addBatch();
                }
            }
            insert.executeBatch();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int parseRoomCount(TextField input) {
        try {
            return Math.max(0, Integer.parseInt(input.getText().trim()));
        } catch (Exception e) {
            return 0;
        }
    }

    private void saveFloors(int propertyId) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql = "INSERT INTO property_floors (property_id, floor_number, room_count) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < roomInputs.size(); i++) {
                TextField input = roomInputs.get(i);
                int rooms = parseRoomCount(input);

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
                int roomCount = parseRoomCount(roomInputs.get(i));

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
        txtFloors.setText(property.getFloors() != null ? property.getFloors().trim() : "1");
        if (property.getType() != null && cmbType.getItems().contains(property.getType())) {
            cmbType.setValue(property.getType());
        }

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

        if (property.getImagePath() != null && !property.getImagePath().isEmpty()) {
            File img = new File(property.getImagePath());
            lblImageName.setText(img.getName());
            // Only keep it as the "selected" file if it still exists; otherwise the old path is preserved on save
            if (img.exists()) selectedImageFile = img;
        }

        generateRoomInputs(property.getFloors());
        prefillRoomCounts(property.getId());
    }

    private void prefillRoomCounts(int propertyId) {
        String sql = "SELECT floor_number, room_count FROM property_floors WHERE property_id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int idx = rs.getInt("floor_number") - 1;
                if (idx >= 0 && idx < roomInputs.size()) {
                    roomInputs.get(idx).setText(String.valueOf(rs.getInt("room_count")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
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