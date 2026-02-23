package nw;

/**
 * Launcher class for the JavaFX application.
 * Needed as a workaround for JavaFX module path issues.
 */
public class Main {
    public static void main(String[] args) {
        nw.ui.App.main(args);
    }
}
