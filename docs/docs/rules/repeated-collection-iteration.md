# Repeated Collection Iteration

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `repeated-collection-iteration` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | INFO — worth knowing, may not matter at your scale |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Redundancy |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(k·n) → O(n) |

Flags multiple passes over the same collection that could potentially be fused into a single pass.

## What it detects

**Two stream pipelines on the same source:**

```java
double total = orders.stream().mapToDouble(Order::getAmount).sum();
long count = orders.stream().filter(o -> o.getAmount() > 100).count();
// Two passes over orders — could be one
```

**Two for-each loops over the same collection:**

```java
for (Item item : items) { validate(item); }
for (Item item : items) { transform(item); }
// Could combine into a single loop
```

## What it skips

- Streams on different collections (independent operations)
- Streams in mutually exclusive branches (if/else — only one executes)

## Why INFO severity

Multiple passes are often intentional for readability. Two clear, separate pipelines can be easier to understand than one combined pipeline. This rule surfaces the opportunity; the developer decides if fusing is worth the readability tradeoff.

## Fix

Combine into a single pass when the operations are compatible:

```java
double total = 0;
long count = 0;
for (Order o : orders) {
    total += o.getAmount();
    if (o.getAmount() > 100) count++;
}
```
