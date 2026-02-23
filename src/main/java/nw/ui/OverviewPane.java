package nw.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Overview page explaining the Needleman-Wunsch algorithm.
 */
public class OverviewPane extends ScrollPane {

    public OverviewPane() {
        VBox content = new VBox(32);
        content.setPadding(new Insets(40, 48, 60, 48));
        content.getStyleClass().add("overview-content");

        content.getChildren().addAll(
            createHeroSection(),
            createSeparator(),
            createWhatIsSection(),
            createSeparator(),
            createStepsSection(),
            createSeparator(),
            createAffineGapSection(),
            createSeparator(),
            createComplexitySection(),
            createSeparator(),
            createFormulaSection()
        );

        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        getStyleClass().add("overview-scroll");
    }

    private VBox createHeroSection() {
        Label title = new Label("Needleman-Wunsch Algorithmus");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Globales Sequenz-Alignment mittels dynamischer Programmierung");
        subtitle.getStyleClass().add("hero-subtitle");

        Label description = new Label(
            "Der Needleman-Wunsch Algorithmus (1970) ist einer der fundamentalsten Algorithmen " +
            "in der Bioinformatik. Er findet das optimale globale Alignment zweier Sequenzen – " +
            "d.h. die bestmögliche Gegenüberstellung der gesamten Sequenzen unter Berücksichtigung " +
            "von Übereinstimmungen (Matches), Fehlpaarungen (Mismatches) und Lücken (Gaps)."
        );
        description.getStyleClass().add("hero-description");
        description.setWrapText(true);

        VBox hero = new VBox(12, title, subtitle, description);
        hero.getStyleClass().add("hero-section");
        hero.setPadding(new Insets(24));
        return hero;
    }

    private VBox createWhatIsSection() {
        Label heading = new Label("Was ist Sequenz-Alignment?");
        heading.getStyleClass().add("section-heading");

        Label text = new Label(
            "Sequenz-Alignment ist der Prozess, zwei oder mehr biologische Sequenzen " +
            "(DNA, RNA oder Proteine) so anzuordnen, dass Bereiche mit Ähnlichkeiten " +
            "identifiziert werden können. Diese Ähnlichkeiten können auf funktionelle, " +
            "strukturelle oder evolutionäre Beziehungen hinweisen.\n\n" +
            "Beim globalen Alignment (Needleman-Wunsch) werden die Sequenzen über ihre " +
            "gesamte Länge gegenübergestellt. Im Gegensatz dazu sucht das lokale Alignment " +
            "(Smith-Waterman) nur nach den am besten übereinstimmenden Teilbereichen."
        );
        text.getStyleClass().add("section-text");
        text.setWrapText(true);

        // Example alignment
        VBox example = createExampleAlignment();

        VBox section = new VBox(16, heading, text, example);
        return section;
    }

    private VBox createExampleAlignment() {
        Label exTitle = new Label("Beispiel-Alignment:");
        exTitle.getStyleClass().add("example-title");

        HBox seq1Row = createColoredSequence("Sequenz 1:  ", "A G T A C G C A", "- - - - - - - -");
        HBox matchRow = createMatchLine("            ", "| · |   | |   ·");
        HBox seq2Row = createColoredSequence("Sequenz 2:  ", "A T T - - G - C", "- - - - - - - -");

        VBox box = new VBox(4, exTitle, seq1Row, matchRow, seq2Row);
        box.getStyleClass().add("example-box");
        box.setPadding(new Insets(16, 20, 16, 20));
        return box;
    }

    private HBox createColoredSequence(String prefix, String bases, String types) {
        Label prefixLabel = new Label(prefix);
        prefixLabel.getStyleClass().add("mono-text");

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(prefixLabel);

        String[] chars = bases.split(" ");
        for (String ch : chars) {
            Label label = new Label(ch);
            label.getStyleClass().add("mono-text");
            if (ch.equals("-")) {
                label.getStyleClass().add("gap-char");
            } else {
                label.getStyleClass().add("base-" + ch.toLowerCase());
            }
            label.setMinWidth(20);
            label.setAlignment(Pos.CENTER);
            row.getChildren().add(label);
        }
        return row;
    }

