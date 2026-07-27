## Context

During the previous session, branch protection was enabled on `main`
(`allow_force_pushes: false`, `allow_deletions: false`, `required_pull_request_reviews`
with `required_approving_review_count: 0`, `enforce_admins: true`) via `gh api`, with no
corresponding OpenSpec update. A `required_status_checks` entry for the `android` job
was also added and then removed after it was found to permanently block doc-only PRs:
`ci.yml`'s `paths-ignore` means the `android` job never runs for changes limited to
`openspec/**`, `docs/**`, `**/*.md`, or `web/**`, and GitHub's branch protection treats a
required check that never reports any status as blocking, not passing.

## Goals / Non-Goals

**Goals:**
- Record the current branch protection state as a spec requirement so it's not just
  tribal knowledge from a `gh api` command run once.
- Prevent a future re-introduction of the required-check / `paths-ignore` deadlock by
  documenting the constraint explicitly.
- Broaden `git-pr-workflow`'s archive-before-PR exception wording to match how it's
  actually been applied (any PR-must-be-merged-first task, not just CI-result ones).

**Non-Goals:**
- No change to the actual branch protection configuration (it already reflects the
  intended end state from this session).
- No change to `ci.yml`'s `paths-ignore` behavior.

## Decisions

- **No required status checks while `paths-ignore` is in use**: document this as an
  explicit constraint rather than re-deriving it next time branch protection is touched.
  If CI ever needs to be a hard merge gate again, the `paths-ignore` skip and the
  required-check config must change together (e.g. make the job always run but skip its
  own steps conditionally, instead of skipping the whole workflow via `paths-ignore`).
- **Fold both findings into the existing `claude-workflow-conventions` capability**
  rather than a new one — branch protection and PR/merge mechanics are already part of
  that capability's scope (see its "CI-gated merge" and "Branch and PR discipline"
  requirements).

## Risks / Trade-offs

- [Branch protection is edited again directly via `gh api` without updating this spec] →
  Same class of drift this change is fixing; no automated enforcement, relies on
  `session-wrapup`'s completeness check to catch it next time.
