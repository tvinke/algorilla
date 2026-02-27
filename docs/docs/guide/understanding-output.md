# Understanding Output

## Console Output Format

Each finding is displayed with the following structure:

```
SEVERITY  rule-id  file:line:column
  Message describing the detected pattern
  Current: O(...)  →  Suggested: O(...)
  Suggestion: How to fix it
  Evidence:
    1. file:line description                    [CONTEXT]
    2. file:line description                    [CONTEXT]
```

### Severity Levels

- **ERROR** — High-impact issues that should be addressed
- **WARNING** — Likely performance problems worth investigating
- **INFO** — Informational findings that may or may not matter

### Evidence Chain

The evidence chain shows the execution path that leads to the finding. Each entry identifies a source location and its execution context:

- `[INSIDE_LOOP]` — The operation occurs inside a loop body
- `[INSIDE_SORT]` — The operation occurs inside a sort comparator
- `[SINGLE]` — A standalone occurrence

### Summary Line

Every scan ends with a summary:

```
Found X issues (Y errors, Z warnings) in N files analyzed (T.Ts)
```

## SARIF Output

SARIF (Static Analysis Results Interchange Format) output follows the SARIF 2.1.0 specification and integrates with:

- GitHub Code Scanning
- Visual Studio Code (SARIF Viewer extension)
- Azure DevOps

```bash
java -jar algorilla.jar --format sarif --output results.sarif .
```

## JSON Output

Machine-readable JSON output for custom tooling:

```bash
java -jar algorilla.jar --format json --output results.json .
```
