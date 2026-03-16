---
tags:
  - JavaScript
  - React
  - Angular
  - Node.js
  - performance
description: "JavaScript and TypeScript complexity analysis — detects Array.includes() in loops, sequential awaits, regex recompilation, and more across Vue, React, Angular, and Node.js codebases."
---

# JavaScript / TypeScript

!!! note "Beta"
    TypeScript and JavaScript analysis relies on name-based heuristics since these languages lack static type declarations. Expect higher false-positive rates than JVM languages, especially on large codebases. Use `--confidence high` to see only the most certain findings.

JavaScript arrays have a method called `includes()`. It does what you think — and it's O(n):

```javascript
const processed = getProcessedIds();

for (const event of incoming) {
    if (processed.includes(event.id)) {
        skip(event);
    }
}
```

Algorilla catches it:

```
warning  · nested-lookup · Loop amplifiers · O(incoming × processed) → O(incoming + processed)

  Linear includes on 'processed' inside for-each loop
  → Build a HashSet/Map from 'processed' before the loop
  ↳ https://tvinke.github.io/algorilla/rules/nested-lookup

      3 │ for (const event of incoming) {
      4 │     if (processed.includes(event.id)) {

  ⎿  for-each loop over incoming   O(incoming)
    ⎿  includes on 'processed'   O(processed) ← bottleneck
```

The fix is `new Set(processed)` and `.has()` instead of `.includes()`. Whether that matters depends on the size of `processed` — but now you can make that call consciously.

## Top complexity traps in JavaScript/TypeScript

### Array.includes() inside filter()

The most common finding in JS codebases. `Array.includes()` is O(n) — fine on its own, quadratic inside a loop or `filter()`.

```javascript
const allowedIds = getAllowedIds(); // returns Array

const filtered = items.filter(item =>
    allowedIds.includes(item.id) // O(n) per item
);
```

```
warning  · nested-lookup · O(items × allowedIds) → O(items + allowedIds)

  → Build a HashSet/Map from 'allowedIds' before the loop
  ↳ https://tvinke.github.io/algorilla/rules/nested-lookup
```

Fix: `const allowedSet = new Set(allowedIds)` then `allowedSet.has(item.id)`. See [nested-lookup](../rules/nested-lookup.md).

### await in a for loop

Sequential `await` inside a loop makes each iteration wait for the previous one. 100 API calls at 50ms each = 5 seconds. With `Promise.all`, it's ~50ms total.

```javascript
for (const id of orderIds) {
    const response = await fetch(`/api/orders/${id}`);
    results.push(await response.json());
}
```

```
warning  · io-in-loop · O(orderIds·IO) → O(1·IO + orderIds)

  fetch() called inside for-each loop — IO per iteration
  → Batch the IO operation outside the loop, or use a bulk API
  ↳ https://tvinke.github.io/algorilla/rules/io-in-loop
```

Fix: [`Promise.all`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all) with `orderIds.map(id => fetch(...))`, or a batch endpoint. See [io-in-loop](../rules/io-in-loop.md).

### new RegExp() on every iteration

Regex literals inside loops get recompiled per iteration. Not cheap — the regex engine builds an NFA/DFA on every call.

```javascript
for (const line of lines) {
    const parts = line.replace(/\s+/g, ' ');
    const matched = line.match(/^(\d+)\s+(.*)$/);
}
```

```
warning  · regex-recompilation-in-loop · O(|lines| × compile) → O(|lines|)

  replace() compiles a regex on every call inside for-each loop
  ↳ https://tvinke.github.io/algorilla/rules/regex-recompilation-in-loop
```

Pull the regex out of the loop and assign it to a `const`. See [regex-recompilation-in-loop](../rules/regex-recompilation-in-loop.md).

### React re-renders and expensive callbacks

Expensive operations inside callbacks passed to `filter()`, `map()`, or event handlers. In React, these run on every render if not memoized.

