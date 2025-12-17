package application;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; // Added VBox
import javafx.scene.shape.Rectangle;

import java.io.File;

public class PropertyCardController {

    @FXML private VBox cardContainer; // Injected from FXML
    @FXML private ImageView propertyImage;
    @FXML private Label nameLabel;
    @FXML private Label locationLabel;
    @FXML private Label priceLabel;
    @FXML private Label typeLabel;
    @FXML private Label floorsLabel;
    @FXML private HBox actionBox;
    @FXML private FlowPane amenitiesContainer;

    // REMOVED statusLabel since we deleted it from FXML

    private Property property;
    private MyPropertiesController parentController;

    public void setData(Property property, MyPropertiesController parentController) {
        this.property = property;
        this.parentController = parentController;

        // 1. Set Text
        nameLabel.setText(property.getName());
        locationLabel.setText(property.getLocation());
        priceLabel.setText("₱ " + String.format("%,.0f", property.getPrice()));
        typeLabel.setText(property.getType());
        floorsLabel.setText(property.getFloors() + (property.getFloors().equals("1") ? " Floor" : " Floors"));

        // 2. Load Image
        if (property.getImagePath() != null && !property.getImagePath().isEmpty()) {
            File file = new File(property.getImagePath());
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                propertyImage.setImage(img);
                centerImage(propertyImage);
            }
        }

        // Rounded Image
        Rectangle clip = new Rectangle(300, 160);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        propertyImage.setClip(clip);

        // 3. Populate Amenities
        populateAmenities(property.getAmenities());

        // 4. Set Click Listener on the WHOLE CARD
        // This makes the Image, the Text, and the Whitespace all click to open details once.
        if (parentController != null) {
            actionBox.setVisible(true);
            actionBox.setManaged(true);

            // Clear any old listeners to be safe
            cardContainer.setOnMouseClicked(null);

            // Set new single listener on the root container
            cardContainer.setOnMouseClicked(e -> {
                parentController.openPropertyDetails(property);
            });
        }
    }

    private void populateAmenities(String amenitiesString) {
        amenitiesContainer.getChildren().clear();

        // Check if null or empty
        if (amenitiesString == null || amenitiesString.trim().isEmpty()) {
            Label placeholder = new Label("None listed");
            placeholder.setStyle("-fx-text-fill: #ccc; -fx-font-size: 10px; -fx-font-style: italic;");
            amenitiesContainer.getChildren().add(placeholder);
            return;
        }

        String[] items = amenitiesString.split(",");
        int maxItemsToShow = 4;
        int count = 0;

        for (String item : items) {
            if (item.trim().isEmpty()) continue; // skip empty splits

            if (count >= maxItemsToShow) {
                Label more = new Label("+" + (items.length - count));
                more.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #666; -fx-font-size: 9px; -fx-padding: 2 5; -fx-background-radius: 4;");
                amenitiesContainer.getChildren().add(more);
                break;
            }

            Label tag = new Label(item.trim());
            // Style: Clean blue pill
            tag.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-font-size: 9px; -fx-padding: 2 6; -fx-background-radius: 4;");
            amenitiesContainer.getChildren().add(tag);

            count++;
        }
    }

    private void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w = 0;
            double h = 0;
            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();
            double reducCoeff = 0;
            if(ratioX >= ratioY) {
                reducCoeff = ratioX;
            } else {
                reducCoeff = ratioY;
            }
            w = imageView.getFitWidth() / reducCoeff;
            h = imageView.getFitHeight() / reducCoeff;
            imageView.setViewport(new Rectangle2D((img.getWidth() - w) / 2, (img.getHeight() - h) / 2, w, h));
        }
    }

    // REMOVED handleView() method entirely as we use the lambda in setData now.
}