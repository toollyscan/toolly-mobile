# Content Guidelines

Defines the voice, tone and content standards for all user-facing text in Toolly.

---

## Voice and tone

Toolly communicates with users in a clear, direct and respectful manner.

| Principle | Meaning in practice |
|-----------|---------------------|
| Clear | Use plain language; avoid jargon |
| Honest | Do not exaggerate capabilities |
| Calm | Non-alarming language for recoverable situations |
| Respectful | Treat users as capable adults |
| Privacy-first | Reinforce that data belongs to the user |

---

## Privacy-first language

Toolly stores documents locally by default. All copy must reinforce this principle.

**Required patterns:**

- Describe backup as optional and user-controlled: "Back up your documents to keep them safe across devices" — not "Sync your documents to the cloud".
- Never imply that local documents require a network connection.
- When describing backup, always use "your encrypted backup" to reinforce user ownership.
- Do not describe Toolly servers as the authoritative copy of documents.

**Prohibited patterns:**

- "Your documents are safe in the cloud" — implies cloud is the primary copy.
- "Upload your documents" without explicit user intent.
- Any copy that implies Toolly can read document content.

---

## Offline messaging

When a feature is unavailable because the device is offline, message it clearly and without alarm.

**Required patterns:**

- "You're offline. [Feature] isn't available right now."
- "Connect to the internet to [action]."
- "Your documents are still accessible offline."

**Prohibited patterns:**

- "Cannot connect — data may be lost."
- "Error: network unavailable." (for non-critical offline states)
- Alarming language for expected offline behaviour.

---

## Non-alarming recoverable errors

When an error is recoverable, language must reflect that.

**Required patterns:**

- "Something went wrong. Tap to try again."
- "Export failed. Check your storage and try again."
- "[Feature] failed. Your documents are safe."

**Prohibited patterns:**

- "Fatal error."
- "Data corrupted." (unless data is actually corrupted and unrecoverable)
- Technical error codes displayed to users.
- Stack traces or internal error identifiers displayed to users.

---

## Destructive-action wording

Any action that cannot be undone must use precise, explicit language.

| Action | Required copy pattern |
|--------|-----------------------|
| Delete page | "Delete this page? This cannot be undone." |
| Delete document | "Delete [document name]? This cannot be undone." |
| Remove trusted device | "Remove [device name]? You'll need to re-approve this device." |
| Account deletion | "Delete your account? All local documents and your encrypted backup will be permanently deleted. This cannot be undone." |

Confirm buttons must use the specific action verb, not generic "OK" or "Confirm":

- Use "Delete page", "Delete document", "Remove device", "Delete account".
- Do not use "Yes" alone.

---

## Backup and encryption explanations

When describing backup and encryption, use language that is accurate and understandable.

**Required accuracy:**

- Backup is end-to-end encrypted: "Your backup is encrypted before it leaves your device."
- Toolly cannot read document content: "Your documents are encrypted with a key only you control."
- Recovery phrase explanation: "If you lose access to your account, your recovery phrase is the only way to restore your encrypted documents. Toolly cannot recover your documents without it."

**Prohibited claims:**

- "Military-grade encryption" — do not use marketing terms for encryption.
- "100% secure" — no system is perfectly secure.
- "Unbreakable" — do not overstate security guarantees.

---

## Subscription transparency

All subscription copy must be clear, honest and complete.

**Required information at point of purchase:**

- Exact price and billing period.
- Trial duration and what happens after trial.
- What features are included.
- How to cancel.
- That local documents are not deleted on expiry.

**Required patterns:**

- "₹[price]/month, billed monthly. Cancel any time."
- "Free trial for [N] days, then ₹[price]/month."
- "Your documents remain accessible after your subscription ends."

**Prohibited patterns:**

- "Unlimited" claims for features that have limits.
- Countdown timers creating false urgency ("Offer expires in 10 minutes!").
- Pre-selected paid option: the comparison screen must not have any plan pre-selected.
- Automatic enrollment in paid plans without explicit confirmation.
- Hiding the cancellation path.

---

## No misleading unlimited claims

- Do not claim "unlimited" for any feature that has a documented limit (see ENTITLEMENTS.md).
- Free-tier batch page limits must be clearly stated, not hidden.
- Cloud storage quotas must be clearly stated.

---

## No exaggerated OCR accuracy

- OCR accuracy claims must reflect benchmark results (see DA-004 in DESIGN_AUDIT.md).
- Do not claim "99% accuracy", "perfect text recognition" or similar unless benchmark evidence supports the claim.
- Hindi and Kannada OCR accuracy must be tested separately; accuracy claims must be language-specific if needed.
- Use language such as "Recognise text in your scanned documents" rather than accuracy-specific claims.

---

## No dark patterns

The following patterns are explicitly prohibited:

- **Roach motel:** Making it easy to subscribe but difficult to cancel.
- **Hidden costs:** Subscription price must be fully disclosed before purchase confirmation.
- **Bait and switch:** Free-trial terms changing without notification.
- **Confirmshaming:** Dismissing upgrade offers with self-deprecating text ("No thanks, I don't want great documents").
- **Nagging:** Prompting for subscription more than once per session after the user has dismissed it.
- **Preselection:** Paid option must not be preselected in plan comparison.
- **False urgency:** Do not create artificial time pressure to subscribe.
- **Disguised ads:** No promotional content that mimics document content.

---

## No forced urgency

- Do not display countdown timers for subscription offers unless the offer genuinely expires.
- Do not repeatedly prompt users who have declined an upgrade in the same session.
- Subscription expiry banners must use calm, factual language.

---

## Clear purchase and cancellation expectations

Before purchase confirmation, the following must be visible:

- Price and billing frequency.
- Trial duration (if applicable).
- Cancellation instructions or link.
- What happens to documents on expiry ("Your documents remain accessible").

Cancellation instructions must be reachable from Settings → Subscription management without requiring the user to contact support.

---

## Error message standards

| Error category | Tone | Required elements |
|---------------|------|-------------------|
| Network error | Calm, actionable | What failed; how to retry; that data is safe |
| Storage full | Factual | Amount used; how to free space |
| Auth error | Clear, non-blaming | What went wrong; what to do next |
| OCR failure | Honest | That recognition failed; what is still available |
| Export failure | Actionable | What failed; retry option |
| Backup failure | Calm | Backup paused; local data safe; how to resolve |

---

## Writing for accessibility

- Error messages must be fully descriptive without requiring visual context.
- Do not write "Tap the red button" — describe the action: "Tap Delete document".
- Loading messages must describe what is loading: "Loading your documents" — not just "Loading…".
- Success messages must be specific: "Document exported as PDF" — not just "Done".

---

## Localization notes for content

- All user-facing text must be in string resources; no hardcoded text in code or raster assets.
- Placeholders in strings must use named parameters, not positional: `{document_name}`, not `%s`.
- Strings with plurals must use the platform pluralization API.
- Content guidelines apply equally in English, Hindi and Kannada; translation must preserve tone and intent.
