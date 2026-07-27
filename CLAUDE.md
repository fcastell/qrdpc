# Conventions for Claude Code on this project

This file documents the working conventions expected on the QrDPC project
(`io.github.fcastell.qrdpc`), established over the course of past sessions. It is loaded
automatically at the start of every Claude Code session on this repo — these rules apply
by default, without needing to be restated.

## Git, PR, merge, OpenSpec archiving

The full detail (mandatory branch + PR, when to commit, commit message format,
when/how to archive an OpenSpec change, when to merge, branch deletion...) lives in the
**`git-pr-workflow`** skill (`.claude/skills/git-pr-workflow/SKILL.md`) — that's the
single source of truth, don't duplicate it here. This skill applies by default to any
git/PR/archiving action on this repo, even without explicit invocation.

## CI

- The `.github/workflows/ci.yml` workflow ignores changes that only touch `openspec/**`,
  `docs/**`, or `**/*.md` files (`paths-ignore`) — don't be surprised by the absence of a
  CI check on a PR that only contains doc/spec content.
- The `release` job only triggers on a `v*.*.*` tag, never on a push to `main`.

## Releases

Tag/publish procedure: **`release-app`** skill (`.claude/skills/release-app/SKILL.md`).

## UI verification

Any UI implementation is verified on a real device before being considered done: skill
**`verify-on-device`** (`.claude/skills/verify-on-device/SKILL.md`).

## Session close-out

Before closing a session (or on request for a check-in/wrap-up): skill
**`session-wrapup`** (`.claude/skills/session-wrapup/SKILL.md`) — git/OpenSpec state,
recurring patterns worth formalizing, verification that no decision from the session is
lost.

## Language

Everything in this repo — code, comments, docs, commit messages, PR bodies, release
notes, and all OpenSpec artifacts — is written in English. QrDPC is a public,
community-facing project on the personal GitHub account `fcastell`; unlike sibling Danone
projects, there is no French-language convention here.
