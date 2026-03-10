# Repeated Linear Scan

**Rule ID:** `repeated-linear-scan` · **Severity:** WARNING · **Complexity:** O(n·k) → O(n)

## Description

Detects multiple linear scan operations (`find`, `filter`, `any`, `contains`) on the same collection within a single method. Each scan traverses the collection independently when they could be combined into a single pass or the first result could be cached.

## Bad Example

```java
Order highestTotal = orders.stream().max(Comparator.comparing(Order::getTotal)).orElse(null);
Order firstCancelled = orders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).findFirst().orElse(null);
long paidCount = orders.stream().filter(o -> "PAID".equals(o.getStatus())).count();
```

## Good Example

```java
Order highestTotal = null;
Order firstCancelled = null;
long paidCount = 0;
for (Order o : orders) {
    if (highestTotal == null || o.getTotal().compareTo(highestTotal.getTotal()) > 0) highestTotal = o;
    if (firstCancelled == null && "CANCELLED".equals(o.getStatus())) firstCancelled = o;
    if ("PAID".equals(o.getStatus())) paidCount++;
}
```

## Suggestion

> Cache the result of the first lookup, or combine into a single pass
