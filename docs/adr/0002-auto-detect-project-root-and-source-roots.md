# ADR-0002: Auto-detect project root and source roots

**Status**: Accepted

**Date**: 2026-03-08

## Context

When a user runs `algorilla ../animal-app/src/main`, Algorilla scans that directory but also places its `.algorilla/` cache directory there. This leads to an unexpected `src/main/.algorilla/` folder inside the target project. Users expect tool metadata directories (like `.git/`, `.gradle/`, `.idea/`) to live in the project root, not in an arbitrary subdirectory.

Additionally, users currently have to know the internal source layout of a project and point Algorilla at the right subdirectory. For a Gradle project like `animal-app`, the idiomatic invocation should be `algorilla ../animal-app` — Algorilla should figure out that sources live in `src/main/` (and in submodule equivalents).

The existing `ProjectStructureDetector` already walks up the directory tree to find build markers (`build.gradle.kts`, `pom.xml`) and parses `settings.gradle.kts` for submodules, but this logic is only used for test-directory exclusion. It is not used to resolve source roots or to determine where `.algorilla/` should be placed.

## Decision

Extend `ProjectStructureDetector` to serve as the single source of truth for project layout. When Algorilla receives a path argument:

1. **Resolve the project root.** Walk up from the given path looking for build system markers (`build.gradle.kts`, `build.gradle`, `settings.gradle.kts`, `settings.gradle`, `pom.xml`, `package.json`). If found, that directory is the project root. If not found, the given path itself is treated as the project root.

2. **Place `.algorilla/` in the project root.** Cache, baseline, and any other Algorilla metadata always go in `<project-root>/.algorilla/`, regardless of which path the user passed on the CLI.

3. **Auto-detect source roots by build system.** When the user points at a project root (or a parent that contains one), derive the directories to scan:

   | Build system | Detection marker | Source roots |
   |---|---|---|
   | Gradle (single module) | `build.gradle(.kts)` without `settings.gradle(.kts)`, or settings with no `include` | `src/main/java`, `src/main/kotlin`, `src/main/groovy` (whichever exist) |
   | Gradle (multi-module) | `settings.gradle(.kts)` with `include(...)` | Per module: `<module>/src/main/{java,kotlin,groovy}` (whichever exist) |
   | Maven (single module) | `pom.xml` without `<modules>` | `src/main/java` |
   | Maven (multi-module) | `pom.xml` with `<modules>` | Per module: `<module>/src/main/java` |
   | JS/TS | `package.json` | Project root (rely on `node_modules/`, `dist/`, `build/` exclusions) |
   | None detected | — | The given path as-is (current behavior, no change) |

4. **Explicit paths override auto-detection.** If the user passes a path that is clearly a subdirectory within a project (e.g. `src/main/java/com/example`), scan exactly that path — but still resolve the project root upwards for `.algorilla/` placement. This preserves the ability to do targeted scans.

5. **Multiple path arguments.** When multiple paths are given, resolve each independently. Place `.algorilla/` in the project root of the first path.

## Consequences

- `algorilla .` at a Gradle project root now does the right thing without the user needing to specify `src/main`.
- `.algorilla/` always appears in the project root, alongside `.git/`, `.gradle/`, etc. Users can add it to `.gitignore` in one predictable location.
- The existing `--exclude` flag and `.algorilla.yml` config remain available for fine-tuning.
- For unrecognized build systems, behavior is unchanged — no risk of regression.
- JS/TS projects with non-standard layouts (monorepos, custom bundler configs) may scan more files than necessary. This is acceptable: the exclusion list already filters `node_modules/` and `dist/`, and users can add `--exclude` patterns. Smarter JS/TS detection can be added later without changing this architecture.
- `ProjectStructureDetector` gains more responsibility but remains a single, cohesive class focused on project layout concerns.
