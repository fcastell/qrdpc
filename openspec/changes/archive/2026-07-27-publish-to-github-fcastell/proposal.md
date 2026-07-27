## Why

QrDPC has no version control yet — there is no `.git` directory in this working copy — so the
existing scaffold (Gradle project, Apache-2.0 `LICENSE`, `README.md`, `.gitignore`) is not
tracked, backed up, or shareable. The project is meant to be a public, community-facing repo
under the personal GitHub account `fcastell`, matching the `io.github.fcastell.qrdpc`
application ID. Publishing it now, while the project is still an early scaffold, establishes
version history from the start and makes the code citable/clonable for anyone who wants to
follow along or contribute.

## What Changes

- Initialize a local git repository in the project root.
- Verify `.gitignore` covers standard Android/Gradle/IDE build artifacts (`build/`, `.gradle/`,
  `.kotlin/`, local `.idea/` or `.vscode/` state, local `local.properties`, etc.) before the
  first commit, so no generated or machine-local files are committed.
- Create the initial commit containing the current scaffold (Gradle config, `:app` module,
  `LICENSE`, `README.md`, OpenSpec artifacts).
- Create a new public GitHub repository named `qrdpc` under the `fcastell` account.
- Add the GitHub repository as the `origin` remote and push the initial commit (and default
  branch) to it.
- Confirm the repository's default branch, description, and visibility (public) on GitHub
  match the project's intent.

## Capabilities

### New Capabilities
- `github-publishing`: establishes and documents the project's git/GitHub setup — local repo
  initialization, ignore rules, remote configuration, and the initial push to a public
  `fcastell/qrdpc` repository on GitHub.

### Modified Capabilities
(none — no existing specs to modify)

## Impact

- Affected paths: repository root (new `.git/` directory), `.gitignore` (reviewed/updated),
  no application code changes.
- Affected systems: GitHub (new remote repository under the `fcastell` account).
- No impact on the Android app's runtime behavior, build configuration, or dependencies.
