# Date in Sort

**Rule ID:** `date-in-sort` · **Severity:** INFO

## Description

Detects Date or LocalDate object creation inside sort comparators. Parsing date strings on every comparison is wasteful — either compare ISO date strings directly (they sort lexicographically) or parse dates once before sorting.

## Bad Example

```java
items.sort((a, b) -> {
    Date dateA = dateFormat.parse(a.getDateStr()); // Parsed on every comparison
    Date dateB = dateFormat.parse(b.getDateStr());
    return dateA.compareTo(dateB);
});
```

## Good Example

```java
// Option 1: Parse once, sort by parsed value
items.sort(Comparator.comparing(item -> dateFormat.parse(item.getDateStr())));

// Option 2: If ISO format, compare strings directly
items.sort(Comparator.comparing(Item::getDateStr));
```

## Suggestion

> Compare ISO date strings directly, or parse dates once before sorting
