package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyPropertiesController {

    // Matches the fx:id="propertiesGrid" in your FXML
    @FXML private GridPane propertiesGrid;

    // Matches the fx:id="emptyStateBox"
    @FXML private VBox emptyStateBox;

    @FXML
    public void initialize() {
        loadPropertiesFromDatabase();
    }

    public void loadPropertiesFromDatabase() {
        // Clear previous items
        propertiesGrid.getChildren().clear();

        String query = "SELECT * FROM properties";

        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            int column = 0;
            int row = 1;

            boolean hasProperties = false;

            while (rs.next()) {
                hasProperties = true;

                // Create Property Object
                Property p = new Property(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getDouble("price"),
                        rs.getString("type"),
                        rs.getString("floors"),
                        rs.getString("image_path")
                );

                // Load Card
                FXMLLoader loader = new FXMLLoader(getClass().getResource("PropertyCard.fxml"));
                VBox card = loader.load();

                // Pass Data
                PropertyCardController controller = loader.getController();
                controller.setData(p, this);

                // Add to Grid (column, row)
                propertiesGrid.add(card, column, row);

                // Grid Logic (3 columns max)
                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            // Toggle Empty State Visibility
            if (hasProperties) {
                emptyStateBox.setVisible(false);
                emptyStateBox.setManaged(false);
                propertiesGrid.setVisible(true);
            } else {
                emptyStateBox.setVisible(true);
                emptyStateBox.setManaged(true);
                propertiesGrid.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProperty() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddPropertyDialog.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Property");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);

            // Icon
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/homeicon.png")));
            } catch (Exception ignored) {}

            stage.showAndWait();

            // Refresh after closing dialog
            loadPropertiesFromDatabase();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}