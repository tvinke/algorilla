package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FunctionDecl

/**
 * Project-wide symbol table mapping qualified function names to their declarations.
 * Used during call graph construction to resolve function calls across files and languages.
 */
public class SymbolTable {
    private val symbols: MutableMap<String, MutableList<FunctionDecl>> = mutableMapOf()
    private val bySimpleName: MutableMap<String, MutableList<FunctionDecl>> = mutableMapOf()
    private val byClassAndName: MutableMap<String, MutableList<FunctionDecl>> = mutableMapOf()
    private val typeMap: MutableMap<String, String> = mutableMapOf()

    /**
     * Registers a function declaration under its qualified name, simple name,
     * and class-qualified index (when declaringClass is known).
     */
    public fun register(decl: FunctionDecl) {
        symbols.getOrPut(decl.qualifiedName) { mutableListOf() }.add(decl)
        bySimpleName.getOrPut(decl.name) { mutableListOf() }.add(decl)
        if (decl.declaringClass != null) {
            val key = "${decl.declaringClass}.${decl.name}"
            byClassAndName.getOrPut(key) { mutableListOf() }.add(decl)
        }
    }

    /**
     * Looks up function declarations by qualified name.
     */
    public fun lookup(qualifiedName: String): List<FunctionDecl> = symbols[qualifiedName] ?: emptyList()

    /**
     * Looks up function declarations by simple name (unqualified). Returns all matches.
     */
    public fun lookupBySimpleName(name: String): List<FunctionDecl> = bySimpleName[name] ?: emptyList()

    /**
     * Looks up function declarations by declaring class and method name (e.g. "MyService.process").
     */
    public fun lookupByClassAndName(classAndName: String): List<FunctionDecl> = byClassAndName[classAndName] ?: emptyList()

    /**
     * Registers a variable-to-type mapping (e.g. "service" → "UserService").
     * Used for resolving method calls on typed variables.
     */
    public fun registerType(
        variableName: String,
        typeName: String,
    ) {
        typeMap[variableName] = typeName
    }

    /**
     * Resolves the declared type of a variable, or null if unknown.
     */
    public fun resolveType(variableName: String): String? = typeMap[variableName]

    /**
     * Returns all registered function declarations.
     */
    public fun allDeclarations(): List<FunctionDecl> = symbols.values.flatten()

    /**
     * Returns the number of registered symbols.
     */
    public fun size(): Int = symbols.size
}
