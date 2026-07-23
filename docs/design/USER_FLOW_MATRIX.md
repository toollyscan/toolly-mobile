# User Flow Matrix

Documents the required user flows and their completion status for Toolly.

---

## Status key

| Status | Meaning |
|--------|---------|
| Not started | Flow not designed |
| In progress | Flow is being designed |
| Evidence pending | Specification complete; Figma prototype not verified |
| Approved | Flow reviewed and accepted |

> All flows are currently **Evidence pending**.

---

## PT-01 — First-time user

**Entry condition:** App installed; no existing account.

**Steps:**

1. Splash screen displayed.
2. Welcome screen shown.
3. Login method selection.
4. Phone number entry (India dial code pre-selected).
5. OTP sent; OTP verification screen.
6. Profile completion.
7. Empty home screen.
8. Permission introduction for camera.
9. Camera permission granted.
10. Live camera preview (immersive; navigation hidden).
11. Document captured.
12. Automatic edge detection result.
13. Confirm or adjust crop.
14. Enhancement applied.
15. Document editor shown.
16. Document saved to encrypted local vault.
17. Document appears in home/library.

**Success outcome:** Document captured, processed and stored without network connectivity.

**Alternate path:** User skips profile completion; completes later from settings.

**Failure path — OTP not received:** OTP resend screen; back to phone-number entry.

**Failure path — Camera permission denied:** Permission denied screen; settings redirect offered.

**Offline behaviour:** Authentication fails gracefully if first-time login requires network. App must not force the user past an unskippable network-dependent screen that blocks all local functionality.

**Accessibility evidence:** Evidence pending — TalkBack / VoiceOver traversal not verified.

**Localization evidence:** Evidence pending — Hindi and Kannada strings not verified.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-01. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-02 — Returning user

**Entry condition:** App installed; account exists; session may be active or expired.

**Steps — active session:**

1. Splash.
2. Home screen with recent documents.
3. Document opened from library.

**Steps — expired session:**

1. Splash.
2. Session expired prompt.
3. Re-authentication (phone OTP or email).
4. Home screen.

**Steps — offline returning user:**

1. Splash.
2. Cached credential check passes.
3. Home screen (offline mode indicator visible).
4. Local documents accessible; cloud-dependent features unavailable.

**Success outcome:** User accesses documents without unnecessary re-authentication.

**Failure path — re-auth fails:** Auth error; account recovery option offered.

**Offline behaviour:** Local vault accessible without network.

**Accessibility evidence:** Evidence pending.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-02. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-03 — Batch scan

**Entry condition:** User authenticated; camera permission granted.

**Steps:**

1. Scan capture screen (immersive).
2. First page captured (manual or auto).
3. Batch counter incremented.
4. Second page captured.
5. Continued until user taps done (or batch limit reached for free tier).
6. Post-capture review: all page thumbnails shown.
7. Pages reordered if needed.
8. Crop and enhancement applied per page.
9. Document editor: pages assembled.
10. Document named and saved.

**Success outcome:** Multi-page document in encrypted vault.

**Alternate path:** User deletes a page from the review screen before proceeding.

**Failure path — batch limit reached (free tier):** Batch limit banner shown; upgrade prompt offered; existing pages can proceed.

**Offline behaviour:** All capture and processing offline; no cloud dependency.

**Accessibility evidence:** Evidence pending — accessible batch counter required.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending — wider capture workspace.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-03. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-04 — Library search

**Entry condition:** User authenticated; documents exist in library.

**Steps:**

1. Home or library screen.
2. Search bar tapped.
3. Query entered.
4. Results shown (title match, folder match; OCR text for premium users).
5. Document tapped; opens in document editor/viewer.

**Alternate path:** No results found — empty search state with suggestions.

**Failure path:** Search index unavailable — fallback to title-only search with notification.

**Offline behaviour:** Local search index used; cloud-dependent OCR search may be unavailable.

**Accessibility evidence:** Evidence pending — search results announced to screen reader.

**Localization evidence:** Evidence pending — Hindi and Kannada search.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending — search within two-pane layout.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-04. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-05 — Offline workflow

**Entry condition:** Device has no network connectivity.

**Steps:**

1. App launched (returning user; cached credentials valid).
2. Offline indicator visible.
3. Existing documents accessible.
4. New document captured, processed and stored locally.
5. Export to device succeeds (local operation).
6. Cloud-dependent features (backup, sync, OCR search, subscription verification) show appropriate offline states.

**Success outcome:** All core local features work without network; offline indicators are clear and non-alarming.

**Failure path — first-time login attempted offline:** Graceful error; user informed network is required for account creation.

**Accessibility evidence:** Evidence pending — offline indicator announced.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-05. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-06 — Multi-device restore

