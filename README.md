# NeedleAlign – Needleman-Wunsch Sequence Aligner

A high-performance implementation of the **Needleman-Wunsch** global sequence alignment algorithm for nucleotide sequences, built with **Java 19** and **JavaFX**.

## Features

- **High-Performance Algorithm** – Optimized implementation using primitive arrays and an affine gap penalty model (gap open + gap extension)
- **Configurable Parameters** – Adjust match, mismatch, gap open, and gap extension scores via intuitive UI controls
- **Interactive Visualization** – Color-coded alignment results, score matrix heatmap with traceback path
- **Algorithm Overview** – Built-in educational page explaining the Needleman-Wunsch algorithm step-by-step
- **Nucleotide Validation** – Input validation for DNA/RNA sequences (A, T, G, C, U + IUPAC ambiguity codes)

## Requirements

- **Java 19** or higher
- No additional installations required (Gradle wrapper included)

## Build & Run

```bash
# Make gradlew executable (first time only)
chmod +x gradlew

# Run the application
./gradlew run

# Build the project
./gradlew build
```

## Algorithm

The Needleman-Wunsch algorithm performs **global sequence alignment** using dynamic programming. It finds the optimal alignment between two sequences by:

1. **Initialization** – Fill the first row and column with gap penalties
2. **Matrix Fill** – Compute scores for each cell using match/mismatch and gap penalties
3. **Traceback** – Follow the optimal path from bottom-right to top-left to construct the alignment

### Affine Gap Model

This implementation uses an affine gap penalty model with separate costs for:
- **Gap Open** – Penalty for starting a new gap
- **Gap Extension** – Penalty for extending an existing gap

This produces more biologically realistic alignments than a linear gap penalty.

## Project Structure

```
src/main/java/nw/
├── Main.java              # Application launcher
├── core/
│   ├── ScoringConfig.java     # Scoring parameters
│   ├── NeedlemanWunsch.java   # Algorithm implementation
│   ├── AlignmentResult.java   # Result data container
│   └── SequenceValidator.java # Input validation
└── ui/
    ├── App.java               # JavaFX application
    ├── OverviewPane.java      # Algorithm explanation
    ├── AlignmentPane.java     # Input & configuration
    └── ResultPane.java        # Results visualization
```

## License

MIT
