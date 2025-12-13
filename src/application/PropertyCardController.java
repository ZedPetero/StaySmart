package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.io.File;
import java.util.Objects;

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

        String imagePath = property.getImagePath();

        if (property.getImagePath() != null && !property.getImagePath().isEmpty()) {
            File file = new File(property.getImagePath());
            if (file.exists()) {
                imgProperty.setImage(new Image(file.toURI().toString()));
            } else {
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
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
    private void loadDefaultImage() {
        var url = getClass().getResource("/application/images/homeicon.png");
        if (url != null) {
            imgProperty.setImage(new Image(url.toExternalForm()));
        } else {
            System.err.println("⚠ Default image not found: /application/images/homeicon.png");
        }
    }
}