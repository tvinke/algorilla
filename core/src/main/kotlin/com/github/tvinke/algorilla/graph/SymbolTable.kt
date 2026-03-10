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
     * Returns all registered function declarations.
     */
    public fun allDeclarations(): List<FunctionDecl> = symbols.values.flatten()

    /**
     * Returns the number of registered symbols.
     */
    public fun size(): Int = symbols.size
}
