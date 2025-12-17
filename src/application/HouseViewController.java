package application;

import application.model.Floor;
import application.model.Room;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private boolean isTenantMode = false;

    public void setTenantMode(boolean isTenant) {
        this.isTenantMode = isTenant;
        if (currentProperty != null) {
            refreshHouseData();
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) houseContainer.getScene().getWindow();
        stage.close();
    }

    public void setupPropertyData(Property property, List<Floor> dbFloors) {
        this.currentProperty = property;
        propertyNameLabel.setText(property.getName());
        ensureRoomsExistInDatabase(dbFloors);

        // KEEPING THIS: Forces the window to be wide enough so the layout doesn't break
        forceWindowDimensions();

        refreshHouseData();
    }

    private void forceWindowDimensions() {
        Platform.runLater(() -> {
            if (houseContainer.getScene() != null && houseContainer.getScene().getWindow() instanceof Stage) {
                Stage stage = (Stage) houseContainer.getScene().getWindow();
                if (stage.getWidth() < 800) {
                    stage.setWidth(950);
                    stage.setHeight(750);
                    stage.centerOnScreen();
                }
            }
        });
    }

    private void buildHouseVisuals(List<Floor> floors) {
        houseContainer.getChildren().clear();

        // 1. Structure Container
        VBox buildingVBox = new VBox();
        buildingVBox.setAlignment(Pos.BOTTOM_CENTER);
        buildingVBox.setMaxWidth(Double.MAX_VALUE);

        // A. Roof
        StackPane roof = new StackPane();
        String roofClass = currentProperty.getType().equalsIgnoreCase("Rural") ? "rural-roof" : "urban-roof";
        roof.getStyleClass().add(roofClass);
        buildingVBox.getChildren().add(roof);

        // B. Floors
        String type = currentProperty.getType().equalsIgnoreCase("Rural") ? "rural" : "urban";
        for (Floor f : floors) {
            buildingVBox.getChildren().add(createFloorContainer(f.getLevel(), floors.size(), floors, type));
        }

        // C. Base
        Pane base = new Pane();
        String baseClass = currentProperty.getType().equalsIgnoreCase("Rural") ? "rural-base" : "urban-base";
        base.getStyleClass().add(baseClass);
        base.setMinHeight(25); base.setMaxHeight(25);
        base.setMaxWidth(Double.MAX_VALUE);
        buildingVBox.getChildren().add(base);

        // 2. Layout Wrapper (Keeps base grounded)
        StackPane scrollContent = new StackPane(buildingVBox);
        scrollContent.setAlignment(Pos.BOTTOM_CENTER);
        scrollContent.setStyle("-fx-background-color: transparent;");

        // 3. ScrollPane
        ScrollPane mainScroll = new ScrollPane(scrollContent);
        mainScroll.setFitToHeight(true);
        mainScroll.setFitToWidth(true);
        mainScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        mainScroll.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        mainScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");
        mainScroll.setViewportBounds(null);

        VBox.setVgrow(mainScroll, Priority.ALWAYS);
        houseContainer.getChildren().add(mainScroll);

        // D. Background
        int totalRooms = 0;
        for(Floor f : floors) totalRooms += f.getRoomCount();
        if(totalRooms == 0) totalRooms = calculateTotalRooms(floors);

        setDynamicBackground(currentProperty.getType(), totalRooms);
    }

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

        // Add Existing Rooms
        for (int i = 0; i < existingRooms.size(); i++) {
            Room room = existingRooms.get(i);
            roomBox.getChildren().add(createRoomNode(room, floorLevel, i, type));
        }

        // --- CHANGE HERE: HIDE "+" IF TENANT ---
        if (!isTenantMode) {
            Room newRoomPlaceholder = new Room("+", "New", 0.0, "Unconfigured", "Pending", null);
            roomBox.getChildren().add(createRoomNode(newRoomPlaceholder, floorLevel, existingRooms.size() + 1, type));
        }

        // Horizontal Scroll for Rooms
        ScrollPane floorScroll = new ScrollPane(roomBox);
        floorScroll.getStyleClass().add(type.equals("rural") ? "rural-scroll" : "urban-scroll");
        floorScroll.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        floorScroll.setVbarPolicy(ScrollBarPolicy.NEVER);
        floorScroll.setFitToWidth(true);
        floorScroll.setFitToHeight(true);
        floorScroll.setPannable(true);

        floorScroll.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) event.consume();
        });

        // Fixed Widths
        double fixedWidth = type.equals("rural") ? 400 : 420;
        floorScroll.setMinWidth(fixedWidth);
        floorScroll.setMaxWidth(fixedWidth);
        floorScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        floorContainer.getChildren().add(floorScroll);
        floorContainer.setMinHeight(type.equals("rural") ? 120 : 130);

        return floorContainer;
    }

    private StackPane createRoomNode(Room room, int floorLevel, int roomIndex, String type) {
        StackPane roomPane = new StackPane();
        String stylePrefix = type.equals("rural") ? "rural" : "urban";
        roomPane.getStyleClass().add(stylePrefix + "-room");

        int patternIndex = (floorLevel - 1) % 3;
        switch (patternIndex) {
            case 0: roomPane.getStyleClass().add(stylePrefix + "-window-door"); break;
            case 1: roomPane.getStyleClass().add(stylePrefix + "-window-triangle"); break;
            case 2: roomPane.getStyleClass().add(stylePrefix + "-window-arched"); break;
        }

        String displayRoomNum = room.getRoomNumber().equals("+") ? "+" : room.getRoomNumber();
        Label lbl = new Label(displayRoomNum);
        lbl.getStyleClass().add(stylePrefix + "-label");

        setupRoomStatus(room, roomPane, lbl, stylePrefix);

        roomPane.setOnMouseClicked(e -> {
            if (room.getStatus().equals("New")) {
                if (!isTenantMode) {
                    openRoomEditor(floorLevel, room);
                }
            } else {
                if (isTenantMode) {
                    openApplicationDialog(room);
                } else {
                    openRoomEditor(floorLevel, room);
                }
            }
        });

        return roomPane;
    }

    private void setupRoomStatus(Room room, StackPane pane, Label lbl, String type) {
        pane.getStyleClass().removeAll(
                type + "-status-new", type + "-status-placeholder",
                type + "-status-occupied", type + "-status-vacant", type + "-status-maintenance"
        );
        pane.setBackground(Background.EMPTY);
        pane.setStyle("");

        if (room.getStatus().equals("New")) {
            pane.getStyleClass().add(type + "-status-new");
            StackPane.setAlignment(lbl, Pos.CENTER);
            pane.getChildren().add(lbl);
            Tooltip.install(pane, new Tooltip("Click to add a new room"));
        } else {
            boolean isUnconfigured = room.getStatus() == null || room.getStatus().equalsIgnoreCase("Unconfigured");
            if (isUnconfigured) {
                pane.getStyleClass().add(type + "-status-placeholder");
                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);
            } else {
                String status = room.getStatus();
                if (status.equalsIgnoreCase("Occupied")) pane.getStyleClass().add(type + "-status-occupied");
                else if (status.equalsIgnoreCase("Maintenance")) pane.getStyleClass().add(type + "-status-maintenance");
                else pane.getStyleClass().add(type + "-status-vacant");

                StackPane.setAlignment(lbl, Pos.CENTER);
                pane.getChildren().add(lbl);

                Tooltip tooltip = new Tooltip();
                tooltip.setGraphic(createRoomDetailHoverView(room));
                tooltip.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-radius: 0; -fx-effect: null;");
                tooltip.setShowDelay(javafx.util.Duration.millis(100));
                Tooltip.install(pane, tooltip);
            }
        }
    }

    // --- STANDARD HELPER METHODS ---

    private int calculateNextRoomNumber(int floorLevel) {
        int maxRoom = 0;
        int defaultBase = floorLevel * 100;
        String sql = "SELECT room_number FROM rooms WHERE property_id = ? AND floor_level = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentProperty.getId());
            pstmt.setInt(2, floorLevel);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                try {
                    int rNum = Integer.parseInt(rs.getString("room_number"));
                    if (rNum > maxRoom) maxRoom = rNum;
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) { e.printStackTrace(); }
        return (maxRoom == 0) ? defaultBase + 1 : maxRoom + 1;
    }

    private void openApplicationDialog(Room room) {
        if (room.getStatus().equalsIgnoreCase("Unconfigured") || room.getStatus().equals("New")) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ApplicationDialog.fxml"));
            Parent root = loader.load();
            ApplicationDialogController controller = loader.getController();
            controller.setRoomData(room);
            Stage stage = new Stage();
            stage.setTitle("Apply for Room " + room.getRoomNumber());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openRoomEditor(int floorLevel, Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RoomEditor.fxml"));
            Parent page = loader.load();
            RoomEditorController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle(room == null || "New".equals(room.getStatus()) ? "Add Room" : "Edit Room");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(houseContainer.getScene().getWindow());
            Scene scene = new Scene(page);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            controller.setDialogStage(dialogStage);

            if (room == null || "New".equals(room.getStatus())) {
                int nextRoomNumber = calculateNextRoomNumber(floorLevel);
                controller.setMetadata(currentProperty.getId(), floorLevel, nextRoomNumber);
            } else {
                controller.setMetadata(currentProperty.getId(), floorLevel, 0);
                controller.setRoomData(room);
            }
            dialogStage.showAndWait();
            if (controller.isSaveClicked()) refreshHouseData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void refreshHouseData() {
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
                int level = rs.getInt("floor_number");
                Floor f = new Floor(rs.getInt("id"), level, rs.getInt("room_count"));
                f.setRooms(fetchRoomsForFloor(currentProperty.getId(), level));
                freshFloors.add(f);
            }
        } catch (Exception e) { e.printStackTrace(); }
        buildHouseVisuals(freshFloors);
    }

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
                        rs.getString("facilities"),
                        rs.getString("payment_status"),
                        rs.getString("image_path")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return rooms;
    }

    private void ensureRoomsExistInDatabase(List<Floor> dbFloors) {
        try (Connection conn = DatabaseHandler.getConnection()) {
            String insertSql = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, payment_status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            boolean needsUpdate = false;
            for (Floor floor : dbFloors) {
                int existingCount = 0;
                String checkSql = "SELECT COUNT(*) FROM rooms WHERE property_id = ? AND floor_level = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setInt(1, currentProperty.getId());
                    checkStmt.setInt(2, floor.getLevel());
                    ResultSet rs = checkStmt.executeQuery();
                    if (rs.next()) existingCount = rs.getInt(1);
                }
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
            if (f.isPresent()) total += f.get().getRoomCount();
            else total += fetchRoomCountForFloor(currentProperty.getId(), currentLevel);
        }
        return total;
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

    private VBox createRoomDetailHoverView(Room room) {
        VBox card = new VBox(10);
        card.getStyleClass().add("room-detail-card");
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            try {
                File imgFile = new File(room.getImagePath());
                if (imgFile.exists()) {
                    ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
                    iv.setFitWidth(230);
                    iv.setFitHeight(150);
                    iv.setPreserveRatio(true);
                    Rectangle clip = new Rectangle(230, 150);
                    clip.setArcWidth(10); clip.setArcHeight(10);
                    iv.setClip(clip);
                    card.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        }
        Label title = new Label("Room " + room.getRoomNumber());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #113970;");
        card.getChildren().add(title);
        Label price = new Label("₱ " + String.format("%,.2f", room.getPrice()) + " / month");
        price.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        card.getChildren().add(price);
        HBox pills = new HBox(8);
        Label statusLbl = new Label(room.getStatus());
        statusLbl.getStyleClass().add("status-pill");
        if(room.getStatus().equalsIgnoreCase("Occupied")) statusLbl.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-padding: 3 8; -fx-background-radius: 10;");
        else statusLbl.setStyle("-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-padding: 3 8; -fx-background-radius: 10;");
        pills.getChildren().add(statusLbl);
        card.getChildren().add(pills);
        Label facHeader = new Label("Amenities:");
        facHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 5 0 0 0;");
        card.getChildren().add(facHeader);
        VBox facList = new VBox(2);
        String facs = room.getFacilities();
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