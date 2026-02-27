# How Detection Works

Algorilla uses a multi-pass static analysis pipeline to detect algorithmic complexity anti-patterns.

## The Four-Pass Pipeline

### Pass 1: Parse & Collect

Each source file is parsed into a language-agnostic **Intermediate Representation (IR)**. The IR captures the structural elements relevant to complexity analysis:

- **LoopNode** — for, while, forEach, stream operations, Groovy `.each{}`
- **LookupCall** — contains, indexOf, find, filter, any, includes
- **SortCall** — sort, sorted, sortBy, Collections.sort
- **FunctionDecl** — method declarations with parameters
- **FunctionCall** — method invocations
- **ObjectCreation** — `new` expressions
- **VariableDecl** — local variable declarations with types
- **CollectionAccess** — findAll, getAll, list operations

### Pass 2: Call Graph Construction

Function declarations are registered in a project-wide **Symbol Table**. A **Call Graph** is built by resolving function calls to their declarations using qualified names and heuristic matching.

### Pass 3: Complexity Annotation

The call graph is traversed bottom-up to propagate execution context labels. This allows detection of patterns that span multiple methods (e.g., a lookup helper called from inside a loop).

### Pass 4: Rule Evaluation

Each rule traverses the annotated IR trees and reports findings. Rules have access to the full analysis context: IR trees, symbol table, call graph, and configuration.

## O(1) Type Detection

To avoid false positives, Algorilla checks whether a collection target is an O(1) data structure:

1. **Expression text** — checks for `Set`, `Map`, `HashMap`, `HashSet`, `TreeSet`, `TreeMap` in the target expression
2. **Parameter types** — if the target variable is a method parameter, checks its declared type
3. **Variable declarations** — checks the declared type of local variables
4. **Type hints** — user-provided hints in `.algorilla.yml`

## Suppression Handling

After rule evaluation, the **Suppression Filter** removes findings that have `// algorilla:ignore` comments on or near the finding's location and evidence chain locations.
