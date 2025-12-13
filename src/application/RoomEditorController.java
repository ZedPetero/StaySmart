package application;

import application.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RoomEditorController {

    @FXML private TextField txtRoomNumber;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> comboStatus;

    private Stage dialogStage;
    private int propertyId;
    private int floorLevel;
    private Room existingRoom; // If editing, this is not null
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        comboStatus.getItems().addAll("Available", "Occupied", "Maintenance");
        comboStatus.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Call this for ADDING a new room
    public void setMetadata(int propertyId, int floorLevel) {
        this.propertyId = propertyId;
        this.floorLevel = floorLevel;
    }

    // Call this for EDITING an existing room
    public void setRoomData(Room room) {
        this.existingRoom = room;
        txtRoomNumber.setText(room.getRoomNumber());
        txtPrice.setText(String.valueOf(room.getPrice()));
        comboStatus.setValue(room.getStatus());
        // In edit mode, we disable room number editing to prevent ID conflicts (optional)
        txtRoomNumber.setDisable(true);
    }

    public boolean isSaveClicked() { return saveClicked; }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            saveToDatabase();
            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML private void handleCancel() { dialogStage.close(); }

    private boolean isInputValid() {
        // Add basic validation checks here (e.g., price is a number)
        return !txtRoomNumber.getText().isEmpty() && !txtPrice.getText().isEmpty();
    }

    private void saveToDatabase() {
        try (Connection conn = DatabaseHandler.getConnection()) {
            if (existingRoom == null) {
                // INSERT NEW ROOM
                String query = "INSERT INTO rooms (property_id, floor_level, room_number, price, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, propertyId);
                pstmt.setInt(2, floorLevel);
                pstmt.setString(3, txtRoomNumber.getText());
                pstmt.setDouble(4, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(5, comboStatus.getValue());
                pstmt.executeUpdate();
            } else {
                // UPDATE EXISTING ROOM
                // Note: We usually update by ID, but if you don't have room ID in model, update by Number + PropID
                String query = "UPDATE rooms SET price = ?, status = ? WHERE room_number = ? AND property_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setDouble(1, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(2, comboStatus.getValue());
                pstmt.setString(3, existingRoom.getRoomNumber());
                pstmt.setInt(4, propertyId); // Assuming you passed propertyId even during edit, or store it in Room object
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}