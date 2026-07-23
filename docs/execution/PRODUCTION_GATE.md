# Production Gate

Production feature implementation is blocked until every item in this gate is approved.

The gate must be reviewed by `@shivayogih` before any code that processes real user documents, captures images, writes to the encrypted vault or connects to cloud infrastructure is merged to `main`.

---

## Architecture gate

- [ ] ADR-0001 (KMP boundary) accepted and prototype evidence attached.
- [ ] ADR-0002 (local vault) accepted and vault encryption implementation reviewed.
- [ ] ADR-0003 (cloud provider portability) accepted and provider-neutral interface implemented.
- [ ] ADR-0004 (authentication and account boundary) accepted and `ToollyAccountId` generation implemented.
- [ ] No Firebase, AWS or other provider SDK type appears in domain models, use cases or repository interfaces.
- [ ] Compose Multiplatform benchmark completed (DA-001) or native UI path confirmed.
- [ ] OCR engine selected with completed dependency analysis (DA-004).

---

## Security gate

- [ ] Vault encryption reviewed by a qualified security practitioner.
- [ ] OTP abuse controls (rate limiting, lockout) tested in staging.
- [ ] Trusted-device approval and account recovery tested end to end.
- [ ] Phone numbers confirmed to be stored as HMAC only; no plaintext in logs or database.
- [ ] Document content, OCR text and PII confirmed absent from all logs and analytics.
- [ ] Firebase credentials confirmed absent from source control (Gitleaks passes).
- [ ] Firebase data-residency configuration confirmed for Indian user data.

---

## Quality gate

- [ ] Definition of Done adopted by the team.
- [ ] Benchmark corpus and representative device matrix defined (BENCHMARK_PLAN.md).
- [ ] Performance targets documented and baseline measurements taken.
- [ ] Accessibility audit scope confirmed (minimum WCAG 2.1 AA).

---

## Legal and compliance gate

- [ ] Domain clearance: toollyscan.com and toollyscan.in owned by Toolly.
- [ ] Trademark clearance: "Toolly" cleared in India.
- [ ] DPDP Act 2023 obligations mapped.
- [ ] Privacy policy reviewed by qualified legal counsel.
- [ ] Grievance officer designated.

---

## Operational gate

- [ ] Firebase budget alerts configured (see COST_CONTROLS.md).
- [ ] Firebase quota limits and kill-switch controls in place.
- [ ] GitHub branch protection rules configured (see GITHUB_SETUP.md).
- [ ] GitHub Environments (`staging`, `production`) configured with required reviewers.

---

## Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Repository owner | shivayogih | | |
