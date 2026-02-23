package nw.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import nw.core.*;

import java.util.function.Consumer;

/**
 * Alignment input page with sequence text areas, parameter controls, and presets.
 */
public class AlignmentPane extends ScrollPane {

    private final TextArea seq1Input;
    private final TextArea seq2Input;
    private final Label seq1Error;
    private final Label seq2Error;
    private final Label seq1Length;
    private final Label seq2Length;

    private final Spinner<Integer> matchSpinner;
    private final Spinner<Integer> mismatchSpinner;
    private final Spinner<Integer> gapOpenSpinner;
    private final Spinner<Integer> gapExtendSpinner;
    private final ComboBox<String> presetCombo;

    private final Button alignButton;
    private final Label statusLabel;

    private final Consumer<AlignmentResult> onResult;

    public AlignmentPane(Consumer<AlignmentResult> onResult) {
        this.onResult = onResult;

        VBox content = new VBox(28);
        content.setPadding(new Insets(40, 48, 60, 48));
        content.getStyleClass().add("alignment-content");

        // --- Page title ---
        Label title = new Label("Sequenz-Alignment");
        title.getStyleClass().add("page-title");

        Label desc = new Label("Gib zwei Nukleotidsequenzen ein und konfiguriere die Scoring-Parameter.");
        desc.getStyleClass().add("page-description");
        desc.setWrapText(true);

        VBox header = new VBox(8, title, desc);

        // --- Sequence Inputs ---
        seq1Input = createSequenceInput("Sequenz 1", "z.B. AGTACGCA oder FASTA-Format...");
        seq2Input = createSequenceInput("Sequenz 2", "z.B. TATGC oder FASTA-Format...");

        seq1Error = new Label();
        seq1Error.getStyleClass().add("error-label");
        seq1Error.setVisible(false);
        seq1Error.setManaged(false);

        seq2Error = new Label();
        seq2Error.getStyleClass().add("error-label");
        seq2Error.setVisible(false);
        seq2Error.setManaged(false);

        seq1Length = new Label("Länge: 0");
        seq1Length.getStyleClass().add("length-label");
        seq2Length = new Label("Länge: 0");
        seq2Length.getStyleClass().add("length-label");

        seq1Input.textProperty().addListener((obs, o, n) -> updateSequenceInfo(n, seq1Length, seq1Error));
        seq2Input.textProperty().addListener((obs, o, n) -> updateSequenceInfo(n, seq2Length, seq2Error));

        VBox seq1Box = new VBox(6,
            createFieldLabel("Sequenz 1 (DNA/RNA)"),
            seq1Input, seq1Length, seq1Error
        );

        VBox seq2Box = new VBox(6,
            createFieldLabel("Sequenz 2 (DNA/RNA)"),
            seq2Input, seq2Length, seq2Error
        );

        VBox sequencesBox = new VBox(20, seq1Box, seq2Box);
        sequencesBox.getStyleClass().add("card");
        sequencesBox.setPadding(new Insets(24));

        // --- Parameters ---
        Label paramTitle = new Label("Scoring-Parameter");
        paramTitle.getStyleClass().add("card-title");

        // Preset selector
        presetCombo = new ComboBox<>();
        presetCombo.getItems().addAll("Standard DNA", "Strikt", "Tolerant", "Benutzerdefiniert");
        presetCombo.setValue("Standard DNA");
        presetCombo.getStyleClass().add("preset-combo");
        presetCombo.setOnAction(e -> applyPreset());

        HBox presetRow = new HBox(12,
            createFieldLabel("Preset:"),
            presetCombo
        );
        presetRow.setAlignment(Pos.CENTER_LEFT);

        // Spinners
        matchSpinner = createSpinner("Match-Score", 2, -10, 10);
        mismatchSpinner = createSpinner("Mismatch-Penalty", -1, -10, 10);
        gapOpenSpinner = createSpinner("Gap-Open-Penalty", -2, -20, 0);
        gapExtendSpinner = createSpinner("Gap-Extension-Penalty", -1, -10, 0);

        // Add listeners to detect manual changes → switch to custom preset
        matchSpinner.valueProperty().addListener((o, a, b) -> presetCombo.setValue("Benutzerdefiniert"));
        mismatchSpinner.valueProperty().addListener((o, a, b) -> presetCombo.setValue("Benutzerdefiniert"));
        gapOpenSpinner.valueProperty().addListener((o, a, b) -> presetCombo.setValue("Benutzerdefiniert"));
        gapExtendSpinner.valueProperty().addListener((o, a, b) -> presetCombo.setValue("Benutzerdefiniert"));

        GridPane paramGrid = new GridPane();
        paramGrid.setHgap(24);
        paramGrid.setVgap(16);

        paramGrid.add(createParamBox("Match", "Punkte bei Übereinstimmung", matchSpinner), 0, 0);
        paramGrid.add(createParamBox("Mismatch", "Strafe bei Fehlpaarung", mismatchSpinner), 1, 0);
        paramGrid.add(createParamBox("Gap Open", "Strafe für neue Lücke", gapOpenSpinner), 0, 1);
        paramGrid.add(createParamBox("Gap Extension", "Strafe für Lückenverlängerung", gapExtendSpinner), 1, 1);

        VBox paramsBox = new VBox(16, paramTitle, presetRow, paramGrid);
        paramsBox.getStyleClass().add("card");
        paramsBox.setPadding(new Insets(24));

        // --- Align button ---
        alignButton = new Button("▶  Alignment starten");
        alignButton.getStyleClass().add("align-button");
        alignButton.setOnAction(e -> performAlignment());

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setVisible(false);

        HBox actionRow = new HBox(16, alignButton, statusLabel);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        // --- Info box ---
        Label infoText = new Label(
            "💡 Tipp: Für große Sequenzen (>1000 Basen) kann die Berechnung einen Moment dauern. " +
            "Die Matrix-Visualisierung wird für Sequenzen >100 Basen vereinfacht dargestellt."
        );
        infoText.getStyleClass().add("info-box");
        infoText.setWrapText(true);

        content.getChildren().addAll(header, sequencesBox, paramsBox, actionRow, infoText);

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("alignment-scroll");
    }

