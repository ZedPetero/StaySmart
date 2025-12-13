package application;

import application.model.Room;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RoomEditorController {

    @FXML private TextField txtRoomNumber;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> comboStatus;
    @FXML private TextArea txtFacilities;
    @FXML private Label lblImagePath;
    @FXML private ImageView imgPreview;
    @FXML private CheckBox chkPaid; // Add this at the top with other @FXML variables
    private Stage dialogStage;
    private int propertyId;
    private int floorLevel;
    private Room existingRoom;
    private boolean saveClicked = false;
    private String selectedImagePath = "";

    @FXML
    public void initialize() {
        comboStatus.getItems().addAll("Available", "Occupied", "Maintenance");
        comboStatus.getSelectionModel().selectFirst();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // Logic: Pass the count of existing rooms to determine the next number
    public void setMetadata(int propertyId, int floorLevel, int existingRoomCount) {
        this.propertyId = propertyId;
        this.floorLevel = floorLevel;
        // Auto-Generate ID: If there are 0 rooms, this is Room 1.
        this.txtRoomNumber.setText(String.valueOf(existingRoomCount + 1));
    }

    public void setRoomData(Room room) {
        this.existingRoom = room;
        txtRoomNumber.setText(room.getRoomNumber());
        txtPrice.setText(String.valueOf(room.getPrice()));
        comboStatus.setValue(room.getStatus());
        txtFacilities.setText(room.getFacilities());

        // LOAD IMAGE
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            selectedImagePath = room.getImagePath();
            loadImagePreview(selectedImagePath);
        }

        // LOAD PAYMENT STATUS (NEW)
        // If status is "Paid", check the box. Otherwise unchecked.
        if (room.getPaymentStatus() != null && room.getPaymentStatus().equalsIgnoreCase("Paid")) {
            chkPaid.setSelected(true);
        } else {
            chkPaid.setSelected(false);
        }
    }

    public boolean isSaveClicked() { return saveClicked; }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Room Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(dialogStage);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImagePath.setText(file.getName());
            loadImagePreview(selectedImagePath);
        }
    }

    private void loadImagePreview(String path) {
        try {
            File f = new File(path);
            if(f.exists()) imgPreview.setImage(new Image(f.toURI().toString()));
        } catch (Exception ignored) {}
    }

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
        return !txtPrice.getText().isEmpty();
    }

    private void saveToDatabase() {
        try (Connection conn = DatabaseHandler.getConnection()) {
            // Determine text based on checkbox
            String paymentStatus = chkPaid.isSelected() ? "Paid" : "Pending";

            if (existingRoom == null) {
                // INSERT NEW ROOM
                String query = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, facilities, image_path, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, propertyId);
                pstmt.setInt(2, floorLevel);
                pstmt.setString(3, txtRoomNumber.getText());
                pstmt.setDouble(4, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(5, comboStatus.getValue());
                pstmt.setString(6, txtFacilities.getText());
                pstmt.setString(7, selectedImagePath);
                pstmt.setString(8, paymentStatus); // <--- NEW
                pstmt.executeUpdate();
            } else {
                // UPDATE EXISTING ROOM
                String query = "UPDATE rooms SET price = ?, status = ?, facilities = ?, image_path = ?, payment_status = ? WHERE room_number = ? AND property_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setDouble(1, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(2, comboStatus.getValue());
                pstmt.setString(3, txtFacilities.getText());
                pstmt.setString(4, selectedImagePath);
                pstmt.setString(5, paymentStatus); // <--- NEW
                pstmt.setString(6, existingRoom.getRoomNumber());
                pstmt.setInt(7, propertyId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}