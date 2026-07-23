# Release and Rollback Checklist

Concise manual checklist for staging deployments, production releases and
rollbacks.  These procedures apply once the Production Gate is approved and
actual Android/iOS builds exist.  Until then, this document is the approved
runbook template.

**Deferred items** (not yet applicable): store-submission automation,
signing-key rotation drills, full disaster-recovery exercises.  See
`docs/execution/ROADMAP.md` for the pre-beta scope items.

---

## Pre-release checks

- [ ] All required CI checks pass on the release commit (`main` or release tag).
- [ ] `validate_dependency_policy.py --self-test` passes locally.
- [ ] `scan_secrets.py` reports zero unexcepted findings.
- [ ] Release commit is tagged `vMAJOR.MINOR.PATCH` and the tag is pushed.
- [ ] Release evidence template (`docs/operations/RELEASE_EVIDENCE_TEMPLATE.md`)
      is completed and linked from the PR.
- [ ] SBOM for the release artifact is generated and attached (post-scaffolding).
- [ ] No signing material, Firebase credentials or production secrets are in Git.

## Staging deployment

```text
Trigger: merge to main or manual workflow_dispatch on the staging environment.
Approval: required reviewer in the 'staging' GitHub environment.
```

1. Confirm the `staging` environment has the correct Firebase project binding.
2. Approve the deployment in GitHub → Environments → staging.
3. Verify the deployed build connects to the Firebase staging project only.
4. Run smoke tests against the staging environment.
5. Confirm no production data was accessed.

## Production release

```text
Trigger: tag matching v*.*.* pushed to the repository.
Approval: required reviewer in the 'production' GitHub environment.
Wait timer: 5 minutes (cooling-off period).
```

1. Ensure staging deployment was verified (step above).
2. Approve the production deployment after the wait timer.
3. Confirm the release artifact digest matches the build provenance record.
4. Monitor error-rate and crash-rate dashboards for 30 minutes post-deploy.
5. Confirm release is visible in Google Play / App Store (manual step).

## Rollback

Use rollback when a production release causes elevated error rates, crashes
or data integrity issues that cannot be fixed forward within the incident
window.

1. **Decision**: incident commander calls rollback.
2. **Revert**: redeploy the last known-good tagged version through the
   production environment (same approval gate applies).
3. **Store**: submit a priority update to Google Play / App Store if the
   installed version must be replaced (manual step; store review times vary).
4. **Users**: send in-app notification or update status page if users are
   affected.
5. **Post-mortem**: complete the incident-response checklist within 48 hours.

## Secret rotation after a finding

If `scan_secrets.py` or any other tool finds a committed credential:

1. **Rotate immediately**: invalidate the credential with the issuing service
   before any other action.
2. **Purge history**: use `git filter-repo` to remove the credential from all
   commits.  Force-push to all branches after team coordination.
3. **Notify**: follow the incident-response checklist if the credential had
   production scope.
4. **Prevent recurrence**: add the credential type to `scan_secrets.py` if it
   is not already detected.

---

*Owner: shivayogih | Review due: before first staging deployment*
