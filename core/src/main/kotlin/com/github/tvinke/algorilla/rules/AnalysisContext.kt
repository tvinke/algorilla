package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

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
    val registry: LanguageSemanticsRegistry = LanguageSemanticsRegistry.loadDefaults(),
)
