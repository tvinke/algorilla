package com.github.tvinke.algorilla.cache

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest

private val logger = KotlinLogging.logger {}

private const val CACHE_DIR = ".algorilla"
private const val CACHE_FILE = "analysis-cache.json"

/**
 * Manages an on-disk cache of per-file analysis results. Files are identified by
 * content hash; only files whose hash has changed since the last run are re-analyzed.
 */
public class AnalysisCache(
    private val baseDir: File,
) {
    private val cacheFile = File(baseDir, "$CACHE_DIR/$CACHE_FILE")
    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

    /**
     * Loads the cached entries from disk. Returns an empty map if no cache exists.
     */
    public fun load(): Map<String, CachedFileEntry> {
        if (!cacheFile.exists()) return emptyMap()
        return try {
            val data = json.decodeFromString<CacheData>(cacheFile.readText())
            data.files.associateBy { it.filePath }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn { "Cache file corrupted, starting fresh: ${e.message}" }
            emptyMap()
        }
    }

    /**
     * Saves the given entries to the cache file on disk.
     */
    public fun save(entries: List<CachedFileEntry>) {
        cacheFile.parentFile.mkdirs()
        val data = CacheData(files = entries)
        cacheFile.writeText(json.encodeToString(CacheData.serializer(), data))
        logger.debug { "Cache saved: ${entries.size} file entries" }
    }

    /**
     * Deletes the cache file if it exists.
     */
    public fun clear() {
        if (cacheFile.exists()) {
            cacheFile.delete()
            logger.info { "Cache cleared" }
        }
    }

    public companion object {
        /**
         * Computes a SHA-256 content hash for the given file.
         */
        public fun hashFile(filePath: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = File(filePath).readBytes()
            return digest.digest(bytes).joinToString("") { "%02x".format(it) }
        }
    }
}

@Serializable
internal data class CacheData(
    val files: List<CachedFileEntry>,
)

/**
 * A cached analysis result for a single source file.
 */
@Serializable
public data class CachedFileEntry(
    val filePath: String,
    val contentHash: String,
    val findings: List<CachedFinding>,
)

/**
 * Serializable representation of a [Finding] for cache storage.
 */
@Serializable
public data class CachedFinding(
    val ruleId: String,
    val ruleName: String,
    val severity: String,
    val file: String,
    val line: Int,
    val column: Int,
    val message: String,
    val suggestion: String,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<CachedEvidence> = emptyList(),
) {
    internal fun toFinding(): Finding =
        Finding(
            ruleId = ruleId,
            ruleName = ruleName,
            severity = Severity.valueOf(severity),
            location = SourceLocation(file, line, column),
            message = message,
            suggestion = suggestion,
            currentComplexity = currentComplexity,
            suggestedComplexity = suggestedComplexity,
            evidence = evidence.map { it.toEvidence() },
        )

    internal companion object {
        fun fromFinding(finding: Finding): CachedFinding =
            CachedFinding(
                ruleId = finding.ruleId,
                ruleName = finding.ruleName,
                severity = finding.severity.name,
                file = finding.location.file,
                line = finding.location.line,
                column = finding.location.column,
                message = finding.message,
                suggestion = finding.suggestion,
                currentComplexity = finding.currentComplexity,
                suggestedComplexity = finding.suggestedComplexity,
                evidence = finding.evidence.map { CachedEvidence.fromEvidence(it) },
            )
    }
}

/**
 * Serializable representation of an [Evidence] entry for cache storage.
 */
@Serializable
public data class CachedEvidence(
    val file: String,
    val line: Int,
    val column: Int,
    val label: String,
    val executionContext: String,
) {
    internal fun toEvidence(): Evidence =
        Evidence(
            location = SourceLocation(file, line, column),
            label = label,
            executionContext = ExecutionContext.valueOf(executionContext),
        )

    internal companion object {
        fun fromEvidence(evidence: Evidence): CachedEvidence =
            CachedEvidence(
                file = evidence.location.file,
                line = evidence.location.line,
                column = evidence.location.column,
                label = evidence.label,
                executionContext = evidence.executionContext.name,
            )
    }
}
