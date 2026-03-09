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

## Rule-Level Disabling

```yaml
rules:
  date-in-sort:
    enabled: false
```
