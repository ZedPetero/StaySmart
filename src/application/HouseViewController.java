package application;

import application.model.Floor;
import application.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HouseViewController {

    @FXML private Label propertyNameLabel;
    @FXML private VBox houseContainer;

    private Property currentProperty;

    // CONFIGURATION: Max rooms per floor
    private static final int MAX_ROOMS_PER_FLOOR = 2;

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

        // 1. Draw Roof
        StackPane roofPane = new StackPane();
        Polygon roof = new Polygon();
        roof.getPoints().addAll(0.0, 50.0, 300.0, 0.0, 600.0, 50.0);
        roof.setFill(Color.web("#2c3e50"));
        roofPane.getChildren().add(roof);
        houseContainer.getChildren().add(roofPane);

        // 2. Parse total floors
        int totalFloors = 1;
        try {
            String fStr = currentProperty.getFloors().toLowerCase().replace(" floors", "").replace(" floor", "").trim();
            totalFloors = Integer.parseInt(fStr);
        } catch (Exception ignored) {}

        // 3. Build Floors
        for (int i = totalFloors; i >= 1; i--) {
            int currentLevel = i;
            Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == currentLevel).findFirst();

            HBox floorRow = new HBox(15); // Increased spacing
            floorRow.setAlignment(Pos.CENTER);
            floorRow.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 2 2 2; -fx-padding: 15;");
            floorRow.setMaxWidth(580);
            floorRow.setPrefHeight(100); // Taller rows for images

            int currentRoomCount = 0;

            if (floorData.isPresent()) {
                List<Room> rooms = floorData.get().getRooms();
                currentRoomCount = rooms.size();
                for (Room room : rooms) {
                    floorRow.getChildren().add(createRoomNode(room, currentLevel));
                }
            }

            // ONLY Show "+" Button if room count is below Max Limit
            if (currentRoomCount < MAX_ROOMS_PER_FLOOR) {
                // Pass the current count so the button knows the next ID
                floorRow.getChildren().add(createAddButton(currentLevel, currentRoomCount));
            }

            houseContainer.getChildren().add(floorRow);
        }
    }

    // UPDATED: Room Node with Image Background + Top Left Number
    private StackPane createRoomNode(Room room, int level) {
        StackPane roomPane = new StackPane();
        roomPane.setPrefSize(120, 80); // Bigger box
        roomPane.setStyle("-fx-background-color: white; -fx-border-color: #95a5a6; -fx-border-width: 1; -fx-cursor: hand;");

        // 1. Background Image (if exists)
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            try {
                File file = new File(room.getImagePath());
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    ImageView bgView = new ImageView(img);
                    bgView.setFitWidth(120);
                    bgView.setFitHeight(80);
                    bgView.setPreserveRatio(false); // Stretch to fill box
                    roomPane.getChildren().add(bgView);
                }
            } catch (Exception ignored) {}
        }

        // 2. Room Number Label (Top Left)
        Label numLbl = new Label(room.getRoomNumber());
        numLbl.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-text-fill: white; -fx-padding: 2 6; -fx-font-weight: bold;");
        StackPane.setAlignment(numLbl, Pos.TOP_LEFT);
        roomPane.getChildren().add(numLbl);

        // 3. Status Indicator (Small dot at top right)
        String statusColor = room.getStatus().equalsIgnoreCase("Occupied") ? "red" : "#2ecc71";
        Label statusDot = new Label("");
        statusDot.setPrefSize(10, 10);
        statusDot.setStyle("-fx-background-color: " + statusColor + "; -fx-background-radius: 50%;");
        StackPane.setAlignment(statusDot, Pos.TOP_RIGHT);
        StackPane.setMargin(statusDot, new javafx.geometry.Insets(5));
        roomPane.getChildren().add(statusDot);

        // 4. Click to Edit
        roomPane.setOnMouseClicked(e -> openRoomEditor(level, room, 0));

        // 5. Rich Tooltip (Hover Details)
        Tooltip tooltip = new Tooltip();
        tooltip.setGraphic(createTooltipContent(room));
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        Tooltip.install(roomPane, tooltip);

        return roomPane;
    }

    // Helper to build the visual hover card
    private VBox createTooltipContent(Room room) {
        VBox v = new VBox(5);
        v.setStyle("-fx-padding: 10; -fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1;");

        // 1. Image (Large)
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            try {
                File f = new File(room.getImagePath());
                if (f.exists()) {
                    ImageView iv = new ImageView(new Image(f.toURI().toString()));
                    iv.setFitWidth(220); // Make it wide enough to see clearly
                    iv.setPreserveRatio(true);
                    v.getChildren().add(iv);
                }
            } catch (Exception ignored) {}
        }

        // 2. Room Number Header
        Label title = new Label("Room " + room.getRoomNumber());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        v.getChildren().add(title);

        // 3. Status & Payment Row
        HBox statusRow = new HBox(10);

        // Occupancy Status
        Label statusLbl = new Label(room.getStatus());
        String statusColor = room.getStatus().equalsIgnoreCase("Occupied") ? "#e74c3c" : "#2ecc71"; // Red or Green
        statusLbl.setStyle("-fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 3 8; -fx-background-radius: 4; -fx-font-weight: bold;");

        // Payment Status
        String payText = (room.getPaymentStatus() == null || room.getPaymentStatus().isEmpty()) ? "Pending" : room.getPaymentStatus();
        Label payLbl = new Label(payText);
        String payColor = payText.equalsIgnoreCase("Paid") ? "#27ae60" : "#f39c12"; // Green or Orange
        payLbl.setStyle("-fx-text-fill: " + payColor + "; -fx-border-color: " + payColor + "; -fx-padding: 2 6; -fx-border-radius: 4;");

        statusRow.getChildren().addAll(statusLbl, payLbl);
        v.getChildren().add(statusRow);

        // 4. Price
        Label price = new Label("Price: ₱ " + String.format("%,.2f", room.getPrice()) + " / month");
        price.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        v.getChildren().add(price);

        // 5. Facilities
        Label facHeader = new Label("Facilities:");
        facHeader.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 0 0;");
        v.getChildren().add(facHeader);

        Label facilities = new Label(room.getFacilities() == null || room.getFacilities().isEmpty() ? "No facilities listed" : room.getFacilities());
        facilities.setWrapText(true);
        facilities.setMaxWidth(220);
        facilities.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        v.getChildren().add(facilities);

        return v;
    }

    private StackPane createAddButton(int floorLevel, int currentCount) {
        StackPane btn = new StackPane();
        btn.setPrefSize(120, 80); // Match room size
        btn.setStyle("-fx-border-color: #95a5a6; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: transparent; -fx-cursor: hand;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 30px; -fx-text-fill: #95a5a6;");
        btn.getChildren().add(plus);

        btn.setOnMouseClicked(e -> openRoomEditor(floorLevel, null, currentCount));
        return btn;
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

            if (room == null) {
                // ADD MODE: Pass property ID, floor level, and count for auto-numbering
                controller.setMetadata(currentProperty.getId(), floorLevel, currentCount);
            } else {
                controller.setMetadata(currentProperty.getId(), floorLevel, 0);
                controller.setRoomData(room);
            }

            dialogStage.showAndWait();

            if (controller.isSaveClicked()) {
                // Reload using the helper we added in the main controller
                MyPropertiesController.reloadHouseView(this, currentProperty);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}