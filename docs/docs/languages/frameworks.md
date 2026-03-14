---
tags:
  - Frameworks
---

# Framework Support

Algorilla ships with built-in knowledge of popular frameworks. This doesn't add framework-specific rules — it means the analyzer understands which framework methods are cheap, which types are expensive to construct, and which calls are trivial utilities. The result: fewer false positives when scanning codebases that use these frameworks.

## How it works

When you scan a Spring project, Algorilla knows that `BeanUtils.copyProperties()` is a trivial utility call, not an expensive operation worth flagging. It knows that `ApplicationContext` and `RestTemplate` are heavyweight types — constructing them in a loop is a real problem. And it knows that `repository.findById()` is a cheap lookup, so it won't flag it as an expensive callback.

This knowledge is loaded automatically based on the language being scanned. No configuration needed.

## Supported frameworks

### Java

**Spring** — ApplicationContext, BeanFactory, Environment, RestTemplate, WebClient, Spring MVC response builders, Spring Data repositories (queries, paging, specifications), Spring Security, Spring Validation, transaction management, event publishing. Heavyweight types include `ApplicationContext`, `RestTemplate`, `JdbcTemplate`, and `EntityManagerFactory`.

**Guava** — Immutable collection factories, `Cache`/`LoadingCache`, `CacheBuilder`, `Multimap`, `BiMap`, `Table`, `RangeSet`, `Optional`, `Ordering`, `Preconditions`, `Joiner`/`Splitter`, `CharMatcher`, `Hashing`, `Futures`/`ListenableFuture`, `EventBus`, and primitive utilities.

**Project Reactor** — `Mono` and `Flux` transformations (map, flatMap, filter, zip, merge, concat, etc.), factory methods (just, empty, defer, fromCallable, create, generate), Schedulers, context propagation, and signal inspection. I/O-triggering methods (`block`, `subscribe`, `toFuture`) are classified separately. Heavyweight types include deprecated processors (`FluxProcessor`, `EmitterProcessor`).

**JPA / Hibernate** — EntityManager operations (find, persist, merge, remove, refresh, flush, clear), CriteriaBuilder predicates and query composition, Root/Join/Path navigation, TypedQuery configuration (setParameter, setFirstResult, setMaxResults), Hibernate Session (save, saveOrUpdate, update, delete, load, get, evict), ScrollableResults, legacy Criteria API, JPA transaction management, and Spring Data JPA Specification helpers. IO-triggering methods (persist, merge, flush, getResultList, getSingleResult, executeUpdate) are classified separately from cheap query-building methods. Heavyweight types include `EntityManagerFactory`, `SessionFactory`, and `StatelessSession`.

**Apache Commons** — StringUtils (150+ string operations), ObjectUtils, BooleanUtils, NumberUtils, ArrayUtils, ClassUtils, SystemUtils, CharUtils, RegExUtils. CollectionUtils, MapUtils, IterableUtils, IteratorUtils, ListUtils (collection wrappers and utilities). IOUtils (stream copy, read, write), FileUtils (file operations, directory traversal), and FilenameUtils (path string operations). IO-triggering methods (copyFile, moveFile, writeStringToFile, readFileToString, deleteDirectory) are classified separately from in-memory utility methods.

### Kotlin

**Kotlin Coroutines** — Coroutine builders, `Flow` (builders, operators, combining, terminal operations), `Channel`, `Deferred`, `Dispatchers`, `Job` management, `Mutex`/`Semaphore`, and structured concurrency patterns.

**Ktor** — Routing DSL, client, request/response handling, content negotiation, sessions, status pages, WebSockets, and URL building.

### Groovy

**Grails / GORM** — GORM CRUD operations, dynamic finders, criteria/where queries, criteria builder, domain class helpers, controller methods, and service patterns.

**Spock** — Mock creation, interaction DSL, mocking operators, and specification helpers.

### JavaScript / TypeScript

**Vue 3** — Composition API reactivity (`ref`, `reactive`, `computed`, `watch`), lifecycle hooks, provide/inject, Vue Router, Pinia stores, and Vuetify utilities.

**React** — Hooks (`useState`, `useEffect`, `useMemo`, etc.), component factories, render methods, state checks, and React Router.

**Angular** — Dependency injection, signals, change detection, lifecycle hooks, router, and template utilities.

**RxJS** — Creation, transformation, filtering, join, multicast, error handling, and subscription management operators.

**Lodash / Underscore** — Array, collection, object, string, function, math, and general utility methods.

**Node.js / Browser DOM** — DOM event registration, DOM factories, query methods, Node.js `EventEmitter`, `Buffer`, `path`, `process`, `Stream`, `Crypto`, timers, and `Promise` creation. Heavyweight types include `Worker`, `WebSocket`, and `XMLHttpRequest`.

## What "cheap," "trivial," and "heavyweight" mean

- **Cheap methods** are framework calls that don't involve significant computation or I/O. Algorilla won't flag them as expensive operations inside loops.
- **Trivial methods** are utility functions (string helpers, assertion methods, factory calls) that are effectively free. They're excluded from expensive-callback and redundant-call detection.
- **Heavyweight types** are types where construction is genuinely expensive — creating a new `ApplicationContext` or `WebSocket` in a loop is a real performance problem, and Algorilla flags it.

## Missing your framework?

Framework knowledge is defined in YAML overlay files. If your framework isn't listed, you can add method classifications in your `.algorilla.yml` [configuration](../getting-started/configuration.md). If you think it should ship as a built-in overlay, [open an issue](https://github.com/tvinke/algorilla/issues) — or see the [Adding Framework Overlays](../developer/adding-frameworks.md) guide and contribute it yourself. It's just YAML, no Kotlin required.
