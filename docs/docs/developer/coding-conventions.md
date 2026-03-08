# Coding Conventions

This page documents the project-specific coding patterns used in Algorilla. For general Kotlin style, the project enforces [ktlint](https://pinterest.github.io/ktlint/) and [detekt](https://detekt.dev/) — those tools handle formatting and common code smells automatically.

## Rules Are Stateless

Every rule implements the `Rule` interface and must be stateless. Rules receive an `AnalysisContext` as input and return a list of `Finding` objects. They must not store mutable state between calls.

```kotlin
public class NestedLookupRule : Rule {
    override val id: String = "nested-lookup"
    override val name: String = "Nested Lookup"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        // ... scan IR trees, accumulate findings ...
        return findings
    }
}
```

Use local mutable collections for accumulation inside `evaluate()`. Never store state in class fields.

### Rule naming

- `id`: lowercase kebab-case (`"nested-lookup"`, `"sort-for-last"`)
- `name`: human-readable title (`"Nested Lookup"`, `"Sort for Last"`)
- Class name: PascalCase matching the name, suffixed with `Rule` (`NestedLookupRule`)

### Rule helper structure

Rules typically have three levels of private helpers:

1. **`scanNode()`** — recursive tree traversal
2. **`checkFunction()`** — business logic on a specific node type
3. **`buildFinding()`** — assembles the `Finding` with `Evidence` entries

## Parsers Implement `LanguageParser`

Each language parser implements the `LanguageParser` interface:

```kotlin
public interface LanguageParser {
    public val language: Language
    public fun canParse(filePath: String): Boolean
    public fun parse(filePath: String): FileRoot
}
```

Parsers must handle errors gracefully: log a warning and return an empty `FileRoot` rather than throwing exceptions (except for "file not found", which throws `ParseException`).

```kotlin
return try {
    parseFile(file)
} catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
    logger.debug { "File not parseable: $filePath (${e.message})" }
    emptyFileRoot(filePath)
}
```

## Reporters Implement `Reporter`

Output format implementations follow the `Reporter` interface:

```kotlin
public interface Reporter {
    public fun report(result: AnalysisResult, output: Appendable)
}
```

Reporters write to an `Appendable`, not directly to `System.out` or a file. The caller decides the destination.

## Visibility Modifiers

- **`public`**: interfaces, data classes, and functions that form the module's API
- **`internal`**: implementation classes not meant for use outside the module
- **`private`**: helper functions and constants within a file

Use `public` explicitly — the project enforces explicit API mode (`-Xexplicit-api=strict`).

## Data Classes for Domain Objects

Use `data class` with `val` properties for domain objects:

```kotlin
public data class Finding(
    val ruleId: String,
    val ruleName: String,
    val severity: Severity,
    val location: SourceLocation,
    val message: String,
    val suggestion: String,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<Evidence> = emptyList(),
)
```

Domain classes are immutable. Use `.copy()` for modifications.

## Serialization

Use **kotlinx.serialization** (not Jackson or Gson). Apply `@Serializable` to internal DTOs, not to domain model classes. Convert between domain and DTO using extension functions:

```kotlin
private fun SourceLocation.toJsonLocation(): JsonLocation =
    JsonLocation(file, line, column)
```

## Logging

Use kotlin-logging for all logging:

```kotlin
private val logger = KotlinLogging.logger {}
```

One logger per file, declared at the top level. Use lazy message syntax:

```kotlin
logger.debug { "Processing file: $filePath" }
logger.info { "Detected Gradle project at ${projectRoot.path}" }
```

## Extension Functions

Use extension functions for domain-specific operations that enhance readability:

```kotlin
public inline fun <reified T : IRNode> IRNode.findDescendants(): List<T>
```

Place extension functions in a dedicated file (e.g., `IRNodeExtensions.kt`) when they are used across multiple classes.

## Test Conventions

- Class name: `<Subject>Test` (e.g., `CallGraphTest`)
- Visibility: `internal class`
- Method names: backtick-wrapped natural language (`` `should track caller-callee edges`() ``)
- Assertions: Kotest matchers (`shouldBe`, `shouldHaveSize`, `shouldContainExactlyInAnyOrder`)
- Fixture loading: helper method pattern with `javaClass.classLoader.getResource("fixtures/...")`
- Structure: Arrange-Act-Assert, one concept per test method
- Use `@Nested` inner classes to group related tests
- Use `@TempDir` for filesystem tests (JUnit 5 temp directory support)
