package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.baseline.IgnoreList
import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.lang.groovy.parser.GroovyParser
import com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParser
import com.github.tvinke.algorilla.lang.javascript.parser.JavaScriptParser
import com.github.tvinke.algorilla.lang.kotlin.parser.KotlinParser
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.reporting.Ansi
import com.github.tvinke.algorilla.reporting.ConsoleReporter
import com.github.tvinke.algorilla.reporting.JsonReporter
import com.github.tvinke.algorilla.reporting.SarifReporter
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.builtin.ChainedGettersRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveCallbackRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSerializationInLoopRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSortComparatorRule
import com.github.tvinke.algorilla.rules.builtin.FilterAfterSortRule
import com.github.tvinke.algorilla.rules.builtin.FullScanForSingleLookupRule
import com.github.tvinke.algorilla.rules.builtin.HeavyweightObjectPerInvocationRule
import com.github.tvinke.algorilla.rules.builtin.HiddenNestedLoopRule
import com.github.tvinke.algorilla.rules.builtin.ImplicitRegexInLoopRule
import com.github.tvinke.algorilla.rules.builtin.InLoopCollectionBuildingRule
import com.github.tvinke.algorilla.rules.builtin.NPlusOneRepositoryCallRule
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.rules.builtin.QuadraticRemovalRule
import com.github.tvinke.algorilla.rules.builtin.RedundantExpensiveCallRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedLinearScanRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedReflectionInLoopRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedRegexInLoopRule
import com.github.tvinke.algorilla.rules.builtin.SequentialAsyncJoinInLoopRule
import com.github.tvinke.algorilla.rules.builtin.SortForLastRule
import com.github.tvinke.algorilla.rules.builtin.StringConcatInLoopRule
import com.github.tvinke.algorilla.rules.builtin.UncachedGetterRule
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import picocli.CommandLine.Spec
import java.io.File
import java.util.concurrent.Callable
import kotlin.system.exitProcess

