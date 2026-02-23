package nw.core;

/**
 * High-performance Needleman-Wunsch global sequence alignment.
 * <p>
 * Uses an affine gap penalty model with three DP matrices:
 * <ul>
 *   <li><b>M</b> – best score ending with a match/mismatch</li>
 *   <li><b>Ix</b> – best score ending with a gap in sequence X (top sequence)</li>
 *   <li><b>Iy</b> – best score ending with a gap in sequence Y (left sequence)</li>
 * </ul>
 * Performance optimizations:
 * <ul>
 *   <li>Primitive double[][] arrays (no boxing)</li>
 *   <li>char[] for sequence access (no String.charAt overhead)</li>
 *   <li>Byte-encoded traceback directions (compact memory)</li>
 * </ul>
 */
public final class NeedlemanWunsch {

    // Traceback direction constants
    private static final int DIAG = 0;   // match/mismatch
    private static final int UP   = 1;   // gap in seq2
    private static final int LEFT = 2;   // gap in seq1

    private NeedlemanWunsch() {}

    /**
     * Aligns two nucleotide sequences using the Needleman-Wunsch algorithm
     * with affine gap penalties.
     *
     * @param seq1   first sequence (will appear on top in alignment)
     * @param seq2   second sequence (will appear on bottom in alignment)
     * @param config scoring configuration
     * @return alignment result with scores, aligned sequences, and matrices
     */
    public static AlignmentResult align(String seq1, String seq2, ScoringConfig config) {
        final char[] s1 = seq1.toUpperCase().toCharArray();
        final char[] s2 = seq2.toUpperCase().toCharArray();
        final int m = s1.length;
        final int n = s2.length;

        final int gapOpen   = config.getGapOpen();
        final int gapExtend = config.getGapExtend();

        // DP matrices
        final double[][] M  = new double[m + 1][n + 1]; // match/mismatch
        final double[][] Ix = new double[m + 1][n + 1]; // gap in seq1 (insertion)
        final double[][] Iy = new double[m + 1][n + 1]; // gap in seq2 (deletion)
        final int[][] trace = new int[m + 1][n + 1];    // traceback

        // Score matrix for visualization (best score at each cell)
        final double[][] scoreVis = new double[m + 1][n + 1];

        final double NEG_INF = Double.NEGATIVE_INFINITY;

        // --- Initialization ---
        M[0][0] = 0;
        Ix[0][0] = NEG_INF;
        Iy[0][0] = NEG_INF;
        scoreVis[0][0] = 0;

        // First column: gaps in seq2
        for (int i = 1; i <= m; i++) {
            M[i][0]  = NEG_INF;
            Ix[i][0] = gapOpen + (i - 1) * gapExtend;
            Iy[i][0] = NEG_INF;
            trace[i][0] = UP;
            scoreVis[i][0] = Ix[i][0];
        }

        // First row: gaps in seq1
        for (int j = 1; j <= n; j++) {
            M[0][j]  = NEG_INF;
            Ix[0][j] = NEG_INF;
            Iy[0][j] = gapOpen + (j - 1) * gapExtend;
            trace[0][j] = LEFT;
            scoreVis[0][j] = Iy[0][j];
        }

        // --- Fill ---
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                // Score for match/mismatch
                int substScore = config.score(s1[i - 1], s2[j - 1]);

                // M[i][j]: best score ending with match/mismatch at (i,j)
                double diagFromM  = M[i - 1][j - 1] + substScore;
                double diagFromIx = Ix[i - 1][j - 1] + substScore;
                double diagFromIy = Iy[i - 1][j - 1] + substScore;
                M[i][j] = Math.max(diagFromM, Math.max(diagFromIx, diagFromIy));

                // Ix[i][j]: best score ending with gap in seq1 (moving down)
                double openIx  = M[i - 1][j] + gapOpen;
                double extIx   = Ix[i - 1][j] + gapExtend;
                double fromIyX = Iy[i - 1][j] + gapOpen;
                Ix[i][j] = Math.max(openIx, Math.max(extIx, fromIyX));

                // Iy[i][j]: best score ending with gap in seq2 (moving right)
                double openIy  = M[i][j - 1] + gapOpen;
                double fromIxY = Ix[i][j - 1] + gapOpen;
                double extIy   = Iy[i][j - 1] + gapExtend;
                Iy[i][j] = Math.max(openIy, Math.max(fromIxY, extIy));

                // Best overall score and traceback direction
                double best = M[i][j];
                int dir = DIAG;

                if (Ix[i][j] > best) {
                    best = Ix[i][j];
                    dir = UP;
                }
                if (Iy[i][j] > best) {
                    best = Iy[i][j];
                    dir = LEFT;
                }

                trace[i][j] = dir;
                scoreVis[i][j] = best;
            }
        }

        // --- Traceback ---
        StringBuilder aln1 = new StringBuilder();
        StringBuilder aln2 = new StringBuilder();

        int i = m, j = n;
        while (i > 0 || j > 0) {
            if (i > 0 && j > 0 && trace[i][j] == DIAG) {
                aln1.append(s1[i - 1]);
                aln2.append(s2[j - 1]);
                i--; j--;
            } else if (i > 0 && (j == 0 || trace[i][j] == UP)) {
                aln1.append(s1[i - 1]);
                aln2.append('-');
                i--;
            } else {
                aln1.append('-');
                aln2.append(s2[j - 1]);
                j--;
            }
        }

        // Reverse (traceback is bottom-right to top-left)
        String aligned1 = aln1.reverse().toString();
        String aligned2 = aln2.reverse().toString();

        // --- Compute statistics ---
        int matchCount = 0, mismatchCount = 0, gapCount = 0;
        StringBuilder matchLine = new StringBuilder();

        for (int k = 0; k < aligned1.length(); k++) {
            char c1 = aligned1.charAt(k);
            char c2 = aligned2.charAt(k);
            if (c1 == '-' || c2 == '-') {
                gapCount++;
                matchLine.append(' ');
            } else if (c1 == c2) {
                matchCount++;
                matchLine.append('|');
            } else {
                mismatchCount++;
                matchLine.append('·');
            }
        }

        int alnLength = aligned1.length();
        double identity = (alnLength > 0) ? (100.0 * matchCount / alnLength) : 0;

        // Final score = best of M, Ix, Iy at (m, n)
        double finalScore = Math.max(M[m][n], Math.max(Ix[m][n], Iy[m][n]));

        return new AlignmentResult(
            aligned1, aligned2, matchLine.toString(),
            finalScore, identity, gapCount, alnLength,
            matchCount, mismatchCount,
            scoreVis, trace,
            seq1, seq2
        );
    }
}
