---
name: session-wrapup
description: Checklist to run through before closing a Claude Code session on this project — git/OpenSpec state, recurring patterns worth formalizing into a skill/CLAUDE.md, verification that nothing discussed is lost (spec/code drift). Use when the user asks for a check before closing the session, a wrap-up, or "make sure nothing is lost".
metadata:
  author: fabien-castell
  version: "1.0"
---

Session close-out point for this project. Runs in 4 steps, in order. Don't
create/fix anything automatically at each step: list findings, propose, wait for
confirmation before acting (see `git-pr-workflow` for caution around irreversible
actions).

## 1. Git/repo state

- `git status --short`: any uncommitted changes? If so, which are related to the
  session's work (propose for commit) vs. unrelated noise (IDE files, etc., left aside as
  usual)?
- `git branch --show-current` + `git log main..HEAD --oneline` (if relevant): does the
  current branch have unpushed commits?
- `gh pr list --state open`: any open PRs waiting on CI or a merge?
- Local merged branches not yet cleaned up (`git branch --merged main`).

## 2. OpenSpec state

- `openspec list --json`: any active (unarchived) changes? For each, check whether the
  implementation is done (tasks at 100%) — if so, remind the user they can request
  archiving (never archive without them asking, see `git-pr-workflow`).

## 3. Recurring patterns worth formalizing

Re-read the session: spot working conventions given repeatedly (corrections, preferences
confirmed ≥2 times — not one-off decisions). For each pattern that has genuinely
recurred, propose formalizing it:
- **`CLAUDE.md`** (repo root) if it's a passive rule that should always apply.
- **Project skill** (`.claude/skills/<name>/SKILL.md`) if it's a multi-step procedure
  invoked at a specific moment.
- If it overlaps an existing skill (`git-pr-workflow`, `release-app`,
  `verify-on-device`), propose extending it rather than creating a duplicate.
Don't create anything without the user's explicit confirmation.

## 4. Completeness check (nothing lost)

For every substantial decision/change in the session:
- **Application behavior**: must be reflected in an up-to-date OpenSpec spec
  (`openspec/specs/`). In particular, check that no code change (config, behavior, CI...)
  was made directly without a corresponding OpenSpec update.
- **Workflow convention**: must be in `CLAUDE.md`/a skill, not only in the assistant's
  memory (local, unshared) or in this conversation (not re-read afterward).
- Explicitly list what is well covered and what isn't, before proposing fixes.

## Expected output

A short summary: git state (clean or not), OpenSpec state (nothing pending or changes to
archive), proposed patterns for formalization (pending agreement), and any spec/code
drift found (with a proposed fix, pending agreement). Don't close the session with
uncommitted/unpushed work or a known, unreported drift.
