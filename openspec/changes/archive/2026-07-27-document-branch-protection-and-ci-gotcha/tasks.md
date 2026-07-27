## 1. Update CLAUDE.md

- [x] 1.1 Add a note to the CI section calling out that no required status check may be
      configured while `ci.yml` uses `paths-ignore`, since a skipped-but-required check
      blocks merge indefinitely

## 2. Update git-pr-workflow skill

- [x] 2.1 Broaden the "archive before PR opens" exception in
      `.claude/skills/git-pr-workflow/SKILL.md` to cover any task that structurally
      requires the PR to already be merged to `main` (not just CI-result-dependent ones)

## 3. Verify

- [x] 3.1 `openspec validate --all --strict` passes (10/10)
