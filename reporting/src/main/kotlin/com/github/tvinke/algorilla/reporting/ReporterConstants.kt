package com.github.tvinke.algorilla.reporting

/**
 * Shared constants for all reporter implementations.
 */
public object ReporterConstants {
    public const val DOCS_BASE_URL: String = "https://tvinke.github.io/algorilla/rules/"

    public fun ruleUrl(ruleId: String): String = "$DOCS_BASE_URL$ruleId"
}
