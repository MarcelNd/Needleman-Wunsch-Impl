package nw.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import nw.core.AlignmentResult;

/**
 * Results visualization page with aligned sequences, score matrix heatmap,
 * and statistics.
 */
public class ResultPane extends ScrollPane {

    private final VBox content;
    private final Label placeholder;

    // Traceback constants (must match NeedlemanWunsch)
    private static final int DIAG = 0;
    private static final int UP   = 1;
    private static final int LEFT = 2;

    public ResultPane() {
        content = new VBox(28);
        content.setPadding(new Insets(40, 48, 60, 48));
        content.getStyleClass().add("result-content");

        placeholder = new Label("Noch kein Alignment durchgeführt.\nWechsle zum \"Alignment\"-Tab, um zu starten.");
        placeholder.getStyleClass().add("placeholder-label");
        placeholder.setAlignment(Pos.CENTER);

        content.getChildren().add(placeholder);
        content.setAlignment(Pos.CENTER);

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("result-scroll");
    }

    /**
     * Displays alignment results.
     */
    public void displayResult(AlignmentResult result) {
        content.getChildren().clear();
        content.setAlignment(Pos.TOP_LEFT);

        // --- Title ---
        Label title = new Label("Alignment-Ergebnis");
        title.getStyleClass().add("page-title");

        content.getChildren().addAll(
            title,
            createStatsSection(result),
            createAlignmentDisplay(result),
            createMatrixSection(result)
        );
    }

    // --- Statistics Cards ---
    private HBox createStatsSection(AlignmentResult result) {
        VBox scoreCard = createStatCard("Score", String.format("%.0f", result.getScore()), "stat-score");
        VBox identityCard = createStatCard("Identität", String.format("%.1f%%", result.getIdentityPercent()), "stat-identity");
        VBox matchCard = createStatCard("Matches", String.valueOf(result.getMatchCount()), "stat-matches");
        VBox mismatchCard = createStatCard("Mismatches", String.valueOf(result.getMismatchCount()), "stat-mismatches");
        VBox gapCard = createStatCard("Gaps", String.valueOf(result.getGapCount()), "stat-gaps");
        VBox lengthCard = createStatCard("Länge", result.getAlignmentLength() + " bp", "stat-length");

        HBox stats = new HBox(16, scoreCard, identityCard, matchCard, mismatchCard, gapCard, lengthCard);
        stats.setAlignment(Pos.CENTER_LEFT);
        return stats;
    }

    private VBox createStatCard(String label, String value, String styleClass) {
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().addAll("stat-value", styleClass);

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        VBox card = new VBox(4, valueLabel, nameLabel);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(120);
        return card;
    }

