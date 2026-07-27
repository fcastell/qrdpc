# web-tool-hosting Specification

## Purpose
Defines how the `web/` QR config generator is published to GitHub Pages: an
Actions-based deploy pipeline scoped to changes under `web/**`.

## Requirements

### Requirement: GitHub Pages deployment via Actions
The `web/` app SHALL be published to GitHub Pages using the Actions-based deployment
flow (`build_type: "workflow"`), via a dedicated `.github/workflows/pages.yml` using
`actions/configure-pages`, `actions/upload-pages-artifact`, and `actions/deploy-pages`.

#### Scenario: Site is live at the expected URL
- **WHEN** `pages.yml` completes successfully on `main`
- **THEN** the app is reachable at `https://fcastell.github.io/qrdpc/`

### Requirement: Deploy triggers only on relevant main pushes
`pages.yml` SHALL trigger on pushes to `main` that touch `web/**`, plus manual dispatch,
so unrelated changes elsewhere in the repo don't redeploy the site.

#### Scenario: Non-web change doesn't redeploy
- **WHEN** a push to `main` only modifies files outside `web/**`
- **THEN** `pages.yml` does not run

#### Scenario: Web change redeploys
- **WHEN** a push to `main` modifies a file under `web/**`
- **THEN** `pages.yml` runs and, on success, the live site reflects the change

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
