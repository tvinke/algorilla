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

## Configuration

The maximum call depth for cross-file analysis is configurable:

```yaml
max-call-depth: 5  # default
```

This bounds the traversal depth to prevent excessive analysis time on deeply nested call chains.
