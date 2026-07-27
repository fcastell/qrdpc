# github-publishing Specification

## Purpose
Defines how QrDPC's source is tracked in git and published as a public GitHub repository
under the `fcastell` account, so the project has version history and is clonable/citable by
anyone who wants to follow along or contribute.

## Requirements

### Requirement: Local git repository
The project SHALL be tracked by a local git repository whose initial commit contains the
Gradle scaffold, `LICENSE`, `README.md`, and OpenSpec artifacts, and excludes generated
build output and IDE/local-machine state.

#### Scenario: Initial commit excludes build artifacts
- **WHEN** the initial commit is created
- **THEN** no files under `build/`, `.gradle/`, `.kotlin/`, or `local.properties` are
  present in the commit

#### Scenario: Working tree is clean after initial commit
- **WHEN** `git status` is run immediately after the initial commit
- **THEN** it reports no untracked or modified files other than expected build-time
  regenerated artifacts already covered by `.gitignore`

### Requirement: Public GitHub repository
The project SHALL be published as a public GitHub repository named `qrdpc` under the
`fcastell` account, with `main` as its default branch.

#### Scenario: Repository is publicly accessible
- **WHEN** an unauthenticated user visits `github.com/fcastell/qrdpc`
- **THEN** the repository page loads and shows the project's README, LICENSE, and source
  files

#### Scenario: Default branch is main
- **WHEN** the repository is created
- **THEN** its default branch is named `main`

#### Scenario: Repository has a description and topics
- **WHEN** the repository is created
- **THEN** it has a description matching the README's opening summary and topics that
  reflect the project's nature (e.g. `android`, `kotlin`, `qr-code`, `mdm`,
  `managed-configurations`)

### Requirement: Local repository tracks the GitHub remote
The local git repository SHALL have an `origin` remote pointing at
`github.com/fcastell/qrdpc`, with the local `main` branch pushed and tracking
`origin/main`.

#### Scenario: Push succeeds and branch tracks origin
- **WHEN** the initial commit is pushed
- **THEN** `git status` reports the local `main` branch is up to date with `origin/main`
