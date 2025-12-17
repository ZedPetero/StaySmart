package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.io.File;

public class HouseViewController {

    @FXML private StackPane rootStackPane;
    @FXML private Label propertyNameLabel;
    @FXML private VBox houseContainer;

    private Property currentProperty;

    @FXML
    private void handleBack() {
        Stage stage = (Stage) houseContainer.getScene().getWindow();
        stage.close();
    }

    public void setupPropertyData(Property property, List<Floor> dbFloors) {
        this.currentProperty = property;
        propertyNameLabel.setText(property.getName());

        // 1. AUTO-FIX: Create rooms for old properties if they are missing
        ensureRoomsExistInDatabase(dbFloors);

        // 2. BUILD: Load the visuals
        refreshHouseData();
    }

    // 1. UPDATED: Title removed from Roof
    private void buildHouseVisuals(List<Floor> floors) {
        houseContainer.getChildren().clear();

        // A. Setup Roof
        StackPane roof = new StackPane();
        if (currentProperty.getType().equalsIgnoreCase("Rural")) {
            roof.getStyleClass().add("rural-roof");
        } else {
            roof.getStyleClass().add("urban-roof");
        }

        // [REMOVED] The label code is deleted here.

        houseContainer.getChildren().add(roof);

        // B. Setup Floors
        String type = currentProperty.getType().equalsIgnoreCase("Rural") ? "rural" : "urban";
        for (Floor f : floors) {
            houseContainer.getChildren().add(createFloorContainer(f.getLevel(), floors.size(), floors, type));
        }

        // C. Base (Bottom Bar)
        Pane base = new Pane();
        if (currentProperty.getType().equalsIgnoreCase("Rural")) {
            base.getStyleClass().add("rural-base");
        } else {
            base.getStyleClass().add("urban-base");
        }
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);

        // D. Background Logic
        int totalRooms = 0;
        for(Floor f : floors) totalRooms += f.getRoomCount();
        if(totalRooms == 0) totalRooms = calculateTotalRooms(floors);

