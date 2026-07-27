---
name: git-pr-workflow
description: Explicit checklist for commit/push/PR/merge on this project (QrDPC). Use before opening or merging a Pull Request, or when the user asks to commit/push/merge/archive an OpenSpec change.
metadata:
  author: fabien-castell
  version: "1.0"
---

Checklist for this project's git + PR flow — the single source of truth for these rules
(the root `CLAUDE.md` points here instead of duplicating them). Applies by default to any
git/PR/archiving action on this repo, whether explicitly invoked or not.

## 1. Before committing

- [ ] On a dedicated branch, never on `main`. If still on `main`, create the branch now
      (`git checkout -b <name>`).
- [ ] The commit is only made after an explicit request from the user.
- [ ] The commit message is in English, via heredoc, **without** a
      `Co-Authored-By: Claude ...` trailer.
- [ ] Files unrelated to the change (e.g. `.vscode/settings.json` modified by the IDE) are
      excluded from `git add` — stage by explicit file list rather than `git add -A`/`.`.

## 2. Before opening a PR

- [ ] If the branch implements an OpenSpec change: **verify it has been archived**
      (`openspec/changes/<name>/` no longer exists, moved to
      `openspec/changes/archive/YYYY-MM-DD-<name>/`, and its spec synced into
      `openspec/specs/`).
  - If not yet archived: do NOT open the PR. Do not archive on your own initiative — wait
    for the user to explicitly ask, then do it on this same branch before (re)pushing and
    opening the PR.
  - **Exception**: if a task in the change structurally requires the PR to already be
    merged to `main` — depends on the CI result of its own PR (e.g. "confirm CI passes on
    the PR"), needs to tag a release from `main`, or needs to confirm a deploy that only
    runs on `main` (e.g. GitHub Pages) — that task is unverifiable before the PR exists or
    is merged. In that case, the order reverses: push the branch and open the PR first,
    get it merged, THEN archive and commit that task's real outcome (checked if it
    succeeded, fixed if not) — never archive the change as complete while that outcome is
    still unknown. Archiving itself still goes through its own branch + PR afterward, per
    this same checklist.
- [ ] The commit(s) have been made and pushed on the branch.
- [ ] The PR body (`gh pr create --body`) summarizes the changes and the test plan, in
      English, consistent with the commits.

## 3. After opening the PR

- [ ] Wait for all CI checks to pass (`gh pr checks <n>`), including when no check
      triggers at all (doc-only PR, see `paths-ignore` in `ci.yml` — that's expected, not
      a blocker). Branch protection has no required status check configured specifically
      so this case can't deadlock a merge — see `CLAUDE.md`'s CI section before adding one
      back.
- [ ] Never merge on a failing or still-running check.
- [ ] After a new follow-up push on an already-open PR (fix, version bump...), cancel any
      still-running CI runs on previous commits (`gh run cancel <id>`, found via
      `gh run list --branch <branch>`) rather than letting them run alongside the run for
      the latest commit.
- [ ] Once green: merge (`gh pr merge <n> --squash --delete-branch`) without asking for
      confirmation again, unless the user indicated they want to review first.
- [ ] After merge: `git checkout main && git pull --ff-only`, clean up the local branch if
      it's still around (`git branch -d <name>`).
- [ ] Never `--force`/`--force-with-lease` on `main`, and no destructive merge/rebase
      without explicit consent.

## Quick reference

- OpenSpec status command: `openspec status --change "<name>" --json` (the `changeRoot`
  field still present under `openspec/changes/<name>/` = not archived yet).
- Squash merge + branch deletion is the default mode on this repo — no merge commit or
  manual rebase expected.
