## MODIFIED Requirements

### Requirement: Continuous integration on push and pull request
A GitHub Actions workflow SHALL run on every push to `main` and on every pull request
targeting `main`, executing `spotlessCheck`, `detekt`, `test`, `lint`, and `assemble` in
that order (fastest checks first), and SHALL be skipped for changes that only touch
`openspec/**`, `docs/**`, `**/*.md`, or `web/**`.

#### Scenario: CI runs the full check sequence on a code change
- **WHEN** a pull request modifies a file under `app/src/**`
- **THEN** the CI workflow runs `spotlessCheck`, `detekt`, `test`, `lint`, and `assemble`,
  in that order, and the PR cannot merge unless all of them succeed

#### Scenario: Doc-only changes don't trigger CI
- **WHEN** a pull request only modifies files under `openspec/**`, `docs/**`, or matching
  `**/*.md`
- **THEN** the CI workflow does not run for that push/PR

#### Scenario: Web-tool-only changes don't trigger the Android CI job
- **WHEN** a pull request only modifies files under `web/**`
- **THEN** the Android CI workflow does not run for that push/PR (the web tool has its
  own deploy workflow, defined by the `web-tool-hosting` capability)
