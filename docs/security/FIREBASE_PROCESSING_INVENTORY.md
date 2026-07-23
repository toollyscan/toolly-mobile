# Firebase Processing Inventory

This inventory records planned Firebase use and official service behavior known on 2026-07-23.
Service-specific terms and configuration must be revalidated before staging and release.

## Key findings

- Firebase Authentication processes passwords, email addresses, phone numbers, user agents and IP
  addresses to provide authentication and abuse prevention.
- Official Firebase documentation states Authentication is operated from US data centres.
- Official phone-auth documentation states phone numbers are sent/stored by Google for spam and
  abuse prevention and requires appropriate user consent.
- Firebase reports that non-IP authentication data is retained until customer-initiated user
  deletion, with removal from live/backup systems within the period stated in its current terms.
- Service locations, retention and controllable collection vary by product. A project region does
  not make every Firebase service India-local.

These are provider statements, not Toolly legal conclusions or guarantees.

## Planned services

| Service | Planned purpose | Data | Location/configuration status | Approval |
|---------|-----------------|------|-------------------------------|----------|
| Authentication | Phone, email/password, Google, Apple identity | Provider identity, IP, user agent, tokens | Auth documented as US-only | Legal/privacy and notice pending |
| Cloud Firestore or approved metadata service | Account mapping, encrypted manifests, sync/entitlement metadata | Canonical IDs and ciphertext metadata; no document plaintext | Region not selected | TLY-008 plus legal review |
| Cloud Storage for Firebase | Optional encrypted backup objects | Ciphertext, lengths/digests/object keys | Region not selected | Backup crypto and TLY-008 pending |
| Cloud Functions | Identity/entitlement/deletion orchestration | Minimum event metadata; IP for HTTP invocation | Region/runtime pending | TLY-008/TLY-009 |
| App Check | App/device integrity and abuse control | Attestation material/tokens | Provider-dependent/global behavior | Privacy inventory and rollout pending |
| FCM | Security/backup notifications | Installation ID and non-sensitive payload | Global service behavior | Opt-in/config review pending |
| Remote Config | Signed/validated operational policy input | Installation ID; no document identity | Global behavior | Kill-switch design pending |
| Crashlytics | Reliability diagnostics | Installation IDs and crash data | Collection disabled until allowlist tests | Privacy approval pending |
| Performance Monitoring | Coarse performance | Installation ID/IP and traces | Collection disabled until allowlist tests | Privacy approval pending |
| Analytics | Product metrics | Only approved events if enabled | Not approved by inclusion | Separate privacy decision |

## Explicit exclusions

- document/page plaintext in Firestore, Storage, Functions, logs, Crashlytics or Analytics;
- OCR text, filenames and user titles in provider metadata;
- Firebase UID as canonical Toolly account/document owner;
- provider tokens in domain, telemetry or support;
- automatic Analytics, Crashlytics or Performance collection before approval;
- Firebase AI/ML or generative services for user documents in V1;
- production data in Emulator Suite or test projects.

## Identity processing

Before phone authentication, the UI explains that the number is processed by Google/Firebase for
authentication and abuse prevention, links the privacy notice and obtains the reviewed user action.
Email/password and federated methods have corresponding provider notices.

Toolly stores an opaque credential mapping to `ToollyAccountId` only when necessary. Provider user
deletion, Toolly mapping deletion and encrypted object deletion are independent tracked steps.

## Storage and metadata

- Only client-encrypted document objects are uploaded.
- Security Rules authorize canonical account/device scope and deny by default.
- App Check complements but never replaces authentication/authorization.
- Object and metadata paths are adapter-private and versioned.
- Export, logs, indexes, backups and support tooling are reviewed for plaintext leakage.
- Region selection is immutable or migration-sensitive and requires legal/operations approval.

## Collection controls

For SDKs that support automatic initialization/collection:

1. default disabled in build/config;
2. register data and purpose;
3. present reviewed notice/control where required;
4. initialize after decision/consent;
5. test first launch, upgrade and restored-device behavior;
6. verify network traffic and provider console;
7. document deletion and retention.

## Processor verification checklist

- [ ] Current Firebase/GCP terms and data-processing terms reviewed.
- [ ] Service location and transfer assessment completed per service.
- [ ] Subprocessor and support-access terms reviewed.
- [ ] Retention/deletion behavior verified and recorded.
- [ ] Security Rules and IAM least privilege reviewed.
- [ ] Development, staging and production projects isolated.
- [ ] App Check monitor/enforce rollout tested.
- [ ] Logs/exports/backups contain no prohibited data.
- [ ] Data principal access/deletion workflow tested.
- [ ] Privacy/store disclosures match observed collection.

## Official references

- Firebase privacy and security: <https://firebase.google.com/support/privacy>
- Firebase Android phone authentication:
  <https://firebase.google.com/docs/auth/android/phone-auth>
- Firebase App Check enforcement:
  <https://firebase.google.com/docs/app-check/enable-enforcement>
- Firebase user management:
  <https://firebase.google.com/docs/auth/admin/manage-users>

Accessed 2026-07-23. Revalidate at implementation and release.
