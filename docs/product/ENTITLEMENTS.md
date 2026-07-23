# Entitlements

Toolly free and premium entitlement matrix.

Numerical limits marked **[H]** are hypotheses requiring cost and user validation before they
are finalised. Do not treat them as commitments.

---

## Guiding principles

1. Core scanning must remain genuinely useful for free users.
2. Normal free exports must not contain a watermark.
3. Do not introduce intrusive advertisements.
4. Do not block access to locally owned documents after subscription expiry.
5. Do not hold user documents hostage behind a subscription.
6. Cloud backup must remain optional.
7. Subscription status must not become the source of truth for document ownership.
8. Failed billing verification must not delete or corrupt local documents.
9. Monetization must not weaken privacy, security, accessibility or offline functionality.

---

## Entitlement matrix

| Capability | Free | Premium | Offline | Cloud dependency | Cost driver | Expiry behaviour | Abuse control | Rationale |
|------------|------|---------|---------|-----------------|-------------|-----------------|---------------|-----------|
| **Document capture** | Unlimited captures | Unlimited captures | Yes | None | Device compute | Unchanged | Rate-limit OTP; capture is local | Core value; restricting capture would harm free users. |
| **Batch page limit per document** | Up to **[H]** 10 pages | Unlimited | Yes | None | Device compute, storage | Reverts to free limit on expiry; existing documents unchanged | Server-side enforcement if server processing is used | Hypothesis H-001; requires cost and usage validation. |
| **Local document storage** | Unlimited local storage | Unlimited local storage | Yes | None | Device storage only | Unchanged after expiry | None; device storage is user-owned | Documents are owned by the user; storage cannot be capped. |
| **PDF export** | Standard PDF export | Standard PDF export + advanced options | Yes | None | Negligible | Unchanged after expiry | None | Export of owned documents must always be available. |
| **Export watermark** | No watermark | No watermark | Yes | None | None | No change | None | Product principle 2: no normal-export watermark. |
| **Image enhancement — standard** | Contrast, brightness, de-shadow, B&W, greyscale | Same | Yes | None | Device compute | Unchanged | None | Core scanning quality. |
| **Image enhancement — advanced** | Not included | Included (despeckling, auto-rotate, adaptive thresholding) | Yes | None | Device compute | Reverts to standard on expiry | None | Advanced processing as a premium differentiator. |
| **Search — title and folder** | Yes | Yes | Yes | None | Local index | Unchanged | None | Basic organisation is free. |
| **Search — OCR text** | Not included | Included | Yes (cached index) | None for on-device; server cost if cloud OCR | OCR compute | Reverts to title search on expiry; existing index retained but not updated | Per-document rate limit | OCR is a cost driver; premium candidate. |
| **OCR — on-device** | Not included | Included — accuracy subject to OCR engine evaluation | Yes | None | Device compute | Reverts to no OCR on expiry | Per-document rate limit | Accuracy claims require DA-004 benchmarks. |
| **Searchable PDF** | Not included | Included | Yes for cached output | None for on-device OCR | OCR compute + PDF embedding | Reverts to non-searchable export on expiry | Per-document rate limit | Linked to OCR capability. |
| **PDF merge** | Not included | Included | Yes | None | Device compute | Reverts to no merge on expiry | None | Advanced PDF tool; premium candidate. |
| **PDF split** | Not included | Included | Yes | None | Device compute | Reverts to no split on expiry | None | Advanced PDF tool; premium candidate. |
| **PDF compression** | Not included | Included | Yes | None | Device compute | Reverts to no compression on expiry | None | Advanced PDF tool; premium candidate. |
| **Document organisation** | Unlimited folders, rename, move, delete | Same | Yes | None | Local storage | Unchanged | None | Core organisation is free. |
| **Cloud backup** | Not included | Included — opt-in, end-to-end encrypted | No | Firebase Storage | Storage, egress, encryption | Backup paused on expiry; local documents unchanged; data retained per retention policy | Backup quota enforced | Cloud backup is a premium capability and a cost driver. |
| **Cloud-storage allowance** | None | **[H]** Up to 5 GB encrypted backup storage | No | Cloud | Storage cost per user | Quota enforced on expiry; uploads paused; existing backup retained per retention policy | Quota hard limit + alerts | Hypothesis H-002; requires cloud cost validation. |
| **Multi-device synchronisation** | Not included | Included (requires backup) | Partial — local copy synced when online | Cloud | Sync compute, storage, egress | Sync paused on expiry; last synced state retained | Concurrent device limit **[H]** 5 devices | Hypothesis H-003; requires cost validation. |
| **Trusted devices** | Security baseline; mechanism and limits evidence pending | Same security baseline | Evidence pending | Provider interaction may be required | Security and support | Unchanged after expiry | Revocation and abuse controls pending TLY-005 | Security controls are not weakened by subscription tier. |
| **Recovery** | Secure recovery baseline; mechanism evidence pending | Same secure baseline | Evidence pending | Provider interaction may be required | Security and support | Unchanged after expiry | Recovery abuse controls pending TLY-005 | Access recovery is a security capability, not a paywall. |
| **Premium support** | Community / self-serve | Priority email support | No | Support channel | Support staff cost | Reverts to standard on expiry | Anti-abuse at support tier | Differentiator for premium users. |
| **Advanced processing** | Not included | Included (future server-side operations where explicitly permitted) | No | Server compute | Cloud compute, egress | Reverts to no advanced processing on expiry | Per-operation rate limit; explicit user consent required | Future premium capability; must not be enabled before Production Gate is approved. |
| **Future AI features** | Not included | Included (subject to individual feature gates) | Subject to feature design | Subject to feature design | Subject to feature design | Reverts on expiry | Per-feature controls | Future capability; no commitment made in V1. |

