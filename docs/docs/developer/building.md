# Building

## Prerequisites

- **JDK 21** or later
- **Git**

## Clone & Build

```bash
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew build
```

## Run Tests

```bash
./gradlew check
```

This runs unit tests, Detekt static analysis, and ktlint formatting checks.

## Build the Fat JAR

```bash
./gradlew shadowJar
```

Output: `cli/build/libs/algorilla-<version>.jar`

## Run Locally

```bash
java -jar cli/build/libs/algorilla-<version>.jar --help
```

## Build & serve documentation locally

```bash
pip3 install -r docs/requirements.txt
python3 -m mkdocs serve -f docs/mkdocs.yml
```

Opens a dev server at [http://127.0.0.1:8000/](http://127.0.0.1:8000/) with auto-reload on file changes. Documentation source lives in `docs/docs/`.

## Project Structure

```
algorilla/
├── build-logic/        # Convention plugins (composite build)
├── core/               # IR model, rules, engine, config
├── lang-java/          # Java parser (ANTLR)
├── lang-groovy/        # Groovy parser (Java grammar + preprocessing)
├── lang-kotlin/        # Kotlin parser (Java grammar + preprocessing)
├── lang-javascript/    # JS/TS parser (regex scanner)
├── reporting/          # Console, SARIF, JSON reporters
├── cli/                # Picocli CLI, config loading, custom rules
└── docs/               # MkDocs documentation site
```
