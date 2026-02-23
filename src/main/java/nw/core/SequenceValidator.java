package nw.core;

import java.util.Set;

/**
 * Validates nucleotide sequences (DNA/RNA).
 * Accepts standard bases and IUPAC ambiguity codes.
 */
public final class SequenceValidator {

    /** Standard DNA/RNA bases */
    private static final Set<Character> STANDARD_BASES = Set.of(
        'A', 'T', 'G', 'C', 'U'
    );

    /** IUPAC ambiguity codes */
    private static final Set<Character> IUPAC_CODES = Set.of(
        'A', 'T', 'G', 'C', 'U',
        'R', // A or G
        'Y', // C or T
        'S', // G or C
        'W', // A or T
        'K', // G or T
        'M', // A or C
        'B', // C or G or T
        'D', // A or G or T
        'H', // A or C or T
        'V', // A or C or G
        'N'  // any base
    );

    private SequenceValidator() {}

    /**
     * Validates and normalizes a nucleotide sequence.
     *
     * @param input raw sequence input
     * @return validation result
     */
    public static ValidationResult validate(String input) {
        if (input == null || input.isBlank()) {
            return ValidationResult.error("Die Sequenz darf nicht leer sein.", -1);
        }

        // Strip whitespace, numbers, and newlines
        String cleaned = input.replaceAll("[\\s\\d\\n\\r>]", "").toUpperCase();

        // Remove FASTA header if present
        if (cleaned.startsWith(">")) {
            int newlineIdx = input.indexOf('\n');
            if (newlineIdx >= 0) {
                cleaned = input.substring(newlineIdx + 1)
                               .replaceAll("[\\s\\d\\n\\r]", "")
                               .toUpperCase();
            }
        }

        if (cleaned.isEmpty()) {
            return ValidationResult.error("Die Sequenz enthält keine gültigen Zeichen.", -1);
        }

        // Validate each character
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (!IUPAC_CODES.contains(c)) {
                return ValidationResult.error(
                    String.format("Ungültiges Zeichen '%c' an Position %d. " +
                                  "Erlaubt sind: A, T, G, C, U und IUPAC-Codes (R, Y, S, W, K, M, B, D, H, V, N).",
                                  c, i + 1),
                    i
                );
            }
        }

        return ValidationResult.success(cleaned);
    }

    // --- Result record ---

    public static final class ValidationResult {
        private final boolean valid;
        private final String cleanedSequence;
        private final String errorMessage;
        private final int errorPosition;

        private ValidationResult(boolean valid, String cleanedSequence, String errorMessage, int errorPosition) {
            this.valid = valid;
            this.cleanedSequence = cleanedSequence;
            this.errorMessage = errorMessage;
            this.errorPosition = errorPosition;
        }

        static ValidationResult success(String cleaned) {
            return new ValidationResult(true, cleaned, null, -1);
        }

        static ValidationResult error(String message, int position) {
            return new ValidationResult(false, null, message, position);
        }

        public boolean isValid()           { return valid; }
        public String getCleanedSequence() { return cleanedSequence; }
        public String getErrorMessage()    { return errorMessage; }
        public int    getErrorPosition()   { return errorPosition; }
    }
}
