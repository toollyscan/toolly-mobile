# Foundation Audit — 2026-07-23

## Scope

Post-TLY-003 audit of product, architecture, security, design, CI and GitHub governance on `main`.

## Outcome

The repository direction is sound, but production implementation remains gated. TLY-003A corrects internal contradictions before TLY-004 begins.

## Findings addressed by TLY-003A

| Finding | Severity | Resolution |
|---------|----------|------------|
| Guest mode contradicted login-before-first-scan | Critical | Removed from V1 and recorded as an accepted decision |
| OTP-only wording omitted approved providers | Critical | Restored phone OTP, email/password, Google and Apple on iOS |
| Phone-number HMAC claim ignored Firebase Auth processing | Critical | Separated provider processing from Toolly-owned persistence |
| Firebase wording implied all future releases | High | Standardized Firebase-first and AWS-evaluation-later language |
| Crypto and OTP values presented as final | High | Reframed as proposed mechanisms with evidence gates |
| Figma G10 blocked all implementation | High | Applied proportional vertical-slice gating |
| 2025 decision dates and copyright | Medium | Corrected to 2026 |
| “National language requirements” wording | Medium | Replaced with selected market/audience rationale |
| Documentation PRs self-certified tests | Medium | Added explicit N/A and evidence rules |
| Android minimum version undecided | Medium | Restored approved API 26 baseline |

## Current verified repository state

- Private organization repository with `main` as default.
- Squash merge enabled; merge and rebase merging disabled.
- CODEOWNERS maps all paths to `@shivayogih`.
- Markdown lint and Gitleaks workflows exist.
- TLY-001, TLY-002 and TLY-003 repository documentation merged.
- Duplicate TLY-003 PR #8 closed without merge.
- Live Figma implementation and visual audit remain unverified.

## Pending work inventory

| Workstream | Status | Next owner/action |
|------------|--------|-------------------|
| TLY-003A consistency remediation | In progress | Merge PR after CI and owner review |
| TLY-004 core architecture decisions | Not started | Create after TLY-003A |
| Live Figma G1–G10 evidence | Blocked | Figma access, design execution and reviewers |
| Camera/KMP/geometry/image/PDF/OCR spikes | Not started | Execute after architecture issue definitions |
| Cryptographic review and recovery design | Evidence pending | Qualified security review and prototypes |
| Firebase project/environments/cost controls | Not started | Requires explicit cloud-configuration authorization |
| Domain and trademark clearance | Blocked | Owner/legal action |
| DPDP/privacy/retention/legal review | Blocked | Qualified legal counsel |
| Store products, pricing and tax review | Blocked | Product research and store/tax access |
| Production gate sign-off | Blocked | Complete applicable evidence first |

## Direction

Proceed with a Firebase-first implementation behind Toolly-owned contracts. Do not implement AWS now. Preserve the option to evaluate migration after approximately two years without treating that date as a commitment.

The encrypted local vault remains the source of truth. Production implementation must use proportional evidence gates so that an approved walking slice can proceed without falsely declaring the entire product or Figma file complete.
