package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load every Outfit weight once, so CSS families like "Outfit Bold" resolve everywhere
        loadFonts();
        // Create/open the SQLite file now so problems show up at startup instead of at login
        if (!DatabaseHandler.isDatabaseReachable()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Database Unavailable");
            alert.setHeaderText(null);
            alert.setContentText(DatabaseHandler.DB_UNREACHABLE_MESSAGE);
            alert.showAndWait();
        }
        // Load the homepage at its design size (scaled down automatically on small screens)
        Parent root = FXMLLoader.load(getClass().getResource("Homepage.fxml"));
        AppWindow.showFixed(primaryStage, root, "StaySmart", AppWindow.HOMEPAGE_WIDTH, AppWindow.HOMEPAGE_HEIGHT);
    }

    private void loadFonts() {
        String[] weights = { "Thin", "ExtraLight", "Light", "Regular", "Medium", "SemiBold", "Bold", "ExtraBold", "Black" };
        for (String w : weights) {
            try {
                java.net.URL url = getClass().getResource("/application/fonts/Outfit-" + w + ".ttf");
                if (url != null) {
                    Font.loadFont(url.toExternalForm(), 14);
                }
            } catch (Exception e) {
                // Missing font weight: JavaFX falls back to the system font
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
