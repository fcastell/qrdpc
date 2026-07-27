## ADDED Requirements

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
