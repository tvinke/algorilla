package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.lang.groovy.parser.GroovyParser
import com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParser
import com.github.tvinke.algorilla.lang.javascript.parser.JavaScriptParser
import com.github.tvinke.algorilla.lang.kotlin.parser.KotlinParser
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.reporting.ConsoleReporter
import com.github.tvinke.algorilla.reporting.JsonReporter
import com.github.tvinke.algorilla.reporting.SarifReporter
import com.github.tvinke.algorilla.rules.builtin.DateInSortRule
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSortComparatorRule
import com.github.tvinke.algorilla.rules.builtin.FullScanForSingleLookupRule
import com.github.tvinke.algorilla.rules.builtin.HeavyweightObjectPerInvocationRule
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.rules.builtin.RepeatedLinearScanRule
import com.github.tvinke.algorilla.rules.builtin.SortForLastRule
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
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

    override fun call(): Int {
        configureLogging()
        val result = runAnalysis()
        writeReport(result)
        return exitCodeFor(result)
    }

    private fun configureLogging() {
        if (verbose) {
            val loggerContext = org.slf4j.LoggerFactory.getILoggerFactory() as ch.qos.logback.classic.LoggerContext
            loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.DEBUG
        }
    }

    private fun runAnalysis(): com.github.tvinke.algorilla.engine.AnalysisResult {
        val config = buildConfig()
        val parsers = listOf(JavaLanguageParser(), GroovyParser(), KotlinParser(), JavaScriptParser())
        val rules =
            listOf(
                NestedLookupRule(),
                SortForLastRule(),
                ExpensiveSortComparatorRule(),
                DateInSortRule(),
                RepeatedLinearScanRule(),
                FullScanForSingleLookupRule(),
                HeavyweightObjectPerInvocationRule(),
            )
        val engine = AnalysisEngine(parsers = parsers, rules = rules, config = config, verbose = verbose)
        return engine.analyze(collectSourceFiles(paths))
    }

    private fun buildConfig(): AnalysisConfig {
        val minSeverity =
            when (severity.lowercase()) {
                "info" -> Severity.INFO
                "error" -> Severity.ERROR
                else -> Severity.WARNING
            }
        return AnalysisConfig(excludePatterns = excludePatterns, minSeverity = minSeverity)
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

    private fun collectSourceFiles(paths: List<File>): List<String> {
        val files = mutableListOf<String>()
        for (path in paths) {
            if (path.isFile) {
                if (isSupportedFile(path)) files.add(path.absolutePath)
            } else if (path.isDirectory) {
                path
                    .walkTopDown()
                    .filter { it.isFile && isSupportedFile(it) }
                    .filter { file -> excludePatterns.none { pattern -> matchGlob(pattern, file.path) } }
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
