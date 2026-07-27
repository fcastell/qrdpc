## 1. Prepare local repository

- [ ] 1.1 Review `.gitignore` and confirm it covers `build/`, `.gradle/`, `.kotlin/`,
      `local.properties`, and any local IDE state (`.idea/`), adding missing rules if needed
- [ ] 1.2 Run `git init` in the project root
- [ ] 1.3 Stage the intended files and run `git status`/`git add -n` (or equivalent) to
      confirm only source, config, `LICENSE`, `README.md`, and `openspec/` are staged —
      no build output or local-only files
- [ ] 1.4 Create the initial commit

## 2. Create and configure the GitHub repository

- [ ] 2.1 Confirm the target GitHub account context is `fcastell` (e.g. `gh auth status`)
- [ ] 2.2 Create the public repository `fcastell/qrdpc` with `main` as the default branch,
      using `gh repo create fcastell/qrdpc --public --source=. --remote=origin` if the `gh`
      CLI is available and authenticated, otherwise create it manually via the GitHub web UI
- [ ] 2.3 If the repository was created manually, add the remote: `git remote add origin
      git@github.com:fcastell/qrdpc.git` (or the HTTPS equivalent)
- [ ] 2.4 Set the repository description to match the README's opening summary
- [ ] 2.5 Set repository topics (e.g. `android`, `kotlin`, `qr-code`, `mdm`,
      `managed-configurations`)

## 3. Push and verify

- [ ] 3.1 Push the initial commit and set upstream tracking: `git push -u origin main`
- [ ] 3.2 Verify `git status` reports `main` up to date with `origin/main`
- [ ] 3.3 Verify on github.com that the repository is public, shows the correct files,
      renders the README, and detects the Apache-2.0 `LICENSE`