    // --- Aligned Sequences Display ---
    private VBox createAlignmentDisplay(AlignmentResult result) {
        Label heading = new Label("Ausgerichtete Sequenzen");
        heading.getStyleClass().add("result-section-heading");

        String aln1 = result.getAlignedSeq1();
        String aln2 = result.getAlignedSeq2();
        String matchLine = result.getMatchLine();

        VBox alignmentBox = new VBox(2);
        alignmentBox.getStyleClass().add("alignment-display-box");
        alignmentBox.setPadding(new Insets(20));

        // Display in blocks of 60 characters
        int blockSize = 60;
        int length = aln1.length();

        for (int start = 0; start < length; start += blockSize) {
            int end = Math.min(start + blockSize, length);

            // Position labels
            Label posLabel = new Label(String.format("  %d – %d", start + 1, end));
            posLabel.getStyleClass().add("position-label");

            // Sequence 1 row
            HBox row1 = createSequenceRow("Seq1: ", aln1.substring(start, end), aln2.substring(start, end), true);
            // Match line
            HBox matchRow = createMatchRow("      ", matchLine.substring(start, end));
            // Sequence 2 row
            HBox row2 = createSequenceRow("Seq2: ", aln2.substring(start, end), aln1.substring(start, end), false);

            VBox block = new VBox(0, posLabel, row1, matchRow, row2);
            block.setPadding(new Insets(4, 0, 12, 0));
            alignmentBox.getChildren().add(block);
        }

        ScrollPane scrollBox = new ScrollPane(alignmentBox);
        scrollBox.setFitToWidth(true);
        scrollBox.setMaxHeight(400);
        scrollBox.getStyleClass().add("alignment-scroll-inner");

        VBox section = new VBox(12, heading, scrollBox);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(24));
        return section;
    }

    private HBox createSequenceRow(String prefix, String seq, String otherSeq, boolean isTop) {
        Label prefixLabel = new Label(prefix);
        prefixLabel.getStyleClass().add("mono-prefix");

        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(prefixLabel);

        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            char other = otherSeq.charAt(i);
            Label label = new Label(String.valueOf(c));
            label.getStyleClass().add("base-cell");
            label.setMinWidth(14);
            label.setAlignment(Pos.CENTER);

            if (c == '-') {
                label.getStyleClass().add("gap-base");
            } else if (c == other) {
                label.getStyleClass().add("match-base");
            } else {
                label.getStyleClass().add("mismatch-base");
            }

            row.getChildren().add(label);
        }
        return row;
    }

    private HBox createMatchRow(String prefix, String matchLine) {
        Label prefixLabel = new Label(prefix);
        prefixLabel.getStyleClass().add("mono-prefix");

        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(prefixLabel);

        for (char c : matchLine.toCharArray()) {
            Label label = new Label(String.valueOf(c));
            label.getStyleClass().add("match-cell");
            label.setMinWidth(14);
            label.setAlignment(Pos.CENTER);
            row.getChildren().add(label);
        }
        return row;
    }

    // --- Score Matrix Heatmap ---
    private VBox createMatrixSection(AlignmentResult result) {
        Label heading = new Label("Scoring-Matrix & Traceback");
        heading.getStyleClass().add("result-section-heading");

        double[][] matrix = result.getScoreMatrix();
        int[][] traceback = result.getTracebackMatrix();
        int rows = matrix.length;
        int cols = matrix[0].length;

        // For large matrices, skip visualization
        if (rows > 101 || cols > 101) {
            Label tooLarge = new Label(
                String.format("Matrix zu groß für Visualisierung (%d × %d). " +
                              "Maximal 100 × 100 wird dargestellt.", rows - 1, cols - 1)
            );
            tooLarge.getStyleClass().add("info-box");
            tooLarge.setWrapText(true);

            VBox section = new VBox(12, heading, tooLarge);
            section.getStyleClass().add("card");
            section.setPadding(new Insets(24));
            return section;
        }

        // Draw matrix on Canvas
        int cellSize = Math.max(32, Math.min(50, 800 / Math.max(rows, cols)));
        int headerSize = cellSize; // space for sequence labels

        String seq1 = result.getOriginalSeq1().toUpperCase();
        String seq2 = result.getOriginalSeq2().toUpperCase();

        int canvasWidth = headerSize + cols * cellSize;
        int canvasHeight = headerSize + rows * cellSize;

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Find min/max for color scaling
        double minVal = Double.MAX_VALUE, maxVal = Double.MIN_VALUE;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] != Double.NEGATIVE_INFINITY) {
                    minVal = Math.min(minVal, matrix[i][j]);
                    maxVal = Math.max(maxVal, matrix[i][j]);
                }
            }
        }
        double range = maxVal - minVal;
        if (range == 0) range = 1;

        // Determine traceback path
        boolean[][] onPath = new boolean[rows][cols];
        {
            int ti = rows - 1, tj = cols - 1;
            onPath[ti][tj] = true;
            while (ti > 0 || tj > 0) {
                if (ti > 0 && tj > 0 && traceback[ti][tj] == DIAG) {
                    ti--; tj--;
                } else if (ti > 0 && (tj == 0 || traceback[ti][tj] == UP)) {
                    ti--;
                } else {
                    tj--;
                }
                onPath[ti][tj] = true;
            }
        }

        Font labelFont = Font.font("Menlo, Monaco, monospace", FontWeight.BOLD, Math.max(10, cellSize * 0.35));
        Font scoreFont = Font.font("Menlo, Monaco, monospace", Math.max(8, cellSize * 0.28));
        gc.setFont(labelFont);

        // Draw sequence labels (top row)
        gc.setFill(Color.web("#a0aec0"));
        gc.fillText("–", headerSize + cellSize * 0.35, headerSize * 0.65);
        for (int j = 1; j < cols; j++) {
            String ch = String.valueOf(seq2.charAt(j - 1));
            gc.setFill(getBaseColor(ch));
            gc.fillText(ch, headerSize + j * cellSize + cellSize * 0.35, headerSize * 0.65);
        }

        // Draw sequence labels (left column)
        gc.setFill(Color.web("#a0aec0"));
        gc.fillText("–", headerSize * 0.35, headerSize + cellSize * 0.65);
        for (int i = 1; i < rows; i++) {
            String ch = String.valueOf(seq1.charAt(i - 1));
            gc.setFill(getBaseColor(ch));
            gc.fillText(ch, headerSize * 0.35, headerSize + i * cellSize + cellSize * 0.65);
        }

        // Draw matrix cells
        gc.setFont(scoreFont);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double x = headerSize + j * cellSize;
                double y = headerSize + i * cellSize;

                double val = matrix[i][j];

                // Background color (heatmap)
                if (val == Double.NEGATIVE_INFINITY) {
                    gc.setFill(Color.web("#1a1a2e"));
                } else {
                    double normalized = (val - minVal) / range;
                    Color cellColor = interpolateColor(normalized);
                    gc.setFill(cellColor);
                }

                gc.fillRect(x, y, cellSize, cellSize);

                // Traceback path highlight
                if (onPath[i][j]) {
                    gc.setStroke(Color.web("#f6e05e"));
                    gc.setLineWidth(2.5);
                    gc.strokeRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
                }

                // Grid lines
                gc.setStroke(Color.web("#2d3748", 0.5));
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, cellSize, cellSize);

                // Score text
                if (val != Double.NEGATIVE_INFINITY) {
                    String text = (val == (int) val) ? String.valueOf((int) val) : String.format("%.1f", val);
                    gc.setFill(onPath[i][j] ? Color.web("#f6e05e") : Color.web("#e2e8f0"));
                    gc.fillText(text, x + cellSize * 0.15, y + cellSize * 0.65);
                }
            }
        }

        // Legend
        HBox legend = new HBox(20);
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(8, 0, 0, 0));

        Region pathSample = new Region();
        pathSample.setPrefSize(16, 16);
        pathSample.setStyle("-fx-border-color: #f6e05e; -fx-border-width: 2; -fx-background-color: transparent;");
        Label pathLabel = new Label("Traceback-Pfad");
        pathLabel.getStyleClass().add("legend-label");

        Region lowSample = new Region();
        lowSample.setPrefSize(16, 16);
        lowSample.setStyle("-fx-background-color: #1a365d;");
        Label lowLabel = new Label("Niedriger Score");
        lowLabel.getStyleClass().add("legend-label");

        Region highSample = new Region();
        highSample.setPrefSize(16, 16);
        highSample.setStyle("-fx-background-color: #38a169;");
        Label highLabel = new Label("Hoher Score");
        highLabel.getStyleClass().add("legend-label");

        legend.getChildren().addAll(pathSample, pathLabel, lowSample, lowLabel, highSample, highLabel);

        StackPane canvasWrapper = new StackPane(canvas);
        canvasWrapper.setAlignment(Pos.TOP_LEFT);

        ScrollPane matrixScroll = new ScrollPane(canvasWrapper);
        matrixScroll.setMaxHeight(500);
        matrixScroll.setPrefHeight(Math.min(canvasHeight + 30, 500));
        matrixScroll.getStyleClass().add("matrix-scroll");

        VBox section = new VBox(12, heading, legend, matrixScroll);
        section.getStyleClass().add("card");
        section.setPadding(new Insets(24));
        return section;
    }

    private Color interpolateColor(double t) {
        // Blue (low) → Teal (mid) → Green (high)
        if (t < 0.5) {
            double local = t * 2;
            return Color.web("#1a365d").interpolate(Color.web("#2c7a7b"), local);
        } else {
            double local = (t - 0.5) * 2;
            return Color.web("#2c7a7b").interpolate(Color.web("#38a169"), local);
        }
    }

    private Color getBaseColor(String base) {
        return switch (base) {
            case "A" -> Color.web("#63b3ed"); // blue
            case "T" -> Color.web("#fc8181"); // red
            case "G" -> Color.web("#68d391"); // green
            case "C" -> Color.web("#f6e05e"); // yellow
            case "U" -> Color.web("#d6bcfa"); // purple
            default  -> Color.web("#a0aec0"); // gray
        };
    }
}
