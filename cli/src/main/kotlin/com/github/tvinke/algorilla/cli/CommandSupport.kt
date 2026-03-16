package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.baseline.IgnoreList
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.lang.groovy.parser.GroovyLanguageParser
import com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParser
import com.github.tvinke.algorilla.lang.javascript.parser.JavaScriptLanguageParser
import com.github.tvinke.algorilla.lang.kotlin.parser.KotlinLanguageParser
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.reporting.Ansi
import com.github.tvinke.algorilla.reporting.ConsoleReporter
import com.github.tvinke.algorilla.reporting.JsonReporter
import com.github.tvinke.algorilla.reporting.SarifReporter
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.builtin.BuiltinRules
import picocli.CommandLine
import picocli.CommandLine.Model.CommandSpec
import java.io.File

internal fun builtinRules(): List<Rule> = BuiltinRules.all()

internal fun resolveRules(
    projectRoot: File,
    languageFilter: Set<Language>?,
    ruleFilter: List<String>,
): List<Rule> =
    (builtinRules() + CustomRuleLoader.loadRules(projectRoot))
        .let { list ->
            if (languageFilter != null) list.filter { r -> r.languages.any { it in languageFilter } } else list
        }.let { list ->
            if (ruleFilter.isNotEmpty()) {
                val ids = ruleFilter.toSet()
                list.filter { it.id in ids || it.name in ids }
            } else {
                list
            }
        }

internal fun resolveLanguageFilter(
    languages: List<String>,
    spec: CommandSpec,
): Set<Language>? {
    if (languages.isEmpty()) return null
    return languages
        .map { name ->
            Language.fromName(name)
                ?: throw CommandLine.ParameterException(
                    spec.commandLine(),
                    "Unknown language '$name'. Available: ${Language.entries.joinToString { it.displayName.lowercase() }}",
                )
        }.toSet()
}

/**
 * Ensures all built-in language parsers are registered in [ParserRegistry].
 * Each parser's companion object registers itself, but that only happens when
 * the class is first loaded — so we touch each companion here to trigger it.
 */
internal fun configureLogging(verbose: Boolean) {
    if (verbose) {
        val loggerContext = org.slf4j.LoggerFactory.getILoggerFactory() as ch.qos.logback.classic.LoggerContext
        loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
    }
}

internal fun registerAllParsers() {
    JavaLanguageParser.Companion
    GroovyLanguageParser.Companion
    KotlinLanguageParser.Companion
    JavaScriptLanguageParser.Companion
}

internal fun applyBaseline(
    result: AnalysisResult,
    baselineFile: File?,
    saveBaselineFile: File?,
): AnalysisResult {
    if (saveBaselineFile != null) {
        Baseline.save(result.findings, saveBaselineFile)
    }
    val baseline = baselineFile?.let { Baseline.load(it) }
    return if (baseline != null) {
        result.copy(findings = baseline.filterNew(result.findings))
    } else {
        result
    }
}

internal fun applyIgnoreList(
    result: AnalysisResult,
    projectRoot: File,
): AnalysisResult {
    val ignoreFile = IgnoreList.defaultFile(projectRoot)
    val ignoreList = IgnoreList.load(ignoreFile)
    if (ignoreList.size == 0) return result
    return result.copy(findings = ignoreList.filter(result.findings))
}

internal fun resolveSeverity(severity: String): Severity =
    when (severity.lowercase()) {
        "info" -> Severity.INFO
        "error" -> Severity.ERROR
        else -> Severity.WARNING
    }

internal fun resolveConfidence(confidence: String): Confidence =
    when (confidence.lowercase()) {
        "low" -> Confidence.LOW
        "high" -> Confidence.HIGH
        else -> Confidence.MEDIUM
    }

internal fun resolveFailOn(failOn: String): Severity =
    when (failOn.lowercase()) {
        "error" -> Severity.ERROR
        "warning" -> Severity.WARNING
        else -> Severity.INFO
    }

internal fun writeReport(
    result: AnalysisResult,
    format: String,
    outputFile: File?,
    useColor: Boolean,
    projectRoot: File,
    scanRoots: List<File> = emptyList(),
) {
    val baseDir = projectRoot.absoluteFile.normalize().path
    val sourceRootPaths = scanRoots.map { it.absoluteFile.normalize().path }
    val reporter =
        when (format.lowercase()) {
            "sarif" -> SarifReporter()
            "json" -> JsonReporter()
            else -> ConsoleReporter(color = useColor, baseDir = baseDir, sourceRoots = sourceRootPaths)
        }
    val output = outputFile?.bufferedWriter() ?: System.out.bufferedWriter()
    output.use { reporter.report(result, it) }
}

