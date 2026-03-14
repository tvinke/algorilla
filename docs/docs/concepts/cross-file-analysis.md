# Cross-File Analysis

Algorilla can detect complexity issues that span multiple files through its call graph and symbol table.

## How It Works

1. **Symbol Table**: All function declarations across all files and languages are registered with qualified names
2. **Call Graph**: Function calls are resolved to their declarations using name matching and parameter count heuristics
3. **Context Propagation**: Execution context (e.g., "inside a loop") is propagated through the call graph

## Cross-Language Support

The unified IR is language-agnostic, enabling detection across language boundaries:

- Java method calling a Groovy service
- Groovy service calling a Java utility
- TypeScript component using a shared JavaScript library

Resolution uses qualified names and heuristic matching, making it resilient to minor naming differences between languages.

## Parameter Flow Analysis

Beyond the call graph, Algorilla tracks where function parameters flow within each function body. The `ParameterFlowAnnotator` pass records:

- **LoopIteration** — the parameter is iterated in a for-each or stream
- **MethodCallReceiver** — the parameter is used as a method call target (e.g. `param.contains()`)
- **FunctionArgument** — the parameter is passed to another function call

This enables cross-method detection: when a parameter flows through a helper function into an expensive operation (like IO), Algorilla reports the full path — even though the IO call isn't directly visible at the call site.

Variable aliasing is tracked one hop deep: `val filtered = items.filter{...}` links `filtered` back to the `items` parameter, so a loop over `filtered` is recognized as iterating the original parameter.

Rules use `ParameterFlowQuery` to ask questions like "does this parameter flow through this call into a loop?" without traversing the flow data directly.

## Configuration

The maximum call depth for cross-file analysis is configurable:

```yaml
max-call-depth: 5  # default
```

This bounds the traversal depth to prevent excessive analysis time on deeply nested call chains.
