package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment

/**
 * The complete context available to rules during evaluation. Contains parsed IR trees,
 * the project-wide symbol table, call graph, user configuration, and the collection
 * semantics registry.
 */
public data class AnalysisContext(
    val irTrees: Map<String, FileRoot>,
    val symbolTable: SymbolTable,
    val callGraph: CallGraph,
    val config: AnalysisConfig,
    val registry: LanguageSemanticsRegistry = LanguageSemanticsRegistry.DEFAULT,
    val typeEnvironments: Map<String, TypeEnvironment> = emptyMap(),
) {
    /**
     * Returns the [TypeEnvironment] for the given function, or null if not available.
     * Keyed by a signature string (qualifiedName + parameter types) so lookup works
     * regardless of tree transforms and distinguishes overloaded methods.
     */
    public fun typeEnvironmentFor(fn: FunctionDecl): TypeEnvironment? = typeEnvironments[signatureKey(fn)]
}

/**
 * Builds a unique key for a function that distinguishes overloads.
 * Used by both the engine (when storing) and AnalysisContext (when looking up).
 */
public fun signatureKey(fn: FunctionDecl): String = "${fn.qualifiedName}(${fn.parameters.joinToString(",") { it.typeName ?: "_" }})"
