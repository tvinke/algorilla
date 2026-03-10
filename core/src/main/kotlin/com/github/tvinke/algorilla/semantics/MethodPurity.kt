package com.github.tvinke.algorilla.semantics

/**
 * Classifies a method call as pure, side-effectful, or unknown based on naming conventions.
 */
public enum class Purity { PURE, SIDE_EFFECT, UNKNOWN }

public object MethodPurity {
    // Methods starting with these prefixes are likely pure (return value is the point)
    private val PURE_PREFIXES =
        listOf(
            "get",
            "find",
            "compute",
            "calculate",
            "parse",
            "format",
            "convert",
            "resolve",
            "to",
            "is",
            "has",
            "can",
            "should",
            "matches",
            "contains",
            "compare",
            "equals",
            "hash",
            "size",
            "length",
            "count",
            "index",
            "check",
            "validate",
            "determine",
            "derive",
            "extract",
            "build",
            "create",
            "make",
            "of",
            "from",
            "with",
            "map",
            "flat",
            "collect",
            "stream",
            "filter",
            "sort",
            "reduce",
            "join",
            "split",
            "replace",
            "trim",
            "strip",
            "substring",
        )

    // Methods starting with these prefixes are likely side-effectful
    private val SIDE_EFFECT_PREFIXES =
        listOf(
            "set",
            "add",
            "put",
            "remove",
            "delete",
            "clear",
            "reset",
            "write",
            "save",
            "persist",
            "store",
            "insert",
            "update",
            "send",
            "emit",
            "dispatch",
            "publish",
            "notify",
            "fire",
            "log",
            "print",
            "debug",
            "info",
            "warn",
            "error",
            "trace",
            "import",
            "export",
            "upload",
            "download",
            "close",
            "shutdown",
            "destroy",
            "dispose",
            "assert",
            "verify",
            "expect",
            "should",
            "require",
            "throw",
            "fail",
            "register",
            "subscribe",
            "on",
            "append",
            "enable",
            "disable",
            "configure",
            "init",
            "apply",
        )

    // Targets that indicate side-effectful context regardless of method name
    private val SIDE_EFFECT_TARGETS =
        listOf(
            "log",
            "logger",
            "console",
            "system.out",
            "system.err",
            "builder",
            "uriBuilder",
            "response",
            "request",
        )

    /**
     * Classifies a method name by its likely purity.
     * Side-effect prefixes are checked first so that e.g. "setFormat" is SIDE_EFFECT.
     */
    public fun classify(methodName: String): Purity {
        val lower = methodName.lowercase()
        if (SIDE_EFFECT_PREFIXES.any { lower.startsWith(it) }) return Purity.SIDE_EFFECT
        if (PURE_PREFIXES.any { lower.startsWith(it) }) return Purity.PURE
        return Purity.UNKNOWN
    }

    /**
     * Classifies a method call including its target context.
     * A call on a known side-effect target (logger, builder, etc.) is always SIDE_EFFECT.
     */
    public fun classify(
        methodName: String,
        qualifiedTarget: String?,
    ): Purity {
        if (qualifiedTarget != null) {
            val lowerTarget = qualifiedTarget.lowercase()
            if (SIDE_EFFECT_TARGETS.any { lowerTarget.contains(it) }) return Purity.SIDE_EFFECT
        }
        return classify(methodName)
    }

    /**
     * Returns true if the method is classified as side-effectful (by name or target).
     */
    public fun isSideEffect(
        methodName: String,
        qualifiedTarget: String? = null,
    ): Boolean = classify(methodName, qualifiedTarget) == Purity.SIDE_EFFECT
}
