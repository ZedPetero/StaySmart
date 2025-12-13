package application;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MyPropertiesController implements Initializable {

    @FXML
    private GridPane propertiesGrid;

    // Simple inner class to hold data for now
    private class PropertyData {
        String name;
        String address;
        String price;
        String status; // "Occupied" or "Vacant"

        public PropertyData(String name, String address, String price, String status) {
            this.name = name;
            this.address = address;
            this.price = price;
            this.status = status;
        }
    }

    private List<PropertyData> properties = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Add some dummy data (Later we will get this from MySQL)
        properties.add(new PropertyData("Sunset Villa", "123 Ocean Drive", "$1,200", "Occupied"));
        properties.add(new PropertyData("Urban Loft", "404 Tech Plaza", "$850", "Vacant"));
        properties.add(new PropertyData("Greenwood Estate", "55 Nature Way", "$2,100", "Occupied"));
        properties.add(new PropertyData("Cozy Cabin", "88 Mountain Rd", "$600", "Vacant"));
        properties.add(new PropertyData("Downtown Studio", "101 City Center", "$950", "Occupied"));

        int column = 0;
        int row = 1;

        try {
            for (PropertyData prop : properties) {
                // 2. Create the Card Programmatically
                VBox card = createPropertyCard(prop);

                // 3. Add to Grid (max 3 columns)
                if (column == 3) {
                    column = 0;
                    row++;
                }

                propertiesGrid.add(card, column++, row);

                // Add margins to cards
                GridPane.setMargin(card, new Insets(10));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private VBox createPropertyCard(PropertyData prop) {
        VBox card = new VBox();
        card.getStyleClass().add("property-card");
        card.setSpacing(10);
        card.setPrefWidth(280); // Fixed width for cards

        // --- Image Placeholder ---
        HBox imageContainer = new HBox();
        imageContainer.getStyleClass().add("property-image-container");
        imageContainer.setPrefHeight(150);

        // (Optional: Load real image if you have one, using a placeholder for now)
        // ImageView img = new ImageView(new Image(getClass().getResourceAsStream("images/house_placeholder.png")));

        // --- Status Badge ---
        Label statusLabel = new Label(prop.status);
        statusLabel.getStyleClass().add(prop.status.equals("Occupied") ? "status-occupied" : "status-vacant");

        // --- Details ---
        Label nameLabel = new Label(prop.name);
        nameLabel.getStyleClass().add("card-title");

        Label addressLabel = new Label(prop.address);
        addressLabel.getStyleClass().add("card-address");

        Label priceLabel = new Label(prop.price + "/mo");
        priceLabel.getStyleClass().add("card-price");

        // --- Edit Button ---
        Button editBtn = new Button("Edit Details");
        editBtn.getStyleClass().add("card-button");
        editBtn.setMaxWidth(Double.MAX_VALUE); // Fill width

        // Assemble the card
        card.getChildren().addAll(imageContainer, statusLabel, nameLabel, addressLabel, priceLabel, editBtn);

        return card;
    }
}