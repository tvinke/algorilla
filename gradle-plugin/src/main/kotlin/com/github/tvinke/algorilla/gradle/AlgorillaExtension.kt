package com.github.tvinke.algorilla.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/** Configuration for the algorilla Gradle plugin. */
public interface AlgorillaExtension {
    /** Minimum severity: info, warning, error. Default: warning. */
    public val minSeverity: Property<String>

    /** Minimum severity that triggers a build failure: info, warning, error. Default: info. */
    public val failOn: Property<String>

    /** Output format: console, json, sarif. Default: json. */
    public val format: Property<String>

    /** File to write the report to. Default: build/reports/algorilla.json */
    public val outputFile: Property<java.io.File>

    /** Glob patterns to exclude from analysis. */
    public val excludePatterns: ListProperty<String>

    /** Only run specific rule IDs. Empty = all rules. */
    public val rules: ListProperty<String>

    /** Include test source files. Default: false. */
    public val includeTests: Property<Boolean>

    /** Baseline file path. */
    public val baseline: Property<java.io.File>

    /** Maximum number of findings to display in console output. 0 = all. */
    public val limit: Property<Int>
}
