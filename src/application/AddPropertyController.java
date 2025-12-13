package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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

    // 🟢 CHANGE 1: We store the actual File object, not just the path string
    private File selectedImageFile = null;

    private boolean saveClicked = false;
    private List<TextField> roomInputs = new ArrayList<>();

    @FXML
    public void initialize() {
        // Setup ComboBox (Urban, Rural, Apartment, etc.)
        cmbType.getItems().addAll( "Urban", "Rural");
        cmbType.getSelectionModel().selectFirst();

        txtFloors.textProperty().addListener((observable, oldValue, newValue) -> {
            generateRoomInputs(newValue);
        });
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
        } catch (NumberFormatException e) {
            // Ignore non-numbers
        }
    }

    // --- Image Picker ---
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Property Image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) txtName.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            // 🟢 CHANGE 2: Save the file object so we can read it later
            selectedImageFile = file;
            lblImageName.setText(file.getName());
        }
    }

    // --- Save to Database ---
    @FXML
    private void handleSave() {
        if (txtName.getText().isEmpty() || txtLocation.getText().isEmpty() || txtPrice.getText().isEmpty()) {
            System.out.println("Please fill required fields");
            return;
        }

        try (Connection conn = DatabaseHandler.getConnection()) {
            String sql = "INSERT INTO properties (name, location, price, type, floors, image_path) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, txtName.getText());
            pstmt.setString(2, txtLocation.getText());
            pstmt.setDouble(3, Double.parseDouble(txtPrice.getText().replace(",", "")));
            pstmt.setString(4, cmbType.getValue());
            pstmt.setString(5, txtFloors.getText());

            if (selectedImageFile != null) {
                pstmt.setString(6, selectedImageFile.getAbsolutePath());
            } else {
                pstmt.setString(6, null);
            }

            pstmt.executeUpdate();

            // Handle Dynamic Floors logic
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int newPropertyId = rs.getInt(1);
                saveFloors(newPropertyId);
            }

            saveClicked = true;
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveFloors(int propertyId) {
        // Keeps your existing logic for the property_floors table
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) txtName.getScene().getWindow();
        stage.close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }
}