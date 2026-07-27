## MODIFIED Requirements

### Requirement: Branch and PR discipline
All non-trivial work SHALL happen on a dedicated branch (never directly on `main`) and be
merged via a pull request, following the `git-pr-workflow` skill checklist: commit only on
explicit user request, commit messages in English via heredoc without a
`Co-Authored-By: Claude` trailer, stage files explicitly rather than `git add -A`/`.` when
unrelated local changes are present.

#### Scenario: Work starts on a dedicated branch
- **WHEN** a non-trivial change begins and the current branch is `main`
- **THEN** a new branch is created before any commit is made

#### Scenario: OpenSpec change is archived before its PR opens
- **WHEN** a branch implements an OpenSpec change and is ready for a pull request
- **THEN** the change has already been archived (moved to
  `openspec/changes/archive/YYYY-MM-DD-<name>/` with its spec synced into
  `openspec/specs/`) before the PR is opened, unless a task in that change structurally
  requires the PR to already be merged to `main` (e.g. it depends on that PR's own CI
  result, on tagging a release from `main`, or on confirming a deploy that only runs on
  `main`) — in which case the PR opens first, and archiving happens after that outcome is
  known and recorded

### Requirement: CI-gated merge
Pull requests SHALL NOT be merged while any CI check is failing or still in progress. Once
all checks pass, the default merge mode is squash-merge with branch deletion. Branch
protection on `main` SHALL NOT list a check as a required status check while `ci.yml`
uses `paths-ignore` to skip that check's job for some file patterns, since GitHub treats
a required check that never reports any status as blocking merge rather than passing.

#### Scenario: Merge waits for green CI
- **WHEN** a pull request has a CI check that is still running or has failed
- **THEN** the PR is not merged until that check completes successfully

#### Scenario: Default merge mode
- **WHEN** a pull request's CI checks are all green
- **THEN** it is merged via squash-merge with the source branch deleted, unless the user
  has asked to review before merging

#### Scenario: Doc-only PR is not blocked by a skipped required check
- **WHEN** a pull request only touches paths covered by `ci.yml`'s `paths-ignore` (so the
  `android` job never runs for it)
- **THEN** branch protection does not require that job's status, and the PR remains
  mergeable once other requirements (e.g. PR existence) are satisfied

## ADDED Requirements

### Requirement: Branch protection state on main
`main` SHALL have branch protection configured to block force pushes and branch
deletion, and to require a pull request before merging (no mandatory approving review
count, since this is a solo-maintainer repo), with rules enforced for admins as well as
other collaborators.

#### Scenario: Force push and deletion are blocked
- **WHEN** a `git push --force` or branch-deletion request targets `main`
- **THEN** GitHub rejects it, for any collaborator including repo admins

#### Scenario: Direct push without a PR is blocked
- **WHEN** a commit is pushed directly to `main` without going through a pull request
- **THEN** GitHub rejects it, consistent with the `git-pr-workflow` convention of never
  committing directly to `main`
