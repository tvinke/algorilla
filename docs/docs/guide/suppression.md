# Suppression

## Inline Comments

Suppress findings with comments on or above the relevant line:

```java
// algorilla:ignore
for (Item item : items) { ... }

// algorilla:ignore nested-lookup
if (list.contains(value)) { ... }

/* algorilla:ignore expensive-construction */
ObjectMapper mapper = new ObjectMapper();
```

### Suppression Scope

- `// algorilla:ignore` — suppresses all rules on that line
- `// algorilla:ignore rule-id` — suppresses only the specified rule

The comment can be placed on the same line as the finding or the line immediately above it. Suppression also works when placed near evidence chain locations (e.g., next to the lookup call inside a loop), including evidence in other files.

## File-Level Suppression

Suppress all findings (or specific rules) for an entire file by placing a comment in the first 5 lines:

```java
// algorilla:ignore-file
package com.example.legacy;
```

```java
// algorilla:ignore-file nested-lookup
package com.example.dao;
```

- `// algorilla:ignore-file` — suppresses all rules for the entire file
- `// algorilla:ignore-file rule-id` — suppresses only the specified rule for the file

This is useful for files that are known to be legacy or generated, where you don't want to annotate every finding individually but also don't want to exclude the file from analysis entirely.

## File-Level Exclusion

To exclude entire files or directories from analysis, use glob patterns in `.algorilla.yml`:

```yaml
exclude:
  - "**/legacy/**"
  - "**/generated/**"
```

## Accept Individual Findings (Ignore List)

For findings you've reviewed and decided are acceptable without modifying the source code, use the ignore list:

```bash
# the fingerprint hash is shown at the end of each finding in console output
algorilla --accept a1b2c3d4e5f6g7h8 .
```

This adds the finding to `.algorilla/ignore-list.json`. Accepted findings are hidden on subsequent runs.

Fingerprints are computed from the file's **relative path** (relative to the project root), the rule ID, and a normalized version of the finding message (quoted identifiers like variable names are stripped). This means:

- Accepted findings survive **line number shifts** (inserting lines above doesn't invalidate them)
- Fingerprints are **portable across machines** (no absolute paths)
- Renaming a variable mentioned in the message won't invalidate the fingerprint

Fingerprints are also included in JSON and SARIF output, so CI pipelines can parse structured output and use `--accept` programmatically.

See the [Workflow](workflow.md#accept-it-ignore-list) page for when to use inline suppression vs. the ignore list.

## Rule-Level Disabling

```yaml
rules:
  expensive-sort-comparator:
    enabled: false
```

## When to Use What

| Mechanism | Scope | Best for |
|-----------|-------|----------|
| `// algorilla:ignore` | Single line | One-off false positives you want visible in code review |
| `// algorilla:ignore-file` | Entire file | Legacy or generated files where inline comments would be noisy |
| `exclude:` globs | File/directory | Whole directories you never want to scan (generated, vendored) |
| `--accept <hash>` | Single finding | Reviewed findings you accept without touching the source |
| `rules: { enabled: false }` | Entire rule | Rules that don't apply to your project at all |
