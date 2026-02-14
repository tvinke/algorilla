package com.github.tvinke.algorilla.engine

/**
 * Interface for reporting analysis results in various formats (console, SARIF, JSON).
 */
public interface Reporter {
    /**
     * Formats and writes the analysis result to the given output.
     */
    public fun report(
        result: AnalysisResult,
        output: Appendable,
    )
}
