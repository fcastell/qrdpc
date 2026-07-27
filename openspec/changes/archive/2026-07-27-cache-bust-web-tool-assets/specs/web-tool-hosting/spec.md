## ADDED Requirements

### Requirement: Deployed assets are cache-busted per deploy
`pages.yml` SHALL append a commit-SHA-based cache-busting query string to the deployed
`style.css`, `vendor/qrcode-generator/qrcode.js`, and `app.js` references before
uploading the Pages artifact, so each deploy serves those assets at a URL unique to
that deploy. The `web/index.html` file committed to the repository SHALL remain
unmodified (plain, query-string-free asset paths) — the substitution SHALL happen only
in the build job's own checkout, on the copy handed to `actions/upload-pages-artifact`.

#### Scenario: A redeploy changes the served asset URLs
- **WHEN** `pages.yml` runs for a new commit on `main`
- **THEN** the live page's `style.css`, `qrcode.js`, and `app.js` references carry a
  `?v=` query string derived from that commit, different from the previous deploy's

#### Scenario: Repository source stays framework-free
- **WHEN** inspecting `web/index.html` in the repository (not the deployed artifact)
- **THEN** its asset references carry no cache-busting query string
