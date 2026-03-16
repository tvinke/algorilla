package com.github.tvinke.algorilla.rules

public sealed class Suggestion {
    public abstract fun render(): String

    public data class PreBuildStructure(
        val structure: String,
        val variable: String,
        val qualifier: String? = null,
    ) : Suggestion() {
        override fun render(): String =
            "Build a $structure from '$variable' before the loop" +
                (if (qualifier != null) " ($qualifier)" else "")
    }

    public data class AlreadyOptimal(
        val variable: String,
        val currentType: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "'$variable' is already a $currentType — $reason"
    }

    public data class UseBulkAPI(
        val bulkMethod: String,
        val singleMethod: String,
        val framework: String? = null,
    ) : Suggestion() {
        override fun render(): String {
            val fw = if (framework != null) " ($framework)" else ""
            return "Use $bulkMethod$fw instead of calling $singleMethod per iteration"
        }
    }

    public data class UseAlternativeAPI(
        val alternative: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "Use $alternative — $reason"
    }

    public data class CacheResult(
        val callDescription: String,
    ) : Suggestion() {
        override fun render(): String = "Cache the result of $callDescription in a local variable"
    }

    public data class HoistBeforeLoop(
        val call: String,
    ) : Suggestion() {
        override fun render(): String = "Hoist $call before the loop — the result is the same on every iteration"
    }

    public data class ReorderOperations(
        val first: String,
        val second: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "Move $first before $second — $reason"
    }

    public data class Freeform(
        val text: String,
    ) : Suggestion() {
        override fun render(): String = text
    }
}
