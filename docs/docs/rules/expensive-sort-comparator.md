# Expensive Sort Comparator

**Rule ID:** `expensive-sort-comparator` · **Severity:** WARNING · **Complexity:** O(n² log n) → O(n log n)

## Description

Detects linear lookup operations inside sort comparators. When a comparator performs a linear search (e.g., `indexOf`, `contains`, `find`) for each comparison, the sort becomes O(n² log n) instead of O(n log n). Pre-building a lookup map brings it back to O(n log n).

## Bad Example

```java
items.sort((a, b) -> {
    int indexA = priorityList.indexOf(a.getCategory()); // O(n) per comparison
    int indexB = priorityList.indexOf(b.getCategory());
    return Integer.compare(indexA, indexB);
});
```

## Good Example

```java
Map<String, Integer> priorityMap = new HashMap<>();
for (int i = 0; i < priorityList.size(); i++) {
    priorityMap.put(priorityList.get(i), i);
}
items.sort(Comparator.comparingInt(item ->
    priorityMap.getOrDefault(item.getCategory(), Integer.MAX_VALUE)
));
```

## Suggestion

> Build a lookup Map from '{lookupCollection}' before the sort
