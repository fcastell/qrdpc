# ci-pipeline Specification

## Purpose
Defines QrDPC's GitHub Actions CI pipeline — the checks that run on every push to `main`
and every pull request, with a pinned, cached, reproducible setup.

## Requirements

### Requirement: Continuous integration on push and pull request
A GitHub Actions workflow SHALL run on every push to `main` and on every pull request
targeting `main`, executing `spotlessCheck`, `detekt`, `test`, `lint`, and `assemble` in
that order (fastest checks first), and SHALL be skipped for changes that only touch
`openspec/**`, `docs/**`, or `**/*.md`.

#### Scenario: CI runs the full check sequence on a code change
- **WHEN** a pull request modifies a file under `app/src/**`
- **THEN** the CI workflow runs `spotlessCheck`, `detekt`, `test`, `lint`, and `assemble`,
  in that order, and the PR cannot merge unless all of them succeed

#### Scenario: Doc-only changes don't trigger CI
- **WHEN** a pull request only modifies files under `openspec/**`, `docs/**`, or matching
  `**/*.md`
- **THEN** the CI workflow does not run for that push/PR

### Requirement: Reproducible, cached CI environment
CI SHALL use a pinned JDK 17 (Temurin), a pinned/commit-SHA-referenced Gradle setup action
with build-cache and configuration-cache caching enabled, and a pinned/commit-SHA-referenced
Android SDK setup action, so runs are reproducible and third-party actions aren't pulled
from a floating tag.

#### Scenario: Third-party actions are pinned to a commit SHA
- **WHEN** the CI workflow references `gradle/actions/setup-gradle` or
  `android-actions/setup-android`
- **THEN** the reference is a full commit SHA (with a version comment), not a floating tag
  like `@v6`
