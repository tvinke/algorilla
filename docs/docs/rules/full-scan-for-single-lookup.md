# Bulk Load for Single Lookup

**Rule ID:** `bulk-load-for-single-lookup` · **Severity:** WARNING · **Complexity:** O(n) → O(1)

## Description

Detects patterns where all records are loaded from a data source (e.g., `findAll()`, `getAll()`, `list()`) and then immediately filtered in memory to find a single item. This is O(n) when a targeted query could retrieve the item directly in O(1).

## Bad Example

```java
List<User> allUsers = userRepository.findAll();
User target = allUsers.stream()
    .filter(u -> u.getId().equals(targetId))
    .findFirst()
    .orElse(null);
```

## Good Example

```java
User target = userRepository.findById(targetId).orElse(null);
```

## Suggestion

> Use a targeted query instead of loading all records and filtering in memory
