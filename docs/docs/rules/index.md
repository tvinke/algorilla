# Rules Overview

Algorilla ships with 6 built-in rules that detect common algorithmic complexity anti-patterns.

| Rule ID | Name | Severity | Detected Complexity |
|---------|------|----------|-------------------|
| `nested-lookup` | [Nested Lookup](nested-lookup.md) | WARNING | O(n²) where O(n) suffices |
| `sort-for-last` | [Sort for Last](sort-for-last.md) | WARNING | O(n log n) where O(n) suffices |
| `expensive-sort-comparator` | [Expensive Sort Comparator](expensive-sort-comparator.md) | WARNING | O(n² log n) where O(n log n) suffices |
| `repeated-linear-scan` | [Repeated Linear Scan](repeated-linear-scan.md) | WARNING | O(n·k) where O(n) suffices |
| `full-scan-for-single-lookup` | [Full Scan for Single Lookup](full-scan-for-single-lookup.md) | WARNING | O(n) where O(1) suffices |
| `heavyweight-object-per-invocation` | [Heavyweight Object per Invocation](heavyweight-object-per-invocation.md) | WARNING | Unnecessary allocation overhead |

## Disabling Rules

In `.algorilla.yml`:

```yaml
rules:
  expensive-sort-comparator:
    enabled: false
```

Or suppress inline:

```java
// algorilla:ignore nested-lookup
for (Order order : orders) {
    if (items.contains(order.getItemId())) { ... }
}
```
