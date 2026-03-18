package com.github.tvinke.algorilla.semantics

/**
 * Single carrier for all external type data passed into [TypeEnvironment.build].
 * Each inference level enriches this context rather than adding parameters to build().
 */
public data class TypeContext(
    /** Class-level field types: fieldName → typeName. */
    val fieldTypes: Map<String, String> = emptyMap(),
    /** Same-file method return types: methodName → returnType. */
    val localMethodReturnTypes: Map<String, String> = emptyMap(),
    /** Cross-file method return types: className.methodName → returnType (L1b). */
    val globalMethodReturnTypes: Map<String, String> = emptyMap(),
    /** Class → set of supertypes (implements/extends), generics stripped (L3). */
    val classHierarchy: Map<String, Set<String>> = emptyMap(),
    /** Variable → narrowed type within a branch scope (L5 flow typing). */
    val branchNarrowings: Map<String, String> = emptyMap(),
) {
    /** Returns a copy with additional branch narrowings merged in. */
    public fun withNarrowings(narrowings: Map<String, String>): TypeContext = copy(branchNarrowings = branchNarrowings + narrowings)
}
