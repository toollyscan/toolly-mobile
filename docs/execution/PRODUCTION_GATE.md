# Production Gate

Production work proceeds through evidence-based, proportional gates. No code may process real user documents or connect to production cloud infrastructure until the applicable architecture, security, design, privacy and operational controls are approved.

The gate must be reviewed by `@shivayogih` before code that processes real user documents, captures images, writes to the encrypted vault or connects to production cloud infrastructure is merged to `main`.

## Design gate

- [ ] Figma foundations and relevant components are approved before UI implementation.
- [ ] Screen coverage and developer handoff are approved for each vertical slice.
- [ ] Error, offline, permission, abuse, deletion, export and recovery states are designed.
- [ ] Accessibility and localization acceptance are included in each slice.
- [ ] Product and design sign-off is approved before beta and GA.

## Architecture gate

- [ ] ADR-0001 KMP boundary prototype evidence is attached.
- [ ] ADR-0002 vault transaction, recovery and security evidence is attached.
- [ ] ADR-0003 provider-neutral Firebase ports are implemented and contract-tested.
- [ ] ADR-0004 canonical account mapping and provider-linking tests pass.
- [ ] ADR-0005 canonical IDs/models and explicit mapper tests pass.
- [ ] ADR-0006 atomic outbox, replay and conflict tests pass.
- [ ] ADR-0007 encryption design is accepted after evidence and qualified review.
- [ ] Module dependency, forbidden-import and public-API fitness functions pass.
- [ ] No platform, provider, billing or database SDK types leak into shared domain contracts.
- [ ] Schema, recipe, wire and backup migrations pass interruption and compatibility tests.
- [ ] Compose Multiplatform benchmark is completed or a native UI path is approved.
- [ ] OCR engine dependency, privacy and performance analysis is approved.

## Security and recovery gate

- [ ] Threat model and data inventory match the implemented release.
- [ ] Local vault confidentiality, integrity, atomicity and corruption behavior are tested.
- [ ] Encryption envelope, nonce uniqueness, associated-data binding and key rotation tests pass.
- [ ] Hardware-backed and non-hardware-backed device behavior is recorded for the supported matrix.
- [ ] Backup restore works across app upgrade and approved device-recovery scenarios.
- [ ] Lost device, key invalidation and lost recovery-material outcomes are tested.
- [ ] Support and cloud administrators cannot bypass encrypted-backup recovery.
- [ ] Phone OTP, email/password, Google, Apple and account-linking abuse controls pass staging tests.
- [ ] App Check configuration and limitations are recorded; it is not treated as authorization.
- [ ] Secrets are absent from source control and release artifacts.
- [ ] Dependency and mobile security checks pass for the release commit.
- [ ] Qualified cryptography review approves ADR-0007 and implementation evidence.

## Privacy and data-protection gate

- [ ] Every collected field and processor appears in the data inventory with purpose, classification, owner, retention and deletion behavior.
- [ ] Generated telemetry allowlist rejects unknown events and prohibited properties.
- [ ] Document content, OCR text, filenames, identity data and secrets are absent from logs, analytics, crash reports and notification payloads.
- [ ] Cloud backup notice, explicit opt-in and consent withdrawal are implemented and tested.
- [ ] Account deletion, backup deletion and export pass end-to-end tests, including retry and partial failure.
- [ ] Firebase production services match the processing inventory.
- [ ] Each Firebase service has a recorded processing location, transfer assessment, retention rule, access owner and deletion test.
- [ ] Firebase Authentication's service-specific processing location is disclosed accurately; no blanket India-residency claim is made.
- [ ] Privacy notice, consent, correction, deletion, export and grievance flows are approved by qualified Indian counsel against provisions effective at launch.
- [ ] Children's/family-document risk and age-related obligations are reviewed before related positioning or features ship.

## Quality gate

- [ ] Definition of Done is adopted by the team.
- [ ] Benchmark corpus and representative device matrix are defined.
- [ ] Performance targets are documented and baseline measurements meet them.
- [ ] Accessibility audit is completed for the supported platforms and languages.
- [ ] Offline, retry, interruption, migration and recovery tests pass.

## Legal and commercial gate

- [ ] Domain and trademark clearance is complete.
- [ ] Privacy policy and terms are reviewed by qualified counsel.
- [ ] Grievance contact and response process are operational.
- [ ] GST display and invoicing requirements are confirmed by a qualified tax adviser.
- [ ] Subscription terms and store disclosures are reviewed.
- [ ] Final prices and entitlement policies are approved in the decision register.
- [ ] Cloud cost and gross-margin models are approved for projected usage.
- [ ] Subscription expiry cannot remove or block local documents.

## Operational gate

- [ ] Firebase budgets, quotas, alerts and approved kill switches are configured.
- [ ] Production access follows least privilege with named owners and periodic review.
- [ ] Backup, restore, deletion and key-compromise exercises are completed.
- [ ] Security incident contacts, decision authority and evidence storage are verified by tabletop.
- [ ] Private vulnerability intake and backup contact are monitored and tested.
- [ ] User communication templates are reviewed; legal deadlines are incident- and jurisdiction-specific.
- [ ] GitHub branch protection and staging/production environments are configured.
- [ ] Rollback and user-safe degradation paths are documented.

## Sign-off

| Role | Name | Date | Evidence link |
|------|------|------|---------------|
| Product owner | shivayogih | | |
| Engineering owner | | | |
| Security reviewer | | | |
| Privacy/legal reviewer | | | |
| Operations owner | | | |
