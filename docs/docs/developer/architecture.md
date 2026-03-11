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

After rule evaluation, two post-processing steps run before findings reach the user:

- **Subsumption** — When two rules fire at the same source location and one is a more specific version of the other, the less specific finding is dropped. See [Rule Subsumption](#rule-subsumption) below.
- **Suppression** — Findings with `// algorilla:ignore` comments are removed.

## Rule Subsumption

Some rules detect overlapping patterns. For example, `nested-lookup` and `expensive-callback` can both fire on `list.filter(x -> other.contains(x.id))` — one from the "linear search inside a loop" angle, the other from the "expensive operation inside a callback" angle. Reporting both is noise.

The subsumption model lets rules declare which other rules they make redundant:

```kotlin
class NestedLookupRule : Rule {
    override val subsumes = setOf("expensive-callback")
    // ...
}
```

The engine groups findings by source location (file + line). Within each group, if rule A fired and rule A declares `subsumes = setOf("B")`, any finding from rule B in that group is dropped. Rule B's findings at other locations (where A did not fire) are unaffected.

Current subsumption declarations:

| Dominant rule | Subsumes |
|--------------|----------|
| `nested-lookup` | `expensive-callback` |
| `redundant-expensive-call` | `uncached-getter` |

When adding a new rule, check whether it overlaps with existing rules and add a `subsumes` declaration if appropriate. See [Adding Rules](adding-rules.md#subsumption).

## Extension Points

- **New language**: Implement `LanguageParser` interface, register in CLI
- **New rule**: Implement `Rule` interface or use the Kotlin Script DSL
- **New reporter**: Implement the `Reporter` interface