@Command(
    name = "algorilla",
    mixinStandardHelpOptions = true,
    versionProvider = AlgorillaCommand::class,
    description = ["Detects algorithmic complexity anti-patterns in source code."],
)
internal class AlgorillaCommand :
    Callable<Int>,
    CommandLine.IVersionProvider {
    override fun getVersion(): Array<String> {
        val props = java.util.Properties()
        javaClass.classLoader.getResourceAsStream("algorilla-version.properties")?.use { props.load(it) }
        return arrayOf("\uD83E\uDD8D algorilla ${props.getProperty("version", "unknown")}")
    }

    @Spec
    private lateinit var spec: CommandSpec

    @Parameters(
        index = "0..*",
        description = ["Files or directories to analyze (default: current directory)"],
        defaultValue = ".",
    )
    private var paths: List<File> = listOf(File("."))

    @Option(
        names = ["-f", "--format"],
        description = ["Output format: console, sarif, json (default: console)"],
        defaultValue = "console",
    )
    private var format: String = "console"

    @Option(
        names = ["-o", "--output"],
        description = ["Write report to file (default: stdout)"],
    )
    private var outputFile: File? = null

    @Option(
        names = ["-v", "--verbose"],
        description = ["Show detailed analysis progress"],
    )
    private var verbose: Boolean = false

    @Option(
        names = ["--severity"],
        description = ["Minimum severity: info, warning, error (default: warning)"],
        defaultValue = "warning",
    )
    private var severity: String = "warning"

    @Option(
        names = ["--exclude"],
        description = ["Glob patterns to exclude"],
    )
    private var excludePatterns: List<String> = emptyList()

    @Option(
        names = ["-c", "--config"],
        description = ["Path to .algorilla.yml config file"],
    )
    private var configFile: File? = null

    @Option(
        names = ["--no-cache"],
        description = ["Disable incremental analysis caching"],
    )
    private var noCache: Boolean = false

    @Option(
        names = ["--baseline"],
        description = ["Baseline file to compare against (only report new findings)"],
    )
    private var baselineFile: File? = null

    @Option(
        names = ["--save-baseline"],
        description = ["Save current findings as a baseline to the specified file"],
    )
    private var saveBaselineFile: File? = null

    @Option(
        names = ["--include-tests"],
        description = ["Include test source files in the analysis"],
    )
    private var includeTests: Boolean = false

    @Option(
        names = ["-l", "--language"],
        description = [
            "Only analyze specified language(s). Comma-separated or repeated: --language java,groovy or -l java -l groovy. " +
                "Available: java, groovy, kotlin, javascript, typescript, vue",
        ],
        split = ",",
    )
    private var languages: List<String> = emptyList()

    @Option(
        names = ["--color"],
        description = ["Color output: auto, always, never (default: auto)"],
        defaultValue = "auto",
    )
    private var color: String = "auto"

    @Option(
        names = ["--list-rules"],
        description = ["List all available rules and exit"],
    )
    private var listRules: Boolean = false

    @Option(
        names = ["--rule"],
        description = ["Only run specific rule(s). Comma-separated or repeated: --rule nested-lookup,sort-for-last"],
        split = ",",
    )
    private var ruleFilter: List<String> = emptyList()

    @Option(
        names = ["--fail-on"],
        description = ["Minimum severity that triggers a non-zero exit code: info, warning, error (default: info)"],
        defaultValue = "info",
    )
    private var failOn: String = "info"

    @Option(
        names = ["--accept"],
        description = ["Accept findings by fingerprint hash (shown in output). Adds them to .algorilla/ignore-list.json"],
        split = ",",
    )
    private var acceptHashes: List<String> = emptyList()

    private var projectRoot: File = File(".")
    private var scanRoots: List<File> = emptyList()

    override fun call(): Int {
        configureLogging()
        val useColor = resolveColor(color, outputFile)

        if (listRules) {
            printRuleList(builtinRules() + CustomRuleLoader.loadRules(File(".")), useColor)
            return EXIT_OK
        }

        if (outputFile == null) printScanTarget(paths, useColor)
        val result = runAnalysis()
        val baselined = applyBaseline(result, baselineFile, saveBaselineFile)
        val accepted = applyIgnoreList(baselined, projectRoot)

        if (acceptHashes.isNotEmpty()) {
            val count =
                IgnoreList.accept(
                    IgnoreList.defaultFile(projectRoot),
                    result.findings,
                    acceptHashes.toSet(),
                )
            System.err.println("Accepted $count ${if (count == 1) "finding" else "findings"}.")
        }

        writeReport(accepted, format, outputFile, useColor, projectRoot, scanRoots)
        return exitCodeFor(accepted, resolveFailOn(failOn))
    }

    private fun configureLogging() {
        if (verbose) {
            val loggerContext = org.slf4j.LoggerFactory.getILoggerFactory() as ch.qos.logback.classic.LoggerContext
            loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
        }
    }

    private fun runAnalysis(): AnalysisResult {
        val config = buildConfig()
        val detector = ProjectStructureDetector()
        projectRoot = detector.resolveProjectRoot(paths.first())
        scanRoots = paths.flatMap { detector.resolveSourceRoots(projectRoot, it) }

        val parsers = listOf(JavaLanguageParser(), GroovyParser(), KotlinParser(), JavaScriptParser())
        val allRules = resolveRules()
        val cache = if (noCache) null else AnalysisCache(projectRoot)
        val languageFilter = resolveLanguageFilter()
        val collector = SourceFileCollector(detector)
        val sourceFiles = collector.collect(scanRoots, config.excludePatterns, includeTests, languageFilter)
        return AnalysisEngine(parsers = parsers, rules = allRules, config = config, cache = cache, verbose = verbose)
            .analyze(sourceFiles)
    }

    private fun resolveRules(): List<Rule> {
        val languageFilter = resolveLanguageFilter()
        return (builtinRules() + CustomRuleLoader.loadRules(projectRoot))
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
    }

    private fun buildConfig(): AnalysisConfig {
        val fileConfig = loadConfig(configFile)
        val minSeverity =
            when (severity.lowercase()) {
                "info" -> Severity.INFO
                "error" -> Severity.ERROR
                else -> Severity.WARNING
            }
        val mergedExclude = if (excludePatterns.isNotEmpty()) excludePatterns else fileConfig.excludePatterns
        val mergedSeverity = if (cliSeverityProvided()) minSeverity else fileConfig.minSeverity
        return fileConfig.copy(excludePatterns = mergedExclude, minSeverity = mergedSeverity)
    }

    private fun cliSeverityProvided(): Boolean = spec.commandLine().parseResult.hasMatchedOption("severity")

    private fun resolveLanguageFilter(): Set<Language>? {
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

    internal companion object {
        const val EXIT_OK = 0
        const val EXIT_FINDINGS = 1
        const val EXIT_ERROR = 2
    }
}

private fun builtinRules(): List<Rule> =
    listOf(
        NestedLookupRule(),
        SortForLastRule(),
        ExpensiveSortComparatorRule(),
        ExpensiveCallbackRule(),
        RepeatedLinearScanRule(),
        FullScanForSingleLookupRule(),
        HeavyweightObjectPerInvocationRule(),
        RepeatedRegexInLoopRule(),
        ExpensiveSerializationInLoopRule(),
        SequentialAsyncJoinInLoopRule(),
        InLoopCollectionBuildingRule(),
        NPlusOneRepositoryCallRule(),
        RedundantExpensiveCallRule(),
        UncachedGetterRule(),
        ChainedGettersRule(),
        FilterAfterSortRule(),
        HiddenNestedLoopRule(),
        ImplicitRegexInLoopRule(),
        StringConcatInLoopRule(),
        QuadraticRemovalRule(),
        RepeatedReflectionInLoopRule(),
    )

private fun applyBaseline(
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

private fun applyIgnoreList(
    result: AnalysisResult,
    projectRoot: File,
): AnalysisResult {
    val ignoreFile = IgnoreList.defaultFile(projectRoot)
    val ignoreList = IgnoreList.load(ignoreFile)
    if (ignoreList.size == 0) return result
    return result.copy(findings = ignoreList.filter(result.findings))
}

private fun resolveFailOn(failOn: String): Severity =
    when (failOn.lowercase()) {
        "error" -> Severity.ERROR
        "warning" -> Severity.WARNING
        else -> Severity.INFO
    }

private fun writeReport(
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

private fun exitCodeFor(
    result: AnalysisResult,
    failOn: Severity = Severity.INFO,
): Int =
    when {
        result.errors.isNotEmpty() && result.findings.isEmpty() -> AlgorillaCommand.EXIT_ERROR
        result.findings.any { it.severity.ordinal >= failOn.ordinal } -> AlgorillaCommand.EXIT_FINDINGS
        else -> AlgorillaCommand.EXIT_OK
    }

private const val SEVERITY_COL_WIDTH = 9
private const val RULE_TABLE_WIDTH = 85

private fun printRuleList(
    rules: List<Rule>,
    color: Boolean,
) {
    val out = System.out.bufferedWriter()
    out.appendLine()
    out.appendLine(
        Ansi.boldWhite(
            "  %-30s  %-9s  %-20s  %s".format("RULE ID", "SEVERITY", "CATEGORY", "LANGUAGES"),
            color,
        ),
    )
    out.appendLine("  ${"─".repeat(RULE_TABLE_WIDTH)}")
    for (rule in rules.sortedWith(compareBy({ it.category.ordinal }, { it.id }))) {
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
        out.appendLine("  %-30s  %s  %-20s  %s".format(rule.id, severityStr, rule.category.displayName, langs))
    }
    out.appendLine()
    out.appendLine(Ansi.dim("  ${rules.size} rules available", color))
    out.appendLine()
    out.flush()
}

private fun resolveColor(
    color: String,
    outputFile: File?,
): Boolean =
    when (color.lowercase()) {
        "always" -> true
        "never" -> false
        else -> outputFile == null && System.console() != null
    }

private fun printScanTarget(
    paths: List<File>,
    color: Boolean,
) {
    val targets = paths.joinToString(", ") { it.absoluteFile.normalize().path }
    System.err.println(Ansi.dim("\uD83E\uDD8D Scanning $targets...", color))
}

@Suppress("SpreadOperator")
public fun main(args: Array<String>) {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val exitCode = CommandLine(AlgorillaCommand()).execute(*args)
    exitProcess(exitCode)
}