---

## Expiry behaviour summary

When a premium subscription expires:

1. Local documents are **never deleted**.
2. All local exports (PDF, JPEG) remain available.
3. Cloud backup **pauses**; existing backup data is retained per the retention policy.
4. Advanced processing and OCR revert to free-tier limits.
5. Sync pauses; local copy remains accessible.
6. Trusted-device count reverts to free limit; excess devices are not immediately revoked but
   prompts the user to manage devices.
7. Search reverts to title-and-folder search; the OCR index is retained but not updated.

---

## Architecture constraints

- Entitlement state must be cacheable for offline use.
- The entitlement cache must carry an explicit `cachedAt` timestamp and a
  `freshnessPolicySeconds` value.
- Receipt validation must not block access to locally owned documents.
- Backend entitlement verification must be idempotent.
- Google Play and Apple App Store transaction types must not appear in domain entitlement models.
- Firebase must not become the permanent entitlement domain model.
- A future provider migration, if ever approved, must preserve canonical entitlement history.
- Entitlement contracts belong to Toolly, not to the billing provider.

---

## Free versus premium summary

| Area | Free | Premium |
|------|------|---------|
| Core scanning | ✓ | ✓ |
| Manual crop | ✓ | ✓ |
| Standard enhancement | ✓ | ✓ |
| Local encrypted storage (unlimited) | ✓ | ✓ |
| Document organisation | ✓ | ✓ |
| Standard PDF export | ✓ | ✓ |
| Sharing | ✓ | ✓ |
| No export watermark | ✓ | ✓ |
| No intrusive advertising | ✓ | ✓ |
| Access to local documents after expiry | ✓ (always) | ✓ (always) |
| Advanced enhancement | — | ✓ |
| Unlimited batch pages | — | ✓ |
| OCR and searchable PDF | — | ✓ |
| Advanced PDF tools (merge, split, compress) | — | ✓ |
| Encrypted cloud backup | — | ✓ |
| Multi-device sync | — | ✓ |
| Higher cloud storage allowance | — | ✓ |
| Secure recovery baseline | ✓ | ✓ |
| Future AI features | — | ✓ |
| Premium support | — | ✓ |

---

## Open hypotheses

| ID | Hypothesis | Validation required |
|----|-----------|-------------------|
| H-001 | Free batch limit of 10 pages per document | User research; cost per-document analysis |
| H-002 | Premium cloud-storage allowance of 5 GB | Cloud storage cost per active premium user |
| H-003 | Multi-device sync limit of 5 devices | Sync compute and egress cost analysis |
| H-004 | Free trusted-device limit of 1 device | Security and UX research |
| H-005 | Premium trusted-device limit of 5 devices | Security and UX research |
