package nw.core;

/**
 * Holds the results of a Needleman-Wunsch alignment.
 */
public final class AlignmentResult {

    private final String alignedSeq1;
    private final String alignedSeq2;
    private final String matchLine;
    private final double score;
    private final double identityPercent;
    private final int gapCount;
    private final int alignmentLength;
    private final int matchCount;
    private final int mismatchCount;
    private final double[][] scoreMatrix;
    private final int[][] tracebackMatrix;
    private final String originalSeq1;
    private final String originalSeq2;

    public AlignmentResult(String alignedSeq1, String alignedSeq2, String matchLine,
                           double score, double identityPercent, int gapCount,
                           int alignmentLength, int matchCount, int mismatchCount,
                           double[][] scoreMatrix, int[][] tracebackMatrix,
                           String originalSeq1, String originalSeq2) {
        this.alignedSeq1 = alignedSeq1;
        this.alignedSeq2 = alignedSeq2;
        this.matchLine = matchLine;
        this.score = score;
        this.identityPercent = identityPercent;
        this.gapCount = gapCount;
        this.alignmentLength = alignmentLength;
        this.matchCount = matchCount;
        this.mismatchCount = mismatchCount;
        this.scoreMatrix = scoreMatrix;
        this.tracebackMatrix = tracebackMatrix;
        this.originalSeq1 = originalSeq1;
        this.originalSeq2 = originalSeq2;
    }

    // --- Getters ---

    public String getAlignedSeq1()       { return alignedSeq1; }
    public String getAlignedSeq2()       { return alignedSeq2; }
    public String getMatchLine()         { return matchLine; }
    public double getScore()             { return score; }
    public double getIdentityPercent()   { return identityPercent; }
    public int    getGapCount()          { return gapCount; }
    public int    getAlignmentLength()   { return alignmentLength; }
    public int    getMatchCount()        { return matchCount; }
    public int    getMismatchCount()     { return mismatchCount; }
    public double[][] getScoreMatrix()   { return scoreMatrix; }
    public int[][] getTracebackMatrix()  { return tracebackMatrix; }
    public String getOriginalSeq1()      { return originalSeq1; }
    public String getOriginalSeq2()      { return originalSeq2; }

    @Override
    public String toString() {
        return String.format(
            "Score: %.1f | Identity: %.1f%% | Gaps: %d | Length: %d%n%s%n%s%n%s",
            score, identityPercent, gapCount, alignmentLength,
            alignedSeq1, matchLine, alignedSeq2
        );
    }
}
