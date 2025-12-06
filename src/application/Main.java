package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the custom font before creating the scene
        Font.loadFont(getClass().getResource("/application/fonts/Outfit-Regular.ttf").toExternalForm(), 14);

        // Load the homepage
        Parent root = FXMLLoader.load(getClass().getResource("Homepage.fxml"));
        Scene scene = new Scene(root, 1400, 750); // Explicit size
        primaryStage.setTitle("StaySmart");
        // Set application icon
        try {
            Image icon = new Image(getClass().getResource("/application/images/homeicon.png").toExternalForm());
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
