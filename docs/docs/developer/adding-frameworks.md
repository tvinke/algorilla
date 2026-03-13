# Adding Framework Overlays

Framework overlays teach Algorilla about framework-specific methods and types, so it doesn't flag them incorrectly. This is one of the most accessible contributions — it's just YAML, no Kotlin or parser knowledge required.

## When to add an overlay

You scanned a project that uses a framework and got false positives. Maybe Algorilla flagged `repository.findById()` as an expensive callback, or it warned about `Assert.notNull()` being called redundantly. These are legitimate framework calls that Algorilla doesn't know about yet.

The easiest way to start is from actual false positives — scan a project, see what's wrong, and add those methods. No need to catalog an entire framework in one go.

## The three categories

Each overlay file has three sections. The key question for each method or type is: **what happens at runtime?**

### cheap-methods

Methods where the call itself is not an expensive operation. They may do I/O or real work, but they're *expected* to be called in loops and shouldn't trigger rules like `expensive-callback` or `redundant-expensive-call`.

**The test:** "If I saw this called inside a `forEach`, would I think twice?" If not, it's cheap.

```yaml
cheap-methods:
  # These are normal API calls — not free, but not expensive enough to flag
  - findById        # single DB lookup, expected usage
  - getBean         # DI container lookup, cached
  - hasRole         # in-memory check
```

Cheap does **not** mean free. `findById()` hits a database — but flagging every repository call inside a loop would drown the developer in noise. The rule that matters there is `n-plus-one-query`, not `expensive-callback`.

### trivial-methods

A stricter subset of cheap. These are effectively free — pure utility functions, null checks, string helpers, assertions. They get excluded from more rules than cheap methods do (including `redundant-expensive-call`, since calling `StringUtils.hasText()` twice with the same input is harmless).

**The test:** "Would anyone ever care that this was called twice?" If not, it's trivial.

```yaml
trivial-methods:
  # These are so lightweight that even redundant calls don't matter
  - hasText          # string null/empty check
  - notNull          # assertion
  - nullSafeEquals   # utility comparison
```

When in doubt between cheap and trivial, pick cheap. It's the safer classification — trivial suppresses more rules.

### heavyweight-types

Types where *construction* is genuinely expensive. Creating these inside a loop is almost always a bug — they open connections, build pools, load configuration, or allocate significant resources.

**The test:** "Would I be alarmed seeing `new X()` inside a for-loop?" If yes, it's heavyweight.

```yaml
heavyweight-types:
  - ApplicationContext     # loads entire Spring context
  - RestTemplate           # creates HTTP client with connection pool
  - WebSocket              # opens network connection
```

Most frameworks only have a handful of heavyweight types. Don't force it — if a framework has none, leave the section out.

### io-methods

Methods that perform I/O — HTTP calls, database queries, file operations. These are relevant for rules like `n-plus-one-query` that detect I/O inside loops. Unlike cheap methods (which suppress findings), I/O methods *enable* detection.

**The test:** "Does this method hit the network, disk, or a database?" If yes, it's an I/O method.

```yaml
io-methods:
  # Each call is a database round-trip
  - save
  - saveAll
  - deleteById
  # Each call is an HTTP request
  - getForObject
  - exchange
  - block          # triggers reactive subscription
```

Not every framework has I/O methods. Utility libraries (Guava, Lodash) don't. Persistence and HTTP frameworks do.

## The workflow

### 1. Run a scan and collect false positives

Scan a real project that uses the framework:

```bash
algorilla /path/to/project --severity info
```

Go through the findings. For each one, ask: "Is this a real performance concern, or is Algorilla misunderstanding a framework method?" Write down the false positives — the method name and which rule flagged it.

### 2. Create the overlay file

Create `core/src/main/resources/semantics/frameworks/<framework>.yml`:

```yaml
# Micronaut — framework overlay for Java
# DI, HTTP client, bean introspection

language: java

methods: {}

cheap-methods:
  # HTTP Client
  - retrieve
  - exchange
  - toBlocking
  # Bean introspection
  - getBean
  - containsBean
  - findBean

trivial-methods:
  # Argument validation
  - requireNonNull
  - requirePresent

heavyweight-types:
  - ApplicationContext
  - EmbeddedServer
```

Group methods by subsystem using comments — it makes the file navigable and helps reviewers understand the reasoning.

### 3. Register it

Append the filename to `core/src/main/resources/semantics/frameworks-index.txt`:

```
micronaut.yml
```

That's it. The registry loads it automatically based on the `language:` key.

### 4. Verify

Re-scan the project. The false positives you identified in step 1 should be gone. If new ones appeared, add those too.

Run the build to make sure nothing broke:

```bash
./gradlew build
```

## Classification guidelines

Some common patterns and how to classify them:

| Pattern | Category | Reasoning |
|---|---|---|
| Getters and property accessors | cheap | Returning a cached value |
| Builder-pattern calls (`.header()`, `.uri()`) | cheap | Configuring state, no I/O |
| Repository/DAO query methods | cheap | Real work, but expected in loops |
| Assertions (`notNull`, `isTrue`) | trivial | Pure validation, effectively free |
| String/collection utility methods | trivial | Simple transformations, no allocation |
| Type-check methods (`isProxy`, `isArray`) | trivial | Boolean check on existing state |
| Connection/context factories | heavyweight | Opens resources, allocates pools |
| Template classes (JdbcTemplate, etc.) | heavyweight | Wraps connection management |

### Edge cases

**Framework method that wraps I/O** (e.g. `restTemplate.getForObject()`): classify as **cheap**, not heavyweight. The method call itself isn't expensive — the issue is calling it *N times in a loop*, which is caught by `n-plus-one-query` or `redundant-expensive-call`. Heavyweight is for *construction*, not *invocation*.

**Method you're unsure about**: leave it out. It's better to have a false positive that someone reports than a false negative that hides a real issue. You can always add methods later.

**Method that exists in multiple frameworks** (e.g. `of()`, `get()`): this is fine. Method names in the YAML are not qualified — they match by name only. If `of()` is cheap in your framework, adding it won't break other frameworks that also have an `of()` method, because overlays are scoped by language.
