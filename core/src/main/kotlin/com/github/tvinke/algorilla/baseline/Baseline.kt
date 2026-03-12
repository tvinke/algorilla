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
     */
    public fun filterNew(findings: List<Finding>): List<Finding> = findings.filter { !entries.contains(fingerprintOf(it)) }

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
        ) {
            file.parentFile?.mkdirs()
            val fingerprints = findings.map { fingerprintOf(it) }
            val data = BaselineData(fingerprints = fingerprints)
            file.writeText(json.encodeToString(BaselineData.serializer(), data))
            logger.info { "Baseline saved: ${fingerprints.size} findings to ${file.path}" }
        }

        /**
         * Creates a fingerprint for a finding that is resilient to line number shifts.
         * Uses file path, rule ID, and a hash of the finding message.
         */
        public fun fingerprintOf(finding: Finding): BaselineFingerprint {
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

private const val BASELINE_FORMAT_VERSION = 1

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
