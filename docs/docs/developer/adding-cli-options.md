# Adding CLI Options

When adding or changing a CLI flag, multiple files need updating in lockstep. This checklist prevents drift between the CLI, Gradle plugin, GitHub Action, and npm wrapper.

## Checklist

1. **Define the option** in `AlgorillaCommand.kt` using PicoCLI's `@Option` annotation. Pick a long name (`--my-flag`), add a description, and set a default value.

2. **Wire it through** in `CommandSupport.kt`. Pass the value to the relevant reporter or engine component. If the option only affects console output, only pass it to `ConsoleReporter`.

3. **Gradle plugin**:
    - Add a `Property<T>` to `AlgorillaExtension.kt`
    - Add a matching task property to `AlgorillaTask.kt` (with the appropriate `@get:Input` annotation)
    - Set the convention (default) in `AlgorillaPlugin.kt` and wire the extension property to the task

4. **GitHub Action** (`action.yml`):
    - Add an input with a default value
    - Add the `INPUT_*` env var to the "Run algorilla" step
    - Pass it to the CLI args (conditionally, if the default means "off")

5. **Contract tests** in `DistributionContractTest.kt`:
    - Add the flag to the action contract list
    - Add the flag to the Gradle contract list
    - Add the flag to the npm passthrough list

6. **CLI tests** in `CliEndToEndTest.kt`:
    - Add the flag to the `--help` assertion list
    - Add at least one end-to-end test exercising the new behavior

7. **User docs**:
    - `quickstart.md` Common Options section (if user-facing)
    - `installation.md` Gradle Plugin Configuration table
    - Relevant guide pages (e.g. `understanding-output.md` for display-related options)

8. **Verify `--help`** shows the new flag with a clear description.

## Tips

- Options that only affect display (like `--limit`, `--color`) should not change exit codes or structured output (JSON/SARIF).
- Use `defaultValue` in the `@Option` annotation so PicoCLI handles the default consistently.
- Short aliases (e.g. `-f` for `--format`) are convenient but check for conflicts first — `-l` is already taken by `--language`.
