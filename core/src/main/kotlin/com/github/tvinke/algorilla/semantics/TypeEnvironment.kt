package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.InferredType
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.TypeSource
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants

private val fallbackStringTypes = setOf("String", "StringBuilder", "StringBuffer", "CharSequence")

private val fallbackListTypes = setOf("List", "ArrayList", "LinkedList", "Vector", "Stack", "CopyOnWriteArrayList", "MutableList", "Array")

private val terminalOps =
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

private val listTerminals = setOf("toList", "toMutableList", "toUnmodifiableList")

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
    private val classHierarchy: Map<String, Set<String>> = emptyMap(),
    private val boundedSmallCollections: Set<String> = emptySet(),
) {
    /** Returns the inferred type for [variableName], or null if unknown. */
    public fun typeOf(variableName: String): InferredType? = types[variableName.removePrefix("this.")]

    /**
     * Returns true if [variableName] is known to be an O(1) lookup type (Set, Map, etc.).
     * Only high-trust sources are considered — [TypeSource.NAME_HEURISTIC] is excluded
     * because e.g. `getCharacterSet()` returning "Set" would suppress real List findings.
     * Also resolves through the class hierarchy (L3): if the type itself isn't O(1),
     * checks whether any of its supertypes are.
     */
    public fun isO1(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        if (type.source == TypeSource.NAME_HEURISTIC) return false
        if (registry.isO1Type(language, type.simpleName)) return true
        return resolvesViaHierarchy(type.simpleName) { registry.isO1Type(language, it) }
    }

    /**
     * Returns true if [variableName] is known to be a collection type (List, Set, Map, etc.).
     * Also resolves through the class hierarchy (L3).
     */
    public fun isCollection(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        if (registry.isCollectionType(language, type.simpleName)) return true
        return resolvesViaHierarchy(type.simpleName) { registry.isCollectionType(language, it) }
    }

    /** Returns true if [variableName] is initialized from a small constant-size collection factory. */
    public fun isBoundedSmallCollection(variableName: String): Boolean = variableName.removePrefix("this.") in boundedSmallCollections

    /**
     * Walks the class hierarchy transitively to check if any supertype satisfies [predicate].
     * Protects against cycles with a visited set.
     */
    private fun resolvesViaHierarchy(
        typeName: String,
        predicate: (String) -> Boolean,
    ): Boolean {
        if (classHierarchy.isEmpty()) return false
        val visited = mutableSetOf(typeName)
        val queue = ArrayDeque(classHierarchy[typeName] ?: return false)
        while (queue.isNotEmpty()) {
            val supertype = queue.removeFirst()
            if (!visited.add(supertype)) continue
            if (predicate(supertype)) return true
            classHierarchy[supertype]?.let { queue.addAll(it) }
        }
        return false
    }

    /** Returns true if [variableName] is known to be a String-like type. */
    public fun isString(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        val yamlStringTypes = registry.extraSection(language, "string-types")
        return if (yamlStringTypes.isNotEmpty()) {
            type.simpleName in yamlStringTypes
        } else {
            type.simpleName in fallbackStringTypes
        }
    }

    /**
     * Returns true if [variableName] is known to be a List type (not Set/Map).
     * Useful for rules that only apply to ordered, shift-on-remove collections.
     */
    public fun isList(variableName: String): Boolean {
        val type = typeOf(variableName) ?: return false
        val yamlListTypes = registry.extraSection(language, "list-types")
        return if (yamlListTypes.isNotEmpty()) {
            type.simpleName in yamlListTypes
        } else {
            type.simpleName in fallbackListTypes
        }
    }

    @Suppress("TooManyFunctions") // Type inference strategies: each handles a distinct source
    public companion object {
        /**
         * Builds a [TypeEnvironment] for [fn] using the given [context] as the source of
         * external type knowledge. Variables assigned more than once get type UNKNOWN (reassignment guard).
         */
        public fun build(
            fn: FunctionDecl,
            context: TypeContext,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): TypeEnvironment {
            val typeMap = mutableMapOf<String, InferredType>()
            val boundedSmallCollectionVars = mutableSetOf<String>()
            for ((name, type) in context.fieldTypes) {
                typeMap[name] = inferredTypeFromAnnotation(type)
            }
            for (param in fn.parameters) {
                if (param.typeName != null) {
                    typeMap[param.name] = inferredTypeFromAnnotation(param.typeName)
                }
            }
            collectLocalVariableTypes(fn, context, language, registry, typeMap, boundedSmallCollectionVars)
            for ((name, type) in context.branchNarrowings) {
                typeMap[name] = InferredType(stripGenerics(type), TypeSource.DECLARED, fullTypeName = type)
            }
            return TypeEnvironment(typeMap, registry, language, context.classHierarchy, boundedSmallCollectionVars)
        }

        /**
         * Legacy overload for backward compatibility with existing callers.
         * Constructs a [TypeContext] from the individual parameters and delegates.
         */
        public fun build(
            fn: FunctionDecl,
            fieldTypes: Map<String, String>,
            language: Language,
            registry: LanguageSemanticsRegistry,
            methodReturnTypes: Map<String, String> = emptyMap(),
        ): TypeEnvironment =
            build(
                fn,
                TypeContext(fieldTypes = fieldTypes, localMethodReturnTypes = methodReturnTypes),
                language,
                registry,
            )

        // Each return handles a distinct inference strategy (declared, constructor, factory, chain, return type, cross-file, heuristic)
        @Suppress("ReturnCount")
        private fun inferVariableType(
            varDecl: VariableDecl,
            language: Language,
            registry: LanguageSemanticsRegistry,
            context: TypeContext,
        ): InferredType? {
            // Explicit type annotation
            if (varDecl.typeName != null) {
                return inferredTypeFromAnnotation(varDecl.typeName)
            }

            // Constructor: new HashMap(), HashMap()
            val construction = varDecl.children.filterIsInstance<ObjectCreation>().firstOrNull()
            if (construction != null) {
                val full = if (construction.typeName.contains('<')) construction.typeName else null
                return InferredType(stripGenerics(construction.typeName), TypeSource.CONSTRUCTOR, fullTypeName = full)
            }

            // O(1) factory method: Set.of(), setOf(), ConcurrentHashMap.newKeySet()
            val factoryType = inferO1Factory(varDecl, language, registry)
            if (factoryType != null) return factoryType

            // Chain-end inference: .collect(toSet()), .toList()
            val chainType = inferChainEnd(varDecl, language, registry)
            if (chainType != null) return chainType

            // Same-file method return type: var x = getAllOrders() → List
            val returnType = inferFromMethodReturnType(varDecl, context.localMethodReturnTypes)
            if (returnType != null) return returnType

            // Cross-file method return type (L1b): var orders = userService.getAllOrders()
            val crossFileType = inferFromCrossFileReturnType(varDecl, context.globalMethodReturnTypes)
            if (crossFileType != null) return crossFileType

            // Method name suffix heuristic (lowest trust)
            return inferFromMethodNameSuffix(varDecl)
        }

        private fun isBoundedSmallCollection(varDecl: VariableDecl): Boolean {
            val init = varDecl.initializer as? FunctionCall ?: return false
            return SmallFactoryCollectionSemantics.isConstantSizeFactory(init)
        }

        private fun collectLocalVariableTypes(
            fn: FunctionDecl,
            context: TypeContext,
            language: Language,
            registry: LanguageSemanticsRegistry,
            typeMap: MutableMap<String, InferredType>,
            boundedSmallCollectionVars: MutableSet<String>,
        ) {
            val assignmentCounts = mutableMapOf<String, Int>()
            for (varDecl in fn.findDescendants<VariableDecl>()) {
                assignmentCounts.merge(varDecl.name, 1, Int::plus)
            }
            fn
                .findDescendants<VariableDecl>()
                .filter { (assignmentCounts[it.name] ?: 0) <= 1 }
                .forEach { varDecl ->
                    val inferred = inferVariableType(varDecl, language, registry, context)
                    if (inferred != null) {
                        typeMap[varDecl.name] = inferred
                        if (registry.isCollectionType(language, inferred.simpleName) && isBoundedSmallCollection(varDecl)) {
                            boundedSmallCollectionVars += varDecl.name
                        }
                    }
                }
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

        @Suppress("ReturnCount") // Each return handles a distinct terminal op type (toList, toSet, toArray, collect)
        private fun inferChainEnd(
            varDecl: VariableDecl,
            language: Language,
            registry: LanguageSemanticsRegistry,
        ): InferredType? {
            val allCalls = varDecl.findDescendants<FunctionCall>()
            val terminal = allCalls.lastOrNull { it.name in terminalOps } ?: return null

            if (registry.isO1Factory(language, terminal.name)) {
                return InferredType("Set", TypeSource.CHAIN_END)
            }
            if (terminal.name in listTerminals) {
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

        /**
         * Infers type from same-file method return types. When `var x = getAllOrders()` is
         * assigned from a method whose return type is known (e.g. `List<Order> getAllOrders()`),
         * we can resolve x as `List`. Higher trust than NAME_HEURISTIC since it comes from
         * an actual declaration.
         */
        private fun inferFromMethodReturnType(
            varDecl: VariableDecl,
            methodReturnTypes: Map<String, String>,
        ): InferredType? {
            if (methodReturnTypes.isEmpty()) return null
            val call = varDecl.children.filterIsInstance<FunctionCall>().firstOrNull() ?: return null
            val returnType = methodReturnTypes[call.name] ?: return null
            if (returnType == "void") return null
            return InferredType(returnType, TypeSource.FACTORY)
        }

        /**
         * Infers type from cross-file method return types (L1b). Tries qualified match
         * (className.methodName) first, then falls back to unambiguous simple-name match.
         */
        private fun inferFromCrossFileReturnType(
            varDecl: VariableDecl,
            globalMethodReturnTypes: Map<String, String>,
        ): InferredType? {
            if (globalMethodReturnTypes.isEmpty()) return null
            val call = varDecl.children.filterIsInstance<FunctionCall>().firstOrNull() ?: return null
            // Try qualified: target.methodName
            if (call.qualifiedTarget != null) {
                val qualifiedKey = "${call.qualifiedTarget}.${call.name}"
                val returnType = globalMethodReturnTypes[qualifiedKey]
                if (returnType != null && returnType != "void") {
                    return InferredType(returnType, TypeSource.CROSS_FILE_RETURN)
                }
            }
            // Fallback: unambiguous simple name match — only if exactly one entry ends with .methodName
            val suffix = ".${call.name}"
            val matches = globalMethodReturnTypes.entries.filter { it.key.endsWith(suffix) }
            if (matches.size == 1) {
                val returnType = matches[0].value
                if (returnType != "void") {
                    return InferredType(returnType, TypeSource.CROSS_FILE_RETURN)
                }
            }
            return null
        }

        @Suppress("CyclomaticComplexMethod") // When/else dispatch over suffix patterns — each branch maps to a distinct type
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

        /** Creates an InferredType from a raw type annotation, preserving generics as fullTypeName. */
        private fun inferredTypeFromAnnotation(rawType: String): InferredType {
            val full = if (rawType.contains('<')) rawType else null
            return InferredType(stripGenerics(rawType), TypeSource.DECLARED, fullTypeName = full)
        }

        /** Strips generic parameters: "List<Order>" → "List", "Map<String,Integer>" → "Map". */
        private fun stripGenerics(type: String): String = type.substringBefore('<')
    }
}
