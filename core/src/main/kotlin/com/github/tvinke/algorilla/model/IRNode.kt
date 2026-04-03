package com.github.tvinke.algorilla.model

/**
 * Unified Intermediate Representation node. All language parsers produce trees of [IRNode],
 * making the rule engine language-agnostic.
 */
public sealed interface IRNode {
    /** Location in the original source file. */
    public val location: SourceLocation

    /** Child nodes in the IR tree. */
    public val children: List<IRNode>
}

/** The kind of loop construct. */
public enum class LoopKind {
    FOR,
    WHILE,
    FOR_EACH,
    STREAM_FOR_EACH,
    PARALLEL_STREAM_FOR_EACH,
    HIGHER_ORDER,
}

/**
 * A loop construct in the source code, such as `for`, `while`, `.forEach()`, or `.each{}`.
 */
public data class LoopNode(
    val kind: LoopKind,
    val iteratedVariable: String?,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode {
    /** Populated by LoopBoundAnnotator — true when the loop iterates a constant-size collection (enum, config list, literal). */
    var isConstantBound: Boolean = false

    /** Populated by LoopBoundAnnotator — true when every code path through the loop body exits (throw/break/return). */
    var isSingleIteration: Boolean = false
}

/** The kind of lookup operation on a collection. */
public enum class LookupKind(
    public val label: String,
) {
    FIND("find"),
    FILTER("filter"),
    CONTAINS("contains"),
    SOME("some"),
    ANY("any"),
    INCLUDES("includes"),
    ANY_MATCH("anyMatch"),
    ALL_MATCH("allMatch"),
    NONE_MATCH("noneMatch"),
    COUNT("count"),
    INDEX_OF("indexOf"),
    ;

    /** Returns true if this lookup kind also exists as a String method (indexOf, contains, includes). */
    public fun isStringApplicable(): Boolean = this == INDEX_OF || this == CONTAINS || this == INCLUDES

    /** Returns the suggested data structure for replacing this linear lookup with an O(1) alternative. */
    public fun suggestedStructure(): String =
        when (this) {
            CONTAINS, INCLUDES, SOME, ANY, ANY_MATCH, ALL_MATCH, NONE_MATCH -> "HashSet"
            FIND -> "Map (via associateBy/groupBy)"
            FILTER -> "Map (via groupBy)"
            INDEX_OF -> "Map (element → index)"
            COUNT -> "Map (via groupingBy/eachCount)"
        }
}

/**
 * A linear lookup call on a collection, such as `list.contains()` or `stream().anyMatch()`.
 */
public data class LookupCall(
    val kind: LookupKind,
    val targetVariable: String?,
    val isO1: Boolean,
    override val location: SourceLocation,
    override val children: List<IRNode>,
    val isScalar: Boolean = false,
) : IRNode

/** The kind of sort operation. */
public enum class SortKind(
    public val label: String,
) {
    SORT("sort"),
    SORTED("sorted"),
    ORDER_BY("orderBy"),
    SORT_BY("sortBy"),
}

/**
 * A sort call on a collection, such as `Collections.sort()` or `stream().sorted()`.
 */
public data class SortCall(
    val kind: SortKind,
    val hasComparator: Boolean,
    val comparatorBody: List<IRNode>?,
    override val location: SourceLocation,
    override val children: List<IRNode>,
    val qualifiedTarget: String? = null,
) : IRNode

/**
 * Object creation expression, such as `new Date()` or `ObjectMapper()`.
 */
public data class ObjectCreation(
    val typeName: String,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/** The kind of indexed or positional access on a collection. */
public enum class AccessKind(
    public val label: String,
) {
    FIRST("first"),
    LAST("last"),
    INDEX_ZERO("get(0)"),
    POP("pop"),
    GET_SIZE_MINUS_1("get(size-1)"),
    FIND_FIRST("findFirst"),
    FIND_ANY("findAny"),
}

/**
 * Positional access on a collection, such as `.first()`, `.get(0)`, or `.findFirst()`.
 */
public data class CollectionAccess(
    val kind: AccessKind,
    override val location: SourceLocation,
    override val children: List<IRNode>,
    val qualifiedTarget: String? = null,
) : IRNode

/**
 * A function/method declaration in the source code.
 */
public data class FunctionDecl(
    val name: String,
    val qualifiedName: String,
    val parameters: List<Parameter>,
    val isConstructor: Boolean = false,
    val declaringClass: String? = null,
    /** Declared return type, e.g. "List", "Map", "void". Null when unresolvable (JS, Groovy dynamic). */
    val returnType: String? = null,
    var estimatedComplexity: Complexity? = null,
    var executionContext: ExecutionContext = ExecutionContext.SINGLE,
    var parameterFlows: List<ParameterFlow> = emptyList(),
    var isRecursive: Boolean = false,
    /** Architectural path context: REQUEST, LIFECYCLE, BATCH, MIXED, or null (unknown). */
    var pathContext: PathContext? = null,
    /** Simple annotation names on this method, e.g. ["PostConstruct", "Bean"]. */
    val annotations: List<String> = emptyList(),
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * A function/method parameter.
 */
public data class Parameter(
    val name: String,
    val typeName: String?,
)

/**
 * A function/method call expression.
 */
public data class FunctionCall(
    val name: String,
    val qualifiedTarget: String?,
    val arguments: List<IRNode> = emptyList(),
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * A variable declaration with optional initializer.
 */
public data class VariableDecl(
    val name: String,
    val typeName: String?,
    val initializer: IRNode? = null,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * Represents mutually exclusive code branches, such as if/else or switch/case.
 * Calls in different branches cannot co-execute in a single invocation.
 */
public data class BranchNode(
    val branches: List<List<IRNode>>,
    override val location: SourceLocation,
) : IRNode {
    override val children: List<IRNode> get() = branches.flatten()
}

/** The kind of control flow exit. */
public enum class ExitKind {
    THROW,
    BREAK,
    RETURN,
    CONTINUE,
}

/**
 * An abrupt control flow exit: `throw`, `break`, `return`, or `continue`.
 * Used by [com.github.tvinke.algorilla.graph.LoopBoundAnnotator] to detect loops
 * that exit after at most one iteration.
 */
public data class ControlFlowExit(
    val kind: ExitKind,
    override val location: SourceLocation,
    override val children: List<IRNode> = emptyList(),
) : IRNode

/**
 * A generic statement or expression node that does not map to a specific IR category.
 */
public data class GenericNode(
    val nodeType: String,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * A class/interface/object declaration. Wraps the class body and captures
 * the supertype list for hierarchy resolution (L3).
 */
public data class ClassNode(
    val name: String,
    /** Supertypes (implements/extends), generics stripped. */
    val supertypes: List<String> = emptyList(),
    /** Simple annotation names on this class, e.g. ["Component", "ApplicationScoped"]. */
    val annotations: List<String> = emptyList(),
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * A type check expression, e.g. `x instanceof Set` (Java) or `x is Set` (Kotlin).
 * Used by flow typing (L5) to narrow variable types within branch scopes.
 */
public data class TypeCheck(
    val variableName: String,
    val checkedType: String,
    override val location: SourceLocation,
) : IRNode {
    override val children: List<IRNode> get() = emptyList()
}

/**
 * The root node of an IR tree for a single source file.
 */
public data class FileRoot(
    val filePath: String,
    val language: Language,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode

/**
 * Estimated algorithmic complexity of a function or operation.
 */
public data class Complexity(
    val notation: String,
    val description: String = "",
)

/**
 * Execution context label indicating how a node is reached at runtime.
 */
public enum class ExecutionContext {
    /** Executed once at top level. */
    SINGLE,

    /** Directly inside a loop body. */
    INSIDE_LOOP,

    /** Called from a function that is itself called from inside a loop. */
    INSIDE_REPEATED_CALL_FROM_LOOP,
}
