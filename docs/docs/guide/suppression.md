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

The comment can be placed on the same line as the finding or the line immediately above it. Suppression also works when placed near evidence chain locations (e.g., next to the lookup call inside a loop).

## File-Level Exclusion

In `.algorilla.yml`:

```yaml
exclude:
  - "**/legacy/**"
  - "**/generated/**"
```

## Accept Individual Findings (Ignore List)

For findings you've reviewed and decided are acceptable without modifying the source code, use the ignore list:

```bash
# the fingerprint hash is shown at the end of each finding in console output
java -jar algorilla.jar --accept a1b2c3d4e5f6g7h8 .
```

This adds the finding to `.algorilla/ignore-list.json`. Accepted findings are hidden on subsequent runs. The ignore list uses content-based fingerprints, so accepted findings survive line number shifts.

See the [Workflow](workflow.md#accept-it-ignore-list) page for when to use inline suppression vs. the ignore list.

## Rule-Level Disabling

```yaml
rules:
  date-in-sort:
    enabled: false
```
