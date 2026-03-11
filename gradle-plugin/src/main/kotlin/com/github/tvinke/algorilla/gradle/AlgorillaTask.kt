package com.github.tvinke.algorilla.gradle

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.AnalysisEngine
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.lang.groovy.parser.GroovyParser
import com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParser
import com.github.tvinke.algorilla.lang.javascript.parser.JavaScriptParser
import com.github.tvinke.algorilla.lang.kotlin.parser.KotlinParser
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.reporting.ConsoleReporter
import com.github.tvinke.algorilla.reporting.JsonReporter
import com.github.tvinke.algorilla.reporting.SarifReporter
import com.github.tvinke.algorilla.rules.builtin.BuiltinRules
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@Suppress("TooManyFunctions")
public abstract class AlgorillaTask : DefaultTask() {
    @get:Input
    public abstract val minSeverity: Property<String>

    @get:Input
    public abstract val failOn: Property<String>

    @get:Input
    public abstract val format: Property<String>

    @get:OutputFile
    public abstract val outputFile: Property<java.io.File>

    @get:Input
    public abstract val excludePatterns: ListProperty<String>

    @get:Input
    public abstract val ruleIds: ListProperty<String>

    @get:Input
    public abstract val includeTests: Property<Boolean>

    @get:Input
    @get:Optional
    public abstract val baseline: Property<java.io.File>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val sourceDirectories: ConfigurableFileCollection

    @TaskAction
    public fun run() {
        val sourceFiles = collectSourceFiles()
        if (sourceFiles.isEmpty()) {
            logger.lifecycle("algorilla: no source files found, skipping")
            return
        }

        val config = buildConfig()
        val rules = resolveRules()
        val parsers = listOf(JavaLanguageParser(), GroovyParser(), KotlinParser(), JavaScriptParser())
        val result = AnalysisEngine(parsers = parsers, rules = rules, config = config).analyze(sourceFiles)

        val filtered = applyBaseline(result)
        writeReport(filtered)

        val failSeverity = parseSeverity(failOn.get())
        val failing = filtered.findings.filter { it.severity.ordinal >= failSeverity.ordinal }
        if (failing.isNotEmpty()) {
            throw GradleException(
                "algorilla found ${failing.size} finding(s) at or above ${failSeverity.name.lowercase()} severity. " +
                    "See report: ${outputFile.get().absolutePath}",
            )
        }

        logger.lifecycle("algorilla: ${filtered.findings.size} finding(s) written to ${outputFile.get().absolutePath}")
    }

    private fun applyBaseline(result: AnalysisResult): AnalysisResult =
        if (baseline.isPresent) {
            val bl = Baseline.load(baseline.get())
            result.copy(findings = bl.filterNew(result.findings))
        } else {
            result
        }

    private fun collectSourceFiles(): List<String> =
        sourceDirectories.files
            .filter { it.isDirectory }
            .flatMap { dir ->
                dir
                    .walkTopDown()
                    .filter { it.isFile && it.extension in SUPPORTED_EXTENSIONS }
                    .map { it.absolutePath }
                    .toList()
            }

    private fun buildConfig(): AnalysisConfig =
        AnalysisConfig(
            minSeverity = parseSeverity(minSeverity.get()),
            excludePatterns = excludePatterns.get(),
        )

    private fun resolveRules() =
        BuiltinRules.all().let { all ->
            val ids = ruleIds.get()
            if (ids.isNotEmpty()) {
                val idSet = ids.toSet()
                all.filter { it.id in idSet || it.name in idSet }
            } else {
                all
            }
        }

    private fun writeReport(result: AnalysisResult) {
        val output = outputFile.get()
        output.parentFile?.mkdirs()
        val reporter =
            when (format.get().lowercase()) {
                "sarif" -> SarifReporter()
                "console" -> ConsoleReporter(color = false)
                else -> JsonReporter()
            }
        output.bufferedWriter().use { reporter.report(result, it) }
    }

    private fun parseSeverity(value: String): Severity =
        when (value.lowercase()) {
            "error" -> Severity.ERROR
            "warning" -> Severity.WARNING
            else -> Severity.INFO
        }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("java", "groovy", "kt", "kts", "js", "ts", "vue")
    }
}
