package application;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * One place that decides how StaySmart windows look: every window gets the app icon, and the
 * fixed-size pages (homepage, login, signup) are opened at their design size and shrunk
 * uniformly when the screen is smaller than that, so the title bar always stays reachable.
 */
public final class AppWindow {

    /** Design sizes of the fixed-layout pages (the AnchorPane coordinates in their FXML assume these). */
    public static final double HOMEPAGE_WIDTH = 1400, HOMEPAGE_HEIGHT = 750;
    public static final double LOGIN_WIDTH = 1450, LOGIN_HEIGHT = 750;
    public static final double SIGNUP_WIDTH = 1440, SIGNUP_HEIGHT = 750;

    private static Image icon;

    private AppWindow() {
    }

    /** Adds the application icon to a stage (silently does nothing if the image is missing). */
    public static void applyIcon(Stage stage) {
        try {
            if (icon == null) {
                icon = new Image(AppWindow.class.getResource("/application/images/homeicon.png").toExternalForm());
            }
            if (!stage.getIcons().contains(icon)) {
                stage.getIcons().add(icon);
            }
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }

    /**
     * Shows a non-resizable window with the page at its design size. If the design size does not
     * fit on the screen the whole page is scaled down to fit, keeping its layout intact.
     */
    public static void showFixed(Stage stage, Parent root, String title, double width, double height) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        // Leave room for the window frame and title bar
        double scale = Math.min(1.0, Math.min((screen.getWidth() - 16) / width, (screen.getHeight() - 60) / height));

        if (root instanceof Region) {
            Region region = (Region) root;
            region.setPrefSize(width, height);
            region.setMinSize(width, height);
            region.setMaxSize(width, height);
        }

        Parent content = root;
        if (scale < 1.0) {
            root.getTransforms().add(new Scale(scale, scale));
            content = new Group(root); // a Group's bounds include the transform, so the scene fits the scaled page
        }

        Scene scene = new Scene(content, Math.floor(width * scale), Math.floor(height * scale));
        stage.setTitle(title);
        applyIcon(stage);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    /** Shows a window whose size comes from the content (dialogs, detail views, dashboards). */
    public static void show(Stage stage, Parent root, String title) {
        stage.setTitle(title);
        applyIcon(stage);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
