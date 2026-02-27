# Architecture

## Module Dependency Graph

```
cli ──→ core
   ├──→ lang-java
   ├──→ lang-groovy ──→ lang-java
   ├──→ lang-kotlin ──→ lang-java
   ├──→ lang-javascript
   └──→ reporting ──→ core
```

All language modules and reporting depend on `core`. Language modules are independent of each other (except Groovy and Kotlin reusing Java's ANTLR grammar).

## Core Module

The `core` module defines all abstractions and contains no language-specific code:

- **`model/`** — IR node types (`IRNode`, `LoopNode`, `LookupCall`, etc.), `SourceLocation`, `Language`, `Severity`
- **`rules/`** — `Rule` interface, `Finding`, `Evidence`, `AnalysisContext`, built-in rule implementations
- **`engine/`** — `AnalysisEngine` (4-pass orchestrator), `SuppressionFilter`, `LanguageParser` interface
- **`graph/`** — `SymbolTable`, `CallGraph`
- **`config/`** — `AnalysisConfig` data class
- **`cache/`** — `AnalysisCache` for incremental analysis
- **`baseline/`** — `Baseline` for diff mode

## Four-Pass Engine

1. **Parse & Collect** — Each file is parsed into IR by its language parser. Symbols are registered in the SymbolTable.
2. **Call Graph Construction** — Function calls are resolved to declarations.
3. **Complexity Annotation** — Execution contexts are propagated through the call graph.
4. **Rule Evaluation** — Rules traverse IR trees and produce findings.

## Extension Points

- **New language**: Implement `LanguageParser` interface, register in CLI
- **New rule**: Implement `Rule` interface or use the Kotlin Script DSL
- **New reporter**: Implement the `Reporter` interface
