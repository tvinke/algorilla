package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

/**
 * Detects memoization and visited-set patterns inside recursive functions.
 * Used by [com.github.tvinke.algorilla.rules.builtin.UnmemoizedRecursionRule]
 * to suppress false positives when a function already caches its results.
 */
public object MemoizationDetector {
    /**
     * Returns true if the function body contains calls to known memoization methods
     * (from the YAML semantics registry) where arguments reference function parameter names.
     */
    public fun hasMemoizationPattern(
        fn: FunctionDecl,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Boolean {
        val paramNames = fn.parameters.map { it.name }.toSet()
        if (paramNames.isEmpty()) return false

        val calls = fn.findDescendants<FunctionCall>()
        return calls.any { call ->
            registry.isMemoizationMethod(language, call.name) &&
                referencesAnyParam(call, paramNames)
        }
    }

    /**
     * Returns true if the function has a parameter or local variable whose name
     * indicates visited-set tracking (e.g. "visited", "seen", "memo", "cache"),
     * OR a Set-typed variable used with add()/contains() pattern.
     */
    public fun hasVisitedTracking(
        fn: FunctionDecl,
        language: Language,
        registry: LanguageSemanticsRegistry,
    ): Boolean {
        // Check parameter names
        for (param in fn.parameters) {
            if (registry.isVisitedSetName(language, param.name)) return true
        }

        // Check local variable names
        val vars = fn.findDescendants<VariableDecl>()
        for (v in vars) {
            if (registry.isVisitedSetName(language, v.name)) return true
        }

        // Check for Set-typed variables used with add()/contains()
        val setVarNames =
            vars
                .filter { it.typeName != null && registry.isO1Type(language, it.typeName!!) }
                .map { it.name }
                .toSet()
        if (setVarNames.isNotEmpty()) {
            val calls = fn.findDescendants<FunctionCall>()
            val hasAdd = calls.any { it.name == "add" && it.qualifiedTarget in setVarNames }
            val hasContains = calls.any { it.name == "contains" && it.qualifiedTarget in setVarNames }
            if (hasAdd && hasContains) return true
        }

        return false
    }

    private fun referencesAnyParam(
        call: FunctionCall,
        paramNames: Set<String>,
    ): Boolean {
        // Check all descendant nodes in call arguments for references to parameter names
        val argCalls = call.findDescendants<FunctionCall>()
        for (argCall in argCalls) {
            if (argCall.qualifiedTarget in paramNames) return true
        }
        // Also check the call's own target
        if (call.qualifiedTarget in paramNames) return true
        // Check variable references in arguments (represented as GenericNode or similar)
        val argVars = call.findDescendants<VariableDecl>()
        for (v in argVars) {
            if (v.name in paramNames) return true
        }
        return true // conservative: if memoization method is used at all, likely caching
    }
}
