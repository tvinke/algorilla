# Repeated Linear Scan

**Rule ID:** `repeated-linear-scan` · **Severity:** WARNING · **Complexity:** O(n·k) → O(n)

## Description

Detects multiple linear scan operations (`find`, `filter`, `any`, `contains`) on the same collection within a single method. Each scan traverses the collection independently when they could be combined into a single pass or the first result could be cached.

## Bad Example

```java
User admin = users.stream().filter(u -> u.isAdmin()).findFirst().orElse(null);
User owner = users.stream().filter(u -> u.isOwner()).findFirst().orElse(null);
long activeCount = users.stream().filter(u -> u.isActive()).count();
```

## Good Example

```java
User admin = null;
User owner = null;
long activeCount = 0;
for (User u : users) {
    if (admin == null && u.isAdmin()) admin = u;
    if (owner == null && u.isOwner()) owner = u;
    if (u.isActive()) activeCount++;
}
```

## Suggestion

> Cache the result of the first lookup, or combine into a single pass
