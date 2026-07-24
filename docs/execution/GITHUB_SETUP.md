# GitHub Setup Guide

This document describes the GitHub repository settings that must be configured manually after the initial repository is created.

These settings cannot be committed to source control. Apply them via the GitHub repository settings UI or the GitHub API.

---

## Branch protection — `main`

Navigate to **Settings → Branches → Add rule** and apply the following to the `main` branch:

| Setting | Value |
|---------|-------|
| Require a pull request before merging | Enabled |
| Required approving reviews | 1 |
| Dismiss stale pull request approvals when new commits are pushed | Enabled |
| Require review from Code Owners | Enabled |
| Require status checks to pass before merging | Enabled |
| Required status checks | `markdown-lint`, `benchmark-contracts`, `dependency-policy`, `firebase-governance`, `ci-trust-policy`, `secret-scan` |
| Require branches to be up to date before merging | Enabled |
| Restrict who can push to matching branches | `shivayogih` only |
| Allow force pushes | Disabled |
| Allow deletions | Disabled |

---

## Environments

Create two environments: `staging` and `production`.

### `staging`

| Setting | Value |
|---------|-------|
| Required reviewers | `shivayogih` |
| Deployment branches | `main` only |
| Secrets | Firebase staging credentials (managed outside source control) |

### `production`

| Setting | Value |
|---------|-------|
| Required reviewers | `shivayogih` |
| Deployment branches | Tags matching `v*.*.*` |
| Wait timer | 5 minutes (cooling-off period) |
| Secrets | Firebase production credentials (managed outside source control) |

---

## Security settings

Navigate to **Settings → Security** and enable:

| Setting | Value |
|---------|-------|
| Dependency graph | Enabled |
| Dependabot alerts | Enabled |
| Dependabot security updates | Enabled |
| Secret scanning | Enabled |
| Secret scanning — push protection | Enabled |

---

## Actions settings

Navigate to **Settings → Actions → General**:

| Setting | Value |
|---------|-------|
| Allow actions | Selected repositories and reusable workflows |
| Fork pull request workflows | Require approval for first-time contributors |
| Workflow permissions | Read repository contents and packages |

---

## Labels

Create the following labels for issue and PR management:

| Label | Colour | Description |
|-------|--------|-------------|
| `architecture` | `#0075ca` | Architecture decisions and reviews |
| `adr` | `#0075ca` | Architecture Decision Record |
| `feature` | `#84b6eb` | New feature request |
| `security` | `#e4e669` | Security concern or fix |
| `documentation` | `#cfd3d7` | Documentation change |
| `blocked` | `#d93f0b` | Blocked on a dependency or decision |
| `production-gate` | `#d93f0b` | Required for Production Gate approval |

---

## Remaining web-only settings

The following settings cannot be configured via source control and must be applied manually:

1. Branch protection rules (above).
2. Environment secrets for Firebase credentials.
3. Dependabot and secret-scanning configuration.
4. GitHub Actions permissions.
5. Issue label creation.
6. Repository description and website URL (toollyscan.com).
7. Disable merge commits (allow squash merge only) in **Settings → General**.
