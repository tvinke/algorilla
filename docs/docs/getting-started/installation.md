# Installation

## Requirements

- **Java 11** or later (to run)
- **Java 21** or later (only needed to build from source)

## Download

Download the latest `algorilla.jar` from the [releases page](https://github.com/tvinke/algorilla/releases).

## Build from Source

```bash
git clone https://github.com/tvinke/algorilla.git
cd algorilla
./gradlew shadowJar
```

The fat JAR will be at `cli/build/libs/algorilla-<version>.jar`.

## Verify Installation

```bash
java -jar algorilla.jar --version
```

```
algorilla 0.1.0
```
