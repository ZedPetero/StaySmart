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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class HouseViewController {

    @FXML private Label propertyNameLabel;
    @FXML private VBox houseContainer;

    private Property currentProperty;

    // Modified to accept the full Property object
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

        // 2. Determine Total Floors from Property Object
        // If the string is "2 Floors", we parse the "2". Default to 1 if error.
        int totalFloors = 1;
        try {
            String fStr = currentProperty.getFloors().toLowerCase().replace(" floors", "").replace(" floor", "").trim();
            totalFloors = Integer.parseInt(fStr);
        } catch (Exception e) {
            System.err.println("Could not parse floor count: " + currentProperty.getFloors());
        }

        // 3. Loop from Top Floor down to 1
        for (int i = totalFloors; i >= 1; i--) {
            int currentLevel = i;

            // Find if we have DB data for this level
            Optional<Floor> floorData = dbFloors.stream().filter(f -> f.getLevel() == currentLevel).findFirst();

            HBox floorRow = new HBox(10);
            floorRow.setAlignment(Pos.CENTER);
            floorRow.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 2 2 2; -fx-padding: 15;");
            floorRow.setMaxWidth(580);

            // A. Draw Existing Rooms
            if (floorData.isPresent()) {
                for (Room room : floorData.get().getRooms()) {
                    Pane roomNode = createRoomNode(room, currentLevel);
                    floorRow.getChildren().add(roomNode);
                }
            }

            // B. Draw "ADD ROOM (+)" Button for this floor
            StackPane addBtn = createAddButton(currentLevel);
            floorRow.getChildren().add(addBtn);

            houseContainer.getChildren().add(floorRow);
        }
    }

    // Creates the green/red room box
    private Pane createRoomNode(Room room, int level) {
        StackPane roomPane = new StackPane();
        roomPane.setPrefSize(80, 60);
        String color = room.getStatus().equalsIgnoreCase("Occupied") ? "#e74c3c" : "#2ecc71";
        roomPane.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 4; -fx-cursor: hand; -fx-border-color:white;");

        Label nameLbl = new Label(room.getRoomNumber());
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        roomPane.getChildren().add(nameLbl);

        // CLICK to Edit
        roomPane.setOnMouseClicked(e -> openRoomEditor(level, room));

        // HOVER for Details (Keep your existing tooltip code here if you want)
        return roomPane;
    }

    // Creates the dashed "+" box
    private StackPane createAddButton(int floorLevel) {
        StackPane btn = new StackPane();
        btn.setPrefSize(80, 60);
        btn.setStyle("-fx-border-color: #95a5a6; -fx-border-style: dashed; -fx-border-width: 2; -fx-background-color: transparent; -fx-cursor: hand;");

        Label plus = new Label("+");
        plus.setStyle("-fx-font-size: 24px; -fx-text-fill: #95a5a6;");
        btn.getChildren().add(plus);

        btn.setOnMouseClicked(e -> openRoomEditor(floorLevel, null)); // Null room means ADD mode
        return btn;
    }

    private void openRoomEditor(int floorLevel, Room room) {
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
                // ADD MODE
                controller.setMetadata(currentProperty.getId(), floorLevel);
            } else {
                // EDIT MODE
                controller.setMetadata(currentProperty.getId(), floorLevel); // Pass ID for saving
                controller.setRoomData(room);
            }

            dialogStage.showAndWait();

            // REFRESH VIEW AFTER SAVE
            if (controller.isSaveClicked()) {
                refreshView();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void refreshView() {
        // We need to fetch the data again.
        // NOTE: This requires a reference back to the MainController or duplicating the fetch logic here.
        // For simplicity, let's just duplicate the fetch logic briefly or pass a callback.

        // SIMPLE WAY: Close this window and ask user to reopen (easiest to code now)
        // BETTER WAY: Call the fetch method from MyPropertiesController using a static helper or DB handler

        // To make it instant, we can reload existing data if we had a callback.
        // For now, let's just re-fetch using a DatabaseHandler helper:
        MyPropertiesController.reloadHouseView(this, currentProperty);
    }
    @FXML
    private void handleBack() {
        // Get the current window (Stage) and close it
        Stage stage = (Stage) houseContainer.getScene().getWindow();
        stage.close();
    }
}