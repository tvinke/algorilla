// Primary export is the markScalarLookups function, not the result type
@file:Suppress("MatchingDeclarationName", "ktlint:standard:filename")

package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.signatureKey
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeContext
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
 * Uses [TypeContext] to carry field types, method return types, class hierarchy, and
 * cross-file return types to [TypeEnvironment.build].
 */
public fun markScalarLookups(
    irTrees: Map<String, FileRoot>,
    registry: LanguageSemanticsRegistry,
): MarkScalarLookupsResult {
    val globalFieldTypes = collectGlobalFieldTypes(irTrees)
    val allGlobalFields = globalFieldTypes.values.fold(emptyMap<String, String>()) { acc, m -> acc + m }
    val globalMethodReturnTypes = collectGlobalMethodReturnTypes(irTrees)
    val classHierarchy = collectClassHierarchy(irTrees)
    val result = mutableMapOf<String, FileRoot>()
    val typeEnvs = mutableMapOf<String, TypeEnvironment>()
    for ((file, fileRoot) in irTrees) {
        val transformed =
            processFile(
                fileRoot,
                globalFieldTypes,
                allGlobalFields,
                globalMethodReturnTypes,
                classHierarchy,
                registry,
                typeEnvs,
            )
        result[file] = transformed
    }
    return MarkScalarLookupsResult(result, typeEnvs)
}

private fun processFile(
    fileRoot: FileRoot,
    globalFieldTypes: Map<String, Map<String, String>>,
    allGlobalFields: Map<String, String>,
    globalMethodReturnTypes: Map<String, String>,
    classHierarchy: Map<String, Set<String>>,
    registry: LanguageSemanticsRegistry,
    typeEnvs: MutableMap<String, TypeEnvironment>,
): FileRoot {
    val language = fileRoot.language
    val localFieldTypes = collectFieldTypes(fileRoot)
    val localMethodReturnTypes = collectMethodReturnTypes(fileRoot)
    val transformed =
        fileRoot.transform { node ->
            if (node is FunctionDecl) {
                val classFields = node.declaringClass?.let { globalFieldTypes[it] } ?: emptyMap()
                val mergedFields = allGlobalFields + classFields + localFieldTypes
                val context =
                    TypeContext(
                        fieldTypes = mergedFields,
                        localMethodReturnTypes = localMethodReturnTypes,
                        globalMethodReturnTypes = globalMethodReturnTypes,
                        classHierarchy = classHierarchy,
                    )
                val typeEnv = TypeEnvironment.build(node, context, language, registry)
                typeEnvs[signatureKey(node)] = typeEnv
                markScalarLookupsInFunction(node, typeEnv)
            } else {
                node
            }
        } as FileRoot
    return transformed
}

/**
 * Builds a cross-file field type registry: className → (fieldName → typeName).
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
 * direct children of the FileRoot or ClassNode, not nested inside a FunctionDecl).
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

/**
 * Collects declared return types from all methods in a file: methodName → returnType.
 */
private fun collectMethodReturnTypes(fileRoot: FileRoot): Map<String, String> =
    fileRoot
        .findDescendants<FunctionDecl>()
        .filter { !it.isConstructor && it.returnType != null }
        .associate { it.name to it.returnType!! }

/**
 * Builds cross-file method return type index: "ClassName.methodName" → returnType (L1b).
 * Uses qualified keys so different classes with the same method name don't conflict.
 */
private fun collectGlobalMethodReturnTypes(irTrees: Map<String, FileRoot>): Map<String, String> {
    val global = mutableMapOf<String, String>()
    for ((_, fileRoot) in irTrees) {
        fileRoot
            .findDescendants<FunctionDecl>()
            .filter { !it.isConstructor && it.returnType != null && it.declaringClass != null }
            .forEach { fn -> global["${fn.declaringClass}.${fn.name}"] = fn.returnType!! }
    }
    return global
}

/**
 * Builds the class hierarchy from [ClassNode] supertypes across all files (L3).
 * Returns className → set of direct supertypes.
 */
private fun collectClassHierarchy(irTrees: Map<String, FileRoot>): Map<String, Set<String>> {
    val hierarchy = mutableMapOf<String, MutableSet<String>>()
    for ((_, fileRoot) in irTrees) {
        for (classNode in fileRoot.findDescendants<ClassNode>()) {
            if (classNode.supertypes.isNotEmpty()) {
                hierarchy.getOrPut(classNode.name) { mutableSetOf() }.addAll(classNode.supertypes)
            }
        }
    }
    return hierarchy
}

private fun markScalarLookupsInFunction(
    fn: FunctionDecl,
    typeEnv: TypeEnvironment,
): FunctionDecl =
    fn.transform { node ->
        if (node is LookupCall && node.targetVariable != null && !node.isO1) {
            val varName = node.targetVariable.removePrefix("this.")
            val type = typeEnv.typeOf(varName)
            when {
                type == null -> node
                typeEnv.isO1(varName) -> node.copy(isO1 = true)
                !typeEnv.isCollection(varName) -> node.copy(isScalar = true)
                else -> node
            }
        } else {
            node
        }
    } as FunctionDecl
