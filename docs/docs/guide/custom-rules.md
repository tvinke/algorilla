# Custom Rules

Algorilla supports custom rules written in Kotlin Script (`.kts`). Place rule scripts in `.algorilla/rules/` and they are automatically loaded.

## Writing a Custom Rule

Create a file like `.algorilla/rules/large-loop-body.kts`:

```kotlin
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.custom.rule

rule("custom-large-loop") {
    name = "Large Loop Body"
    severity = Severity.INFO
    suggestion = "Consider extracting the loop body into a separate method"

    onNode<LoopNode> { node, file ->
        if (node.children.size > 10) {
            report(node.location, "Loop body has ${node.children.size} IR nodes")
        }
    }
}
```

## DSL Reference

### `rule(id) { ... }`

Creates a custom rule with the given ID.

### Builder Properties

| Property | Type | Default |
|----------|------|---------|
| `name` | String | Same as rule ID |
| `severity` | Severity | WARNING |
| `suggestion` | String | Empty |
| `languages` | Set&lt;Language&gt; | All languages |

### `onNode<T> { node, file -> ... }`

Registers a visitor that is called for every IR node matching type `T`. Available node types:

- `LoopNode` — for, while, forEach, higher-order iterations
- `LookupCall` — contains, indexOf, find, filter, etc.
- `SortCall` — sort, sorted, sortBy, etc.
- `FunctionDecl` — method/function declarations
- `FunctionCall` — method/function invocations
- `ObjectCreation` — `new` expressions
- `VariableDecl` — variable declarations
- `CollectionAccess` — get, findAll, list, etc.

### `report(location, message, suggestion?)`

Reports a finding at the given source location.

## Disabling Custom Rules

Custom rules can be disabled in `.algorilla.yml` like built-in rules:

```yaml
rules:
  custom-large-loop:
    enabled: false
```
