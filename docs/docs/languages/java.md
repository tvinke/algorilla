---
tags:
  - Java
---

# Java

!!! success "GA — Production Ready"
    Java is Algorilla's most mature language. Findings are trustworthy with a low false-positive rate, validated against enterprise codebases.

Here's a pattern that shows up in reviewed, production-quality code:

```java
List<String> processedIds = orderRepository.getProcessedOrderIds();

incoming.stream()
    .filter(event -> !processedIds.contains(event.orderId()))
    .forEach(this::process);
```

Readable, idiomatic — and O(n²). `ArrayList.contains()` is O(n), called once per event. Algorilla catches it:

```
warning  · nested-lookup · Loop amplifiers · O(events × processedIds) → O(events + processedIds)

  Linear contains on 'processedIds' inside filter()
  → Build a HashSet/Map from 'processedIds' before the loop

      1 │ incoming.stream()
      2 │     .filter(event -> !processedIds.contains(event.orderId()))

  ⎿  filter() over incoming   O(events)
    ⎿  contains on 'processedIds'   O(processedIds) ← bottleneck
```

At list sizes in the hundreds this is fine. At tens of thousands it isn't. Now you can make that call consciously.

## Why Java gets full coverage

Java's static type system is what makes this detection reliable. When the parser sees `processedIds.contains(...)`, it resolves `processedIds` back to its declaration — `List<String>` — and knows `contains()` is O(n). Switch that to `Set<String>`, it doesn't flag it.

That resolution extends through generics, method calls, and common third-party types. All 23 rules apply.

## What's covered

Full Java syntax up to Java 21 — records, sealed classes, pattern matching, generics, lambdas, Stream API chains, concurrent utilities (`CompletableFuture.join()`, `Future.get()`), reflection, and parallel streams with shared state detection.

## Collection type inference

The O(1)/O(n) distinction works because the parser resolves variable types from declarations:

```java
Set<String> ids = new HashSet<>(list);    // contains() is O(1) — not flagged
List<String> ids = new ArrayList<>(list); // contains() is O(n) — flagged
```

When types can't be resolved statically, use [type hints](../getting-started/configuration.md) in `.algorilla.yml`.

## Cross-file analysis

The parser tracks method calls across class boundaries. A loop that looks innocent at the call site gets flagged when the callee contains a loop of its own:

```java
public void generateReport(List<Department> departments) {
    departments.forEach(dept -> summarize(dept));  // looks fine here
}

private void summarize(Department dept) {
    for (Employee emp : dept.getEmployees()) {     // loop is in here
        calculateBonus(emp);
    }
}
```

```
warning  · hidden-nested-loop · O(departments × dept.getEmployees())

  summarize() contains a for-each loop — hidden O(n²) complexity

  ⎿  forEach() over departments   O(departments)
    ⎿  summarize() called per iteration
      ⎿  for-each loop inside summarize()   O(dept.getEmployees()) ← bottleneck
```

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 23 apply to Java
- [Framework support](frameworks.md) — built-in knowledge of Spring and Guava for fewer false positives
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
