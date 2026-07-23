# Content Guidelines

This document defines the content and copy standards for all user-visible text in the Toolly
V1 application. All strings must adhere to these guidelines before they are added to the app.

---

## Principles

1. **Clear.** Use plain language. Avoid technical jargon that users do not need to understand.
2. **Concise.** Say only what is necessary. Eliminate filler words.
3. **Honest.** Do not make claims that are unverified (e.g., accuracy claims for OCR before
   DA-004 benchmarks are complete). Do not use superlatives.
4. **Respectful.** Do not use manipulative or dark-pattern language (e.g., shame-based
   unsubscribe copy, forced-choice opt-in).
5. **Privacy-respecting.** Never reference document content, filenames or personal information
   in UI strings. Error messages must never include user data.
6. **Consistent.** Use the same terminology for the same concept throughout the app.

---

## Voice and tone

| Context | Tone | Notes |
|---------|------|-------|
| Onboarding and setup | Warm, welcoming | Welcome the user without being over-familiar. |
| Document capture | Focused, helpful | Brief instructions; do not distract from the task. |
| Errors and failures | Calm, solution-oriented | Explain what went wrong and what to do next. Never blame the user. |
| Premium and subscription | Clear, non-pushy | Explain the value; do not pressure. |
| Deletion and destructive actions | Direct, cautious | Be explicit about what will be deleted. Give the user an out. |
| Legal and privacy | Plain language | Required disclosures must be comprehensible to a non-legal reader. |

---

## Capitalisation

| Element | Convention | Example |
|---------|-----------|---------|
| Screen titles | Title case | "Document Library", "Settings" |
| Button labels | Title case | "Scan Document", "Export as PDF" |
| Body text and descriptions | Sentence case | "Your document has been saved." |
| Error messages | Sentence case | "Enter a valid 10-digit mobile number." |
| Menu items | Title case | "Cloud Backup", "Language" |
| Toast / snackbar | Sentence case | "Document deleted." |

---

## Terminology

Use consistent terminology throughout. The following terms are canonical:

| Term | Use | Do not use |
|------|-----|-----------|
| Document | A scanned document in the local vault | "file", "scan" (as noun) |
| Page | A single captured image within a document | "image", "photo" |
| Scan / Capture | The action of scanning a document | These two are interchangeable; prefer "Scan" in CTAs |
| Folder | A folder containing documents | "album", "category" |
| Local vault | The encrypted local storage | "database", "storage", "cloud" |
| Cloud backup | The optional encrypted backup | "sync", "upload" (unless describing the action) |
| Premium | The paid subscription tier | "Pro", "Plus", "Paid" |
| Free | The free tier | "Basic", "Lite", "Trial" |
| Export | Generating a PDF or JPEG for sharing | "download" (documents are local, not downloaded) |
| Share | Sending via the OS share sheet | "send", "transfer" |
| Enhancement | Image processing applied after capture | "filter", "edit" |

---

## Button labels

- Use verbs for action buttons: "Scan", "Save", "Export", "Delete", "Cancel", "Continue".
- Be specific about what the button does: "Delete Document" not "Delete"; "Save to Library"
  not "Save".
- Destructive actions must use a clearly destructive label: "Delete", "Remove", "Disable".
- Do not use vague labels: "OK", "Yes", "No" are acceptable only in confirmation dialogs where
  the question is unambiguous.
- Confirmation dialogs: the confirm button must restate the action
  (e.g., "Delete Document" / "Cancel").

---

## Error messages

Error messages must:

1. Explain what happened in one sentence.
2. Tell the user what to do next (if applicable).
3. Never include technical error codes visible to the user.
4. Never include user data (filename, phone number, document content).
5. Be specific, not generic.

| Situation | Acceptable | Unacceptable |
|-----------|-----------|--------------|
| Invalid phone number | "Enter a valid 10-digit Indian mobile number." | "Invalid input." |
| Wrong OTP | "That code is incorrect. Check the code and try again." | "Error." |
| Camera permission denied | "Camera access is required to scan documents. Enable it in Settings." | "Permission denied." |
| Export failed | "The export failed. Please try again." | "Error code 500." |
| Network error (during OTP) | "Check your internet connection and try again." | "Network error." |
| Low storage | "Your device is running low on storage. Free up space to continue scanning." | "Insufficient storage." |

---

## Empty states

Empty state messages must:

1. Confirm what is empty (one sentence).
2. Tell the user how to add content (one sentence or a CTA button).
3. Not apologise or be negative.

| Screen | Heading | Body | CTA |
|--------|---------|------|-----|
| LIB-010 Library empty | "No documents yet" | "Tap the button below to scan your first document." | "Scan a document" |
| LIB-009 Search empty | "No results" | "No documents match your search. Try different words." | — |

---

## Destructive action copy

Before any irreversible action, the confirmation dialog must:

1. State clearly what will be deleted or removed.
2. State that the action cannot be undone.
3. Use a destructive button label.

Example — delete document:

> **Delete document?**
>
> This will permanently delete the document and all its pages. This cannot be undone.
>
> [Delete document] [Cancel]

Example — delete account:

> **Delete your account?**
>
> Your account and all cloud backup data will be permanently deleted. Documents stored on
> this device are not affected. This cannot be undone.
>
> [Delete account] [Cancel]

---

## Subscription and paywall copy

- Do not use countdown timers or artificial urgency to pressure users into subscribing.
- Do not use dark patterns: no pre-checked opt-ins, no confusing cancel flows.
- Clearly state the price and billing period before the user confirms a purchase.
- Clearly state what is included in the free tier and what requires premium.
- Expiry notices must be informative, not threatening: "Your premium subscription has ended.
  Your local documents are safe and accessible."

---

## Privacy and data copy

- Privacy notices and data-deletion flows must be written in plain language.
- Do not bury grievance officer contact details.
- Data-deletion confirmation must clearly list what will be deleted and what will not
  (e.g., local documents are not affected by account deletion).

---

## Content prohibitions

The following must never appear in user-visible strings:

- References to specific document content or OCR text.
- User's phone number in full (display only masked form: `+91 ••••• •1234` if needed).
- Technical error codes or stack traces.
- Claims about OCR accuracy before DA-004 benchmarks are complete.
- Marketing comparisons to named competitors.
- Profanity or offensive language.
