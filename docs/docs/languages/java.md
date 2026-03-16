---
tags:
  - Java
  - Hibernate
  - Guava
  - performance
description: "Java complexity analysis with Algorilla — full coverage of 28 rules, static type resolution, cross-file analysis, and framework-aware detection for Spring, JPA, Guava, and more."
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
  ↳ https://tvinke.github.io/algorilla/rules/nested-lookup

      1 │ incoming.stream()
      2 │     .filter(event -> !processedIds.contains(event.orderId()))

  ⎿  filter() over incoming   O(events)
    ⎿  contains on 'processedIds'   O(processedIds) ← bottleneck
```

At list sizes in the hundreds this is fine. At tens of thousands it isn't. Now you can make that call consciously.

## Top complexity traps in Java

### N+1 queries with Spring Data JPA

A common performance trap in Spring applications. A loop calls `findById()` once per entity — barely noticeable at small scale, but it adds up with larger datasets.

```java
for (Long orderId : orderIds) {
    Order order = orderRepository.findById(orderId).orElseThrow();
    results.add(process(order));
}
```

```
warning  · n-plus-one-query · Query patterns · O(orderIds·IO) → O(1·IO + orderIds)

  findById() called inside for-each loop — N+1 query pattern
  → Use findAllById (Spring Data) instead of calling findById per iteration
  ↳ https://tvinke.github.io/algorilla/rules/n-plus-one-query
```

Fix: `orderRepository.findAllById(orderIds)` — one query instead of N. See [n-plus-one-query](../rules/n-plus-one-query.md).

### The hidden O(n²) in ArrayList.contains()

Java's `List.contains()` is O(n). Inside a loop, that's O(n²). The type system makes this detectable — Algorilla resolves the variable type and knows `List.contains()` is linear while `Set.contains()` is constant.

```java
List<String> blocklist = loadBlocklist();
for (Request req : requests) {
    if (blocklist.contains(req.getIp())) {
        reject(req);
    }
}
```

```
warning  · nested-lookup · O(requests × blocklist) → O(requests + blocklist)

  → Build a HashSet from 'blocklist' before the loop
  ↳ https://tvinke.github.io/algorilla/rules/nested-lookup
```

One line: `Set<String> blockSet = new HashSet<>(blocklist);`. See [nested-lookup](../rules/nested-lookup.md).

### ObjectMapper in a loop

Jackson's `ObjectMapper` is heavyweight to construct — thread pool, configuration, codec registry. Creating one per iteration wastes memory and CPU on an object meant to be reused.

```java
for (Order order : orders) {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(order);
    send(json);
}
```

```
warning  · expensive-construction · Construction cost

  ObjectMapper constructed inside for-each loop — heavyweight allocation per iteration
  ↳ https://tvinke.github.io/algorilla/rules/expensive-construction
```

Move the construction before the loop (or make it a field). See [expensive-construction](../rules/expensive-construction.md).

### String concatenation the hard way

`String.concat()` in a loop copies the entire accumulated string on every iteration — classic O(n²). For 1,000 items, that's ~500,000 character copies.

```java
String report = "";
for (Order order : orders) {
    report = report.concat(order.getName()).concat(", ");
}
```

```
warning  · string-concat-in-loop · O(n²) → O(n)

  → Use a StringBuilder to accumulate the result, then call toString() after the loop
  ↳ https://tvinke.github.io/algorilla/rules/string-concat-in-loop
```

`StringBuilder` or `Collectors.joining(", ")`. See [string-concat-in-loop](../rules/string-concat-in-loop.md).

### The method call that hides a loop

A loop calls `validateItems(order)`, which internally iterates over `order.getItems()`. At the call site it looks like O(n), but it's actually O(orders × items).

```java
for (Order order : orders) {
    validateItems(order);  // hides a for-each loop inside
}
```

```
warning  · hidden-nested-loop · O(orders × order.getItems())

  validateItems() contains a for-each loop — hidden O(n²) complexity
  ↳ https://tvinke.github.io/algorilla/rules/hidden-nested-loop
```

Algorilla uses cross-method analysis to trace through the call. See [hidden-nested-loop](../rules/hidden-nested-loop.md).

## Why Java gets full coverage

Java's static type system makes detection reliable. When the parser sees `processedIds.contains(...)`, it resolves `processedIds` back to its declaration — `List<String>` — and knows `contains()` is O(n). Switch that to `Set<String>`, it doesn't flag it.

That resolution extends through generics, method calls, and common third-party types. All 28 rules apply.

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

## Framework knowledge

Algorilla ships with built-in awareness of common Java frameworks, reducing false positives without any configuration.

| Framework | What Algorilla knows | Most relevant rules |
|-----------|---------------------|-------------------|
| **Spring** | RestTemplate, WebClient, Data repositories, Security, Validation, transaction management | [n-plus-one-query](../rules/n-plus-one-query.md), [io-in-loop](../rules/io-in-loop.md) |
| **JPA / Hibernate** | EntityManager, CriteriaBuilder, Session, TypedQuery | [n-plus-one-query](../rules/n-plus-one-query.md), [lazy-loading-in-loop](../rules/lazy-loading-in-loop.md) |
| **Guava** | Immutable collections, Cache, Multimap, Optional, Ordering | [nested-lookup](../rules/nested-lookup.md), [redundant-expensive-call](../rules/redundant-expensive-call.md) |
| **Project Reactor** | Mono/Flux, Schedulers, blocking calls | [io-in-loop](../rules/io-in-loop.md), [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md) |
| **Apache Commons** | StringUtils, CollectionUtils, IOUtils, FileUtils | [io-in-loop](../rules/io-in-loop.md) |
| **Jackson** | ObjectMapper, JsonParser, JsonGenerator | [expensive-construction](../rules/expensive-construction.md), [expensive-serialization-in-loop](../rules/expensive-serialization-in-loop.md) |

For the full list, see [Framework support](frameworks.md).

## See also

- [All rules](../rules/index.md) — the full set of patterns Algorilla detects, all 28 apply to Java
- [Framework support](frameworks.md) — built-in knowledge of Spring, JPA, Guava, Reactor, and more
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
