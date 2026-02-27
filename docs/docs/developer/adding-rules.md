# Adding Rules

## Built-in Rule

1. Create a new class in `core/src/main/kotlin/.../rules/builtin/`:

```kotlin
class MyNewRule : Rule {
    override val id = "my-new-rule"
    override val name = "My New Rule"
    override val severity = Severity.WARNING
    override val languages = Language.entries.toSet()

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, findings)
        }
        return findings
    }

    private fun scanNode(node: IRNode, findings: MutableList<Finding>) {
        // Your detection logic here
        for (child in node.children) {
            scanNode(child, findings)
        }
    }
}
```

2. Register in `AlgorillaCommand.kt`:

```kotlin
val rules = listOf(
    // ... existing rules
    MyNewRule(),
)
```

3. Add test fixtures in `src/test/resources/fixtures/my-new-rule/positive/` and `negative/`.

## Custom Rule via DSL

See [Custom Rules](../guide/custom-rules.md) for the Kotlin Script approach.

## Testing

- Create positive fixtures (files that should trigger findings)
- Create negative fixtures (files that should not trigger findings)
- Write parameterized tests covering both cases
- Run the full pipeline (parse → IR → rule → finding) in integration tests
