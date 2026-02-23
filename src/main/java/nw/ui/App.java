package nw.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main JavaFX application with sidebar navigation.
 */
public class App extends Application {

    private StackPane contentArea;
    private OverviewPane overviewPane;
    private AlignmentPane alignmentPane;
    private ResultPane resultPane;

    private Button btnOverview;
    private Button btnAlignment;
    private Button btnResults;
    private Button activeButton;

    @Override
    public void start(Stage primaryStage) {
        // --- Sidebar ---
        VBox sidebar = createSidebar();

        // --- Content area ---
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        HBox.setHgrow(contentArea, Priority.ALWAYS);

        // --- Create pages ---
        overviewPane = new OverviewPane();
        alignmentPane = new AlignmentPane(this::showResults);
        resultPane = new ResultPane();

        // --- Root layout ---
        HBox root = new HBox();
        root.getChildren().addAll(sidebar, contentArea);
        root.getStyleClass().add("root-container");

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/nw/ui/styles.css").toExternalForm());

        primaryStage.setTitle("NeedleAlign – Needleman-Wunsch Sequence Aligner");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Show overview by default
        showOverview();
    }

    private VBox createSidebar() {
        // Logo / Title
        Label logo = new Label("🧬");
        logo.getStyleClass().add("sidebar-logo");

        Label title = new Label("NeedleAlign");
        title.getStyleClass().add("sidebar-title");

        Label subtitle = new Label("Sequence Aligner");
        subtitle.getStyleClass().add("sidebar-subtitle");

        VBox logoBox = new VBox(4, logo, title, subtitle);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(24, 16, 32, 16));

        // Navigation buttons
        btnOverview = createNavButton("📖  Übersicht", "overview-btn");
        btnAlignment = createNavButton("🔬  Alignment", "alignment-btn");
        btnResults = createNavButton("📊  Ergebnisse", "results-btn");

        btnOverview.setOnAction(e -> showOverview());
        btnAlignment.setOnAction(e -> showAlignment());
        btnResults.setOnAction(e -> showResultsPage());

        VBox navButtons = new VBox(4, btnOverview, btnAlignment, btnResults);
        navButtons.setPadding(new Insets(0, 12, 0, 12));

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer
        Label footer = new Label("Needleman-Wunsch\nGlobal Alignment");
        footer.getStyleClass().add("sidebar-footer");
        footer.setMaxWidth(Double.MAX_VALUE);

        // Developer tag
        Label devTag = new Label("Dev – Marcel Ndrecaj");
        devTag.getStyleClass().add("dev-tag");
        devTag.setMaxWidth(Double.MAX_VALUE);

        // Links
        Hyperlink ghLink = new Hyperlink("GitHub");
        ghLink.getStyleClass().add("sidebar-link");
        ghLink.setOnAction(e -> getHostServices().showDocument("https://github.com/MarcelNd/Needleman-Wunsch-Impl"));

        Hyperlink licenseLink = new Hyperlink("MIT License");
        licenseLink.getStyleClass().add("sidebar-link");
        licenseLink.setOnAction(e -> getHostServices()
                .showDocument("https://github.com/MarcelNd/Needleman-Wunsch-Impl/blob/main/LICENSE"));

        HBox linksBox = new HBox(8, ghLink, licenseLink);
        linksBox.setAlignment(Pos.CENTER);
        linksBox.setPadding(new Insets(0, 16, 12, 16));

        VBox sidebar = new VBox();
        sidebar.getChildren().addAll(logoBox, navButtons, spacer, footer, devTag, linksBox);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);

        return sidebar;
    }

    private Button createNavButton(String text, String id) {
        Button btn = new Button(text);
        btn.setId(id);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
        }
        btn.getStyleClass().add("nav-button-active");
        activeButton = btn;
    }

    private void showOverview() {
        contentArea.getChildren().setAll(overviewPane);
        setActiveButton(btnOverview);
    }

    private void showAlignment() {
        contentArea.getChildren().setAll(alignmentPane);
        setActiveButton(btnAlignment);
    }

    private void showResultsPage() {
        contentArea.getChildren().setAll(resultPane);
        setActiveButton(btnResults);
    }

    /**
     * Called when alignment completes – switches to results tab.
     */
    public void showResults(nw.core.AlignmentResult result) {
        resultPane.displayResult(result);
        showResultsPage();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