**Entry condition:** User installs Toolly on a new device; account and encrypted backup exist.

**Steps:**

1. Login on new device.
2. Trusted-device approval flow (existing device approves or recovery codes used).
3. Backup found; restore introduction shown.
4. Restore started; progress displayed.
5. Restore success; documents available.

**Alternate path — no trusted device available:** Recovery code entry path.

**Failure path — restore partial failure:** Partial-failure screen; option to retry remaining files.

**Failure path — quota exceeded on restore:** Restore blocked; quota management offered.

**Offline behaviour:** Restore requires network; offline state shown if unavailable.

**Accessibility evidence:** Evidence pending — progress announced.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-06. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-07 — Account and key recovery

**Entry condition:** User cannot access existing trusted device; recovery is needed.

**Steps:**

1. Login screen; account recovery option selected.
2. Account recovery entry: phone number or email.
3. Recovery codes entered.
4. Account access restored.
5. If encryption key is unavailable: "support cannot decrypt" explanation displayed (see BS-21).

**Alternate path — recovery codes lost:** Support contact offered; no bypass of encryption.

**Failure path — recovery failed:** Recovery failure screen; support contact offered.

**Offline behaviour:** Recovery requires network.

**Accessibility evidence:** Evidence pending.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-07. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-08 — Subscription purchase and restore

**Entry condition:** User is on free plan and encounters a premium feature.

**Steps — purchase:**

1. Premium feature tapped; premium comparison screen shown.
2. Monthly or annual plan selected (no option preselected).
3. Purchase initiated; purchase pending state.
4. Purchase success; premium features enabled.

**Steps — restore:**

1. Settings → Subscription management.
2. Restore purchase tapped.
3. Restore loading; success confirmation.

**Alternate path — trial eligible:** Trial offer shown before purchase.

**Failure path — purchase failure:** Error screen; retry or contact support.

**Failure path — store unavailable:** Store unavailable state; offline entitlement cached.

**Offline behaviour:** Cached entitlement used; verification deferred.

**Accessibility evidence:** Evidence pending — subscription comparison must be screen-reader-friendly.

**Localization evidence:** Evidence pending — INR pricing; correct locale formatting.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-08. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-09 — Privacy and account deletion

**Entry condition:** User wants to delete their account and data.

**Steps:**

1. Settings → Privacy or Account deletion.
2. Account deletion screen; explanation of what is deleted.
3. Confirmation step 1: user acknowledges local data will be deleted.
4. Confirmation step 2: user acknowledges cloud backup will be deleted.
5. Final confirmation with explicit text entry or strong confirmation pattern.
6. Deletion initiated; progress shown.
7. App returns to welcome screen.

**Alternate path — data export before deletion:** Data export flow (PS-17) can be initiated first.

**Failure path — deletion fails (network):** Error state; retry option.

**Offline behaviour:** Account deletion requires network for cloud data removal.

**Accessibility evidence:** Evidence pending — destructive-action confirmation announced.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-09. Evidence pending.

**Completion status:** Evidence pending.

---

## PT-10 — Permission-denied recovery

**Entry condition:** User has denied camera permission; attempts to scan.

**Steps:**

1. Scan action tapped.
2. Camera permission denied screen shown (soft or permanent denial).
3. If soft denial: explanation and request again option.
4. If permanent denial: settings redirect with explanation.
5. User grants permission; scan capture resumes.

**Alternate path — user declines to grant permission:** Import from gallery offered as alternative.

**Failure path — settings redirect unavailable:** Fallback explanation; support contact.

**Offline behaviour:** Not applicable (local permission check).

**Accessibility evidence:** Evidence pending — permission explanation announced.

**Localization evidence:** Evidence pending.

**Phone evidence:** Evidence pending.

**Tablet evidence:** Evidence pending.

**Figma prototype reference:** Page 13 — Prototypes, flow PT-10. Evidence pending.

**Completion status:** Evidence pending.

---

## Flow coverage summary

| Flow | Entry | Steps | Phone | Tablet | Prototype | Status |
|------|-------|-------|-------|--------|-----------|--------|
| PT-01 First-time user | App installed | 17 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-02 Returning user | Account exists | 8 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-03 Batch scan | Authenticated | 10 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-04 Library search | Documents exist | 5 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-05 Offline workflow | No network | 6 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-06 Multi-device restore | New device | 5 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-07 Account/key recovery | No trusted device | 5 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-08 Subscription purchase | Free plan | 4 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-09 Privacy/account deletion | Authenticated | 7 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
| PT-10 Permission-denied recovery | Camera denied | 5 | Evidence pending | Evidence pending | Evidence pending | Evidence pending |
