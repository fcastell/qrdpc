## Context

The QrDPC working directory currently has no `.git` directory — the Gradle scaffold,
`LICENSE`, `README.md`, and OpenSpec artifacts exist only on the local filesystem. The
project is intended to live as a public repository at `github.com/fcastell/qrdpc`, matching
the `io.github.fcastell.qrdpc` application ID and the Apache-2.0 license already committed
to disk. This is a one-time setup task (initialize version control, create the remote,
push the first commit), not an ongoing capability, but it still needs a deliberate first
commit so the public history starts clean.

## Goals / Non-Goals

**Goals:**
- Get the existing scaffold into git and pushed to a new public GitHub repository
  `fcastell/qrdpc`, with an initial commit that doesn't include build output or IDE state.
- Leave the repo in a state where `git status` is clean and `origin` points at GitHub.

**Non-Goals:**
- No CI/CD, branch protection, issue templates, or GitHub Actions workflows — those are
  future changes if/when the project needs them.
- No changes to application code, package name, or build configuration.
- No decision here about the final DPC delegation mechanism (`DELEGATION_APP_RESTRICTIONS`
  vs. device/profile owner) — that's tracked separately in the README as unresolved.

## Decisions

- **Repo name**: `qrdpc`, matching the existing local directory name and the app ID suffix
  (`io.github.fcastell.qrdpc`). Keeps the GitHub URL, package name, and folder name aligned.
- **Visibility**: public, per the proposal's stated intent (community-facing tool, Apache-2.0
  license already in place expects public consumption).
- **Remote creation method**: use the `gh` CLI (`gh repo create fcastell/qrdpc --public
  --source=. --remote=origin`) if available, since it creates the remote repo and wires up
  `origin` in one step and avoids a manual web UI detour. Fall back to manual creation via
  the GitHub web UI + `git remote add origin` if `gh` is not installed or not authenticated.
- **Default branch name**: `main`, matching current GitHub defaults and avoiding a rename
  step later.
- **`.gitignore` review before first commit**: rather than committing everything and cleaning
  up after, verify ignore rules up front (`build/`, `.gradle/`, `.kotlin/`, `local.properties`,
  `.idea/` if present) so the initial commit — which becomes permanent public history — is
  already clean. Re-committing to strip accidentally-tracked build artifacts later is wasted
  history noise on a repo meant to be public from day one.
- **Description and topics set at creation time**: both the repository description (short
  summary matching the README's opening line) and GitHub topics (e.g. `android`, `kotlin`,
  `qr-code`, `mdm`, `managed-configurations`) are set as part of this change rather than
  deferred, since they cost nothing extra during setup and make the repo discoverable and
  self-explanatory from the first push.

## Risks / Trade-offs

- [Sensitive/local-only files (e.g. `local.properties` with SDK paths, signing configs) get
  committed by mistake] → Review `.gitignore` and run `git status`/`git add -n` before the
  first commit to confirm only intended files are staged.
- [`gh` CLI not installed or not authenticated under the `fcastell` account] → Fall back to
  creating the repository manually via github.com, then `git remote add origin`; verify with
  `gh auth status` or by checking the account context before creating the repo.
- [Wrong GitHub account used if multiple accounts are configured locally] → Confirm the
  target account is `fcastell` before running `gh repo create` or pushing (`gh auth status`
  or checking `git config user.*` / SSH remote).

## Migration Plan

1. Review and finalize `.gitignore`.
2. `git init`, stage the intended files, create the initial commit.
3. Create the `fcastell/qrdpc` GitHub repository (public, `main` default branch).
4. Add `origin` remote (if not already set by repo creation) and push `main`.
5. Verify on GitHub: correct files present, no build artifacts, license detected, README
   renders.

No rollback needed beyond deleting the GitHub repo and/or local `.git` directory if something
goes wrong before the push — nothing external depends on this repo yet.

## Open Questions

None outstanding. The repo description/topics question is resolved below (see Decisions):
both are set as part of this change.
