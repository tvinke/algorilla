---
tags:
  - Frameworks
  - Spring
  - JPA
  - Guava
  - Reactor
  - Coroutines
  - Ktor
  - GORM
  - Grails
  - Vue
  - React
  - Angular
  - RxJS
  - Node.js
description: "Built-in framework knowledge in Algorilla — how framework overlays reduce false positives for Spring, JPA, Guava, Vue, React, Angular, and 10+ more frameworks."
---

# Framework Support

Algorilla ships with built-in knowledge of popular frameworks. This doesn't add framework-specific rules — it means the analyzer understands which framework methods are cheap, which types are expensive to construct, and which calls are trivial utilities. The result: fewer false positives when scanning codebases that use these frameworks.

## How it works

When you scan a Spring project, Algorilla knows that `BeanUtils.copyProperties()` is a trivial utility call, not an expensive operation worth flagging. It knows that `ApplicationContext` and `RestTemplate` are heavyweight types — constructing them in a loop is a real problem. And it knows that `repository.findById()` is a database call, so it flags it inside loops as an [N+1 query](../rules/n-plus-one-query.md).

This knowledge is loaded automatically based on the language being scanned. No configuration needed.

## Supported frameworks

=== "Java"

    | Framework | What Algorilla knows | Most relevant rules |
    |-----------|---------------------|-------------------|
    | **Spring** | ApplicationContext, BeanFactory, RestTemplate, WebClient, Spring MVC, Data repositories, Security, Validation, transaction management, event publishing | [n-plus-one-query](../rules/n-plus-one-query.md), [io-in-loop](../rules/io-in-loop.md), [expensive-construction](../rules/expensive-construction.md) |
    | **JPA / Hibernate** | EntityManager, CriteriaBuilder, Session, TypedQuery, ScrollableResults, Specification helpers. IO-triggering methods (`persist`, `flush`, `getResultList`) are classified separately from cheap query builders | [n-plus-one-query](../rules/n-plus-one-query.md), [lazy-loading-in-loop](../rules/lazy-loading-in-loop.md), [io-in-loop](../rules/io-in-loop.md) |
    | **Guava** | Immutable collection factories, `Cache`/`LoadingCache`, `Multimap`, `BiMap`, `Table`, `RangeSet`, `Optional`, `Ordering`, `Joiner`/`Splitter`, `Hashing`, `Futures`, `EventBus` | [nested-lookup](../rules/nested-lookup.md), [redundant-expensive-call](../rules/redundant-expensive-call.md) |
    | **Project Reactor** | `Mono`/`Flux` transformations, factory methods, Schedulers, context propagation. `block()`, `subscribe()`, `toFuture()` are I/O-triggering | [io-in-loop](../rules/io-in-loop.md), [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md) |
    | **Apache Commons** | StringUtils (150+ ops), CollectionUtils, IOUtils, FileUtils, NumberUtils, ArrayUtils. IO methods (`copyFile`, `writeStringToFile`) classified separately from in-memory utilities | [io-in-loop](../rules/io-in-loop.md), [redundant-expensive-call](../rules/redundant-expensive-call.md) |
    | **Jackson** | ObjectMapper, JsonParser, JsonGenerator, serialization/deserialization methods | [expensive-construction](../rules/expensive-construction.md), [expensive-serialization-in-loop](../rules/expensive-serialization-in-loop.md) |

=== "Kotlin"

    | Framework | What Algorilla knows | Most relevant rules |
    |-----------|---------------------|-------------------|
    | **Kotlin Coroutines** | Builders (`launch`, `async`, `runBlocking`), `Flow` (builders, operators, terminal ops), `Channel`, `Deferred`, `Dispatchers`, `Job`, `Mutex`/`Semaphore`, structured concurrency | [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md), [io-in-loop](../rules/io-in-loop.md) |
    | **Ktor** | Routing DSL, client (`get`, `post`, `submitForm`), content negotiation, sessions, WebSockets, URL building | [io-in-loop](../rules/io-in-loop.md), [expensive-construction](../rules/expensive-construction.md) |

    Kotlin also inherits all Java framework knowledge — Spring, JPA, Guava, Reactor, and Apache Commons all apply when detected in Kotlin code.

=== "Groovy"

    | Framework | What Algorilla knows | Most relevant rules |
    |-----------|---------------------|-------------------|
    | **Grails / GORM** | CRUD operations (`save`, `delete`, `get`), dynamic finders (`findBy*`, `countBy*`), criteria/where queries, domain class helpers, controller methods | [n-plus-one-query](../rules/n-plus-one-query.md), [io-in-loop](../rules/io-in-loop.md) |
    | **Spock** | Mock creation, interaction DSL, mocking operators, specification helpers | — (test framework, mainly used for false-positive suppression) |

    Groovy also inherits all Java framework knowledge.

=== "JavaScript / TypeScript"

    | Framework | What Algorilla knows | Most relevant rules |
    |-----------|---------------------|-------------------|
    | **Vue 3** | `ref`, `reactive`, `computed`, `watch`, `watchEffect`, lifecycle hooks, `provide`/`inject`, Vue Router, Pinia stores, Vuetify utilities | [nested-lookup](../rules/nested-lookup.md), [expensive-callback](../rules/expensive-callback.md) |
    | **React** | `useState`, `useEffect`, `useMemo`, `useCallback`, `useRef`, component factories, render methods, React Router | [nested-lookup](../rules/nested-lookup.md), [expensive-callback](../rules/expensive-callback.md) |
    | **Angular** | DI, signals, change detection, lifecycle hooks, router, template utilities | [nested-lookup](../rules/nested-lookup.md), [io-in-loop](../rules/io-in-loop.md) |
    | **RxJS** | Creation (`of`, `from`, `interval`), transformation (`map`, `flatMap`, `switchMap`), filtering, join, multicast, error handling, subscription management | [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md) |
    | **Lodash / Underscore** | Array, collection, object, string, function, math, and general utility methods | [nested-lookup](../rules/nested-lookup.md), [repeated-linear-scan](../rules/repeated-linear-scan.md) |
    | **Node.js / Browser DOM** | DOM events, DOM factories, query methods, `EventEmitter`, `Buffer`, `path`, `process`, `Stream`, `Crypto`, timers, `Promise`. Heavyweight: `Worker`, `WebSocket`, `XMLHttpRequest` | [io-in-loop](../rules/io-in-loop.md), [expensive-construction](../rules/expensive-construction.md) |

## What "cheap," "trivial," and "heavyweight" mean

- **Cheap methods** are framework calls that don't involve significant computation or I/O. Algorilla won't flag them as expensive operations inside loops.
- **Trivial methods** are utility functions (string helpers, assertion methods, factory calls) that are effectively free. They're excluded from expensive-callback and redundant-call detection.
- **Heavyweight types** are types where construction is genuinely expensive — creating a new `ApplicationContext` or `WebSocket` in a loop is a real performance problem, and Algorilla flags it.

## Missing your framework?

Framework knowledge is defined in YAML overlay files. If your framework isn't listed, you can add method classifications in your `.algorilla.yml` [configuration](../getting-started/configuration.md). If you think it should ship as a built-in overlay, [open an issue](https://github.com/tvinke/algorilla/issues) — or see the [Adding Framework Overlays](../developer/adding-frameworks.md) guide and contribute it yourself. It's just YAML, no Kotlin required.
