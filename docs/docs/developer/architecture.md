# Architecture

## Module Dependency Graph

```
cli ──→ core
   ├──→ lang-java ──→ core
   ├──→ lang-groovy ──→ core
   ├──→ lang-kotlin ──→ core
   ├──→ lang-javascript ──→ core
   └──→ reporting ──→ core

gradle-plugin ──→ core
              ├──→ lang-java
              ├──→ lang-groovy
              ├──→ lang-kotlin
              └──→ lang-javascript
```

All language modules depend on `core` only. Language modules are independent of each other. The shared ANTLR Java grammar and parsing utilities (`BlockAnalysis`, `MethodClassification`) live in `core` so that `lang-groovy` and `lang-kotlin` can use them as fallback parsers without depending on `lang-java`.

## Core Module

The `core` module defines all abstractions and shared utilities. It contains no language-specific parsing logic.

- **`model/`** — IR node types (`IRNode`, `LoopNode`, `LookupCall`, `SortCall`, `FunctionDecl`, etc.), `SourceLocation`, `Language`, `Severity`, `ExecutionContext`
- **`rules/`** — `Rule` interface, `Finding`, `Evidence`, `AnalysisContext`, all built-in rule implementations
- **`engine/`** — `AnalysisEngine` (four-pass orchestrator), `LanguageParser` interface, `ParserRegistry`, `SuppressionFilter`
- **`semantics/`** — `LanguageSemanticsRegistry` (loads and merges language + framework YAML files)
- **`graph/`** — `SymbolTable`, `CallGraph`
- **`config/`** — `AnalysisConfig` data class
- **`cache/`** — `AnalysisCache` for incremental analysis
- **`baseline/`** — `Baseline` for diff mode
- **`lang/java/parser/`** — `BlockAnalysis` and `MethodClassification` utilities, shared by all ANTLR-based language modules

## ParserRegistry

`ParserRegistry` is a singleton object in `core/engine/`. Each language module registers its parser once from its parser class's `companion object init {}` block. The CLI and engine retrieve parsers from the registry — there is no hardcoded list of parsers in the engine itself.

Registration happens lazily: Kotlin only runs `companion object init` when the class is first loaded. The CLI touches each companion object explicitly to trigger loading before starting analysis.

## Four-Pass Engine

1. **Parse & Collect** — Each file is parsed into an IR tree by its language parser. Symbols are registered in `SymbolTable`.
2. **Call Graph Construction** — Function calls are resolved to their declarations, building the `CallGraph`.
3. **Complexity Annotation** — Execution contexts (`LOOP`, `CALLBACK`, `SORT_COMPARATOR`) are propagated through the call graph to all reachable call sites.
4. **Rule Evaluation** — Each rule traverses the IR trees and produces `Finding` objects.

After rule evaluation, two post-processing steps run before findings reach the user:

- **Subsumption** — When two rules fire at the same source location and one is a more specific version of the other, the less specific finding is dropped.
- **Suppression** — Findings on lines with `// algorilla:ignore` comments are removed.

## Rule Subsumption

Some rules detect overlapping patterns. For example, `nested-lookup` and `expensive-callback` can both fire on `list.filter(x -> other.contains(x.id))` — one from the "linear search inside a loop" angle, the other from the "expensive operation inside a callback" angle. Reporting both is noise.

The subsumption model lets rules declare which other rules they make redundant via the `subsumes` field on the `Rule` interface (see `NestedLookupRule.kt` for an example).

The engine groups findings by source location (file + line). Within each group, if rule A fired and A declares `subsumes = setOf("B")`, any finding from rule B in that group is dropped. Rule B's findings at other locations are unaffected.

Current subsumption declarations:

| Dominant rule | Subsumes |
|--------------|----------|
| `nested-lookup` | `expensive-callback` |
| `redundant-expensive-call` | `uncached-getter` |

When adding a new rule, check whether it overlaps with existing rules and add a `subsumes` declaration if appropriate. See [Adding Rules](adding-rules.md#subsumption).

## Semantics and Framework Overlays

Language semantics live in YAML files under `core/src/main/resources/semantics/`. Each file defines method categories (expensive, cheap, stream operations, etc.) for one language.

Framework overlays follow the same YAML format and live under `semantics/frameworks/`. They are discovered automatically at runtime from `semantics/frameworks-index.txt` — adding a new framework only requires creating the YAML file and appending its filename to the index. Each framework YAML must include a `language:` key so the registry knows which language it extends.

`AnalysisCache` computes a version hash from all semantics and framework files. When any YAML changes, the hash changes and all cached results are invalidated automatically.

## Extension Points

- **New language** — Implement `LanguageParser`, self-register via `companion object init`, add a semantics YAML, trigger class loading from the CLI. See [Adding Languages](adding-languages.md).
- **New rule** — Implement `Rule`, register in `BuiltinRules.all()`. See [Adding Rules](adding-rules.md).
- **New framework overlay** — Add a YAML file under `semantics/frameworks/` and append its name to `frameworks-index.txt`. No code changes required.
- **New reporter** — Implement the `Reporter` interface in the `reporting` module.
