# Parser fixtures

Add small source files here, one per language construct you want to test. Keep them
focused — a fixture for loop parsing only needs a class with a couple of loops, not
a full application.

Naming convention: `<language>-<construct>.ext`, e.g. `python-loops.py`,
`python-chains.py`, `python-control-flow.py`.

Each fixture should exercise at least:
- Function/method declarations (with parameters and return types)
- For, while, and higher-order loops (forEach, map, etc.)
- Lookup calls: list.contains(), map.get(), indexOf()
- Sort calls: list.sort(), sorted()
- Object/constructor creation
- Variable declarations with type info
- Chained calls (e.g. list.filter { ... }.map { ... })
- Control flow: if/else, switch/match/when, try/catch
