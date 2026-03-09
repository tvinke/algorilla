# Releasing

This page describes how to cut a new release of Algorilla.

## Overview

Releases are automated through GitHub Actions. Pushing a version tag triggers the release workflow, which builds the project, runs tests, assembles the fat JAR, and publishes a GitHub Release with the artifact attached.

## Prerequisites

- You are on the `main` branch with a clean working tree
- All CI checks pass
- The changelog is up to date

## Step by step

### 1. Update the version

Open `build-logic/src/main/kotlin/algorilla.kotlin-common.gradle.kts` and change the version from `SNAPSHOT` to the release version:

```kotlin
// before
version = "0.2.0-SNAPSHOT"

// after
version = "0.2.0"
```

### 2. Update the changelog

Edit `CHANGELOG.md`. Replace the `(unreleased)` marker with the release date:

```markdown
## 0.2.0 (2026-04-15)
```

Review the entries and make sure they cover all user-visible changes since the last release.

### 3. Update documentation references

Check these files for version-specific strings:

- `README.md` — Quick Start examples, download links
- `docs/docs/getting-started/installation.md` — JAR download instructions
- `docs/docs/getting-started/quickstart.md` — command examples

### 4. Commit the release

```bash
git add -A
git commit -m "prepare release 0.2.0"
```

### 5. Tag and push

```bash
git tag v0.2.0
git push origin main --tags
```

This triggers the [release workflow](https://github.com/tvinke/algorilla/actions/workflows/release.yml), which:

1. Checks out the tagged commit
2. Builds the project and runs all tests
3. Assembles the shadow JAR via `./gradlew :cli:shadowJar`
4. Creates a GitHub Release named `algorilla 0.2.0`
5. Attaches `algorilla-0.2.0.jar` as a downloadable asset

### 6. Verify the release

Check the [Releases page](https://github.com/tvinke/algorilla/releases) and confirm:

- The release exists with the correct tag
- The JAR is attached and downloadable
- Release notes look reasonable (auto-generated from commit history)

Edit the release description if you want to add highlights or context beyond the auto-generated notes.

### 7. Prepare the next development cycle

Bump the version to the next snapshot:

```kotlin
version = "0.3.0-SNAPSHOT"
```

Add a new section to `CHANGELOG.md`:

```markdown
## 0.3.0 (unreleased)
```

Commit and push:

```bash
git add -A
git commit -m "prepare next development cycle"
git push origin main
```

## Version scheme

Algorilla follows [Semantic Versioning](https://semver.org/):

- **Patch** (0.1.x): Bug fixes, documentation, minor rule tuning
- **Minor** (0.x.0): New rules, new CLI options, new language support
- **Major** (x.0.0): Breaking changes to config format, rule IDs, or output format

During the 0.x phase, minor versions may include breaking changes as the API stabilizes.

## Troubleshooting

**Release workflow failed?** Check the [Actions tab](https://github.com/tvinke/algorilla/actions/workflows/release.yml). Common causes:

- Test failure on the tagged commit — fix, delete the tag (`git push --delete origin v0.2.0 && git tag -d v0.2.0`), re-tag after fixing
- Permissions issue — the workflow needs `contents: write` permission (already configured)

**Wrong artifact uploaded?** The workflow uploads all JARs matching `cli/build/libs/algorilla-*.jar`. If extra JARs appear, check that `archiveClassifier` is set to `""` in `cli/build.gradle.kts`.
