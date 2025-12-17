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
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
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

    // 1. UPDATED RURAL NODE (With 1-2-3 Repeating Pattern)
    private StackPane createRuralRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("rural-room");

        // (floorLevel - 1) % 3 gives us 0, 1, 2, 0, 1, 2...
        int patternIndex = (floorLevel - 1) % 3;

        switch (patternIndex) {
            case 0: // Floor 1, 4, 7...
                roomPane.getStyleClass().add("rural-window-door");     // 1. Simple Rectangular Door
                break;
            case 1: // Floor 2, 5, 8...
                roomPane.getStyleClass().add("rural-window-triangle"); // 2. Triangle Head
                break;
            case 2: // Floor 3, 6, 9...
                roomPane.getStyleClass().add("rural-window-arched");   // 3. Semicircle/Window Head
                break;
        }

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("rural-label");

        setupRoomStatus(room, roomPane, lbl, "rural");
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    // 2. UPDATED URBAN NODE (With 1-2-3 Repeating Pattern)
    private StackPane createUrbanRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("urban-room");

        int patternIndex = (floorLevel - 1) % 3;

        switch (patternIndex) {
            case 0: // Floor 1, 4, 7...
                roomPane.getStyleClass().add("urban-window-door");     // 1. Simple Rectangular Door
                break;
            case 1: // Floor 2, 5, 8...
                roomPane.getStyleClass().add("urban-window-triangle"); // 2. Triangle Head
                break;
            case 2: // Floor 3, 6, 9...
                roomPane.getStyleClass().add("urban-window-arched");   // 3. Semicircle/Window Head
                break;
        }

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("urban-label");

        setupRoomStatus(room, roomPane, lbl, "urban");
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    private void setupRoomStatus(Room room, StackPane pane, Label lbl, String type) {
        // Clear old styles
        pane.getStyleClass().removeAll(
                type + "-status-new",
                type + "-status-placeholder",
                type + "-status-occupied",
                type + "-status-vacant",
                type + "-status-maintenance" // Added support for maintenance color
        );
        pane.setBackground(Background.EMPTY);
        pane.setStyle(""); // Reset any inline styles (like previous images)

        // A. CASE: NEW ROOM (+)
        if (room.getStatus().equals("New")) {
            pane.getStyleClass().add(type + "-status-new");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);
            Tooltip.install(pane, new Tooltip("Click to add a new room"));

        } else {
            // B. CASE: EXISTING ROOM
            boolean isUnconfigured = room.getStatus() == null || room.getStatus().equalsIgnoreCase("Unconfigured");

            if (isUnconfigured) {
                pane.getStyleClass().add(type + "-status-placeholder");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);
            } else {
                // C. STATUS LOGIC: Set Color Class Only (No Images!)
                String status = room.getStatus();

                if (status.equalsIgnoreCase("Occupied")) {
                    pane.getStyleClass().add(type + "-status-occupied"); // Red
                } else if (status.equalsIgnoreCase("Maintenance")) {
                    pane.getStyleClass().add(type + "-status-maintenance"); // Yellow/Orange (Need to add to CSS)
                } else {
                    pane.getStyleClass().add(type + "-status-vacant"); // Green (Available)
                }

                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);

                // --- HOVER TOOLTIP ---
                // The image will ONLY show here now, inside the hover card
                Tooltip tooltip = new Tooltip();
                tooltip.setGraphic(createRoomDetailHoverView(room));
                tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-radius: 0; -fx-effect: null;");
                tooltip.setShowDelay(javafx.util.Duration.millis(100));
                Tooltip.install(pane, tooltip);
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
            // 1. Load YOUR FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomEditor.fxml"));
            Parent page = loader.load();

            // 2. Get YOUR Controller
            RoomEditorController controller = loader.getController();

            // 3. Setup Stage (Blue/Transparent style matching your FXML design)
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null || "New".equals(room.getStatus()) ? "Add Room" : "Edit Room");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(houseContainer.getScene().getWindow());

            // Allow transparency because your FXML root is transparent/rounded
            Scene scene = new Scene(page);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);

            controller.setDialogStage(dialogStage);

            // 4. Pass Data
            if (room == null || "New".equals(room.getStatus())) {
                // ... (Calculation for nextRoomNumber is fine) ...
                int nextRoomNumber = 101; // Simplification, keep your existing logic here

                // Call YOUR method: setMetadata(int propertyId, int floorLevel, int nextRoomNumber)
                controller.setMetadata(currentProperty.getId(), floorLevel, nextRoomNumber);
            } else {
                // Call YOUR method: setMetadata (to set IDs) AND setRoomData
                controller.setMetadata(currentProperty.getId(), floorLevel, 0);
                controller.setRoomData(room);
            }

            // 5. Show
            dialogStage.showAndWait();

            // 6. Refresh
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

    private List<Room> fetchRoomsForFloor(int propertyId, int floorLevel) {
        List<Room> rooms = new ArrayList<>();
        // Make sure we select * so we get facilities
        String sql = "SELECT * FROM rooms WHERE property_id = ? AND floor_level = ? ORDER BY CAST(room_number AS UNSIGNED) ASC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, propertyId);
            pstmt.setInt(2, floorLevel);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // HERE IS THE FIX: We read 'facilities' from the DB instead of "Apartment"
                rooms.add(new Room(
                        rs.getString("room_number"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        rs.getString("facilities"), // <--- CHANGED THIS
                        rs.getString("payment_status"),
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

    // 2. ADD THIS HELPER METHOD (This builds the card using data from your Editor)
    private VBox createRoomDetailHoverView(Room room) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-detail-card"); // CSS class for the white box

        // A. Image Area
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(room.getImagePath());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(230);
                    iv.setFitHeight(150);
                    iv.setPreserveRatio(true);

                    // Clip to round corners
                    Rectangle clip = new Rectangle(230, 150);
                    clip.setArcWidth(10); clip.setArcHeight(10);
                    iv.setClip(clip);

                    card.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        }

        // B. Title & Price
        Label title = new Label("Room " + room.getRoomNumber());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #113970;");
        card.getChildren().add(title);

        Label price = new Label("₱ " + String.format("%,.2f", room.getPrice()) + " / month");
        price.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        card.getChildren().add(price);

        // C. Status Pills
        HBox pills = new HBox(8);
        Label statusLbl = new Label(room.getStatus());
        statusLbl.getStyleClass().add("status-pill"); // You need this in CSS
        // Simple color logic
        if(room.getStatus().equalsIgnoreCase("Occupied")) statusLbl.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-padding: 3 8; -fx-background-radius: 10;");
        else statusLbl.setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-padding: 3 8; -fx-background-radius: 10;");

        pills.getChildren().add(statusLbl);
        card.getChildren().add(pills);

        // D. Facilities List (Parsed from the string your Editor saved)
        Label facHeader = new Label("Amenities:");
        facHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 5 0 0 0;");
        card.getChildren().add(facHeader);

        VBox facList = new VBox(2);
        String facs = room.getFacilities(); // e.g. "Bed, Aircon, WiFi"
        if (facs != null && !facs.isEmpty()) {
            for (String f : facs.split(",")) {
                Label fl = new Label("• " + f.trim());
                fl.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                facList.getChildren().add(fl);
            }
        } else {
            facList.getChildren().add(new Label("• No amenities listed"));
        }
        card.getChildren().add(facList);

        return card;
    }
}