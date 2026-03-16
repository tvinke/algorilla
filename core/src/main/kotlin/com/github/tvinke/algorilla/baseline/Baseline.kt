package com.github.tvinke.algorilla.baseline

import com.github.tvinke.algorilla.rules.Finding
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

private val logger = KotlinLogging.logger {}

/**
 * Manages a baseline of known findings. When a baseline is active, only new findings
 * (not present in the baseline) are reported. Matching uses file path, rule ID,
 * and a content-based fingerprint that is resilient to line number shifts.
 */
public class Baseline(
    private val entries: Set<BaselineFingerprint>,
) {
    /**
     * Returns only findings that are not present in this baseline.
     * Uses dual-check: tries v2 (relative path + normalized message) first, falls back to v1.
     */
    public fun filterNew(
        findings: List<Finding>,
        projectRoot: File? = null,
    ): List<Finding> =
        findings.filter { finding ->
            val v2 = fingerprintOf(finding, projectRoot)
            val inBaseline = entries.any { it.contentHash == v2.contentHash }
            if (inBaseline) return@filter false
            // Fall back to v1 (absolute path + raw message) for migration
            if (projectRoot != null) {
                val v1 = fingerprintV1(finding)
                val inBaselineV1 = entries.any { it.contentHash == v1.contentHash }
                !inBaselineV1
            } else {
                true
            }
        }

    public companion object {
        private val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

        /**
         * Loads a baseline from a JSON file on disk.
         */
        public fun load(file: File): Baseline {
            if (!file.exists()) {
                logger.warn { "Baseline file not found: ${file.path}" }
                return Baseline(emptySet())
            }
            return try {
                val data = json.decodeFromString<BaselineData>(file.readText())
                Baseline(data.fingerprints.toSet())
                    .also { logger.info { "Baseline loaded: ${data.fingerprints.size} known findings" } }
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                logger.warn { "Failed to load baseline: ${e.message}" }
                Baseline(emptySet())
            }
        }

        /**
         * Saves the given findings as a baseline to the specified file.
         */
        public fun save(
            findings: List<Finding>,
            file: File,
            projectRoot: File? = null,
        ) {
            file.parentFile?.mkdirs()
            val fingerprints = findings.map { fingerprintOf(it, projectRoot) }
            val data = BaselineData(fingerprints = fingerprints)
            file.writeText(json.encodeToString(BaselineData.serializer(), data))
            logger.info { "Baseline saved: ${fingerprints.size} findings to ${file.path}" }
        }

        private val QUOTED_LITERAL = Regex("""'[^']*'""")
        private val DOUBLE_QUOTED_LITERAL = Regex(""""[^"]*"""")

        /**
         * Creates a portable fingerprint for a finding that is resilient to line number shifts,
         * variable renames (quoted literals normalized), and machine differences (relative paths).
         */
        public fun fingerprintOf(
            finding: Finding,
            projectRoot: File? = null,
        ): BaselineFingerprint {
            val filePath =
                if (projectRoot != null) {
                    try {
                        File(finding.location.file).relativeTo(projectRoot).path
                    } catch (_: IllegalArgumentException) {
                        finding.location.file
                    }
                } else {
                    finding.location.file
                }
            val normalizedMessage =
                finding.message
                    .replace(QUOTED_LITERAL, "'_'")
                    .replace(DOUBLE_QUOTED_LITERAL, "\"_\"")

            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(filePath.toByteArray())
            digest.update(finding.ruleId.toByteArray())
            digest.update(normalizedMessage.toByteArray())
            val hash = digest.digest().joinToString("") { "%02x".format(it) }.take(FINGERPRINT_LENGTH)
            return BaselineFingerprint(
                file = filePath,
                ruleId = finding.ruleId,
                contentHash = hash,
            )
        }

        /**
         * Legacy v1 fingerprint using absolute path and raw message — for migration compatibility.
         */
        internal fun fingerprintV1(finding: Finding): BaselineFingerprint {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(finding.location.file.toByteArray())
            digest.update(finding.ruleId.toByteArray())
            digest.update(finding.message.toByteArray())
            val hash = digest.digest().joinToString("") { "%02x".format(it) }.take(FINGERPRINT_LENGTH)
            return BaselineFingerprint(
                file = finding.location.file,
                ruleId = finding.ruleId,
                contentHash = hash,
            )
        }

        private const val FINGERPRINT_LENGTH = 16
    }
}

private const val BASELINE_FORMAT_VERSION = 2

@Serializable
internal data class BaselineData(
    val version: Int = BASELINE_FORMAT_VERSION,
    val fingerprints: List<BaselineFingerprint>,
)

/**
 * A content-based fingerprint for a single finding, resilient to line number shifts.
 */
@Serializable
public data class BaselineFingerprint(
    val file: String,
    val ruleId: String,
    val contentHash: String,
)
