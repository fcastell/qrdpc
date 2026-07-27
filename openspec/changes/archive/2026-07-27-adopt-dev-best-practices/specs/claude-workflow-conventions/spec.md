## ADDED Requirements

### Requirement: Documented default workflow
The repository SHALL have a root `CLAUDE.md` that documents QrDPC-specific working
conventions for Claude Code sessions and references the `git-pr-workflow` and
`session-wrapup` skills rather than duplicating their content, so these conventions apply
by default without needing to be restated each session.

#### Scenario: CLAUDE.md exists and references skills
- **WHEN** a Claude Code session starts in this repository
- **THEN** `CLAUDE.md` is present at the repo root and points to
  `.claude/skills/git-pr-workflow/SKILL.md` and
  `.claude/skills/session-wrapup/SKILL.md` for the detailed procedures

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
  depends on that PR's own CI result — in which case the PR opens first, and archiving
  happens after the CI result is known and recorded

### Requirement: CI-gated merge
Pull requests SHALL NOT be merged while any CI check is failing or still in progress. Once
all checks pass, the default merge mode is squash-merge with branch deletion.

#### Scenario: Merge waits for green CI
- **WHEN** a pull request has a CI check that is still running or has failed
- **THEN** the PR is not merged until that check completes successfully

#### Scenario: Default merge mode
- **WHEN** a pull request's CI checks are all green
- **THEN** it is merged via squash-merge with the source branch deleted, unless the user
  has asked to review before merging

### Requirement: Session close-out checklist
Before closing a session, or on request for a wrap-up, the `session-wrapup` skill SHALL be
used to check git state, OpenSpec state, recurring patterns worth formalizing, and
spec/code drift — without taking any corrective action without explicit user confirmation.

#### Scenario: Wrap-up surfaces uncommitted or unpushed work
- **WHEN** the `session-wrapup` skill runs and uncommitted changes or unpushed commits
  exist on the current branch
- **THEN** they are listed to the user rather than silently left out of the summary
