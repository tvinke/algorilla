# Expensive Sort Comparator

**Rule ID:** `expensive-sort-comparator` · **Severity:** WARNING · **Complexity:** O(n² log n) → O(n log n)

**Aliases:** `date-in-sort` (merged)

## Description

Detects expensive operations inside sort comparators. When a comparator performs O(n) work per comparison, the sort becomes O(n² log n) instead of O(n log n). This includes:

- **Linear lookups** — `indexOf`, `contains`, `find`, `filter` on a collection
- **Date parsing** — `LocalDate.parse()`, `new Date()`, `DateTimeFormatter` calls
- **Indirect expensive calls** — helper methods that internally do the above (detected via cross-method analysis)

## Bad Examples

```java
// Linear lookup in comparator
items.sort((a, b) -> {
    int indexA = priorityList.indexOf(a.getCategory()); // O(n) per comparison
    int indexB = priorityList.indexOf(b.getCategory());
    return Integer.compare(indexA, indexB);
});

// Date parsing in comparator
items.sort((a, b) -> {
    Date dateA = dateFormat.parse(a.getDateStr()); // Parsed every comparison
    Date dateB = dateFormat.parse(b.getDateStr());
    return dateA.compareTo(dateB);
});
```

## Good Examples

```java
// Pre-build lookup map
Map<String, Integer> priorityMap = new HashMap<>();
for (int i = 0; i < priorityList.size(); i++) {
    priorityMap.put(priorityList.get(i), i);
}
items.sort(Comparator.comparingInt(item ->
    priorityMap.getOrDefault(item.getCategory(), Integer.MAX_VALUE)
));

// Pre-parse dates, or compare ISO strings directly
items.sort(Comparator.comparing(Item::getDateStr));
```

## Suggestion

> Build a lookup Map before the sort, or pre-parse expensive values once
