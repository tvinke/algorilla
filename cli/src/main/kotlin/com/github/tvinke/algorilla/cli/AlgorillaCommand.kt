package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.baseline.IgnoreList
import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.ParserRegistry
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
        names = ["--confidence"],
        description = ["Minimum confidence: low, medium, high (default: high)"],
        defaultValue = "high",
    )
    private var confidence: String = "high"

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
        description = ["Minimum severity that triggers a non-zero exit code: info, warning, error (default: warning)"],
        defaultValue = "warning",
    )
    private var failOn: String = "warning"

    @Option(
        names = ["--accept"],
        description = ["Accept findings by fingerprint hash (shown in output). Adds them to .algorilla/ignore-list.json"],
        split = ",",
    )
    private var acceptHashes: List<String> = emptyList()

    private var projectRoot: File = File(".")
    private var scanRoots: List<File> = emptyList()

    override fun call(): Int {
        configureLogging(verbose)
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
        if (outputFile == null) printFrameworkCoverageNotice(scanRoots, useColor)
        return exitCodeFor(accepted, resolveFailOn(failOn))
    }

    private fun runAnalysis(): AnalysisResult {
        val config = buildConfig()
        val detector = ProjectStructureDetector()
        projectRoot = detector.resolveProjectRoot(paths.first())
        scanRoots = paths.flatMap { detector.resolveSourceRoots(projectRoot, it) }

        registerAllParsers()
        val parsers = ParserRegistry.all()
        val languageFilter = resolveLanguageFilter(languages, spec)
        val allRules = resolveRules(projectRoot, languageFilter, ruleFilter)
        val cache = if (noCache) null else AnalysisCache(projectRoot)
        val collector = SourceFileCollector(detector)
        val sourceFiles = collector.collect(scanRoots, config.excludePatterns, includeTests, languageFilter)
        return AnalysisEngine(parsers = parsers, rules = allRules, config = config, cache = cache, verbose = verbose)
            .analyze(sourceFiles)
    }

    private fun buildConfig(): AnalysisConfig {
        val fileConfig = loadConfig(configFile)
        val mergedExclude = if (excludePatterns.isNotEmpty()) excludePatterns else fileConfig.excludePatterns
        val parseResult = spec.commandLine().parseResult
        val cliSev = if (parseResult.hasMatchedOption("severity")) resolveSeverity(severity) else null
        val cliConf = if (parseResult.hasMatchedOption("confidence")) resolveConfidence(confidence) else null
        return fileConfig.copy(
            excludePatterns = mergedExclude,
            minSeverity = cliSev ?: fileConfig.minSeverity,
            minConfidence = cliConf ?: fileConfig.minConfidence,
        )
    }

    internal companion object {
        const val EXIT_OK = 0
        const val EXIT_FINDINGS = 1
        const val EXIT_ERROR = 2
    }
}

@Suppress("SpreadOperator") // picocli requires varargs — spread is unavoidable here
public fun main(args: Array<String>) {
    System.setProperty("slf4j.internal.verbosity", "WARN")
    val exitCode = CommandLine(AlgorillaCommand()).execute(*args)
    exitProcess(exitCode)
}
