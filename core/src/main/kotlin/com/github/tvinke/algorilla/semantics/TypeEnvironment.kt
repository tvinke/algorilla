package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.InferredType
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.TypeSource
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Per-function type map that resolves variable names to their inferred types.
 * Built from field types, parameter types, local declarations, constructor calls,
 * factory methods, chain-end inference, and method name suffix heuristics.
 *
 * Each type carries a [TypeSource] provenance. Low-trust sources (e.g. [TypeSource.NAME_HEURISTIC])
 * are excluded from O(1) promotion to prevent false negatives.
 */
public class TypeEnvironment private constructor(
    private val types: Map<String, InferredType>,
    private val registry: LanguageSemanticsRegistry,
    private val language: Language,
) {
    /** Returns the inferred type for [variableName], or null if unknown. */
    public fun typeOf(variableName: String): InferredType? = types[variableName.removePrefix("this.")]

    /**
     * Returns true if [variableName] is known to be an O(1) lookup type (Set, Map, etc.).
     * Only high-trust sources are considered — [TypeSource.NAME_HEURISTIC] is excluded
     * because e.g. `getCharacterSet()` returning "Set" would suppress real List findings.
     */
    public fun isO1(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        if (type.source == TypeSource.NAME_HEURISTIC) return false
        return registry.isO1Type(language, type.simpleName)
    }

    /** Returns true if [variableName] is known to be a collection type (List, Set, Map, etc.). */
    public fun isCollection(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        return registry.isCollectionType(language, type.simpleName)
    }

    /** Returns true if [variableName] is known to be a String-like type. */
    public fun isString(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        return type.simpleName in STRING_TYPES
    }

    /**
     * Returns true if [variableName] is known to be a List type (not Set/Map).
     * Useful for rules that only apply to ordered, shift-on-remove collections.
     */
    public fun isList(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        return type.simpleName in LIST_TYPES
    }

    public companion object {
        private val STRING_TYPES =
            setOf(
                "String",
                "StringBuilder",
                "StringBuffer",
                "CharSequence",
            )

        private val LIST_TYPES =
            setOf(
                "List",
                "ArrayList",
                "LinkedList",
                "Vector",
                "Stack",
                "CopyOnWriteArrayList",
                "MutableList",
                "Array",
            )

        private val TERMINAL_OPS =
            setOf(
                "collect",
                "toList",
                "toSet",
                "toMap",
                "toMutableList",
                "toMutableSet",
                "toMutableMap",
                "toSortedSet",
                "toSortedMap",
                "toHashSet",
                "toArray",
                "toUnmodifiableList",
                "toUnmodifiableSet",
                "toUnmodifiableMap",
            )

        private val LIST_TERMINALS = setOf("toList", "toMutableList", "toUnmodifiableList")

        /**
         * Builds a [TypeEnvironment] for [fn] using the given field types as a base layer.
         * Variables assigned more than once get type UNKNOWN (reassignment guard).
         */
        public fun build(
            fn: FunctionDecl,
            fieldTypes: Map<String, String>,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): TypeEnvironment {
            val typeMap = mutableMapOf<String, InferredType>()

            // Layer 1: field types (lowest priority, will be overridden by params/locals)
            for ((name, type) in fieldTypes) {
                typeMap[name] = InferredType(type, TypeSource.DECLARED)
            }

            // Layer 2: parameter types
            for (param in fn.parameters) {
                if (param.typeName != null) {
                    typeMap[param.name] = InferredType(param.typeName, TypeSource.DECLARED)
                }
            }

            // Count variable assignments for reassignment guard
            val assignmentCounts = mutableMapOf<String, Int>()
            for (varDecl in fn.findDescendants<VariableDecl>()) {
                assignmentCounts.merge(varDecl.name, 1, Int::plus)
            }

            // Layer 3: local variable types (highest priority)
            // Skip reassigned variables — type is ambiguous when assigned more than once
            fn
                .findDescendants<VariableDecl>()
                .filter { (assignmentCounts[it.name] ?: 0) <= 1 }
                .forEach { varDecl ->
                    val inferred = inferVariableType(varDecl, language, registry)
                    if (inferred != null) typeMap[varDecl.name] = inferred
                }

            return TypeEnvironment(typeMap, registry, language)
        }

        private fun inferVariableType(
            varDecl: VariableDecl,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): InferredType? {
            // Explicit type annotation
            if (varDecl.typeName != null) {
                return InferredType(varDecl.typeName, TypeSource.DECLARED)
            }

            // Constructor: new HashMap(), HashMap()
            val construction = varDecl.children.filterIsInstance<ObjectCreation>().firstOrNull()
            if (construction != null) {
                return InferredType(construction.typeName, TypeSource.CONSTRUCTOR)
            }

            // O(1) factory method: Set.of(), setOf(), ConcurrentHashMap.newKeySet()
            val factoryType = inferO1Factory(varDecl, language, registry)
            if (factoryType != null) return factoryType

            // Chain-end inference: .collect(toSet()), .toList()
            val chainType = inferChainEnd(varDecl, language, registry)
            if (chainType != null) return chainType

            // Method name suffix heuristic (lowest trust)
            return inferFromMethodNameSuffix(varDecl)
        }

        private fun inferO1Factory(
            varDecl: VariableDecl,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): InferredType? {
            val call = varDecl.children.filterIsInstance<FunctionCall>().firstOrNull() ?: return null
            val target = call.qualifiedTarget
            if (target != null && registry.isO1Type(target)) {
                return InferredType(target, TypeSource.FACTORY)
            }
            if (registry.isO1Factory(language, call.name)) {
                return InferredType("Set", TypeSource.FACTORY)
            }
            return null
        }

        @Suppress("ReturnCount")
        private fun inferChainEnd(
            varDecl: VariableDecl,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): InferredType? {
            val allCalls = varDecl.findDescendants<FunctionCall>()
            val terminal = allCalls.lastOrNull { it.name in TERMINAL_OPS } ?: return null

            if (registry.isO1Factory(language, terminal.name)) {
                return InferredType("Set", TypeSource.CHAIN_END)
            }
            if (terminal.name in LIST_TERMINALS) {
                return InferredType("List", TypeSource.CHAIN_END)
            }
            if (terminal.name == "toArray") {
                return InferredType("Array", TypeSource.CHAIN_END)
            }
            if (terminal.name == "collect" && terminal.arguments.isNotEmpty()) {
                val collector = terminal.arguments.filterIsInstance<FunctionCall>().firstOrNull()
                if (collector != null) {
                    val collectorType = inferCollectorType(collector)
                    if (collectorType != null) return InferredType(collectorType, TypeSource.CHAIN_END)
                }
            }
            return null
        }

        private fun inferCollectorType(collector: FunctionCall): String? =
            when (collector.name) {
                "toSet", "toUnmodifiableSet" -> "Set"
                "toList", "toUnmodifiableList" -> "List"
                "toMap", "toUnmodifiableMap", "toConcurrentMap" -> "Map"
                "groupingBy", "partitioningBy" -> "Map"
                else -> null
            }

        @Suppress("CyclomaticComplexMethod")
        private fun inferFromMethodNameSuffix(varDecl: VariableDecl): InferredType? {
            val call =
                varDecl.children.filterIsInstance<FunctionCall>().lastOrNull()
                    ?: varDecl.findDescendants<FunctionCall>().lastOrNull()
                    ?: return null
            val name = call.name
            val minLen = "getX".length
            val typeName =
                when {
                    name.endsWith("Stream") || name.endsWith("stream") -> "Stream"
                    name.endsWith("Set") && name.length > minLen && name[0].isLowerCase() -> "Set"
                    name.endsWith("Map") && name.length > minLen && name[0].isLowerCase() -> "Map"
                    name.endsWith("List") && name.length > minLen && name[0].isLowerCase() -> "List"
                    else -> null
                } ?: return null
            return InferredType(typeName, TypeSource.NAME_HEURISTIC)
        }
    }
}
