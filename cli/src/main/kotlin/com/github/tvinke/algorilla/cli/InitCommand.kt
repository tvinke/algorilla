package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.cache.AnalysisCache
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.ParserRegistry
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.util.concurrent.Callable

@Command(
    name = "init",
    description = ["Initialize Algorilla for a project: scan, create baseline, set up cache directory."],
)
internal class InitCommand : Callable<Int> {
    @Parameters(
        index = "0..*",
        description = ["Project directory to initialize (default: current directory)"],
    )
    private var paths: List<File> = emptyList()

    @Option(
        names = ["--verbose", "-v"],
        description = ["Show detailed progress"],
    )
    private var verbose: Boolean = false

    override fun call(): Int {
        configureLogging(verbose)
        val projectDir = (paths.firstOrNull() ?: File(".")).absoluteFile.normalize()

        System.err.println("Initializing Algorilla for ${projectDir.path}...")

        setupAlgorillaDir(projectDir)
        val result = scanProject(projectDir)
        saveBaseline(result, projectDir)

        return AlgorillaCommand.EXIT_OK
    }

    private fun setupAlgorillaDir(projectDir: File) {
        val algorillaDir = File(projectDir, ".algorilla")
        algorillaDir.mkdirs()
        val gitignore = File(algorillaDir, ".gitignore")
        if (!gitignore.exists()) {
            gitignore.writeText("analysis-cache.json\n")
        }
    }

    private fun scanProject(projectDir: File): AnalysisResult {
        registerAllParsers()
        val parsers = ParserRegistry.all()
        val config = AnalysisConfig()
        val detector = ProjectStructureDetector()
        val scanRoots =
            listOf(projectDir).flatMap { detector.resolveSourceRoots(projectDir, it) }
        val cache = AnalysisCache(projectDir)
        val collector = SourceFileCollector(detector)
        val sourceFiles = collector.collect(scanRoots, config.excludePatterns, false, null)

        System.err.println("Scanning ${sourceFiles.size} files...")

        return AnalysisEngine(
            parsers = parsers,
            rules = builtinRules(),
            config = config,
            cache = cache,
            verbose = verbose,
        ).analyze(sourceFiles)
    }

    private fun saveBaseline(
        result: AnalysisResult,
        projectDir: File,
    ) {
        val baselineFile = File(projectDir, ".algorilla.baseline.json")
        Baseline.save(result.findings, baselineFile, projectDir)

        val findingCount = result.findings.size
        System.err.println(
            "Baselined $findingCount ${if (findingCount == 1) "finding" else "findings"} in ${baselineFile.name}",
        )

        if (result.findings.isNotEmpty()) {
            printTopPatterns(result, projectDir)
        }
    }

    private fun printTopPatterns(
        result: AnalysisResult,
        projectDir: File,
    ) {
        val topRules =
            result.findings
                .groupingBy { it.ruleId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(TOP_RULES_DISPLAY)
        System.err.println("Top patterns:")
        for ((rule, count) in topRules) {
            System.err.println("  $rule: $count")
        }
        System.err.println()
        System.err.println(
            "These are now baselined. Run 'algorilla ${projectDir.path}' to see new findings only.",
        )
    }

    private companion object {
        const val TOP_RULES_DISPLAY = 5
    }
}
