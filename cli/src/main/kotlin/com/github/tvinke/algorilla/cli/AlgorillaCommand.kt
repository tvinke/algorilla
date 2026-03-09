package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.baseline.Baseline
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
import com.github.tvinke.algorilla.reporting.ConsoleReporter
import com.github.tvinke.algorilla.reporting.JsonReporter
import com.github.tvinke.algorilla.reporting.SarifReporter
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.builtin.ChainedGettersRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSerializationInLoopRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSortComparatorRule
import com.github.tvinke.algorilla.rules.builtin.FilterAfterSortRule
import com.github.tvinke.algorilla.rules.builtin.FullScanForSingleLookupRule
import com.github.tvinke.algorilla.rules.builtin.HeavyweightObjectPerInvocationRule
import com.github.tvinke.algorilla.rules.builtin.InLoopCollectionBuildingRule
import com.github.tvinke.algorilla.rules.builtin.NPlusOneRepositoryCallRule
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.rules.builtin.RedundantExpensiveCallRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedLinearScanRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedRegexInLoopRule
import com.github.tvinke.algorilla.rules.builtin.SequentialAsyncJoinInLoopRule
import com.github.tvinke.algorilla.rules.builtin.SortForLastRule
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
    version = ["algorilla 0.1.0-SNAPSHOT"],
    description = ["Detects algorithmic complexity anti-patterns in source code."],
)
internal class AlgorillaCommand : Callable<Int> {
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

    override fun call(): Int {
        configureLogging()
        val result = runAnalysis()
        val filteredResult = applyBaseline(result)
        writeReport(filteredResult)
        return exitCodeFor(filteredResult)
    }

    private fun configureLogging() {
        if (verbose) {
            val loggerContext = org.slf4j.LoggerFactory.getILoggerFactory() as ch.qos.logback.classic.LoggerContext
            loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
        }
    }

    private fun runAnalysis(): com.github.tvinke.algorilla.engine.AnalysisResult {
        val config = buildConfig()
        val detector = ProjectStructureDetector()
        val projectRoot = detector.resolveProjectRoot(paths.first())
        val scanRoots = paths.flatMap { detector.resolveSourceRoots(projectRoot, it) }

        val parsers = listOf(JavaLanguageParser(), GroovyParser(), KotlinParser(), JavaScriptParser())
        val rules = builtinRules()
        val customRules = CustomRuleLoader.loadRules(projectRoot)
        val allRules = rules + customRules
        val cache = if (noCache) null else AnalysisCache(projectRoot)
        val engine = AnalysisEngine(parsers = parsers, rules = allRules, config = config, cache = cache, verbose = verbose)
        return engine.analyze(collectSourceFiles(detector, scanRoots, config.excludePatterns))
    }

    private fun builtinRules(): List<Rule> =
        listOf(
            NestedLookupRule(),
            SortForLastRule(),
            ExpensiveSortComparatorRule(),
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
        )

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

    private fun cliSeverityProvided(): Boolean {
        // picocli sets the default; check if the user explicitly passed --severity
        return spec.commandLine().parseResult.hasMatchedOption("severity")
    }

    private fun applyBaseline(result: AnalysisResult): AnalysisResult {
        if (saveBaselineFile != null) {
            Baseline.save(result.findings, saveBaselineFile!!)
        }
        val baseline = baselineFile?.let { Baseline.load(it) }
        return if (baseline != null) {
            val newFindings = baseline.filterNew(result.findings)
            result.copy(findings = newFindings)
        } else {
            result
        }
    }

    private fun writeReport(result: com.github.tvinke.algorilla.engine.AnalysisResult) {
        val reporter =
            when (format.lowercase()) {
                "sarif" -> SarifReporter()
                "json" -> JsonReporter()
                else -> ConsoleReporter()
            }
        val output = outputFile?.bufferedWriter() ?: System.out.bufferedWriter()
        output.use { reporter.report(result, it) }
    }

    private fun exitCodeFor(result: com.github.tvinke.algorilla.engine.AnalysisResult): Int =
        when {
            result.errors.isNotEmpty() && result.findings.isEmpty() -> EXIT_ERROR
            result.findings.isNotEmpty() -> EXIT_FINDINGS
            else -> EXIT_OK
        }

    private fun collectSourceFiles(
        detector: ProjectStructureDetector,
        paths: List<File>,
        effectiveExcludePatterns: List<String>,
    ): List<String> {
        val testFilter = detector.buildTestExcludeFilter(paths, includeTests)
        val defaultExcludes = detector.defaultExcludePatterns()
        val files = mutableListOf<String>()
        for (path in paths) {
            if (path.isFile) {
                if (isSupportedFile(path)) files.add(path.absolutePath)
            } else if (path.isDirectory) {
                path
                    .walkTopDown()
                    .filter { it.isFile && isSupportedFile(it) }
                    .filter { file -> defaultExcludes.none { excl -> file.path.contains(excl) } }
                    .filter(testFilter)
                    .filter { file -> effectiveExcludePatterns.none { pattern -> matchGlob(pattern, file.path) } }
                    .forEach { files.add(it.absolutePath) }
            }
        }
        return files
    }

    private fun isSupportedFile(file: File): Boolean {
        val ext = file.extension.lowercase()
        return Language.fromExtension(ext) != null
    }

    private fun matchGlob(
        pattern: String,
        path: String,
    ): Boolean {
        val regex =
            pattern
                .replace(".", "\\.")
                .replace("**", "\u00A7\u00A7")
                .replace("*", "[^/]*")
                .replace("\u00A7\u00A7", ".*")
        return Regex(regex).containsMatchIn(path)
    }

    internal companion object {
        const val EXIT_OK = 0
        const val EXIT_FINDINGS = 1
        const val EXIT_ERROR = 2
    }
}

@Suppress("SpreadOperator")
public fun main(args: Array<String>) {
    val exitCode = CommandLine(AlgorillaCommand()).execute(*args)
    exitProcess(exitCode)
}
