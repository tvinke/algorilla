# Algorilla

**Detect algorithmic complexity anti-patterns in your codebase.**

Algorilla is a static analysis tool that finds hidden O(n²) and O(n·m) performance issues in Java, Groovy, Kotlin, and JavaScript/TypeScript code. It analyzes your source code without executing it, identifying patterns where a simple refactoring can turn quadratic behavior into linear.

## What It Finds

| Rule | Pattern | Impact |
|------|---------|--------|
| [Nested Lookup](rules/nested-lookup.md) | `contains()` inside a loop | O(n²) → O(n) |
| [Sort for Last](rules/sort-for-last.md) | `sort().first()` | O(n log n) → O(n) |
| [Expensive Sort Comparator](rules/expensive-sort-comparator.md) | Linear search, date parsing in comparator | O(n² log n) → O(n log n) |
| [Repeated Linear Scan](rules/repeated-linear-scan.md) | Multiple `find()` on same collection | O(n·k) → O(n) |
| [Full Scan for Single Lookup](rules/full-scan-for-single-lookup.md) | Load all, then filter | O(n) → O(1) |
| [Heavyweight Object per Invocation](rules/heavyweight-object-per-invocation.md) | `new ObjectMapper()` in methods | Allocation overhead |

## Quick Example

```bash
java -jar algorilla.jar /path/to/your/project
```

```
WARNING  nested-lookup  OrderService.java:45:5
  Linear contains on 'items' inside for-each loop
  Current: O(n*m)  →  Suggested: O(n+m)
  Suggestion: Build a HashSet/Map from 'items' before the loop
  Evidence:
    1. OrderService.java:45 for-each loop over orders          [INSIDE_LOOP]
    2. OrderService.java:48 contains on 'items'                [INSIDE_LOOP]

Found 1 issues (0 errors, 1 warnings) in 42 files analyzed (0.3s)
```

## Features

- **Multi-language**: Java, Groovy, Kotlin, JavaScript, TypeScript, Vue SFC
- **Fast**: Analyzes thousands of files in seconds with incremental caching
- **SARIF output**: Integrates with GitHub Code Scanning, VS Code, and other tools
- **Baseline mode**: Track only new findings in CI pipelines
- **Custom rules**: Extend with your own rules via Kotlin Script DSL
- **Zero config**: Works out of the box, optional `.algorilla.yml` for customization