```jsx
function OrderList({ orders, discounts }) {
    const display = orders.map(order => {
        const discount = discounts.find(d => d.productId === order.productId);
        return <OrderRow order={order} discount={discount} />;
    });
    // ...
}
```

```
warning  · nested-lookup · O(orders × discounts)

  Linear find on 'discounts' inside map()
  → Build a HashSet/Map from 'discounts' before the loop
  ↳ https://tvinke.github.io/algorilla/rules/nested-lookup
```

Fix: build a `Map` from `discounts` before the `map()`, or wrap in [`useMemo()`](https://react.dev/reference/react/useMemo). See [expensive-callback](../rules/expensive-callback.md).

## Regex recompilation in loops

This one surprises people. Regex literals inside loops get recompiled on every iteration:

```javascript
for (const line of lines) {
    const parts = line.replace(/\s+/g, ' ');
    const matched = line.match(/^(\d+)\s+(.*)$/);
}
```

```
warning  · regex-recompilation-in-loop · O(|lines| × compile) → O(|lines|)

  replace() compiles a regex on every call inside for-each loop
  ↳ https://tvinke.github.io/algorilla/rules/regex-recompilation-in-loop

  ⎿  for-each loop   O(|lines|)
    ⎿  line.replace() inside loop   compile ← bottleneck
```

Pull the regex out of the loop, assign it to a `const`, done.

## TypeScript type inference

When TypeScript type annotations are present, Algorilla uses them for more accurate analysis:

```typescript
const ids: Set<string> = new Set(rawIds);
items.filter(i => ids.has(i.id));  // Not flagged — Set.has is O(1)
```

Without the type annotation, a `new Set()` is still recognized. But explicit types help in ambiguous cases.

## Vue single-file components

The parser extracts `<script>` and `<script setup>` blocks from `.vue` files and analyzes the JavaScript/TypeScript within. Template expressions are not analyzed.

Supported extensions: `.js`, `.mjs`, `.cjs`, `.ts`, `.tsx`, `.vue`

## Framework knowledge

| Framework | What Algorilla knows | Most relevant rules |
|-----------|---------------------|-------------------|
| **Vue 3** | `ref`, `reactive`, `computed`, `watch`, lifecycle hooks, Pinia stores | [nested-lookup](../rules/nested-lookup.md), [expensive-callback](../rules/expensive-callback.md) |
| **React** | Hooks, components, render methods, React Router | [nested-lookup](../rules/nested-lookup.md), [expensive-callback](../rules/expensive-callback.md) |
| **Angular** | DI, signals, change detection, lifecycle hooks | [nested-lookup](../rules/nested-lookup.md), [io-in-loop](../rules/io-in-loop.md) |
| **RxJS** | Creation, transformation, filtering, join operators | [sequential-async-join-in-loop](../rules/sequential-async-join-in-loop.md) |
| **Lodash** | Array, collection, object utilities | [nested-lookup](../rules/nested-lookup.md), [repeated-linear-scan](../rules/repeated-linear-scan.md) |
| **Node.js / DOM** | EventEmitter, Buffer, fs, Stream, Crypto | [io-in-loop](../rules/io-in-loop.md), [expensive-construction](../rules/expensive-construction.md) |

For the full list, see [Framework support](frameworks.md).

## Rules not available

Two rules target JVM-specific APIs and don't apply to JavaScript/TypeScript:

- `repeated-reflection-in-loop` — Java reflection has no JS equivalent
- `parallel-pipeline-bottleneck` — parallel streams are a JVM concept

## See also

- [All rules](../rules/index.md) — 26 of 28 rules apply to JavaScript/TypeScript
- [Framework support](frameworks.md) — built-in knowledge of Vue, React, Angular, RxJS, Lodash, and Node.js
- [Configuration](../getting-started/configuration.md) — type hints, suppression, severity thresholds
- [Understanding the output](../guide/understanding-output.md) — severity levels, the complexity chain, confidence scores