    private HBox createMatchLine(String prefix, String matches) {
        Label prefixLabel = new Label(prefix);
        prefixLabel.getStyleClass().add("mono-text");

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getChildren().add(prefixLabel);

        for (char c : matches.toCharArray()) {
            Label label = new Label(String.valueOf(c));
            label.getStyleClass().add("mono-text");
            if (c == '|') {
                label.getStyleClass().add("match-indicator");
            }
            label.setMinWidth(10);
            label.setAlignment(Pos.CENTER);
            row.getChildren().add(label);
        }
        return row;
    }

    private VBox createStepsSection() {
        Label heading = new Label("Die drei Schritte des Algorithmus");
        heading.getStyleClass().add("section-heading");

        VBox step1 = createStep("1", "Initialisierung",
            "Die erste Zeile und Spalte der Scoring-Matrix werden mit den Gap-Penalties gefüllt. " +
            "Die Zelle (0,0) wird auf 0 gesetzt. Jede weitere Zelle in der ersten Zeile/Spalte " +
            "erhält den kumulierten Gap-Penalty.",
            "F(i,0) = gap_open + (i-1) × gap_extend\nF(0,j) = gap_open + (j-1) × gap_extend"
        );

        VBox step2 = createStep("2", "Matrix-Befüllung (Fill)",
            "Jede Zelle (i,j) wird berechnet als das Maximum von drei möglichen Vorgänger-Werten:\n" +
            "  • Diagonal (i-1, j-1): Match oder Mismatch\n" +
            "  • Von oben (i-1, j): Gap in Sequenz 2\n" +
            "  • Von links (i, j-1): Gap in Sequenz 1\n\n" +
            "Die Richtung (diagonal, oben, links) wird in einer Traceback-Matrix gespeichert.",
            "F(i,j) = max {\n  F(i-1, j-1) + S(xi, yj),\n  F(i-1, j) + gap_penalty,\n  F(i, j-1) + gap_penalty\n}"
        );

        VBox step3 = createStep("3", "Traceback",
            "Beginnend in der Zelle (m,n) unten rechts wird der optimale Pfad zurückverfolgt. " +
            "An jeder Position wird anhand der gespeicherten Richtung entschieden:\n" +
            "  • Diagonal → Match/Mismatch (beide Basen werden übernommen)\n" +
            "  • Nach oben → Gap in Sequenz 2 (Lücke '-' eingefügt)\n" +
            "  • Nach links → Gap in Sequenz 1 (Lücke '-' eingefügt)\n\n" +
            "Der Pfad endet in Zelle (0,0). Das resultierende Alignment wird umgekehrt ausgegeben.",
            null
        );

        VBox steps = new VBox(20, heading, step1, step2, step3);
        return steps;
    }

