package application;

/**
 * Plain entry point that does not extend javafx.application.Application.
 *
 * Launching {@link Main} directly from the classpath (IntelliJ / VS Code "Run" without
 * --module-path) fails with "JavaFX runtime components are missing". Starting through this
 * class avoids that check, so the app runs with the JavaFX jars Maven puts on the classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
