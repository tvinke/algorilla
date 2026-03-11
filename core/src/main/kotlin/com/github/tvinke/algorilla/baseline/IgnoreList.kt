package com.github.tvinke.algorilla.baseline

import com.github.tvinke.algorilla.rules.Finding
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

private val logger = KotlinLogging.logger {}

private const val IGNORE_LIST_DIR = ".algorilla"
private const val IGNORE_LIST_FILE = "ignore-list.json"

/**
 * Manages a list of individually reviewed and accepted findings. Unlike a baseline
 * (which captures all findings at a point in time), the ignore list tracks specific
 * findings a developer has reviewed and decided are acceptable.
 *
 * Matching uses the same content-based fingerprint as [Baseline], so accepted findings
 * survive line number shifts as long as the code around them stays the same.
 */
public class IgnoreList(
    private val entries: Map<String, IgnoredEntry>,
) {
    /**
     * Returns only findings that are not in this ignore list.
     */
    public fun filter(findings: List<Finding>): List<Finding> = findings.filter { Baseline.fingerprintOf(it).contentHash !in entries }

    /**
     * Number of ignored findings.
     */
    public val size: Int get() = entries.size

    public companion object {
        private val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

        /**
         * Resolves the default ignore-list file location for a project root.
         */
        public fun defaultFile(projectRoot: File): File = File(projectRoot, "$IGNORE_LIST_DIR/$IGNORE_LIST_FILE")

        /**
         * Loads the ignore list from disk. Returns an empty list if the file doesn't exist.
         */
        public fun load(file: File): IgnoreList {
            if (!file.exists()) return IgnoreList(emptyMap())
            return try {
                val data = json.decodeFromString<IgnoreListData>(file.readText())
                IgnoreList(data.ignored.associateBy { it.fingerprint })
                    .also { logger.info { "Ignore list loaded: ${data.ignored.size} entries" } }
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                logger.warn { "Failed to load ignore list: ${e.message}" }
                IgnoreList(emptyMap())
            }
        }

        /**
         * Accepts specific findings by their fingerprint hash and adds them to the ignore list.
         * Merges with any existing entries on disk.
         */
        public fun accept(
            file: File,
            findings: List<Finding>,
            hashes: Set<String>,
        ): Int {
            val existing = load(file)
            val matched =
                findings.filter { finding ->
                    Baseline.fingerprintOf(finding).contentHash in hashes
                }
            if (matched.isEmpty()) return 0

            val newEntries = existing.entries.toMutableMap()
            for (finding in matched) {
                val fp = Baseline.fingerprintOf(finding)
                newEntries[fp.contentHash] =
                    IgnoredEntry(
                        fingerprint = fp.contentHash,
                        ruleId = finding.ruleId,
                        file = fp.file,
                        message = finding.message,
                    )
            }
            save(file, newEntries.values.toList())
            return matched.size
        }

        /**
         * Saves the ignore list to disk.
         */
        private fun save(
            file: File,
            entries: List<IgnoredEntry>,
        ) {
            file.parentFile?.mkdirs()
            val data = IgnoreListData(ignored = entries)
            file.writeText(json.encodeToString(IgnoreListData.serializer(), data))
            logger.info { "Ignore list saved: ${entries.size} entries to ${file.path}" }
        }
    }
}

@Serializable
internal data class IgnoreListData(
    val ignored: List<IgnoredEntry>,
)

/**
 * A single entry in the ignore list, recording why a finding was accepted.
 */
@Serializable
public data class IgnoredEntry(
    val fingerprint: String,
    val ruleId: String,
    val file: String,
    val message: String,
)
