package com.github.tvinke.algorilla.engine

/**
 * Central registry for language parsers. Language modules register their parser once;
 * the CLI and engine pull from this registry rather than maintaining a hardcoded list.
 *
 * Usage: call [register] from each language module's entry point (e.g. a top-level
 * `fun registerParser()` function, or a companion object init triggered by the CLI).
 * The CLI calls [all] to get the complete list of parsers before starting analysis.
 */
public object ParserRegistry {
    // Thread-safe: registration happens during class loading (companion object init blocks),
    // which may run from different class loaders or Gradle workers in the future.
    private val parsers = java.util.concurrent.CopyOnWriteArrayList<LanguageParser>()

    /**
     * Registers a parser. Each language module should call this once at startup.
     * Duplicate registrations (same language + class) are silently ignored.
     */
    public fun register(parser: LanguageParser) {
        if (parsers.none { it::class == parser::class }) {
            parsers.add(parser)
        }
    }

    /**
     * Returns all registered parsers in registration order.
     */
    public fun all(): List<LanguageParser> = parsers.toList()

    /**
     * Returns the first parser that can handle the given file path, or null if none applies.
     */
    public fun forFile(filePath: String): LanguageParser? = parsers.firstOrNull { it.canParse(filePath) }

    /** Clears all registrations. Intended for testing only. */
    internal fun clear() {
        parsers.clear()
    }
}
