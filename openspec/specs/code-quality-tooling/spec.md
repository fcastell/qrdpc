# code-quality-tooling Specification

## Purpose
Defines the static analysis and formatting tooling enforced on QrDPC's Kotlin codebase —
detekt, spotless/ktlint, and a lefthook pre-commit hook — ported from `rms-mobile-app` and
adapted to QrDPC's single `:app` module.

## Requirements

### Requirement: Static analysis via detekt
The project SHALL run `detekt` static analysis over Kotlin sources, configured via a root
`detekt.yml` built upon detekt's default ruleset, with `FunctionNaming` ignored for
functions annotated `@Composable`.

#### Scenario: detekt runs and respects the Composable naming exception
- **WHEN** `./gradlew detekt` runs
- **THEN** it completes using `detekt.yml`'s rules, and a `@Composable` function with a
  capitalized name is not flagged by `FunctionNaming`

### Requirement: Formatting enforcement via spotless/ktlint
The project SHALL enforce Kotlin and Kotlin-Gradle formatting via `spotless` using
`ktlint`, covering `src/**/*.kt` and `*.gradle.kts` files.

#### Scenario: spotlessCheck fails on unformatted code
- **WHEN** a Kotlin source file violates the configured `ktlint` formatting rules
- **THEN** `./gradlew spotlessCheck` fails

#### Scenario: spotlessApply fixes formatting
- **WHEN** `./gradlew spotlessApply` runs on a file with formatting violations
- **THEN** the file is rewritten to comply with the configured `ktlint` rules

### Requirement: Pre-commit auto-formatting
The project SHALL provide a `lefthook` pre-commit hook that runs `spotlessApply` on staged
Kotlin files and re-stages the fixed output, so formatting issues are caught before a
commit is made rather than only in CI.

#### Scenario: Staged Kotlin file is reformatted before commit
- **WHEN** a commit is created and a staged `.kt`/`.kts` file has formatting violations,
  with `lefthook` hooks installed
- **THEN** `spotlessApply` runs, the fixed file is re-staged, and the corrected version is
  what gets committed
