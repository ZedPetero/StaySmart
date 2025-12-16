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
        // We ensure the main window allows the house to grow TALL (Vertical Scroll)
        // but does not CRUSH the house (FitToHeight = FALSE).
        if (houseContainer.getParent() instanceof StackPane) {
            StackPane wrapper = (StackPane) houseContainer.getParent();
            if (wrapper.getParent() instanceof ScrollPane) {
                ScrollPane mainScroll = (ScrollPane) wrapper.getParent();
                mainScroll.setFitToWidth(true);
                mainScroll.setFitToHeight(false); // CRITICAL: Allows vertical scrolling of the whole house
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

        // Image Finding Logic
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

        // Apply Image (Stretched to fit window)
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

        // 1. Add Roof
        houseContainer.getChildren().add(createRoof("rural"));

        // 2. Add Floors
        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "rural");
            houseContainer.getChildren().add(floorContainer);
        }

        // 3. Add Base
        Pane base = new Pane();
        base.getStyleClass().add("rural-base");
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    private void buildUrbanHouse(List<Floor> dbFloors) {
        houseContainer.getStyleClass().removeAll("rural-style");

        // 1. Add Roof
        houseContainer.getChildren().add(createRoof("urban"));

        // 2. Add Floors
        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "urban");
            houseContainer.getChildren().add(floorContainer);
        }

        // 3. Add Base
        Pane base = new Pane();
        base.getStyleClass().add("urban-base");
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    /**
     * MISSING FUNCTION ADDED HERE: Creates the roof and forces a minimum height
     */
    private Pane createRoof(String type) {
        Pane roof = new Pane();
        if (type.equalsIgnoreCase("rural")) {
            roof.getStyleClass().add("rural-roof");
            VBox.setMargin(roof, new Insets(0, 0, 0, 0));
        } else {
            roof.getStyleClass().add("urban-roof");
        }

        // CRITICAL: Force minimum height so it doesn't get crushed
        roof.setMinHeight(80);
        roof.setPrefHeight(80);
        VBox.setVgrow(roof, Priority.NEVER);

        return roof;
    }

    private VBox createFloorContainer(int floorLevel, int totalFloors, List<Floor> dbFloors, String type) {
        Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == floorLevel).findFirst();
        List<Room> existingRooms = floorData.map(Floor::getRooms).orElse(List.of());

        // The Container for the whole floor
        VBox floorContainer = new VBox();
        floorContainer.getStyleClass().add(type.equals("rural") ? "rural-floor-wood" : "urban-floor-container");
        floorContainer.setAlignment(Pos.BOTTOM_CENTER);

        // HBox holding the rooms (scrolls horizontally)
        HBox roomBox = new HBox(15);
        roomBox.setStyle("-fx-padding: 0 25 0 25; -fx-alignment: bottom-center;");

        // Determine number of rooms
        int configuredCapacity = 0;
        if (floorData.isPresent()) configuredCapacity = floorData.get().getRoomCount();
        if (configuredCapacity == 0) configuredCapacity = fetchRoomCountForFloor(currentProperty.getId(), floorLevel);
        int totalItemsToRender = Math.max(configuredCapacity, existingRooms.size());

        // Create Rooms
        for (int i = 1; i <= totalItemsToRender; i++) {
            String targetRoomNum = String.valueOf(i);
            Room roomToDisplay = existingRooms.stream()
                    .filter(r -> r.getRoomNumber().equals(targetRoomNum))
                    .findFirst().orElse(null);

            if (roomToDisplay == null) roomToDisplay = new Room(targetRoomNum, "Available", 0.0, "Unconfigured", "Pending", null);

            if (type.equals("rural")) {
                roomBox.getChildren().add(createRuralRoomNode(roomToDisplay, floorLevel, i, totalFloors));
            } else {
                roomBox.getChildren().add(createUrbanRoomNode(roomToDisplay, floorLevel, i, totalFloors));
            }
        }

        // WRAP ROOMS IN HORIZONTAL SCROLLPANE
        ScrollPane scroll = new ScrollPane(roomBox);
        scroll.getStyleClass().add("floor-scroll");

        // Scroll Settings: Horizontal YES, Vertical NO
        scroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollBarPolicy.NEVER);

        scroll.setFitToWidth(false); // Let content grow wide
        scroll.setFitToHeight(true); // Match height of container
        scroll.setPannable(true);

        // Limit width of the scrolling area so it looks like a tower
        scroll.setMaxWidth(type.equals("rural") ? 400 : 420);

        // Add to floor
        floorContainer.getChildren().add(scroll);

        // CRITICAL FIX: Force Minimum Height on the Floor Container
        // This prevents the rooms from being "crushed" vertically.
        // If the screen is too small, the MAIN window will scroll vertically.
        floorContainer.setMinHeight(type.equals("rural") ? 120 : 130);

        return floorContainer;
    }

    // ... (Keep createRuralRoomNode, createUrbanRoomNode, setupRoomStatus, fetchRoomCountForFloor, getFloorCount, openRoomEditor exactly as they were) ...
    // ... Copy them from your previous code or let me know if you need me to paste them again ...

    // FOR COMPLETENESS, here are the room nodes again so you don't lose them:

    private StackPane createRuralRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("rural-room");
        if (floorLevel == totalFloors && totalFloors > 1) roomPane.getStyleClass().add("rural-window-arched");
        else roomPane.getStyleClass().add("rural-window-rect");

        String displayRoomNum = String.format("%d%02d", floorLevel, roomIndex);
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("rural-label");

        setupRoomStatus(room, roomPane, lbl, "rural");

        if (!roomPane.getStyleClass().contains("rural-status-placeholder") && floorLevel != totalFloors) {
            Pane flowerBox = new Pane();
            flowerBox.getStyleClass().add("rural-flower-box");
            flowerBox.setMaxHeight(15); flowerBox.setMaxWidth(60);
            StackPane.setAlignment(flowerBox, Pos.BOTTOM_CENTER);
            flowerBox.setTranslateY(5);
            roomPane.getChildren().add(flowerBox);
        }
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room, 0));
        return roomPane;
    }

    private StackPane createUrbanRoomNode(Room room, int floorLevel, int roomIndex, int totalFloors) {
        StackPane roomPane = new StackPane();
        roomPane.getStyleClass().add("urban-room");
        if (floorLevel == totalFloors && totalFloors > 1) roomPane.getStyleClass().add("urban-window-arched");
        else roomPane.getStyleClass().add("urban-window-rect");

        String displayRoomNum = String.format("%d%02d", floorLevel, roomIndex);
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add("urban-label");

        setupRoomStatus(room, roomPane, lbl, "urban");

        if (!roomPane.getStyleClass().contains("urban-status-placeholder")) {
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
        roomPane.setOnMouseClicked(e -> openRoomEditor(floorLevel, room, 0));
        return roomPane;
    }

    private void setupRoomStatus(Room room, StackPane pane, Label lbl, String type) {
        boolean isUnconfigured = room.getStatus() == null || (room.getStatus().equalsIgnoreCase("Available") && room.getPrice() == 0.0);
        if (isUnconfigured) {
            pane.getStyleClass().add(type + "-status-placeholder");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);
        } else {
            boolean isOccupied = room.getStatus().equalsIgnoreCase("Occupied");
            pane.getStyleClass().add(isOccupied ? type + "-status-occupied" : type + "-status-vacant");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);
            Tooltip.install(pane, new Tooltip("Room " + room.getRoomNumber()));
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

    private void openRoomEditor(int floorLevel, Room room, int currentCount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomEditor.fxml"));
            Parent page = loader.load();
            RoomEditorController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null ? "Add Room" : "Edit Room");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(houseContainer.getScene().getWindow());
            dialogStage.setScene(new Scene(page));
            controller.setDialogStage(dialogStage);
            if (room == null) controller.setMetadata(currentProperty.getId(), floorLevel, currentCount);
            else { controller.setMetadata(currentProperty.getId(), floorLevel, 0); controller.setRoomData(room); }
            dialogStage.showAndWait();
            if (controller.isSaveClicked()) MyPropertiesController.reloadHouseView(this, currentProperty);
        } catch (IOException e) { e.printStackTrace(); }
    }
}