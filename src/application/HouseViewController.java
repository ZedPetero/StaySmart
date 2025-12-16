package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.Region;
import java.io.File;
public class HouseViewController {

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

        // Ensure container fills width and aligns bottom
        houseContainer.setAlignment(Pos.BOTTOM_CENTER);
        houseContainer.setFillWidth(true); // Crucial for stretching the base

        // 1. Calculate Total Rooms for Background Logic
        int totalRooms = calculateTotalRooms(dbFloors);

        // 2. Set Dynamic Background Image
        setDynamicBackground(type, totalRooms);

        // 3. Build House
        if (type != null && type.equalsIgnoreCase("Rural")) {
            buildRuralHouse(dbFloors);
        } else {
            buildUrbanHouse(dbFloors);
        }
    }

    private void setDynamicBackground(String type, int totalRooms) {
        String baseName = "";

        // 1. Determine Image Name
        if (type != null && type.equalsIgnoreCase("Rural")) {
            if (totalRooms <= 2) baseName = "rural1";
            else if (totalRooms <= 4) baseName = "rural2";
            else baseName = "rural3";
        } else {
            if (totalRooms <= 3) baseName = "urban1";
            else if (totalRooms <= 6) baseName = "urban2";
            else baseName = "urban3";
        }

        System.out.println("DEBUG: Attempting to load background: " + baseName);

        // 2. Define all possible locations (paths) and extensions
        String[] paths = {
                "/images/",              // Case 1: src/images (Classpath Root)
                "/application/images/",  // Case 2: src/application/images (Package)
                "images/",               // Case 3: Relative path
                "/"                      // Case 4: Directly in src
        };

        String[] extensions = { ".jpg", ".png", ".jpeg" };

        Image foundImage = null;

        // 3. Search Loop
        search: for (String path : paths) {
            for (String ext : extensions) {
                String fullPath = path + baseName + ext;

                // Method A: Try Classpath (Standard)
                if (getClass().getResource(fullPath) != null) {
                    System.out.println("DEBUG: Found image at Classpath: " + fullPath);
                    foundImage = new Image(getClass().getResource(fullPath).toExternalForm());
                    break search;
                }
            }
        }

        // 4. Fallback: Try File System (If folder is in project root, not src)
        if (foundImage == null) {
            for (String ext : extensions) {
                // Check "images" folder at project root
                File file = new File("images/" + baseName + ext);
                if (file.exists()) {
                    System.out.println("DEBUG: Found image at FileSystem: " + file.toURI().toString());
                    foundImage = new Image(file.toURI().toString());
                    break;
                }
                // Check "src/images" folder manually
                File fileSrc = new File("src/images/" + baseName + ext);
                if (fileSrc.exists()) {
                    System.out.println("DEBUG: Found image at FileSystem (src): " + fileSrc.toURI().toString());
                    foundImage = new Image(fileSrc.toURI().toString());
                    break;
                }
            }
        }

        // 5. Apply Image
        if (foundImage != null) {
            BackgroundSize bgSize = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO,
                    false, false, true, true // Cover
            );

            BackgroundImage bgImg = new BackgroundImage(
                    foundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    bgSize
            );

            Background finalBackground = new Background(bgImg);

            // Apply to the Scene Root (Whole Window)
            if (houseContainer.getScene() != null && houseContainer.getScene().getRoot() instanceof Region) {
                ((Region) houseContainer.getScene().getRoot()).setBackground(finalBackground);
                houseContainer.setBackground(Background.EMPTY); // Clear local background
            } else {
                houseContainer.setBackground(finalBackground);
            }
        } else {
            System.err.println("ERROR: Could not find image '" + baseName + "' in any expected folder.");
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

    /* =========================================================================
     * TYPE 1: RURAL HOUSE
     * ========================================================================= */
    private void buildRuralHouse(List<Floor> dbFloors) {
        houseContainer.getStyleClass().removeAll("urban-style"); // Cleanup

        // 1. ROOF
        Pane roof = new Pane();
        roof.getStyleClass().add("rural-roof");
        VBox.setVgrow(roof, Priority.NEVER);
        // Roof stays centered and fixed width
        VBox.setMargin(roof, new javafx.geometry.Insets(0, 0, 0, 0));
        houseContainer.getChildren().add(roof);

        // 2. FLOORS
        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "rural");
            houseContainer.getChildren().add(floorContainer);
        }

        // 3. BASE (Stretched)
        Pane base = new Pane();
        base.getStyleClass().add("rural-base");
        base.setMinHeight(25);
        base.setMaxHeight(25);
        // Force stretch
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    /* =========================================================================
     * TYPE 2: URBAN HOUSE
     * ========================================================================= */
    private void buildUrbanHouse(List<Floor> dbFloors) {
        houseContainer.getStyleClass().removeAll("rural-style");

        // 1. ROOF
        Pane roof = new Pane();
        roof.getStyleClass().add("urban-roof");
        VBox.setVgrow(roof, Priority.NEVER);
        houseContainer.getChildren().add(roof);

        // 2. FLOORS
        int totalFloors = getFloorCount();
        for (int floorLevel = totalFloors; floorLevel >= 1; floorLevel--) {
            VBox floorContainer = createFloorContainer(floorLevel, totalFloors, dbFloors, "urban");
            houseContainer.getChildren().add(floorContainer);
        }

        // 3. BASE (Stretched)
        Pane base = new Pane();
        base.getStyleClass().add("urban-base");
        base.setMinHeight(25);
        base.setMaxHeight(25);
        // Force stretch
        base.setMaxWidth(Double.MAX_VALUE);
        houseContainer.getChildren().add(base);
    }

    // Shared method to reduce duplication
    private VBox createFloorContainer(int floorLevel, int totalFloors, List<Floor> dbFloors, String type) {
        Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == floorLevel).findFirst();
        List<Room> existingRooms = floorData.map(Floor::getRooms).orElse(List.of());

        VBox floorContainer = new VBox();
        floorContainer.getStyleClass().add(type.equals("rural") ? "rural-floor-wood" : "urban-floor-container");

        // Ensure floor container doesn't stretch to full screen width, only the base does
        // But we want the background to be transparent.
        floorContainer.setAlignment(Pos.BOTTOM_CENTER);

        HBox roomBox = new HBox(15);
        roomBox.setStyle("-fx-padding: 0 25 0 25; -fx-alignment: bottom-center;");

        int configuredCapacity = 0;
        if (floorData.isPresent()) configuredCapacity = floorData.get().getRoomCount();
        if (configuredCapacity == 0) configuredCapacity = fetchRoomCountForFloor(currentProperty.getId(), floorLevel);
        int totalItemsToRender = Math.max(configuredCapacity, existingRooms.size());

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

        ScrollPane scroll = new ScrollPane(roomBox);
        scroll.getStyleClass().add("floor-scroll");
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setMaxWidth(type.equals("rural") ? 400 : 420);

        floorContainer.getChildren().add(scroll);
        return floorContainer;
    }

    // --- (Keep createRuralRoomNode, createUrbanRoomNode, and DB helpers exactly as they were in the previous code) ---
    // Just ensure you paste the createRuralRoomNode and createUrbanRoomNode methods here from the previous step.

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