# Firebase operational evidence checklist

TLY-008 approves a design. It does not prove that Firebase projects, billing, IAM, Rules, App Check
or alerts are configured. Complete the applicable evidence before staging and repeat it before
production.

## Environment evidence

- [ ] Four separate project numbers/IDs are bound to development, test, staging and production.
- [ ] Android/iOS registrations match the intended environment and signing identity.
- [ ] Production build rejects emulator endpoints, test phone numbers and debug App Check.
- [ ] Production data/identity cannot enter non-production projects.
- [ ] Firestore, Storage, Functions and default-resource locations are recorded and reviewed.
- [ ] Enabled APIs and service inventory match `service-boundaries.json`.

## Security evidence

- [ ] Firestore and Storage start deny-by-default; Rules tests pass in CI.
- [ ] Canonical account authorization and cross-account negative tests pass.
- [ ] App Check valid/invalid/reused-token metrics are captured per service.
- [ ] Staging enforcement and rollback tests pass before production enforcement.
- [ ] OTP region policy, provider quota behavior and abuse tests are recorded.
- [ ] No production verification bypass or test phone number exists.
- [ ] Function inputs, identities, scaling, retries, age limits and idempotency are reviewed.
- [ ] FCM payload capture contains no prohibited data.
- [ ] Remote Config contains no secret or authorization grant.
- [ ] Signed-policy signature, generation, expiry, environment and key-rotation tests pass.
- [ ] Invalid policy preserves local scan, vault read/write and local export.

## Storage and lifecycle evidence

- [ ] Only ciphertext and bounded approved metadata are accepted.
- [ ] Resumable duplicate/interrupted upload and digest verification pass.
- [ ] Same key/different payload is rejected.
- [ ] Object-size and metadata Rules match approved limits.
- [ ] Temporary/noncurrent lifecycle rules are tested with synthetic data.
- [ ] Live user backups are not time-expired by a cost control.
- [ ] Document/account deletion is idempotent, retried and verified.
- [ ] Soft-delete/versioning/retention behavior and cost are documented.

## Cost evidence

- [ ] Current regional SKU export, edition, currency and effective date are attached.
- [ ] Free, premium-base and allowance-edge models are calculated.
- [ ] 100, 1k, 10k, 100k and 1m active-user cohorts are modelled.
- [ ] Normal, launch, OTP abuse, backup storm, retry storm and deletion backlog are tested.
- [ ] Separate staging/production budgets and named recipients exist.
- [ ] Actual and forecast alert delivery is tested.
- [ ] Programmatic notifications handle duplicates and reordering.
- [ ] Cost-anomaly thresholds and notification delivery are tested.
- [ ] Per-service operational alerts link to a tested runbook.
- [ ] `contain-cost` time-to-effect and safe rollback are measured.
- [ ] Product owner approves cost per free/premium user and projected gross margin.

## IaC and access evidence

- [ ] Provisioned resources are represented in Terraform or an explicit supported bootstrap path.
- [ ] Provider/tool versions and lock evidence are committed.
- [ ] GitHub deployment uses environment-bound Workload Identity Federation.
- [ ] No long-lived service-account key exists in GitHub or CI.
- [ ] Plan and apply identities are separate and least privilege.
- [ ] Production environment requires named approval.
- [ ] Runtime functions use dedicated least-privilege identities.
- [ ] Secrets are in Secret Manager with per-function bindings and rotation evidence.
- [ ] IAM, Rules, indexes, budgets, alerts and App Check drift checks pass.
- [ ] Break-glass access is time-bounded, alerted and tested.

## Privacy and operations evidence

- [ ] Processing inventory and user/store notices match observed network collection.
- [ ] Authentication, global service and regional service locations are disclosed separately.
- [ ] Telemetry remains disabled or passes generated allowlist and destination capture tests.
- [ ] Account deletion and consent withdrawal succeed under partial provider failure.
- [ ] Incident contacts, evidence storage and user communication are tested.
- [ ] Firebase terms, subprocessors, retention and deletion behavior are revalidated.
- [ ] Qualified privacy/legal review is linked where required.

## Sign-off

| Scope | Environment | Owner | Date | Commit/resource digest | Evidence link | Result |
|---|---|---|---|---|---|---|
| Environment | | | | | | |
| Security/abuse | | | | | | |
| Storage/lifecycle | | | | | | |
| Cost/capacity | | | | | | |
| IaC/IAM/secrets | | | | | | |
| Privacy/operations | | | | | | |

Allowed results are `approved`, `blocked`, `waived-with-owner-and-expiry` and `not-applicable-with-rationale`.
