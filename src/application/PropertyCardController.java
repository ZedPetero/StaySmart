package application;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import java.io.File;

public class PropertyCardController {

    @FXML private VBox cardContainer;
    @FXML private ImageView propertyImage;
    @FXML private Label nameLabel, locationLabel, priceLabel, typeLabel, floorsLabel;
    @FXML private FlowPane amenitiesContainer;
    @FXML private Button deleteBtn, editBtn;
    @FXML private HBox actionBox;

    private Property property;
    private MyPropertiesController parentController;

    public void setData(Property property, MyPropertiesController parentController) {
        this.property = property;
        this.parentController = parentController;

        nameLabel.setText(property.getName());
        locationLabel.setText(property.getLocation());
        priceLabel.setText("₱ " + String.format("%,.0f", property.getPrice()));
        typeLabel.setText(property.getType());
        floorsLabel.setText(property.getFloors() + (property.getFloors().equals("1") ? " Floor" : " Floors"));

        if (property.getImagePath() != null && !property.getImagePath().isEmpty()) {
            File file = new File(property.getImagePath());
            if (file.exists()) {
                propertyImage.setImage(new Image(file.toURI().toString()));
                centerImage(propertyImage);
            }
        }

        populateAmenities(property.getAmenities());

        // DELETE: Consume event so it doesn't trigger cardContainer click
        deleteBtn.setOnAction(event -> {
            event.consume();
            parentController.deleteProperty(property);
        });

        // EDIT: Consume event so it doesn't trigger cardContainer click
        editBtn.setOnAction(event -> {
            event.consume();
            parentController.openEditPropertyDialog(property);
        });

        // VIEW DETAILS
        cardContainer.setOnMouseClicked(e -> {
            parentController.openPropertyDetails(property);
        });
    }

    private void populateAmenities(String amenitiesString) {
        amenitiesContainer.getChildren().clear();
        if (amenitiesString == null || amenitiesString.trim().isEmpty()) return;
        String[] items = amenitiesString.split(",");
        for (int i = 0; i < Math.min(items.length, 4); i++) {
            Label tag = new Label(items[i].trim());
            tag.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 4;");
            amenitiesContainer.getChildren().add(tag);
        }
    }

    private void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = img.getWidth();
            double h = img.getHeight();

            // Calculate the aspect ratio of the ImageView (300/160 = 1.875)
            double targetRatio = 300.0 / 160.0;
            double sourceRatio = w / h;

            double viewW, viewH, viewX, viewY;

            if (sourceRatio > targetRatio) {
                // Image is wider than container
                viewH = h;
                viewW = h * targetRatio;
                viewX = (w - viewW) / 2;
                viewY = 0;
            } else {
                // Image is taller than container
                viewW = w;
                viewH = w / targetRatio;
                viewX = 0;
                viewY = (h - viewH) / 2;
            }

            imageView.setViewport(new Rectangle2D(viewX, viewY, viewW, viewH));
        }
    }
}