internal fun exitCodeFor(
    result: AnalysisResult,
    failOn: Severity = Severity.INFO,
): Int =
    when {
        result.errors.isNotEmpty() && result.findings.isEmpty() -> AlgorillaCommand.EXIT_ERROR
        result.findings.any { it.severity.ordinal >= failOn.ordinal } -> AlgorillaCommand.EXIT_FINDINGS
        else -> AlgorillaCommand.EXIT_OK
    }

private const val SEVERITY_COL_WIDTH = 9
private const val STABILITY_COL_WIDTH = 8
private const val RULE_TABLE_WIDTH = 95

internal fun printRuleList(
    rules: List<Rule>,
    color: Boolean,
) {
    val out = System.out.bufferedWriter()
    out.appendLine()
    out.appendLine(
        Ansi.boldWhite(
            "  %-30s  %-9s  %-20s  %-8s  %s".format("RULE ID", "SEVERITY", "CATEGORY", "STABILITY", "LANGUAGES"),
            color,
        ),
    )
    out.appendLine("  ${"─".repeat(RULE_TABLE_WIDTH)}")
    for (rule in rules.sortedWith(compareBy({ it.category.ordinal }, { it.id }))) {
        out.appendLine(formatRuleRow(rule, color))
    }
    out.appendLine()
    out.appendLine(Ansi.dim("  ${rules.size} rules available", color))
    out.appendLine()
    out.flush()
}

private fun formatRuleRow(
    rule: Rule,
    color: Boolean,
): String {
    val severityLabel =
        rule.severity.name
            .lowercase()
            .padEnd(SEVERITY_COL_WIDTH)
    val severityStr =
        when (rule.severity) {
            Severity.ERROR -> Ansi.red(severityLabel, color)
            Severity.WARNING -> Ansi.yellow(severityLabel, color)
            Severity.INFO -> Ansi.cyan(severityLabel, color)
        }
    val langs = rule.languages.joinToString(", ") { it.displayName.lowercase() }
    val stabilityStr = Ansi.dim("stable".padEnd(STABILITY_COL_WIDTH), color)
    return "  %-30s  %s  %-20s  %s  %s".format(rule.id, severityStr, rule.category.displayName, stabilityStr, langs)
}

internal fun resolveColor(
    color: String,
    outputFile: File?,
): Boolean =
    when (color.lowercase()) {
        "always" -> true
        "never" -> false
        else -> outputFile == null && System.console() != null
    }

private val LIMITED_SUPPORT_FRAMEWORKS =
    mapOf(
        "io.quarkus" to "Quarkus",
        "io.smallrye.mutiny" to "SmallRye Mutiny",
        "reactor.core" to "Project Reactor",
        "io.reactivex" to "RxJava",
        "io.micronaut" to "Micronaut",
        "io.grpc" to "gRPC",
        "io.r2dbc" to "R2DBC",
    )

private const val FRAMEWORK_SAMPLE_FILES = 200
private const val FRAMEWORK_IMPORT_LINES = 30

@Suppress("NestedBlockDepth")
internal fun printFrameworkCoverageNotice(
    scanRoots: List<File>,
    color: Boolean,
) {
    val detected = mutableSetOf<String>()
    val sampleFiles =
        scanRoots
            .flatMap { root -> root.walkTopDown().filter { it.extension in setOf("java", "kt", "groovy") }.toList() }
            .take(FRAMEWORK_SAMPLE_FILES)
    for (file in sampleFiles) {
        if (detected.size == LIMITED_SUPPORT_FRAMEWORKS.size) break
        file.useLines { lines ->
            lines.take(FRAMEWORK_IMPORT_LINES).forEach { line ->
                if (line.trimStart().startsWith("import ")) {
                    for ((pkg, _) in LIMITED_SUPPORT_FRAMEWORKS) {
                        if (pkg !in detected && line.contains(pkg)) detected.add(pkg)
                    }
                }
            }
        }
    }
    if (detected.isNotEmpty()) {
        val names = detected.mapNotNull { LIMITED_SUPPORT_FRAMEWORKS[it] }.sorted().joinToString(", ")
        System.err.println(Ansi.dim("Note: limited coverage for $names patterns. Some findings may not apply.", color))
    }
}

internal fun printScanTarget(
    paths: List<File>,
    color: Boolean,
) {
    val targets = paths.joinToString(", ") { it.absoluteFile.normalize().path }
    System.err.println(Ansi.dim("\uD83E\uDD8D Scanning $targets...", color))
}