    private VBox createStep(String number, String title, String description, String formula) {
        Label numLabel = new Label(number);
        numLabel.getStyleClass().add("step-number");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("step-title");

        HBox header = new HBox(12, numLabel, titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("step-description");
        descLabel.setWrapText(true);

        VBox step = new VBox(8, header, descLabel);

        if (formula != null) {
            Label formulaLabel = new Label(formula);
            formulaLabel.getStyleClass().add("formula-box");
            step.getChildren().add(formulaLabel);
        }

        step.getStyleClass().add("step-card");
        step.setPadding(new Insets(20, 24, 20, 24));
        return step;
    }

    private VBox createAffineGapSection() {
        Label heading = new Label("Affines Gap-Modell");
        heading.getStyleClass().add("section-heading");

        Label text = new Label(
            "Ein einfaches lineares Gap-Modell bestraft jede Lücke gleich. In der Biologie " +
            "ist es jedoch realistischer, das Öffnen einer Lücke stärker zu bestrafen als " +
            "deren Verlängerung, da eine einzige längere Insertion/Deletion wahrscheinlicher " +
            "ist als viele einzelne.\n\n" +
            "Das affine Gap-Modell verwendet zwei Parameter:\n" +
            "  • Gap Open (d): Kosten für das Öffnen einer neuen Lücke\n" +
            "  • Gap Extension (e): Kosten für die Verlängerung einer bestehenden Lücke\n\n" +
            "Hierbei werden drei separate Matrizen verwendet:"
        );
        text.getStyleClass().add("section-text");
        text.setWrapText(true);

        VBox matrixM = createMatrixInfo("M(i,j)", "Match/Mismatch",
            "Bester Score, wenn Position (i,j) mit einem Match oder Mismatch endet.");
        VBox matrixIx = createMatrixInfo("Ix(i,j)", "Gap in Sequenz 1",
            "Bester Score, wenn Position (i,j) mit einer Lücke in Sequenz 1 endet.");
        VBox matrixIy = createMatrixInfo("Iy(i,j)", "Gap in Sequenz 2",
            "Bester Score, wenn Position (i,j) mit einer Lücke in Sequenz 2 endet.");

        HBox matrices = new HBox(16, matrixM, matrixIx, matrixIy);
        matrices.setAlignment(Pos.CENTER);

        VBox section = new VBox(16, heading, text, matrices);
        return section;
    }

    private VBox createMatrixInfo(String name, String title, String desc) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("matrix-name");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("matrix-title");

        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("matrix-desc");
        descLabel.setWrapText(true);

        VBox box = new VBox(6, nameLabel, titleLabel, descLabel);
        box.getStyleClass().add("matrix-info-card");
        box.setPadding(new Insets(16));
        box.setPrefWidth(250);
        return box;
    }

    private VBox createComplexitySection() {
        Label heading = new Label("Komplexität");
        heading.getStyleClass().add("section-heading");

        Label text = new Label(
            "Für zwei Sequenzen der Länge m und n:\n\n" +
            "  • Zeitkomplexität: O(m × n) – jede Zelle wird genau einmal berechnet\n" +
            "  • Speicherkomplexität: O(m × n) – drei vollständige Matrizen werden gespeichert\n\n" +
            "Für das affine Gap-Modell wird dreimal so viel Speicher benötigt (drei Matrizen " +
            "statt einer), die Zeitkomplexität bleibt jedoch identisch."
        );
        text.getStyleClass().add("section-text");
        text.setWrapText(true);

        VBox section = new VBox(16, heading, text);
        return section;
    }

    private VBox createFormulaSection() {
        Label heading = new Label("Rekurrenzrelationen");
        heading.getStyleClass().add("section-heading");

        Label intro = new Label(
            "Die vollständigen Rekurrenzrelationen für das affine Gap-Modell:"
        );
        intro.getStyleClass().add("section-text");
        intro.setWrapText(true);

        Label formulaM = new Label(
            "M(i,j) = max {\n" +
            "    M(i-1, j-1)  + S(xi, yj),\n" +
            "    Ix(i-1, j-1) + S(xi, yj),\n" +
            "    Iy(i-1, j-1) + S(xi, yj)\n" +
            "}"
        );
        formulaM.getStyleClass().add("formula-box");

        Label formulaIx = new Label(
            "Ix(i,j) = max {\n" +
            "    M(i-1, j)  + d,\n" +
            "    Ix(i-1, j) + e,\n" +
            "    Iy(i-1, j) + d\n" +
            "}"
        );
        formulaIx.getStyleClass().add("formula-box");

        Label formulaIy = new Label(
            "Iy(i,j) = max {\n" +
            "    M(i, j-1)  + d,\n" +
            "    Ix(i, j-1) + d,\n" +
            "    Iy(i, j-1) + e\n" +
            "}"
        );
        formulaIy.getStyleClass().add("formula-box");

        Label legend = new Label(
            "Wobei: S(xi, yj) = Substitutionsscore (Match/Mismatch)\n" +
            "       d = Gap-Open-Penalty  |  e = Gap-Extension-Penalty"
        );
        legend.getStyleClass().add("formula-legend");

        VBox section = new VBox(12, heading, intro, formulaM, formulaIx, formulaIy, legend);
        return section;
    }

    private Region createSeparator() {
        Region sep = new Region();
        sep.getStyleClass().add("section-separator");
        sep.setPrefHeight(1);
        sep.setMaxWidth(Double.MAX_VALUE);
        return sep;
    }
}
