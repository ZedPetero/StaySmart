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
        buildHouseVisuals(dbFloors);
    }

    private void buildHouseVisuals(List<Floor> dbFloors) {
        houseContainer.getChildren().clear();
        String type = currentProperty.getType();

        // 1. Setup Main Container
        houseContainer.setAlignment(Pos.BOTTOM_CENTER);
        houseContainer.setFillWidth(true);
        houseContainer.setSpacing(0);

        // 2. Configure the Parent Main ScrollPane
        if (houseContainer.getParent() instanceof StackPane) {
            StackPane wrapper = (StackPane) houseContainer.getParent();
            if (wrapper.getParent() instanceof ScrollPane) {
                ScrollPane mainScroll = (ScrollPane) wrapper.getParent();
                mainScroll.setFitToWidth(true);
                mainScroll.setFitToHeight(false);
                mainScroll.setPannable(true);
            }
        }

        // 3. Background Setup
        int totalRooms = calculateTotalRooms(dbFloors);
        setDynamicBackground(type, totalRooms);

        // 4. Build House
        if (type != null && type.equalsIgnoreCase("Rural")) {
            buildRuralHouse(dbFloors);
        } else {
            buildUrbanHouse(dbFloors);
        }
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

    private VBox createFloorContainer(int floorLevel, int totalFloors, List<Floor> dbFloors, String type) {
        Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == floorLevel).findFirst();
        List<Room> existingRooms = floorData.map(Floor::getRooms).orElse(List.of());

        VBox floorContainer = new VBox();
        floorContainer.getStyleClass().add(type.equals("rural") ? "rural-floor-wood" : "urban-floor-container");
        floorContainer.setAlignment(Pos.BOTTOM_CENTER);

        HBox roomBox = new HBox(15);
        roomBox.setAlignment(Pos.BOTTOM_CENTER);
        roomBox.setStyle("-fx-padding: 0 25 0 25;");

        // 1. Determine how many rooms to render (Capacity vs Existing)
        int configuredCapacity = 0;
        if (floorData.isPresent()) configuredCapacity = floorData.get().getRoomCount();
        if (configuredCapacity == 0) configuredCapacity = fetchRoomCountForFloor(currentProperty.getId(), floorLevel);

        int totalItemsToRender = Math.max(configuredCapacity, existingRooms.size());

        // 2. Render Rooms loop
        for (int i = 1; i <= totalItemsToRender; i++) {
            // Use format "301" matching your DB style
            String targetRoomNum = String.format("%d%02d", floorLevel, i);

            Room roomToDisplay = existingRooms.stream()
                    .filter(r -> r.getRoomNumber().equals(targetRoomNum))
                    .findFirst().orElse(null);

            // Ghost room if not in DB
            if (roomToDisplay == null) roomToDisplay = new Room(targetRoomNum, "Available", 0.0, "Unconfigured", "Pending", null);

            // CALL NODE CREATOR (Clean Signature)
            if (type.equals("rural")) {
                roomBox.getChildren().add(createRuralRoomNode(roomToDisplay, floorLevel, i, totalFloors));
            } else {
                roomBox.getChildren().add(createUrbanRoomNode(roomToDisplay, floorLevel, i, totalFloors));
            }
        }

        // 3. Add the "+" Placeholder Room
        Room newRoomPlaceholder = new Room("+", "New", 0.0, "Unconfigured", "Pending", null);
        if (type.equals("rural")) {
            roomBox.getChildren().add(createRuralRoomNode(newRoomPlaceholder, floorLevel, totalItemsToRender + 1, totalFloors));
        } else {
            roomBox.getChildren().add(createUrbanRoomNode(newRoomPlaceholder, floorLevel, totalItemsToRender + 1, totalFloors));
        }

        ScrollPane scroll = new ScrollPane(roomBox);
        scroll.getStyleClass().add("floor-scroll");
        scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);
        scroll.setMaxWidth(type.equals("rural") ? 400 : 420);

        floorContainer.getChildren().add(scroll);
        floorContainer.setMinHeight(type.equals("rural") ? 120 : 130);

        return floorContainer;
    }

    // CLEAN SIGNATURE: Removed the extra unused arguments
    private StackPane createRuralRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("rural-room");
        if (floorLevel == totalFloors && totalFloors > 1) roomPane.getStyleClass().add("rural-window-arched");
        else roomPane.getStyleClass().add("rural-window-rect");

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("rural-label");

        setupRoomStatus(room, roomPane, lbl, "rural");

        if (!room.getStatus().equals("New") && !roomPane.getStyleClass().contains("rural-status-placeholder") && floorLevel != totalFloors) {
            Pane flowerBox = new Pane();
            flowerBox.getStyleClass().add("rural-flower-box");
            flowerBox.setMaxHeight(15); flowerBox.setMaxWidth(60);
            StackPane.setAlignment(flowerBox, Pos.BOTTOM_CENTER);
            flowerBox.setTranslateY(5);
            roomPane.getChildren().add(flowerBox);
        }

        // CLICK HANDLER: Calls openRoomEditor with just 2 arguments
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    private StackPane createUrbanRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("urban-room");
        if (floorLevel == totalFloors && totalFloors > 1) roomPane.getStyleClass().add("urban-window-arched");
        else roomPane.getStyleClass().add("urban-window-rect");

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("urban-label");

        setupRoomStatus(room, roomPane, lbl, "urban");

        if (!room.getStatus().equals("New") && !roomPane.getStyleClass().contains("urban-status-placeholder")) {
            if (floorLevel > 1) {
                Pane balcony = new Pane();
                balcony.getStyleClass().add("urban-balcony");
                balcony.setMaxHeight(25); balcony.setMaxWidth(70);
                StackPane.setAlignment(balcony, Pos.BOTTOM_CENTER);
                balcony.setTranslateY(10);
                roomPane.getChildren().add(balcony);
            } else if (floorLevel == 1) {
                Pane doorStep = new Pane();
                doorStep.getStyleClass().add("urban-doorstep");
                doorStep.setMaxHeight(5); doorStep.setMaxWidth(70);
                StackPane.setAlignment(doorStep, Pos.BOTTOM_CENTER);
                doorStep.setTranslateY(3);
                roomPane.getChildren().add(doorStep);
            }
        }

        // CLICK HANDLER: Calls openRoomEditor with just 2 arguments
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room));
        return roomPane;
    }

    private void setupRoomStatus(Room room, StackPane pane, Label lbl, String type) {
        // 1. RESET: Clear all dynamic styles and manual backgrounds
        pane.getStyleClass().removeAll(
                type + "-status-new",
                type + "-status-placeholder",
                type + "-status-occupied",
                type + "-status-vacant"
        );
        // Explicitly clear background image to prevent "ghost" images from previous states
        pane.setBackground(Background.EMPTY);
        pane.setStyle(""); // Clear inline styles

        // 2. APPLY STATUS
        if (room.getStatus().equals("New")) {
            pane.getStyleClass().add(type + "-status-new");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);
            Tooltip.install(pane, new Tooltip("Add a new room"));
        } else {
            boolean isUnconfigured = room.getStatus() == null ||
                    (room.getStatus().equalsIgnoreCase("Available") && room.getPrice() == 0.0) ||
                    room.getStatus().equalsIgnoreCase("Unconfigured");

            if (isUnconfigured) {
                pane.getStyleClass().add(type + "-status-placeholder");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);
            } else {
                // OCCUPIED / VACANT
                boolean isOccupied = room.getStatus().equalsIgnoreCase("Occupied");
                pane.getStyleClass().add(isOccupied ? type + "-status-occupied" : type + "-status-vacant");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);

                // 3. IMAGE HANDLING (Safe Mode)
                // If the room has a custom image, apply it ONLY to this specific pane
                // We use 'setStyle' on the specific ID to keep it local
                if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
                    File imgFile = new File(room.getImagePath());
                    if (imgFile.exists()) {
                        // Apply image as background, heavily constrained to containment
                        String imageUrl = imgFile.toURI().toString();
                        pane.setStyle(
                                "-fx-background-image: url('" + imageUrl + "'); " +
                                        "-fx-background-size: cover; " +
                                        "-fx-background-position: center; " +
                                        "-fx-background-repeat: no-repeat;"
                        );
                        // Ensure the label is still visible (maybe add a text shadow or background)
                        lbl.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(one-pass-box, black, 4, 1.0, 0, 0);");
                    }
                }

                Tooltip.install(pane, new Tooltip("Room " + room.getRoomNumber() + "\n" + room.getStatus()));
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

    // 3. OPEN EDITOR: Calculates the correct Next ID (e.g., 103) strictly from the database
    // CLEAN SIGNATURE: 2 arguments only. Logic calculates ID from DB.
    private void openRoomEditor(int floorLevel, Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomEditor.fxml"));
            Parent page = loader.load();
            RoomEditorController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null || room.getStatus().equals("New") ? "Add Room" : "Edit Room");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(houseContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            controller.setDialogStage(dialogStage);

            if (room == null || room.getStatus().equals("New")) {
                // --- FIX: CALCULATE NEXT ID FROM DATABASE ---
                int maxRoomNum = 0;

                // Get the highest room number strictly from the database
                String sql = "SELECT MAX(CAST(room_number AS UNSIGNED)) as max_num FROM rooms WHERE property_id = ? AND floor_level = ?";

                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, currentProperty.getId());
                    pstmt.setInt(2, floorLevel);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        maxRoomNum = rs.getInt("max_num");
                    }
                } catch (Exception e) { e.printStackTrace(); }

                // If no rooms exist, start at X01 (e.g., 301). Otherwise, add 1 to the highest found number.
                int nextRoomNumber = (maxRoomNum == 0) ? (floorLevel * 100) + 1 : maxRoomNum + 1;

                // Safety check: ensure we don't go below the floor start
                if (nextRoomNumber < floorLevel * 100) nextRoomNumber = (floorLevel * 100) + 1;

                controller.setMetadata(currentProperty.getId(), floorLevel, nextRoomNumber);
            } else {
                controller.setMetadata(currentProperty.getId(), floorLevel, 0);
                controller.setRoomData(room);
            }

            dialogStage.showAndWait();

            // REFRESH: Reload the screen if they clicked save
            if (controller.isSaveClicked()) {
                refreshHouseData();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    // 1. REFRESH DATA: Reloads the house after you save so the new room appears instantly
    private void refreshHouseData() {
        List<Floor> freshFloors = new ArrayList<>();
        // Query property_floors using the correct columns
        String sql = "SELECT * FROM property_floors WHERE property_id = ? ORDER BY floor_number DESC";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentProperty.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Fetch basic floor info
                int floorId = rs.getInt("id");
                int level = rs.getInt("floor_number");
                int count = rs.getInt("room_count");

                Floor f = new Floor(floorId, level, count);

                // Fetch the rooms for this floor
                f.setRooms(fetchRoomsForFloor(currentProperty.getId(), level));

                freshFloors.add(f);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Rebuild the screen
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
}