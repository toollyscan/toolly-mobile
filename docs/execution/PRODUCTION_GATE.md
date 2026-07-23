# Production Gate

Production work proceeds through evidence-based, proportional gates. No code may process real user documents or connect to production cloud infrastructure until the applicable architecture, security, design and operational controls are approved.

The gate must be reviewed by `@shivayogih` before any code that processes real user documents, captures images, writes to the encrypted vault or connects to cloud infrastructure is merged to `main`.

---

## Design gate

- [ ] Figma information architecture documented (FIGMA_INFORMATION_ARCHITECTURE.md). Complete.
- [ ] Screen inventory defined (SCREEN_INVENTORY.md). Complete.
- [ ] Component inventory defined (COMPONENT_INVENTORY.md). Complete.
- [ ] Design tokens defined (DESIGN_TOKENS.md). Complete.
- [ ] Accessibility requirements defined (ACCESSIBILITY_REQUIREMENTS.md). Complete.
- [ ] Localization requirements defined (LOCALIZATION_REQUIREMENTS.md). Complete.
- [ ] Figma completion gates G1–G10 defined (FIGMA_COMPLETION_GATE.md). Complete.
- [ ] Figma G2 foundations and relevant G3 components approved before UI implementation.
- [ ] Figma G4 screen coverage approved for each implemented vertical slice.
- [ ] Figma G9 developer handoff approved for each implemented vertical slice.
- [ ] Accessibility and localization acceptance included in each implemented slice.
- [ ] Figma G10 product and design sign-off approved before beta/GA.

---

## Architecture gate

- [ ] ADR-0001 KMP boundary prototype evidence attached.
- [ ] ADR-0002 vault transaction/recovery and security evidence attached.
- [ ] ADR-0003 provider-neutral Firebase ports implemented and contract-tested.
- [ ] ADR-0004 canonical account mapping and provider-linking tests pass.
- [ ] ADR-0005 canonical IDs/models and explicit mapper tests pass.
- [ ] ADR-0006 atomic outbox, replay and conflict tests pass.
- [ ] Module dependency, forbidden-import and public-API fitness functions pass.
- [ ] No platform/provider/billing/database SDK type leaks into shared domain contracts.
- [ ] Schema, recipe, wire and backup migration fixtures pass interruption and compatibility tests.
- [ ] Compose Multiplatform benchmark completed (DA-001) or native UI path confirmed.
- [ ] OCR engine selected with completed dependency analysis (DA-004).

---

## Security gate

- [ ] Vault encryption reviewed by a qualified security practitioner.
- [ ] OTP abuse controls (rate limiting, lockout) tested in staging.
- [ ] Trusted-device approval and account recovery tested end to end.
- [ ] Toolly-owned phone-number persistence, Firebase Authentication processing, retention and deletion are documented and approved; phone numbers are absent from application logs and analytics.
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
- [ ] GST display and invoicing requirements confirmed by qualified tax advisor.
- [ ] Subscription terms reviewed by qualified legal counsel.

---

## Monetization and pricing gate

- [ ] Competitor pricing benchmark completed for India market (top 5 apps).
- [ ] Willingness-to-pay research completed with Indian user segments.
- [ ] Cloud cost per active free user modelled at projected launch volume.
- [ ] Cloud cost per active premium user modelled (including backup storage and egress).
- [ ] Google Play and App Store commission rates confirmed.
- [ ] Target gross margin approved.
- [ ] Final India subscription prices set and recorded in DECISION_REGISTER.md.
- [ ] Free-tier batch page limit hypothesis (H-001) validated or revised.
- [ ] Premium cloud-storage allowance hypothesis (H-002) validated or revised.
- [ ] Entitlement freshness policy hypothesis (H-007) validated or revised.
- [ ] Backup data retention period hypothesis (H-009) validated or revised.
- [ ] Store subscription products created in Google Play Console and App Store Connect.
- [ ] Entitlement service architecture reviewed: billing types do not appear in domain models.
- [ ] Offline entitlement cache and freshness policy implemented and tested.
- [ ] Subscription expiry does not delete local documents — verified by test.

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