        setDynamicBackground(currentProperty.getType(), totalRooms);
    }

    private void setDynamicBackground(String type, int totalRooms) {
        String baseName = "";
        if (type != null && type.equalsIgnoreCase("Rural")) {
            if (totalRooms <= 2) baseName = "rural1";
            else if (totalRooms <= 4) baseName = "rural2";
            else baseName = "rural3";
        } else {
            if (totalRooms <= 3) baseName = "urban1";
            else if (totalRooms <= 6) baseName = "urban2";
            else baseName = "urban3";
        }

        String[] paths = { "/images/", "/application/images/", "images/", "/" };
        String[] extensions = { ".jpg", ".png", ".jpeg" };
        Image foundImage = null;

        search: for (String path : paths) {
            for (String ext : extensions) {
                String fullPath = path + baseName + ext;
                if (getClass().getResource(fullPath) != null) {
                    foundImage = new Image(getClass().getResource(fullPath).toExternalForm());
                    break search;
                }
            }
        }
        if (foundImage == null) {
            for (String ext : extensions) {
                File file = new File("images/" + baseName + ext);
                if (file.exists()) {
                    foundImage = new Image(file.toURI().toString());
                    break;
                }
            }
        }

        if (foundImage != null) {
            BackgroundSize bgSize = new BackgroundSize(1.0, 1.0, true, true, false, false);
            BackgroundPosition bgPos = new BackgroundPosition(Side.LEFT, 0.5, true, Side.BOTTOM, 0.0, true);
            BackgroundImage bgImg = new BackgroundImage(foundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, bgPos, bgSize);
            Background finalBackground = new Background(bgImg);

            if (rootStackPane != null) {
                rootStackPane.setBackground(finalBackground);
                houseContainer.setBackground(Background.EMPTY);
            }
        }
    }

    private int calculateTotalRooms(List<Floor> dbFloors) {
        int total = 0;
        int floorCount = getFloorCount();
        for (int i = 1; i <= floorCount; i++) {
            int currentLevel = i;
            Optional<Floor> f = dbFloors.stream().filter(fl -> fl.getLevel() == currentLevel).findFirst();
            if (f.isPresent()) {
                total += f.get().getRoomCount();
            } else {
                total += fetchRoomCountForFloor(currentProperty.getId(), currentLevel);
            }
        }
        return total;
    }

    // --- BUILDER METHODS ---

    private void buildRuralHouse(List<Floor> dbFloors) {
        houseContainer.getStyleClass().removeAll("urban-style");
        houseContainer.getChildren().add(createRoof("rural"));

        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "rural");
            houseContainer.getChildren().add(floorContainer);
        }

        Pane base = new Pane();
        base.getStyleClass().add("rural-base");
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    private void buildUrbanHouse(List<Floor> dbFloors) {
        houseContainer.getStyleClass().removeAll("rural-style");
        houseContainer.getChildren().add(createRoof("urban"));

        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "urban");
            houseContainer.getChildren().add(floorContainer);
        }

        Pane base = new Pane();
        base.getStyleClass().add("urban-base");
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    private Pane createRoof(String type) {
        Pane roof = new Pane();
        if (type.equalsIgnoreCase("rural")) {
            roof.getStyleClass().add("rural-roof");
            VBox.setMargin(roof, new Insets(0, 0, 0, 0));
        } else {
            roof.getStyleClass().add("urban-roof");
        }
        roof.setMinHeight(80);
        roof.setPrefHeight(80);
        VBox.setVgrow(roof, Priority.NEVER);
        return roof;
    }

    // 2. UPDATED: Full method with Scrollbar Color classes added
    private VBox createFloorContainer(int floorLevel, int totalFloors, List<Floor> dbFloors, String type) {
        Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == floorLevel).findFirst();
        List<Room> existingRooms = floorData.map(Floor::getRooms).orElse(new ArrayList<>());

        existingRooms.sort(Comparator.comparingInt(r -> {
            try { return Integer.parseInt(r.getRoomNumber()); }
            catch (NumberFormatException e) { return 9999; }
        }));

        VBox floorContainer = new VBox();
        floorContainer.getStyleClass().add(type.equals("rural") ? "rural-floor-wood" : "urban-floor-container");
        floorContainer.setAlignment(Pos.BOTTOM_CENTER);

        HBox roomBox = new HBox(15);
        roomBox.setAlignment(Pos.BOTTOM_CENTER);
        roomBox.setStyle("-fx-padding: 0 25 0 25;");

        // Add Rooms
        for (int i = 0; i < existingRooms.size(); i++) {
            Room room = existingRooms.get(i);
            if (type.equals("rural")) {
                roomBox.getChildren().add(createRuralRoomNode(room, floorLevel, i, totalFloors));
            } else {
                roomBox.getChildren().add(createUrbanRoomNode(room, floorLevel, i, totalFloors));
            }
        }

        // Add "+" Button
        Room newRoomPlaceholder = new Room("+", "New", 0.0, "Unconfigured", "Pending", null);
        if (type.equals("rural")) {
            roomBox.getChildren().add(createRuralRoomNode(newRoomPlaceholder, floorLevel, existingRooms.size() + 1, totalFloors));
        } else {
            roomBox.getChildren().add(createUrbanRoomNode(newRoomPlaceholder, floorLevel, existingRooms.size() + 1, totalFloors));
        }

        ScrollPane scroll = new ScrollPane(roomBox);

        // NEW: Tag the scrollpane so CSS can color it
        if (type.equals("rural")) {
            scroll.getStyleClass().add("rural-scroll");
        } else {
            scroll.getStyleClass().add("urban-scroll");
        }

        scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);

        // Block vertical scrolling
        scroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) event.consume();
        });

        scroll.setMaxWidth(type.equals("rural") ? 400 : 420);

        floorContainer.getChildren().add(scroll);
        floorContainer.setMinHeight(type.equals("rural") ? 120 : 130);

        return floorContainer;
    }

    // 2. UPDATED: Adds "rural-window-door" for Ground Floor
    private StackPane createRuralRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("rural-room");

        // --- PATTERN LOGIC ---
        if (floorLevel == 1) {
            roomPane.getStyleClass().add("rural-window-door"); // Ground Floor = Door
        } else if (floorLevel == totalFloors && totalFloors > 1) {
            roomPane.getStyleClass().add("rural-window-arched"); // Top Floor = Arched
        } else {
            roomPane.getStyleClass().add("rural-window-rect"); // Middle = Rect
        }

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("rural-label");

        setupRoomStatus(room, roomPane, lbl, "rural");

        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    // 3. UPDATED: Adds "urban-window-door" for Ground Floor
    private StackPane createUrbanRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("urban-room");

        // --- PATTERN LOGIC ---
        if (floorLevel == 1) {
            roomPane.getStyleClass().add("urban-window-door"); // Ground Floor = Door
        } else if (floorLevel == totalFloors && totalFloors > 1) {
            roomPane.getStyleClass().add("urban-window-arched"); // Top Floor = Arched
        } else {
            roomPane.getStyleClass().add("urban-window-rect"); // Middle = Rect
        }

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("urban-label");

        setupRoomStatus(room, roomPane, lbl, "urban");
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    // 2. UPDATED: Restored Tooltip (Hover Details)
    private void setupRoomStatus(Room room, StackPane pane, Label lbl, String type) {
        // Clear old styles
        pane.getStyleClass().removeAll(
                type + "-status-new",
                type + "-status-placeholder",
                type + "-status-occupied",
                type + "-status-vacant"
        );
        pane.setBackground(Background.EMPTY);
        pane.setStyle("");

        // A. CASE: NEW ROOM (+)
        if (room.getStatus().equals("New")) {
            pane.getStyleClass().add(type + "-status-new");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);

            // Tooltip for Add Button
            Tooltip.install(pane, new Tooltip("Click to add a new room"));

        } else {
            // B. CASE: EXISTING ROOM
            boolean isUnconfigured = room.getStatus() == null ||
                    room.getStatus().equalsIgnoreCase("Unconfigured");

            if (isUnconfigured) {
                pane.getStyleClass().add(type + "-status-placeholder");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);
            } else {
                // Occupied vs Vacant
                boolean isOccupied = room.getStatus().equalsIgnoreCase("Occupied");
                pane.getStyleClass().add(isOccupied ? type + "-status-occupied" : type + "-status-vacant");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);

                // Image Handling
                if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
                    File imgFile = new File(room.getImagePath());
                    if (imgFile.exists()) {
                        String imageUrl = imgFile.toURI().toString();
                        pane.setStyle(
                                "-fx-background-image: url('" + imageUrl + "'); " +
                                        "-fx-background-size: cover; " +
                                        "-fx-background-position: center; " +
                                        "-fx-background-repeat: no-repeat;"
                        );
                        lbl.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, black, 4, 1.0, 0, 0);");
                    }
                }

                // --- RESTORED HOVER DETAILS ---
                String tooltipText = "Room: " + room.getRoomNumber() + "\n" +
                        "Status: " + room.getStatus() + "\n" +
                        "Price: " + room.getPrice();

                Tooltip t = new Tooltip(tooltipText);
                t.setStyle("-fx-font-size: 14px;"); // Make it readable
                Tooltip.install(pane, t);
            }
        }
    }

    private int fetchRoomCountForFloor(int propertyId, int floorNumber) {
        String sql = "SELECT room_count FROM property_floors WHERE property_id = ? AND floor_number = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, propertyId);
            pstmt.setInt(2, floorNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("room_count");
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private int getFloorCount() {
        int totalFloors = 1;
        try {
            String fStr = currentProperty.getFloors().toLowerCase().replace(" floors", "").replace(" floor", "").trim();
            totalFloors = Integer.parseInt(fStr);
        } catch (Exception ignored) {}
        return totalFloors;
    }

    // In HouseViewController.java

    private void openRoomEditor(int floorLevel, Room room) {
        try {
            // 1. Load FXML (Ensure the path matches your project structure, likely just "RoomEditor.fxml" if in same folder)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomEditor.fxml"));
            Parent page = loader.load();

            // 2. Get Controller (No 'controllers.' prefix needed)
            RoomEditorController controller = loader.getController();

            // 3. Setup Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null || room.getStatus().equals("New") ? "Add Room" : "Edit Room");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(houseContainer.getScene().getWindow());

            // Transparent style for the "Blue Card" look
            Scene scene = new Scene(page);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            controller.setDialogStage(dialogStage);

            // 4. Pass Data to Controller
            if (room == null || room.getStatus().equals("New")) {
                // --- NEW ROOM LOGIC: Calculate Next ID ---
                int dbMaxRoomNum = 0;
                String sql = "SELECT MAX(CAST(room_number AS UNSIGNED)) as max_num FROM rooms WHERE property_id = ? AND floor_level = ?";

                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, currentProperty.getId());
                    pstmt.setInt(2, floorLevel);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) dbMaxRoomNum = rs.getInt("max_num");
                } catch (Exception e) { e.printStackTrace(); }

                int nextRoomNumber = (dbMaxRoomNum == 0) ? (floorLevel * 100) + 1 : dbMaxRoomNum + 1;
                if (nextRoomNumber < (floorLevel * 100) + 1) nextRoomNumber = (floorLevel * 100) + 1;

                // Pass ID for creation
                controller.setMetadata(currentProperty.getId(), floorLevel, nextRoomNumber);
            } else {
                // --- EXISTING ROOM LOGIC ---
                controller.setMetadata(currentProperty.getId(), floorLevel, 0); // ID ignored for edit
                controller.setRoomData(room);
            }

            // 5. Show and Wait
            dialogStage.showAndWait();

            // 6. Refresh if Saved
            if (controller.isSaveClicked()) {
                refreshHouseData();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshHouseData() {
        // FIX: Force clear the root background to prevent room images from getting stuck there
        if (rootStackPane != null) {
            rootStackPane.setBackground(Background.EMPTY);
            rootStackPane.setStyle("");
        }

        List<Floor> freshFloors = new ArrayList<>();
        String sql = "SELECT * FROM property_floors WHERE property_id = ? ORDER BY floor_number DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentProperty.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int floorId = rs.getInt("id");
                int level = rs.getInt("floor_number");
                int count = rs.getInt("room_count");

                Floor f = new Floor(floorId, level, count);
                f.setRooms(fetchRoomsForFloor(currentProperty.getId(), level));
                freshFloors.add(f);
            }
        } catch (Exception e) { e.printStackTrace(); }

        buildHouseVisuals(freshFloors);
    }

    // 2. FETCH ROOMS: Gets the list of rooms for a specific floor
    private List<Room> fetchRoomsForFloor(int propertyId, int floorLevel) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE property_id = ? AND floor_level = ? ORDER BY CAST(room_number AS UNSIGNED) ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, propertyId);
            pstmt.setInt(2, floorLevel);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        "Apartment", // Default type
                        "None",      // Default tenant
                        rs.getString("image_path")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rooms;
    }

    // --- ADD THIS HELPER METHOD ---
    private void ensureRoomsExistInDatabase(List<Floor> dbFloors) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String insertSql = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            boolean needsUpdate = false;

            for (Floor floor : dbFloors) {
                // Check if rooms exist
                int existingCount = 0;
                String checkSql = "SELECT COUNT(*) FROM rooms WHERE property_id = ? AND floor_level = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, currentProperty.getId());
                    checkStmt.setInt(2, floor.getLevel());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) existingCount = rs.getInt(1);
                }

                // If missing, create them
                if (existingCount == 0 && floor.getRoomCount() > 0) {
                    for (int i = 1; i <= floor.getRoomCount(); i++) {
                        String roomNum = String.format("%d%02d", floor.getLevel(), i);
                        pstmt.setInt(1, currentProperty.getId());
                        pstmt.setInt(2, floor.getLevel());
                        pstmt.setString(3, roomNum);
                        pstmt.setDouble(4, 0.0);
                        pstmt.setString(5, "Available");
                        pstmt.setString(6, "Pending");
                        pstmt.addBatch();
                        needsUpdate = true;
                    }
                }
            }
            if (needsUpdate) pstmt.executeBatch();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Helper to refresh data immediately after fix
    private List<Room> fetchRoomsForFloorObj(int floorLevel) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE property_id = ? AND floor_level = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentProperty.getId());
            pstmt.setInt(2, floorLevel);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rooms.add(new Room(
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        rs.getString("facilities"),
                        rs.getString("payment_status"),
                        rs.getString("image_path")
                ));
            }
        } catch(Exception e) { e.printStackTrace(); }
        return rooms;
    }
}