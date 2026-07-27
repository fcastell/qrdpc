## Why

This session's work surfaced two undocumented gaps: (1) the live GitHub branch
protection settings on `main` are a real, enforced repo configuration with no
corresponding OpenSpec requirement, and (2) a genuine, non-obvious interaction between
required status checks and `ci.yml`'s `paths-ignore` almost silently broke merging for
every future doc-only PR (a required check that never runs blocks merge — GitHub treats
"never ran" as failing, not passing). Both are worth capturing now, while the details
are fresh, so they don't get rediscovered the hard way later.

## What Changes

- Add a requirement to `claude-workflow-conventions` documenting the current branch
  protection state on `main` (force-push and deletion blocked, PR required with no
  mandatory review count, rules apply to admins too) and the explicit constraint that no
  required status check may be configured while `ci.yml` uses `paths-ignore`, since a
  skipped-but-required check blocks merge indefinitely.
- Broaden the `git-pr-workflow` skill's "archive before PR opens" exception: today it's
  worded narrowly around a task depending on "the CI result of its own PR." In practice
  this session hit the same structural blocker for tasks that need the PR already merged
  to `main` for other reasons (tagging a release, confirming a GitHub Pages deploy).
  Reword the exception to cover any task that structurally requires the PR to already be
  merged, not just CI-result-dependent ones.
- Add a short note to `CLAUDE.md`'s CI section calling out the required-check /
  `paths-ignore` interaction, so future branch-protection or `ci.yml` edits don't
  reintroduce it.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
- `claude-workflow-conventions`: adds a branch-protection-state requirement and
  broadens the existing "archive before PR" scenario's exception condition.

## Impact

- Affected paths: `CLAUDE.md`, `.claude/skills/git-pr-workflow/SKILL.md`,
  `openspec/specs/claude-workflow-conventions/spec.md` (via this change's delta).
- No code or CI behavior changes — documentation/spec only.