    private TextArea createSequenceInput(String name, String prompt) {
        TextArea area = new TextArea();
        area.setPromptText(prompt);
        area.setPrefRowCount(4);
        area.setWrapText(true);
        area.getStyleClass().add("sequence-input");
        return area;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    private Spinner<Integer> createSpinner(String name, int defaultVal, int min, int max) {
        Spinner<Integer> spinner = new Spinner<>(min, max, defaultVal);
        spinner.setEditable(true);
        spinner.setPrefWidth(100);
        spinner.getStyleClass().add("param-spinner");
        return spinner;
    }

    private VBox createParamBox(String name, String hint, Spinner<Integer> spinner) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("param-name");

        Label hintLabel = new Label(hint);
        hintLabel.getStyleClass().add("param-hint");

        VBox box = new VBox(4, nameLabel, spinner, hintLabel);
        box.setPrefWidth(220);
        return box;
    }

    private void applyPreset() {
        String preset = presetCombo.getValue();
        if (preset == null || preset.equals("Benutzerdefiniert")) return;

        // Temporarily remove listeners
        ScoringConfig config = switch (preset) {
            case "Standard DNA" -> ScoringConfig.standardDNA();
            case "Strikt" -> ScoringConfig.strict();
            case "Tolerant" -> ScoringConfig.lenient();
            default -> null;
        };

        if (config != null) {
            // Block listener from resetting to custom
            presetCombo.setOnAction(null);
            matchSpinner.getValueFactory().setValue(config.getMatch());
            mismatchSpinner.getValueFactory().setValue(config.getMismatch());
            gapOpenSpinner.getValueFactory().setValue(config.getGapOpen());
            gapExtendSpinner.getValueFactory().setValue(config.getGapExtend());
            presetCombo.setOnAction(e -> applyPreset());
        }
    }

    private void updateSequenceInfo(String text, Label lengthLabel, Label errorLabel) {
        if (text == null || text.isBlank()) {
            lengthLabel.setText("Länge: 0");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            return;
        }

        SequenceValidator.ValidationResult result = SequenceValidator.validate(text);
        if (result.isValid()) {
            lengthLabel.setText("Länge: " + result.getCleanedSequence().length() + " bp");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        } else {
            lengthLabel.setText("Länge: –");
            errorLabel.setText("⚠ " + result.getErrorMessage());
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void performAlignment() {
        // Validate
        SequenceValidator.ValidationResult v1 = SequenceValidator.validate(seq1Input.getText());
        SequenceValidator.ValidationResult v2 = SequenceValidator.validate(seq2Input.getText());

        if (!v1.isValid()) {
            seq1Error.setText("⚠ " + v1.getErrorMessage());
            seq1Error.setVisible(true);
            seq1Error.setManaged(true);
            return;
        }
        if (!v2.isValid()) {
            seq2Error.setText("⚠ " + v2.getErrorMessage());
            seq2Error.setVisible(true);
            seq2Error.setManaged(true);
            return;
        }

        seq1Error.setVisible(false);
        seq1Error.setManaged(false);
        seq2Error.setVisible(false);
        seq2Error.setManaged(false);

        // Build config
        ScoringConfig config = ScoringConfig.builder()
            .match(matchSpinner.getValue())
            .mismatch(mismatchSpinner.getValue())
            .gapOpen(gapOpenSpinner.getValue())
            .gapExtend(gapExtendSpinner.getValue())
            .build();

        String s1 = v1.getCleanedSequence();
        String s2 = v2.getCleanedSequence();

        // Disable button during computation
        alignButton.setDisable(true);
        statusLabel.setText("⏳ Berechne Alignment...");
        statusLabel.setVisible(true);

        // Run alignment in background thread
        Thread thread = new Thread(() -> {
            try {
                long start = System.nanoTime();
                AlignmentResult result = NeedlemanWunsch.align(s1, s2, config);
                long elapsed = (System.nanoTime() - start) / 1_000_000;

                javafx.application.Platform.runLater(() -> {
                    alignButton.setDisable(false);
                    statusLabel.setText(String.format("✅ Fertig in %d ms", elapsed));
                    onResult.accept(result);
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    alignButton.setDisable(false);
                    statusLabel.setText("❌ Fehler: " + ex.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
