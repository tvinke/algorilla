# In-Loop Collection Building

**Rule ID:** `in-loop-collection-building` · **Severity:** WARNING · **Complexity:** O(n·m) → O(n+m)

## Description

Detects patterns that repeatedly copy or rebuild collections inside loops. Calling `addAll()`, `putAll()`, or `concat()` inside a loop copies elements on every iteration, turning an O(n) loop into O(n·m) due to repeated array/list copying.

## Bad Example

=== "Java"

    ```java
    List<String> allNames = new ArrayList<>();
    for (Department dept : departments) {
        allNames.addAll(dept.getEmployeeNames()); // Copies entire list each iteration
    }
    ```

=== "JavaScript"

    ```javascript
    let allNames = [];
    departments.forEach(dept => {
        allNames = allNames.concat(dept.employeeNames); // Creates new array each iteration
    });
    ```

## Good Example

=== "Java"

    ```java
    List<String> allNames = departments.stream()
        .flatMap(dept -> dept.getEmployeeNames().stream())
        .collect(Collectors.toList());
    ```

=== "JavaScript"

    ```javascript
    const allNames = departments.flatMap(dept => dept.employeeNames);
    ```

## Suggestion

> Accumulate into a single collection, then call addAll() once after the loop

## Related Rules

- [Nested Lookup](nested-lookup.md) — another pattern where loop bodies cause quadratic behavior
