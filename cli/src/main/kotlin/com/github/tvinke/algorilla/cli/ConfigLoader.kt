package com.github.tvinke.algorilla.cli

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.config.RuleOverride
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

private const val DEFAULT_CONFIG_FILENAME = ".algorilla.yml"

@Serializable
public data class RuleConfig(
    val enabled: Boolean = true,
    val severity: String? = null,
)

@Serializable
public data class AlgorillaConfig(
    val rules: Map<String, RuleConfig> = emptyMap(),
    val exclude: List<String> = emptyList(),
    @SerialName("type-hints")
    val typeHints: Map<String, String> = emptyMap(),
    @SerialName("heavyweight-types")
    val heavyweightTypes: List<String> = emptyList(),
    @SerialName("min-severity")
    val minSeverity: String? = null,
    @SerialName("min-confidence")
    val minConfidence: String? = null,
    @SerialName("max-call-depth")
    val maxCallDepth: Int? = null,
)

private val lenientYaml = Yaml(configuration = YamlConfiguration(strictMode = false))

/**
 * Loads an [AlgorillaConfig] from a YAML file and converts it to [AnalysisConfig].
 * When no explicit config file is given, looks for `.algorilla.yml` in the current directory.
 * Unknown YAML keys are silently ignored so that newer config files work with older versions.
 */
public fun loadConfig(configFile: File?): AnalysisConfig {
    val file = resolveConfigFile(configFile) ?: return AnalysisConfig()
    val yamlContent = file.readText()
    val config = lenientYaml.decodeFromString(AlgorillaConfig.serializer(), yamlContent)
    warnExperimentalKeys(config)
    return mapToAnalysisConfig(config)
}

private fun resolveConfigFile(configFile: File?): File? {
    if (configFile != null) return if (configFile.exists()) configFile else null
    val defaultFile = File(DEFAULT_CONFIG_FILENAME)
    return if (defaultFile.exists()) defaultFile else null
}

private fun mapToAnalysisConfig(config: AlgorillaConfig): AnalysisConfig {
    val ruleOverrides =
        config.rules.mapValues { (_, ruleConfig) ->
            RuleOverride(
                enabled = ruleConfig.enabled,
                severity = ruleConfig.severity?.let { parseSeverity(it) },
            )
        }
    val heavyweightTypes =
        if (config.heavyweightTypes.isNotEmpty()) {
            config.heavyweightTypes.toSet()
        } else {
            AnalysisConfig.DEFAULT_HEAVYWEIGHT_TYPES
        }
    return AnalysisConfig(
        excludePatterns = config.exclude,
        ruleOverrides = ruleOverrides,
        typeHints = config.typeHints,
        heavyweightTypes = heavyweightTypes,
        minSeverity = config.minSeverity?.let { parseSeverity(it) } ?: Severity.WARNING,
        minConfidence = config.minConfidence?.let { parseConfidence(it) } ?: Confidence.MEDIUM,
        maxCallDepth = config.maxCallDepth ?: AnalysisConfig.DEFAULT_MAX_CALL_DEPTH,
    )
}

private fun warnExperimentalKeys(config: AlgorillaConfig) {
    if (config.typeHints.isNotEmpty()) {
        System.err.println("[algorilla] 'type-hints' is experimental and may change in future releases")
    }
    if (config.heavyweightTypes.isNotEmpty()) {
        System.err.println("[algorilla] 'heavyweight-types' is experimental and may change in future releases")
    }
}

private fun parseSeverity(value: String): Severity =
    when (value.lowercase()) {
        "info" -> Severity.INFO
        "error" -> Severity.ERROR
        else -> Severity.WARNING
    }

private fun parseConfidence(value: String): Confidence =
    when (value.lowercase()) {
        "low" -> Confidence.LOW
        "high" -> Confidence.HIGH
        else -> Confidence.MEDIUM
    }
