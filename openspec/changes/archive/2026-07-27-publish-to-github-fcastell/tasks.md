## 1. Prepare local repository

- [x] 1.1 Review `.gitignore` and confirm it covers `build/`, `.gradle/`, `.kotlin/`,
      `local.properties`, and any local IDE state (`.idea/`), adding missing rules if needed
- [x] 1.2 Run `git init` in the project root
- [x] 1.3 Stage the intended files and run `git status`/`git add -n` (or equivalent) to
      confirm only source, config, `LICENSE`, `README.md`, and `openspec/` are staged —
      no build output or local-only files
- [x] 1.4 Create the initial commit

## 2. Create and configure the GitHub repository

- [x] 2.1 Confirm the target GitHub account context is `fcastell` (e.g. `gh auth status`)
- [x] 2.2 Create the public repository `fcastell/qrdpc` with `main` as the default branch,
      using `gh repo create fcastell/qrdpc --public --source=. --remote=origin` if the `gh`
      CLI is available and authenticated, otherwise create it manually via the GitHub web UI
      (repo already existed, public and empty — no default branch until first push)
- [x] 2.3 If the repository was created manually, add the remote: `git remote add origin
      git@github.com:fcastell/qrdpc.git` (or the HTTPS equivalent)
- [x] 2.4 Set the repository description to match the README's opening summary
- [x] 2.5 Set repository topics (e.g. `android`, `kotlin`, `qr-code`, `mdm`,
      `managed-configurations`)

## 3. Push and verify

- [x] 3.1 Push the initial commit and set upstream tracking: `git push -u origin main`
      (pushed via HTTPS using `gh auth git-credential` — SSH used the wrong of two locally
      configured GitHub accounts; `origin` now uses the HTTPS URL)
- [x] 3.2 Verify `git status` reports `main` up to date with `origin/main`
- [x] 3.3 Verify on github.com that the repository is public, shows the correct files,
      renders the README, and detects the Apache-2.0 `LICENSE`
