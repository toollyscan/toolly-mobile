# Incident Response Checklist

Minimal manual checklist for responding to security, data or operational
incidents at Toolly.  For the current pre-production phase this checklist
covers the repository, CI/CD and development environment.  Production-scale
procedures (legal notification deadlines, SRE escalation paths, public
status page) are deferred to the pre-beta milestone.

See also: `docs/operations/SECURITY_INCIDENT_RESPONSE.md` for the
full SEV-0 to SEV-3 framework.

---

## Step 1 — Detect and triage

- [ ] Identify the affected system: repository, CI, Firebase dev/staging,
      signing material or user data.
- [ ] Assign an incident commander and a scribe.
- [ ] Assign an initial severity (SEV-0 to SEV-3) from
      `SECURITY_INCIDENT_RESPONSE.md`.
- [ ] Create a timestamped incident record (private channel or issue).

## Step 2 — Contain

| Scenario | Immediate action |
|----------|-----------------|
| Credential committed to Git | Rotate credential; force-push purged history |
| CI compromise suspected | Disable failing workflow; audit recent run logs |
| Firebase staging credential exposed | Revoke key; rotate Firebase service account |
| Signing material suspected exposed | Alert engineering lead; do not deploy |
| Unauthorized repository access | Revoke token; audit access log |

## Step 3 — Assess scope

- [ ] Which systems, environments and data were accessible?
- [ ] Was any production Firebase project or user data reachable?  (Pre-production: no production data exists yet.)
- [ ] Was any signing material (keystore, provisioning profile) in scope?
- [ ] Were any other developers, contributors or third-party integrations affected?

## Step 4 — Remediate

- [ ] Apply the containment action from Step 2.
- [ ] Verify the credential or access path is fully invalidated.
- [ ] Run `scan_secrets.py` on the full repository after any history purge.
- [ ] Confirm CI passes on the remediated state.
- [ ] Update `config/ci/secret-exceptions.json` if a false-positive contributed
      to the incident going undetected.

## Step 5 — Review and improve

- [ ] Complete a written post-mortem within 48 hours (blameless).
- [ ] Identify the root cause and any detection gaps.
- [ ] Add or update a `scan_secrets.py` pattern if a new credential type was
      involved.
- [ ] Update this checklist or `SECURITY_INCIDENT_RESPONSE.md` if a step was
      missing or incorrect.
- [ ] Update `docs/product/DECISION_REGISTER.md` if a process decision changed.

---

## Deferred items

The following are not yet implemented and must not be claimed as operational:

- Automated SLO/SLI alerting and dashboards.
- Pager escalation and on-call rotation.
- Legal notification templates (jurisdiction-specific deadlines apply at launch).
- Disaster-recovery and backup-restore drills.
- Breach-notification workflows for user data.

These are tracked in `docs/execution/ROADMAP.md` under pre-beta milestones.

---

*Owner: shivayogih | Review due: before first production deployment*
