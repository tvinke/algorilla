# Coding Guidelines

Project-specific conventions for algorilla. Every guideline here prevents a real issue from this codebase or codifies a pattern unique to the project. For formatting (indentation, imports, line length), ktlint and detekt handle it — this document covers everything they don't.

For documentation standards (KDoc, README, website content), see the [Documentation Guidelines](https://tvinke.github.io/algorilla/developer/documentation-guidelines/).

## 1. Kotlin Idioms

**`val` over `var`** — mutable state is the exception, not the default. The only justified `var` fields in this codebase are annotation slots on IR nodes (e.g. `estimatedComplexity`, `executionContext`, `parameterFlows`) that get populated by graph passes after construction.

**Data classes for value types.** If a class is primarily a bag of fields with structural equality, make it a data class. IR nodes, findings, evidence entries, suggestions — all data classes.

**Sealed classes/interfaces for closed hierarchies.** When the set of variants is known at compile time and `when` expressions should be exhaustive, use `sealed`. Examples in this codebase:
- `Suggestion` — sealed class with `PreBuildStructure`, `ReorderOperations`, `UseBulkAPI`, etc.
- `FlowTarget` — sealed interface with `LoopIteration`, `FunctionArgument`, `MethodCallReceiver`
- `IRNode` — sealed interface for the entire IR tree

**Extension functions for IR queries.** Tree traversal logic belongs in `IRNodeExtensions.kt` as extension functions, not as methods on `IRNode` itself. Use inline reified generics for type-safe queries:

```kotlin
// Good — extension function with reified type parameter
public inline fun <reified T : IRNode> IRNode.findDescendants(): List<T>

// Bad — method on IRNode that takes a Class parameter
fun IRNode.findDescendants(type: Class<*>): List<IRNode>
```

**`require`/`check` over manual throws.** Use `require` for preconditions (argument validation) and `check` for state invariants. Don't write `if (x == null) throw IllegalArgumentException(...)`.

**Scope function conventions:**
- `apply` for configuring an object after construction
- `let` for null-safe chaining or mapping
- `also` for side effects (logging, debugging)
- Don't nest scope functions — if you need two levels, extract a local variable instead

## 2. YAML-First Principle

**All domain knowledge lives in YAML.** Method names, type names, semantic classifications, skip lists, target patterns, keywords — these belong in YAML files under `core/src/main/resources/semantics/`, never hardcoded in Kotlin.

The `LanguageSemanticsRegistry` is the single source of truth. It loads from YAML at startup via a lazy singleton and provides query methods like `classify()`, `isHeavyweight()`, `isO1Type()`.

**Anti-pattern — hardcoded `when` expressions:**

```kotlin
// BAD — hardcoded method names in Kotlin
fun isExpensive(method: String) = when (method) {
    "findAll", "findBy", "getAll", "fetchAll" -> true
    else -> false
}

// GOOD — query the registry
fun isExpensive(language: Language, method: String) =
    LanguageSemanticsRegistry.DEFAULT.classify(language, method)
        ?.category == MethodCategory.REPOSITORY_CALL
```

**When adding new methods or patterns:**
1. Add the entry to ALL relevant language YAML files — not just the one you're testing with
2. Framework-specific methods go in `core/src/main/resources/semantics/frameworks/` and get registered in `frameworks-index.txt`
3. Run the full build to verify YAML parsing

**Section completeness.** Every language YAML has 17 sections. When adding a new section, add it to all language files, even if some start empty. This prevents silent classification misses.

## 3. Architecture Boundaries

**Module dependency rules:**

```
lang-java   ──→  core
lang-groovy ──→  core
lang-kotlin ──→  core
lang-js     ──→  core
cli         ──→  core, lang-*, reporting
gradle-plugin ──→ core, lang-*, reporting
reporting   ──→  core
```

The critical constraint: **`core/` never imports from `lang-*`**. Rules, the engine, and the semantics registry live in `core/` and must work without knowing which language parsers exist. If you find yourself importing `com.github.tvinke.algorilla.lang.*` from a `core/` file, you're violating this boundary.

Similarly, **rules never import from `lang-*`** modules. Rules operate on the language-neutral IR. If a rule needs language-specific behavior, use `context.language` and the semantics registry, not parser internals.

**One rule per file.** Each rule class gets its own file in `core/src/main/kotlin/.../rules/builtin/`. Don't bundle related rules into one file.

**Parser tests live in `lang-*` modules.** Tests that exercise parsing (fixture → IR) belong in the language module. Tests that exercise rule logic (IR → findings) belong in `core/` or in language-specific rule test classes in `lang-*`.

## 4. The Rule Contract

Every rule implements the `Rule` interface:

```kotlin
public interface Rule {
    val id: String                    // kebab-case, e.g. "filter-after-sort"
    val name: String                  // Human-readable, e.g. "Filter After Sort"
    val severity: Severity
    val languages: Set<Language>
    val category: RuleCategory
    val defaultConfidence: Confidence // Default: MEDIUM
    val subsumes: Set<String>         // Rule IDs this rule makes redundant
    fun evaluate(context: AnalysisContext): List<Finding>
}
```

**Finding requirements.** Every `Finding` must include:

- **Evidence chain** — a `List<Evidence>` tracing the path from root cause to symptom. Each evidence entry has a location, a human-readable label, and an execution context. Don't create findings without `evidence =`.
- **Complexity** — `currentComplexity` and `suggestedComplexity` showing the before/after. Use `ComplexityModel` factory methods.
- **Suggestions** — at least one `Suggestion` subtype telling the user what to do. Be specific ("Move filter() before sort()"), not generic ("Consider optimizing").

```kotlin
Finding(
    ruleId = id,
    ruleName = name,
    severity = severity,
    location = filter.location,
    message = "filter() after sort() — sorting all elements before filtering",
    suggestions = listOf(
        Suggestion.ReorderOperations(
            first = "filter()",
            second = "sort()",
            reason = "sort fewer elements",
        ),
    ),
    currentComplexity = cx.current,
    suggestedComplexity = cx.suggested,
    evidence = buildEvidence(sort, filter),
)
```

**When `@Suppress` is justified on rule classes:** Only when detekt flags structural complexity that's inherent to the rule's pattern matching. A rule that dispatches over 15 IR node types will naturally be complex — that's fine. See §9 for the full suppress policy.

## 5. IR Model Conventions

**Immutability by default.** IR nodes are data classes with `val` fields. The sealed interface hierarchy (`IRNode`) guarantees exhaustive matching.

```kotlin
public data class LoopNode(
    val kind: LoopKind,
    val iteratedVariable: String?,
    override val location: SourceLocation,
    override val children: List<IRNode>,
) : IRNode
```

**Exception: annotation slots.** A small number of fields on `FunctionDecl` are `var` because they're populated by graph passes after construction:

```kotlin
var estimatedComplexity: Complexity? = null
var executionContext: ExecutionContext = ExecutionContext.SINGLE
var parameterFlows: List<ParameterFlow> = emptyList()
var isRecursive: Boolean = false
```

Don't add new `var` fields without a clear justification. If a value can be computed at construction time, make it `val`.

**`findDescendants<T>()`** for queries. Never manually walk `children` lists — use the extension functions in `IRNodeExtensions.kt`:

```kotlin
val loops = fileRoot.findDescendants<LoopNode>()
val lookups = loop.findDescendants<LookupCall>()
```

**`withChildren()` for structural copies.** When you need to replace children in an IR node, use the `withChildren()` extension (exhaustive `when` over all node types) rather than manual `copy()` calls.

**Adding new IR node types.** When you add a new `IRNode` subclass:
1. Add it as a `data class` implementing `IRNode`
2. Update `withChildren()` in `IRNodeExtensions.kt` — the compiler will remind you via exhaustive `when`
3. Update `referencesName()` if the node has identifying fields
4. Consider whether `findDescendantsWithBranchContext()` needs special handling

## 6. Test Patterns

**Fixture-based rule testing.** Tests load source files from `fixtures/` directories on the classpath, parse them, and run the rule:

```kotlin
private fun analyzeFixture(fixturePath: String): List<Finding> {
    val url = javaClass.classLoader.getResource("fixtures/$fixturePath")
        ?: error("Fixture not found: $fixturePath")
    val path = File(url.toURI()).absolutePath
    val fileRoot = parser.parse(path)
    val context = AnalysisContext(
        irTrees = mapOf(path to fileRoot),
        symbolTable = SymbolTable(),
        callGraph = CallGraph(),
        config = AnalysisConfig(),
    )
    return rule.evaluate(context)
}
```

**One test class per rule per language.** A rule like `filter-after-sort` has:
- `FilterAfterSortRuleJavaTest` in `lang-java/`
- `FilterAfterSortRuleGroovyTest` in `lang-groovy/`
- `FilterAfterSortRuleKotlinTest` in `lang-kotlin/`
- etc.

**Three categories of fixtures per rule:**
- **Positive** — code that should trigger the rule (in `fixtures/<rule-id>/positive/`)
- **Negative** — code that should NOT trigger the rule (in `fixtures/<rule-id>/negative/`)
- **Regression** — specific cases from bug reports or false positive reports

**Kotest matchers.** Use `shouldHaveSize`, `shouldBe`, `shouldContain` from kotest for assertions. Keep assertions focused — check the finding count and key fields, not every field on every object.

**Contract tests (`@Tag("contract")`).** Some tests guard the public API surface that external consumers depend on — CLI flags, JSON schema fields, SARIF structure, exit codes, Gradle plugin properties, and GitHub Action inputs. These are tagged with `@Tag("contract")` (or `@ContractTest` from `core/src/test` if the module has it on the classpath).

When a contract test fails, it means something user-visible changed. Before fixing the test:

1. Check which distributions are affected (CLI, Gradle plugin, GitHub Action, npm, Docker)
2. Update all affected configs — `action.yml`, `AlgorillaExtension.kt`, docs, npm wrapper
3. Update any output examples in the documentation that reference the changed behavior

Run contract tests in isolation with `./gradlew test -Dinclude.tags=contract`. This is useful as a quick sanity check before releases. In normal development, contract tests run as part of the regular suite — no special workflow needed.

Where to put new contract tests:

- CLI flag existence and exit code behavior → `CliEndToEndTest` or `DistributionContractTest`
- JSON/SARIF output structure → `CliEndToEndTest` (format tests)
- Gradle plugin extension properties → `AlgorillaPluginFunctionalTest`
- Cross-distribution consistency → `DistributionContractTest`

**Always test all supported languages.** When adding or modifying a rule, add test fixtures for every language the rule claims to support in its `languages` set. A rule that says `Language.entries.toSet()` must have fixtures for Java, Groovy, Kotlin, and JavaScript.

## 7. Design Heuristics

**Top-level functions over utility classes.** Kotlin doesn't need `StringUtils` or `CollectionHelper` classes. Put utility functions at the top level of a well-named file (e.g. `IRNodeExtensions.kt`).

**Sealed + `when` over visitor for closed sets.** When the set of variants is fixed (IR nodes, suggestions, rule categories), use sealed classes with `when` expressions. The compiler enforces exhaustiveness. Don't use the visitor pattern for these.

**Group by feature, not layer.** Files should be organized by what they do, not what kind of thing they are. A rule and its supporting types live together in `rules/builtin/`. Don't create `utils/`, `helpers/`, or `services/` packages.

**Companion self-registration for parsers.** Language parsers register themselves via a companion object `init` block:

```kotlin
public companion object {
    init {
        ParserRegistry.register(JavaLanguageParser())
    }
}
```

This fires when the class is loaded. The CLI and Gradle plugin reference each parser's companion to trigger loading.

**`by lazy` for expensive singletons.** The `LanguageSemanticsRegistry.DEFAULT` is a lazy singleton that parses ~5K lines of YAML once:

```kotlin
public val DEFAULT: LanguageSemanticsRegistry by lazy { loadDefaults() }
```

Use the same pattern for any expensive initialization that should happen at most once.

## 8. Public API Discipline

**Explicit visibility.** The project uses the Kotlin compiler flag that requires explicit visibility modifiers on all declarations. Every class, function, and property must be `public`, `internal`, or `private` — no implicit defaults.

**`internal` for implementation details.** If a class or function is only used within its module, mark it `internal`. This applies to:
- Parser implementation details in `lang-*` modules (IR visitors, ANTLR adapters)
- Helper functions that are specific to one rule
- Test utilities that shouldn't leak across modules

**`public` means contract.** Anything marked `public` in `core/` is part of the API that `lang-*`, `cli/`, `gradle-plugin/`, and `reporting/` depend on. Changing a `public` signature is a breaking change. Think twice before making something `public` — once it's out there, downstream code depends on it.

**CLI options are cross-cutting.** Adding or renaming a CLI flag affects multiple files across the CLI, Gradle plugin, GitHub Action, contract tests, and docs. See [Adding CLI Options](https://tvinke.github.io/algorilla/developer/adding-cli-options/) for the full checklist.

## 9. @Suppress Policy

**Always with exact rule ID.** Never suppress a category or wildcard — always the specific detekt/ktlint rule ID.

```kotlin
// Good
@Suppress("CyclomaticComplexMethod")

// Bad
@Suppress("all")
@Suppress("detekt")
```

**Always with inline justification.** Every `@Suppress` must have a comment on the same line or the line immediately above explaining why the rule doesn't apply here:

```kotlin
@Suppress("LargeClass", "TooManyFunctions") // Pipeline orchestrator — each analysis pass is a method
public class AnalysisEngine(...)

@Suppress("CyclomaticComplexMethod")  // When/else dispatch over all IR node types
public fun IRNode.withChildren(newChildren: List<IRNode>): IRNode = when (this) { ... }

} catch (
    @Suppress("TooGenericExceptionCaught") e: Exception, // Parser failures are unrecoverable — catch broadly and return empty
) {
```

**Never suppress to avoid refactoring.** If a function is genuinely too complex, refactor it. `@Suppress("CyclomaticComplexMethod")` is justified when complexity comes from exhaustive `when` dispatch (which is intentional), not from tangled control flow (which should be simplified).

**Justified suppression patterns in this codebase:**
- `LargeClass` / `TooManyFunctions` on orchestrator classes (AnalysisEngine, LanguageSemanticsRegistry) — these are inherently large by design
- `CyclomaticComplexMethod` on exhaustive `when` expressions over sealed types — compiler-enforced completeness makes this safe
- `TooGenericExceptionCaught` on parser entry points — external parsers (ANTLR, tree-sitter) can throw anything
- `ReturnCount` on functions with multiple early-return guard clauses — often cleaner than nested if/else
- `LongParameterList` on domain model constructors — these fields genuinely belong together

## 10. Naming Conventions

| Thing | Convention | Example |
|-------|-----------|---------|
| Rule class | `PascalCase` + `Rule` suffix | `FilterAfterSortRule` |
| Rule ID | `kebab-case` | `filter-after-sort` |
| Rule category | `SCREAMING_SNAKE_CASE` enum | `RuleCategory.SORT_ABUSE` |
| Test class | Rule class + language + `Test` | `FilterAfterSortRuleJavaTest` |
| Fixture directory | Rule ID | `fixtures/filter-after-sort/positive/` |
| Language YAML | `<language>.yml` | `semantics/java.yml` |
| Framework overlay YAML | `<framework>.yml` | `semantics/frameworks/spring.yml` |
| Extension function file | `<Type>Extensions.kt` | `IRNodeExtensions.kt` |
| IR node classes | Domain noun, `PascalCase` | `LoopNode`, `LookupCall`, `FunctionDecl` |
| Module directories | `lang-<language>` | `lang-java`, `lang-kotlin` |

## 11. Error Handling

**Never let exceptions propagate to callers.** Every module boundary catches broadly and returns a safe default. This keeps the analysis pipeline running even when individual files are malformed or unexpected.

**Parsers:** catch all exceptions, log at `warn`, return `emptyFileRoot()`. The engine wraps parse failures in `AnalysisError.Parse` (a sealed-class variant) so callers can report them without crashing.

```kotlin
try {
    val tree = parser.parse(file)
    irTrees[file] = tree
} catch (e: ParseException) {
    logger.warn { "Failed to parse $file: ${e.message}" }
    errors.add(AnalysisError.Parse(file, e.message ?: "Unknown parse error"))
}
```

**Cache corruption:** log at `warn`, return empty map — stale cache is annoying, crashing is worse.

**Pattern:** `catch` → log at `warn` → return empty/default → let the caller decide what to do with the gap. Don't silently swallow errors (always log), but don't let one bad file kill the whole scan.

## 12. Logging

**Use `io.github.oshai.kotlinlogging.KotlinLogging`** — this is the project's logging facade. No `println`, no `System.err`, no `java.util.logging`.

**Private module-level logger:**

```kotlin
private val logger = KotlinLogging.logger {}
```

One per file, at the top, right after the imports. Never pass loggers as parameters.

**Lazy string building:** always use the lambda form to avoid string concatenation when the level is disabled:

```kotlin
// Good
logger.info { "Pass 1 complete: ${irTrees.size} files parsed" }

// Bad — builds the string even when info is disabled
logger.info("Pass 1 complete: ${irTrees.size} files parsed")
```

**Log levels:**
- `info` for phase milestones (parse complete, annotation complete, analysis done)
- `warn` for recoverable issues (parse failure, cache corruption, unrecognized config)
- `debug` for verbose state dumps (IR tree contents, individual rule timings)
- Never use `error` in library code — let the caller decide what's fatal

## 13. Rule & Parser Registration

**Rules:** add new instances to `BuiltinRules.all()`. The method returns a fresh list on each call (no shared mutable state). The engine, CLI, and Gradle plugin all go through this single entry point.

```kotlin
public fun all(): List<Rule> =
    listOf(
        NestedLookupRule(),
        FilterAfterSortRule(),
        // ... add new rule here
    )
```

**Parsers:** self-register via a companion `init` block that fires on class loading:

```kotlin
public companion object {
    init {
        ParserRegistry.register(JavaLanguageParser())
    }
}
```

The CLI calls `registerAllParsers()` which touches each parser companion to trigger loading. `ParserRegistry` uses a `CopyOnWriteArrayList` with class-based deduplication — calling `register()` twice with the same parser class is harmless.

**Adding a new language module:**
1. Create the parser class implementing `LanguageParser`
2. Add the companion `init` block with `ParserRegistry.register(...)`
3. Add the class touch to `registerAllParsers()` in `CommandSupport.kt`
4. Add the language to the `Language` enum if it doesn't exist yet
