# ADR-0010 — First-party CI trust boundary

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2026-07-23 |
| Author | shivayogih |
| Supersedes | — |
| Related | ADR-0008, TLY-009 |

## Context

Toolly CI previously used two community-maintained tools that ran with
`contents:read` access to the full repository:

- **DavidAnson/markdownlint-cli2-action** — community GitHub Action for Markdown linting.
- **ghcr.io/gitleaks/gitleaks** — community container action for secret scanning.

Both violate the product decision that CI must not execute community GitHub
Actions, external container actions, downloaded scripts or dynamically
installed tools.  Supply-chain risk exists even for well-maintained
open-source tools: the action owner, the image registry and any transitive
scripts have elevated access at CI time.

## Decision

Toolly CI workflows may use only:

1. **Official GitHub-maintained actions** (owner `actions`) pinned to a
   full 40-character immutable commit SHA and registered in
   `config/dependencies/registry.json`.
2. **Toolly-owned Python scripts** under `scripts/` invoked with `run:`.

All other action owners, container actions (`docker://`), mutable tags,
branches and HEAD references are prohibited.  Remote-script execution
(`curl | sh`, `wget | sh`, `eval`, dynamic `pip install`, `npm install`,
`npx`) is prohibited in `run:` steps.

## Enforcement

`scripts/validate_ci_trust_policy.py` reads `config/ci/trust-policy.json`
and validates every workflow file in `.github/workflows/` on every PR.
The trust validator is itself a Toolly-owned Python script that uses only
the Python standard library and includes deterministic self-tests.

## Consequences

**Positive:**

- No community code runs with CI repository access.
- Every permitted tool is Toolly-owned, auditable and has no external
  runtime dependencies.
- The trust boundary is machine-readable and enforced on every PR.
- Markdown linting, secret scanning and CI trust validation continue
  without external dependencies.

**Costs:**

- Toolly-owned replacements require maintenance as requirements change.
- The secret scanner pattern library is narrower than a dedicated tool
  like Gitleaks; patterns must be extended when new credential types are
  introduced.
- Community tools with richer rule sets are unavailable.

## Implementation

| Replaced tool | Replacement |
|---------------|-------------|
| `DavidAnson/markdownlint-cli2-action` | `scripts/validate_markdown.py` |
| `docker://ghcr.io/gitleaks/gitleaks` | `scripts/scan_secrets.py` |
| (new) | `scripts/validate_ci_trust_policy.py` |

Policy: `config/ci/trust-policy.json`

False-positive exceptions: `config/ci/secret-exceptions.json`
(narrow, expiring, owner-attributed entries only).

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Continue using community actions | Violates the first-party CI trust decision |
| Fork community tools into the repository | Adds maintenance burden without a clear safety improvement |
| Trust pinned community actions unconditionally | SHA pinning prevents unintended updates but does not prevent the action owner from having designed the code with malicious intent or from having been compromised at authoring time |
| Allowlist specific well-known community owners | Creates a precedent for expanding the allow-list; a single approved owner is sufficient for the current workflow |

## Evidence

- `scripts/validate_ci_trust_policy.py --self-test` passes in CI.
- `scripts/validate_markdown.py --self-test` passes in CI.
- `scripts/scan_secrets.py --self-test` passes in CI.
- Workflow `.github/workflows/documentation.yml` contains only
  `actions/checkout` and `run:` steps with Toolly-owned scripts.
