package nw.core;

/**
 * Immutable configuration for alignment scoring parameters.
 * Supports affine gap penalties (gap open + gap extension).
 */
public final class ScoringConfig {

    private final int match;
    private final int mismatch;
    private final int gapOpen;
    private final int gapExtend;

    private ScoringConfig(int match, int mismatch, int gapOpen, int gapExtend) {
        this.match = match;
        this.mismatch = mismatch;
        this.gapOpen = gapOpen;
        this.gapExtend = gapExtend;
    }

    // --- Presets ---

    /** Standard DNA scoring: match=+2, mismatch=-1, gapOpen=-2, gapExtend=-1 */
    public static ScoringConfig standardDNA() {
        return new ScoringConfig(2, -1, -2, -1);
    }

    /** Strict scoring for closely related sequences: match=+1, mismatch=-3, gapOpen=-5, gapExtend=-2 */
    public static ScoringConfig strict() {
        return new ScoringConfig(1, -3, -5, -2);
    }

    /** Lenient scoring for divergent sequences: match=+3, mismatch=-1, gapOpen=-1, gapExtend=0 */
    public static ScoringConfig lenient() {
        return new ScoringConfig(3, -1, -1, 0);
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    // --- Getters ---

    public int getMatch()     { return match; }
    public int getMismatch()  { return mismatch; }
    public int getGapOpen()   { return gapOpen; }
    public int getGapExtend() { return gapExtend; }

    /**
     * Returns the gap penalty for a gap of the given length.
     * Affine: gapOpen + (length - 1) * gapExtend for length >= 1.
     */
    public int gapPenalty(int length) {
        if (length <= 0) return 0;
        return gapOpen + (length - 1) * gapExtend;
    }

    /**
     * Returns the score for aligning two nucleotide characters.
     */
    public int score(char a, char b) {
        return (Character.toUpperCase(a) == Character.toUpperCase(b)) ? match : mismatch;
    }

    @Override
    public String toString() {
        return String.format("ScoringConfig[match=%d, mismatch=%d, gapOpen=%d, gapExtend=%d]",
                match, mismatch, gapOpen, gapExtend);
    }

    // --- Builder Class ---

    public static final class Builder {
        private int match = 2;
        private int mismatch = -1;
        private int gapOpen = -2;
        private int gapExtend = -1;

        private Builder() {}

        public Builder match(int match)         { this.match = match; return this; }
        public Builder mismatch(int mismatch)   { this.mismatch = mismatch; return this; }
        public Builder gapOpen(int gapOpen)     { this.gapOpen = gapOpen; return this; }
        public Builder gapExtend(int gapExtend) { this.gapExtend = gapExtend; return this; }

        public ScoringConfig build() {
            return new ScoringConfig(match, mismatch, gapOpen, gapExtend);
        }
    }
}
