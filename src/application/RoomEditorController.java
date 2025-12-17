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
import java.util.ArrayList;
import java.util.List;

public class RoomEditorController {

    @FXML private TextField txtRoomNumber;
    @FXML private TextField txtPrice;
    @FXML private ComboBox<String> comboStatus;

    // --- NEW FACILITY CONTROLS ---
    @FXML private CheckBox chkFan, chkAC, chkCurtains, chkCabinet, chkTable;
    @FXML private CheckBox chkHooks, chkOutlets, chkExtension, chkSubmeter, chkSmoke, chkOther;
    @FXML private TextField txtOther;
    // -----------------------------

    @FXML private Label lblImagePath;
    @FXML private ImageView imgPreview;
    @FXML private CheckBox chkPaid;

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

    public void setMetadata(int propertyId, int floorLevel, int nextRoomNumber) {
        this.propertyId = propertyId;
        this.floorLevel = floorLevel;
        this.txtRoomNumber.setText(String.valueOf(nextRoomNumber));
    }

    public void setRoomData(Room room) {
        this.existingRoom = room;
        txtRoomNumber.setText(room.getRoomNumber());
        txtPrice.setText(String.valueOf(room.getPrice()));
        comboStatus.setValue(room.getStatus());

        // --- LOAD FACILITIES FROM STRING TO CHECKBOXES ---
        String facs = room.getFacilities();
        if (facs != null && !facs.isEmpty()) {
            if (facs.contains("Electric fan")) chkFan.setSelected(true);
            if (facs.contains("Air-conditioned unit")) chkAC.setSelected(true);
            if (facs.contains("Curtain")) chkCurtains.setSelected(true);
            if (facs.contains("Cabinet")) chkCabinet.setSelected(true);
            if (facs.contains("Desk")) chkTable.setSelected(true);
            if (facs.contains("Hangers")) chkHooks.setSelected(true);
            if (facs.contains("Outlets")) chkOutlets.setSelected(true);
            if (facs.contains("Extension")) chkExtension.setSelected(true);
            if (facs.contains("Sub-meter")) chkSubmeter.setSelected(true);
            if (facs.contains("Smoke")) chkSmoke.setSelected(true);

            // Check for "Other" content
            // Simple logic: if checking specific items leaves leftover text, you might handle it here
            // For now, we rely on the user re-entering 'Other' if needed, or you can add custom parsing
        }

        // LOAD IMAGE
        if (room.getImagePath() != null && !room.getImagePath().isEmpty()) {
            selectedImagePath = room.getImagePath();
            lblImagePath.setText(new File(selectedImagePath).getName());
            loadImagePreview(selectedImagePath);
        }

        // LOAD PAYMENT
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
            String paymentStatus = chkPaid.isSelected() ? "Paid" : "Pending";

            // --- GATHER FACILITIES INTO STRING ---
            List<String> facList = new ArrayList<>();
            if (chkFan.isSelected()) facList.add("Electric fan");
            if (chkAC.isSelected()) facList.add("Air-conditioned unit");
            if (chkCurtains.isSelected()) facList.add("Curtain / Window blinds");
            if (chkCabinet.isSelected()) facList.add("Cabinet / Closet");
            if (chkTable.isSelected()) facList.add("Study table / Desk");
            if (chkHooks.isSelected()) facList.add("Wall hooks / Hangers");
            if (chkOutlets.isSelected()) facList.add("Electrical outlets");
            if (chkExtension.isSelected()) facList.add("Extension cord (fixed)");
            if (chkSubmeter.isSelected()) facList.add("Own electric sub-meter");
            if (chkSmoke.isSelected()) facList.add("Smoke detector");
            if (chkOther.isSelected() && !txtOther.getText().isEmpty()) facList.add(txtOther.getText());

            String facilitiesString = String.join(", ", facList);
            // -------------------------------------

            if (existingRoom == null) {
                String query = "INSERT INTO rooms (property_id, floor_level, room_number, price, status, facilities, image_path, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, propertyId);
                pstmt.setInt(2, floorLevel);
                pstmt.setString(3, txtRoomNumber.getText());
                pstmt.setDouble(4, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(5, comboStatus.getValue());
                pstmt.setString(6, facilitiesString); // Saved as comma-separated string
                pstmt.setString(7, selectedImagePath);
                pstmt.setString(8, paymentStatus);
                pstmt.executeUpdate();
            } else {
                String query = "UPDATE rooms SET price = ?, status = ?, facilities = ?, image_path = ?, payment_status = ? WHERE room_number = ? AND property_id = ?";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setDouble(1, Double.parseDouble(txtPrice.getText()));
                pstmt.setString(2, comboStatus.getValue());
                pstmt.setString(3, facilitiesString); // Saved as comma-separated string
                pstmt.setString(4, selectedImagePath);
                pstmt.setString(5, paymentStatus);
                pstmt.setString(6, existingRoom.getRoomNumber());
                pstmt.setInt(7, propertyId);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}