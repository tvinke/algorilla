---
tags:
  - JavaScript
---

# JavaScript / TypeScript

**Parser:** Text-based · **File extensions:** `.js`, `.mjs`, `.cjs`, `.ts`, `.tsx`, `.vue` · **Rules:** 21/23

JavaScript, TypeScript, and Vue single-file components share a lightweight text-based parser.

## What the parser handles

- ES6+ syntax (arrow functions, destructuring, template literals, `async`/`await`)
- TypeScript type annotations (used for collection type inference)
- Array and object methods (`map`, `filter`, `reduce`, `forEach`, `find`, `some`, `every`)
- Promise patterns (`.then()`, `await`)
- Vue SFC `<script>` and `<script setup>` blocks
- CommonJS and ES module imports

## JavaScript-specific patterns

```javascript
// Flagged: nested-lookup
orders.filter(o => priorityIds.includes(o.id));  // Array.includes is O(n)

// Better:
const prioritySet = new Set(priorityIds);
orders.filter(o => prioritySet.has(o.id));  // Set.has is O(1)
```

```javascript
// Flagged: repeated-regex-in-loop
for (const line of lines) {
    if (line.match(/^\d{4}-\d{2}-\d{2}/)) { ... }  // regex compiled each iteration
}

// Better:
const datePattern = /^\d{4}-\d{2}-\d{2}/;
for (const line of lines) {
    if (line.match(datePattern)) { ... }
}
```

## TypeScript type inference

When TypeScript type annotations are present, algorilla uses them for more accurate analysis:

```typescript
const ids: Set<string> = new Set(rawIds);
items.filter(i => ids.has(i.id));  // Not flagged — Set.has is O(1)
```

## Vue single-file components

The parser extracts the `<script>` or `<script setup>` section from `.vue` files and analyzes the JavaScript/TypeScript within. Template expressions are not analyzed.

## Rules not available

Two rules target JVM-specific APIs and don't apply to JavaScript/TypeScript:

- `repeated-reflection-in-loop` — Java reflection has no JS equivalent
- `parallel-stream-bottleneck` — parallel streams are a JVM concept
