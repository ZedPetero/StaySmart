package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public class PropertyCardController {

    @FXML private Label lblType;
    @FXML private Label lblName;
    @FXML private Label lblLocation;
    @FXML private Label lblPrice;
    @FXML private ImageView imgProperty;
    @FXML private Button btnRemove; // Ensure you add fx:id="btnRemove" to the button in FXML if using Java hover

    private Property property;
    private MyPropertiesController parentController;

    public void setData(Property property, MyPropertiesController parentController) {
        this.property = property;
        this.parentController = parentController;

        // Set Text
        lblType.setText(property.getType() != null ? property.getType().toUpperCase() : "PROPERTY");
        lblName.setText(property.getName());
        lblLocation.setText(property.getLocation());
        lblPrice.setText("₱ " + String.format("%,.2f", property.getPrice()) + " / mo");

        // Clip Image Corners
        Rectangle clip = new Rectangle(220, 140);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imgProperty.setClip(clip);

        // 🟢 LOAD IMAGE: Prioritize BLOB (image_data)
        boolean imageLoaded = false;

        if (property.getImageData() != null && property.getImageData().length > 0) {
            try {
                imgProperty.setImage(new Image(new ByteArrayInputStream(property.getImageData())));
                imageLoaded = true;
            } catch (Exception e) {
                System.out.println("Error loading image bytes");
            }
        }

        // If no image loaded, show default
        if (!imageLoaded) {
            try {
                imgProperty.setImage(new Image(getClass().getResourceAsStream("/images/homeicon.png")));
            } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleRemove() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Property");
        alert.setHeaderText("Delete " + property.getName() + "?");
        alert.setContentText("This cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteFromDB();
        }
    }

    @FXML
    private void handleCheckDetails() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details");
        alert.setHeaderText(property.getName());
        alert.setContentText("Details view coming soon.");
        alert.show();
    }

    private void deleteFromDB() {
        String query = "DELETE FROM properties WHERE id = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, property.getId());
            pstmt.executeUpdate();
            if(parentController != null) parentController.loadPropertiesFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}