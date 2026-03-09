# Releasing

This page describes how releases work and how to cut one manually if needed.

## How it works

Releases are automated via [Release Please](https://github.com/googleapis/release-please). The process:

1. You merge PRs with [conventional commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `docs:`, etc.)
2. Release Please accumulates changes and opens a "release PR" that bumps the version in `gradle.properties` and updates `CHANGELOG.md`
3. You review and merge the release PR
4. Release Please creates a git tag (`v0.2.0`)
5. The tag triggers the release workflow, which builds the shadow JAR and publishes a GitHub Release

Your only manual step: **merge the release PR**.

## Version management

The project version lives in one place: `gradle.properties`.

```properties
version=0.2.0
```

All modules read this at build time. The CLI `--version` flag reads it from a generated properties file in the JAR. There is no hardcoded version string anywhere in source code.

Release Please bumps this file automatically. You should not need to edit it by hand.

## Manual release

If you need to release without Release Please (e.g., first release, hotfix):

### 1. Update the version

Edit `gradle.properties`:

```properties
version=0.2.0
```

### 2. Update the changelog

Edit `CHANGELOG.md` and replace the `(unreleased)` marker with the release date.

### 3. Commit, tag, push

```bash
git add gradle.properties CHANGELOG.md
git commit -m "chore: release 0.2.0"
git tag v0.2.0
git push origin main --tags
```

The tag triggers the [release workflow](https://github.com/tvinke/algorilla/actions/workflows/release.yml), which:

1. Builds the project and runs all tests
2. Assembles the shadow JAR
3. Creates a GitHub Release with the JAR attached

### 4. Bump to next snapshot

```properties
version=0.3.0-SNAPSHOT
```

```bash
git add gradle.properties
git commit -m "chore: prepare next development cycle"
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
