// Primary export is the markScalarLookups function, not the result type
@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.signatureKey
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.transform

/**
 * Result of the scalar-lookup marking pass: refined IR trees and per-function type environments.
 */
public data class MarkScalarLookupsResult(
    val irTrees: Map<String, FileRoot>,
    val typeEnvironments: Map<String, TypeEnvironment>,
)

/**
 * Post-parse pass that refines [LookupCall] nodes based on declared variable types:
 * - Marks as `isScalar = true` when the target type is not a known collection (String, Optional, etc.)
 * - Promotes to `isO1 = true` when the target type is a known O(1) type (Set, Map, etc.)
 *
 * Also builds a [TypeEnvironment] per function, which is returned alongside the refined trees
 * for use by rules during evaluation.
 *
 * Uses both same-file field types and a cross-file field type registry (keyed by declaring class)
 * so that `this.field` references resolve even when the cross-method resolver follows calls
 * into other files.
 */
public fun markScalarLookups(
    irTrees: Map<String, FileRoot>,
    registry: LanguageSemanticsRegistry,
): MarkScalarLookupsResult {
    val globalFieldTypes = collectGlobalFieldTypes(irTrees)
    // Merge all field types across all classes as a fallback for inherited fields.
    // Precedence: local file > declaring class > any class in scan scope.
    val allGlobalFields = globalFieldTypes.values.fold(emptyMap<String, String>()) { acc, m -> acc + m }
    val result = mutableMapOf<String, FileRoot>()
    val typeEnvs = mutableMapOf<String, TypeEnvironment>()
    for ((file, fileRoot) in irTrees) {
        val language = fileRoot.language
        val localFieldTypes = collectFieldTypes(fileRoot)
        val transformed =
            fileRoot.transform { node ->
                if (node is FunctionDecl) {
                    val classFields = node.declaringClass?.let { globalFieldTypes[it] } ?: emptyMap()
                    val mergedFields = allGlobalFields + classFields + localFieldTypes
                    val typeEnv = TypeEnvironment.build(node, mergedFields, language, registry)
                    typeEnvs[signatureKey(node)] = typeEnv
                    markScalarLookupsInFunction(node, language, registry, typeEnv)
                } else {
                    node
                }
            }
        result[file] = transformed as FileRoot
    }
    return MarkScalarLookupsResult(result, typeEnvs)
}

/**
 * Builds a cross-file field type registry: className → (fieldName → typeName).
 * For each file, determines the declaring class from FunctionDecl nodes and associates
 * all class-level VariableDecl types with that class.
 */
private fun collectGlobalFieldTypes(irTrees: Map<String, FileRoot>): Map<String, Map<String, String>> {
    val global = mutableMapOf<String, MutableMap<String, String>>()
    for ((_, fileRoot) in irTrees) {
        val classNames = fileRoot.findDescendants<FunctionDecl>().mapNotNull { it.declaringClass }.toSet()
        val fieldTypes = collectFieldTypes(fileRoot)
        for (className in classNames) {
            global.getOrPut(className) { mutableMapOf() }.putAll(fieldTypes)
        }
    }
    return global
}

/**
 * Collects type info from class-level field declarations (VariableDecl nodes that are
 * direct children of the FileRoot, not nested inside a FunctionDecl).
 */
private fun collectFieldTypes(fileRoot: FileRoot): Map<String, String> {
    val fieldTypes = mutableMapOf<String, String>()

    fun collectFromChildren(children: List<IRNode>) {
        for (node in children) {
            if (node is FunctionDecl) continue
            if (node is VariableDecl && node.typeName != null) {
                fieldTypes[node.name] = node.typeName
            }
            collectFromChildren(node.children)
        }
    }
    collectFromChildren(fileRoot.children)
    return fieldTypes
}

private fun markScalarLookupsInFunction(
    fn: FunctionDecl,
    language: Language,
    registry: LanguageSemanticsRegistry,
    typeEnv: TypeEnvironment,
): FunctionDecl =
    fn.transform { node ->
        if (node is LookupCall && node.targetVariable != null && !node.isO1) {
            val varName = node.targetVariable.removePrefix("this.")
            val type = typeEnv.typeOf(varName)
            when {
                type == null -> node
                registry.isO1Type(language, type.simpleName) -> node.copy(isO1 = true)
                !registry.isCollectionType(language, type.simpleName) -> node.copy(isScalar = true)
                else -> node
            }
        } else {
            node
        }
    } as FunctionDecl
