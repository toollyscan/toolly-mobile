# Screen Inventory

Complete inventory of screens and states required in the Toolly Figma design file.

Each screen must exist in at least two variants: **phone** and **tablet**. Tablet variants may share a frame with the phone variant when the layout is identical, but must include a note if that is intentional.

---

## Status key

| Status | Meaning |
|--------|---------|
| Not started | Frame does not exist |
| In progress | Frame is being designed |
| Evidence pending | Specification complete; Figma frame existence not verified |
| Approved | Frame reviewed and accepted |

> All screens below are currently **Evidence pending** — the live Figma file has not been audited.

> **Note on implementation status vs. this table.** The Phone/Tablet columns above track Figma
> *design-frame* evidence specifically, not code. As of the TLY-013 shared-application-flow work
> (issue #52, PR #55), Compose implementations now exist — independent of, and not a substitute
> for, the Figma evidence this table tracks — for: AU-01 through AU-11 (splash through session
> routing, minus Google/Apple provider screens which remain behind the Phase 4 gate), CE-01/02/03/04
> (crop review with draggable corners and a precision loupe — built but not yet wired into the live
> ML Kit capture flow; see the Milestone 2 commit on that branch for why), ES-01/06/12 (export format
> selection, the searchable-PDF premium-lock affordance, and OS share-sheet delivery — ES-02/03 PDF
> and JPEG export themselves were already implemented and physically validated well before this
> table existed), and a Privacy/Backup center (`PrivacyCenterScreen`/`BackupChoiceScreen` in
> `shared-ui/.../ui/PrivacyBackupScreens.kt`) covering the intent of the wireframe's `6.1-6.4`
> screens, presentation-only pending the Phase 5 cloud-backup gate. Physical-device and Figma-frame
> evidence for all of the above remain outstanding per this table's own status key.
>
> **Document naming and categorization (follow-up to TLY-013).** `DocumentSummary` previously had
> no name/title/category field at all, which is why LI-02's filter chips and SE-01/02/03 search
> were descoped from the TLY-013 milestones above rather than built as non-functional UI. That gap
> is now closed: the encrypted vault's manifest schema gained optional, backward-compatible
> `displayName`/`category` fields (`EncryptedDocumentRepository`, `RenameDocumentUseCase`,
> `TagDocumentUseCase`), and Library (`CaptureSpikeScreen.kt`) now has a real search field and
> Receipts/IDs/Other/Untagged filter chips driven by that data, plus a rename affordance and
> category chips on the document viewer. This covers LI-02's filtering intent and a name-based
> subset of SE-01/02/03 (title matching only, per `USER_FLOW_MATRIX.md` PT-04 — OCR-text search
> stays premium/deferred). It is reachable only through Library's own search field, not as a
> separate SEARCH tab destination — shared-ui's `SearchScreen()` stub is unchanged; wiring a real
> SEARCH tab would need the document list state lifted out of `ToollyDocumentApp` so both
> destinations can share it, left as further follow-up rather than rushed into this pass.

---

## Launch and authentication

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| AU-01 | Splash | Default | Evidence pending | Evidence pending | Status-bar colour; no user content |
| AU-02 | Welcome | Default | Evidence pending | Evidence pending | Login must precede first scan |
| AU-03 | Login method selection | Default | Evidence pending | Evidence pending | Phone OTP, email, Google, Apple (iOS only) |
| AU-04 | Phone number entry | Default, error, loading | Evidence pending | Evidence pending | India dial code pre-selected |
| AU-05 | OTP verification | Default, loading, error, expired | Evidence pending | Evidence pending | |
| AU-06 | OTP resend | Countdown, resend active, error | Evidence pending | Evidence pending | |
| AU-07 | Email/password login | Default, error, loading | Evidence pending | Evidence pending | |
| AU-08 | Google authentication | Loading, error | Evidence pending | Evidence pending | |
| AU-09 | Apple authentication | Loading, error | Evidence pending | Evidence pending | iOS only |
| AU-10 | Profile completion | Default, loading, error | Evidence pending | Evidence pending | |
| AU-11 | Authentication loading | Default | Evidence pending | Evidence pending | |
| AU-12 | Authentication error | Network error, invalid credential, locked out | Evidence pending | Evidence pending | |
| AU-13 | Offline returning user | Default (cached credentials) | Evidence pending | Evidence pending | Offline-first requirement |
| AU-14 | Session expired | Default, re-auth prompt | Evidence pending | Evidence pending | |
| AU-15 | Account recovery entry | Default, loading, error | Evidence pending | Evidence pending | |

---

## Home and library

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| HL-01 | Empty home | Default | Evidence pending | Evidence pending | First-use state |
| HL-02 | Populated home | Default, loading | Evidence pending | Evidence pending | Recent documents |
| HL-03 | Recent documents | Default, loading | Evidence pending | Evidence pending | |
| HL-04 | Documents list | Default, loading, offline | Evidence pending | Evidence pending | List mode |
| HL-05 | Documents grid | Default, loading, offline | Evidence pending | Evidence pending | Grid mode |
| HL-06 | Search | Active, empty query, results, no results | Evidence pending | Evidence pending | |
| HL-07 | Filter panel | Default, active filter | Evidence pending | Evidence pending | Bottom sheet on phone; side panel on tablet |
| HL-08 | Sort sheet | Default | Evidence pending | Evidence pending | |
| HL-09 | Selection mode | Single, multiple | Evidence pending | Evidence pending | |
| HL-10 | Empty search | No results | Evidence pending | Evidence pending | |
| HL-11 | Loading state | Skeleton | Evidence pending | Evidence pending | |
| HL-12 | Storage pressure | Warning banner | Evidence pending | Evidence pending | |
| HL-13 | Corrupted document | Error state | Evidence pending | Evidence pending | |
| HL-14 | Offline status | Banner, indicator | Evidence pending | Evidence pending | |
| HL-15 | Sync status | Syncing, synced, error | Evidence pending | Evidence pending | |
| HL-16 | Tablet two-pane library | Default | Evidence pending | Evidence pending | Library list + document preview |

---

## Scan capture

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| SC-01 | Camera permission introduction | Default | Evidence pending | Evidence pending | |
| SC-02 | Camera permission denied | Soft denial, permanent denial | Evidence pending | Evidence pending | |
| SC-03 | Permanently denied permission | Settings-redirect | Evidence pending | Evidence pending | |
| SC-04 | Camera unavailable | Default | Evidence pending | Evidence pending | |
| SC-05 | Live camera preview | Default, immersive | Evidence pending | Evidence pending | Navigation hidden |
| SC-06 | Auto capture | Detecting, captured | Evidence pending | Evidence pending | |
| SC-07 | Manual capture | Default | Evidence pending | Evidence pending | |
| SC-08 | Batch capture | Page counter, progress | Evidence pending | Evidence pending | |
| SC-09 | Flash control | Off, auto, on, torch | Evidence pending | Evidence pending | |
| SC-10 | Import from gallery | Default, loading | Evidence pending | Evidence pending | |
| SC-11 | Blur warning | Active warning overlay | Evidence pending | Evidence pending | |
| SC-12 | Glare warning | Active warning overlay | Evidence pending | Evidence pending | |
| SC-13 | Low-light warning | Active warning overlay | Evidence pending | Evidence pending | |
| SC-14 | Edge-detection failure | Overlay with manual option | Evidence pending | Evidence pending | |
| SC-15 | Capture processing | Spinner/progress | Evidence pending | Evidence pending | |
| SC-16 | Capture failure | Error, retry | Evidence pending | Evidence pending | |
| SC-17 | Storage-full failure | Error, manage storage | Evidence pending | Evidence pending | |
| SC-18 | Interrupted capture recovery | Recovery prompt | Evidence pending | Evidence pending | |
| SC-19 | Scan-complete review | Thumbnails, add page, confirm | Evidence pending | Evidence pending | |

---

## Crop and enhancement

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| CE-01 | Automatic edge result | Default | Evidence pending | Evidence pending | Immersive layout |
| CE-02 | Manual crop | Default, handle active | Evidence pending | Evidence pending | |
| CE-03 | Four-corner handles | Default, drag active | Evidence pending | Evidence pending | |
| CE-04 | Magnified handle adjustment | Active zoom inset | Evidence pending | Evidence pending | |
| CE-05 | Rotation | Rotation dial active | Evidence pending | Evidence pending | |
| CE-06 | Perspective correction | Grid overlay | Evidence pending | Evidence pending | |
| CE-07 | Original preview | Default | Evidence pending | Evidence pending | Before/after comparison |
| CE-08 | Auto enhancement | Default, applied | Evidence pending | Evidence pending | |
| CE-09 | Colour mode | Active | Evidence pending | Evidence pending | |
| CE-10 | Grayscale mode | Active | Evidence pending | Evidence pending | |
| CE-11 | Black and white mode | Active | Evidence pending | Evidence pending | |
| CE-12 | Enhancement processing | Progress | Evidence pending | Evidence pending | |
| CE-13 | Processing failure | Error, retry | Evidence pending | Evidence pending | |
| CE-14 | Reset | Confirmation dialog | Evidence pending | Evidence pending | |
| CE-15 | Undo | Active | Evidence pending | Evidence pending | |
| CE-16 | Confirm | Transition to editor | Evidence pending | Evidence pending | |
| CE-17 | Tablet large-page workspace | Default | Evidence pending | Evidence pending | Wider crop canvas |

---

## Document editor

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| DE-01 | Document preview | Default, loading | Evidence pending | Evidence pending | |
| DE-02 | Page thumbnails | Default, reordering | Evidence pending | Evidence pending | |
| DE-03 | Reorder pages | Drag active, drop target | Evidence pending | Evidence pending | Accessible alternative required |
| DE-04 | Add page | Sheet | Evidence pending | Evidence pending | |
| DE-05 | Delete page | Confirmation dialog | Evidence pending | Evidence pending | Destructive action |
| DE-06 | Rotate page | Controls | Evidence pending | Evidence pending | |
| DE-07 | Replace page | Sheet | Evidence pending | Evidence pending | |
| DE-08 | Rename document | Dialog | Evidence pending | Evidence pending | |
| DE-09 | Document metadata | Sheet | Evidence pending | Evidence pending | |
| DE-10 | OCR pending | Indicator | Evidence pending | Evidence pending | |
| DE-11 | OCR success | Indicator, searchable badge | Evidence pending | Evidence pending | |
| DE-12 | OCR partial failure | Warning indicator | Evidence pending | Evidence pending | |
| DE-13 | OCR unavailable | Locked state | Evidence pending | Evidence pending | |
| DE-14 | Large-document loading | Skeleton | Evidence pending | Evidence pending | |
| DE-15 | Autosave status | Indicator | Evidence pending | Evidence pending | |
| DE-16 | Save failure | Error banner | Evidence pending | Evidence pending | |
| DE-17 | Offline editing | Offline indicator | Evidence pending | Evidence pending | |
| DE-18 | Conflict indication | Banner, resolution options | Evidence pending | Evidence pending | |
| DE-19 | Undo destructive action | Snackbar with undo | Evidence pending | Evidence pending | |

---

## Export and share

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| ES-01 | Export format selection | Default | Evidence pending | Evidence pending | PDF, JPEG |
| ES-02 | PDF export | Default | Evidence pending | Evidence pending | |
| ES-03 | Image export | Default | Evidence pending | Evidence pending | |
| ES-04 | Quality selection | Default | Evidence pending | Evidence pending | |
| ES-05 | Page size selection | Default | Evidence pending | Evidence pending | |
| ES-06 | Searchable PDF premium state | Locked, upgrade prompt | Evidence pending | Evidence pending | |
| ES-07 | PDF tools premium state | Locked, upgrade prompt | Evidence pending | Evidence pending | |
| ES-08 | Export progress | Progress bar | Evidence pending | Evidence pending | |
| ES-09 | Export success | Confirmation | Evidence pending | Evidence pending | |
| ES-10 | Export failure | Error, retry | Evidence pending | Evidence pending | |
| ES-11 | Cancel export | Confirmation dialog | Evidence pending | Evidence pending | |
| ES-12 | Share | System share sheet | Evidence pending | Evidence pending | OS-level; document intent only |
| ES-13 | Print | Print preview | Evidence pending | Evidence pending | |
| ES-14 | Save to device | Folder picker | Evidence pending | Evidence pending | |
| ES-15 | Destination unavailable | Error | Evidence pending | Evidence pending | |
| ES-16 | Permission failure | Error, settings redirect | Evidence pending | Evidence pending | |
| ES-17 | Large-document warning | Warning sheet | Evidence pending | Evidence pending | |

---

## Backup, sync and security

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| BS-01 | Backup disabled | Default | Evidence pending | Evidence pending | |
| BS-02 | Backup introduction | Default | Evidence pending | Evidence pending | Explicit opt-in required |
| BS-03 | Backup enabled | Active, syncing | Evidence pending | Evidence pending | |
| BS-04 | Backup progress | Progress bar | Evidence pending | Evidence pending | |
| BS-05 | Paused backup | Paused indicator | Evidence pending | Evidence pending | |
| BS-06 | Offline backup state | Offline indicator | Evidence pending | Evidence pending | |
| BS-07 | Retry backup | Retry prompt | Evidence pending | Evidence pending | |
| BS-08 | Quota exceeded | Error, upgrade | Evidence pending | Evidence pending | |
| BS-09 | Sync conflict | Conflict banner, options | Evidence pending | Evidence pending | |
| BS-10 | New-device approval | Approval prompt | Evidence pending | Evidence pending | |
| BS-11 | Trusted devices | List | Evidence pending | Evidence pending | |
| BS-12 | Remove trusted device | Confirmation dialog | Evidence pending | Evidence pending | Destructive action |
| BS-13 | Recovery phrase introduction | Instructional | Evidence pending | Evidence pending | |
| BS-14 | Recovery phrase display | Masked, revealed | Evidence pending | Evidence pending | No screenshot prompt |
| BS-15 | Recovery phrase verification | Input fields | Evidence pending | Evidence pending | |
| BS-16 | Recovery failure | Error | Evidence pending | Evidence pending | |
| BS-17 | Key unavailable | Error | Evidence pending | Evidence pending | |
| BS-18 | Restore progress | Progress bar | Evidence pending | Evidence pending | |
| BS-19 | Restore success | Confirmation | Evidence pending | Evidence pending | |
| BS-20 | Restore partial failure | Warning, partial results | Evidence pending | Evidence pending | |
| BS-21 | Support cannot decrypt | Explanation screen | Evidence pending | Evidence pending | Privacy-first content required |

---

## Subscription

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| SU-01 | Free plan | Default | Evidence pending | Evidence pending | |
| SU-02 | Premium comparison | Monthly, annual | Evidence pending | Evidence pending | No preselected paid option |
| SU-03 | Monthly plan | Default | Evidence pending | Evidence pending | |
| SU-04 | Annual plan | Default | Evidence pending | Evidence pending | |
| SU-05 | Trial eligibility | Eligible, not eligible | Evidence pending | Evidence pending | |
| SU-06 | Purchase pending | Loading | Evidence pending | Evidence pending | |
| SU-07 | Purchase success | Confirmation | Evidence pending | Evidence pending | |
| SU-08 | Purchase failure | Error, retry | Evidence pending | Evidence pending | |
| SU-09 | Restore purchase | Loading, success, failure | Evidence pending | Evidence pending | |
| SU-10 | Grace period | Warning banner | Evidence pending | Evidence pending | |
| SU-11 | Billing retry | Retry prompt | Evidence pending | Evidence pending | |
| SU-12 | Subscription expired | Expired state | Evidence pending | Evidence pending | Documents remain accessible |
| SU-13 | Cancelled but active | Active until date | Evidence pending | Evidence pending | |
| SU-14 | Refunded | Status indicator | Evidence pending | Evidence pending | |
| SU-15 | Revoked | Revoked state | Evidence pending | Evidence pending | |
| SU-16 | Store unavailable | Offline/error state | Evidence pending | Evidence pending | |
| SU-17 | Verification pending | Loading | Evidence pending | Evidence pending | |
| SU-18 | Offline entitlement state | Cached state indicator | Evidence pending | Evidence pending | |

---

## Profile and settings

| ID | Screen | States required | Phone | Tablet | Notes |
|----|--------|----------------|-------|--------|-------|
| PS-01 | Profile | Default | Evidence pending | Evidence pending | |
| PS-02 | Edit profile | Default, loading, error | Evidence pending | Evidence pending | |
| PS-03 | Language | Selection list | Evidence pending | Evidence pending | en-IN, hi-IN, kn-IN |
| PS-04 | Theme | Light, dark, system | Evidence pending | Evidence pending | |
| PS-05 | Scan defaults | Default | Evidence pending | Evidence pending | |
| PS-06 | Export defaults | Default | Evidence pending | Evidence pending | |
| PS-07 | Backup settings | Default | Evidence pending | Evidence pending | |
| PS-08 | Storage management | Default | Evidence pending | Evidence pending | |
| PS-09 | Privacy | Default | Evidence pending | Evidence pending | |
| PS-10 | Security | Default | Evidence pending | Evidence pending | |
| PS-11 | Trusted devices | Default | Evidence pending | Evidence pending | |
| PS-12 | Subscription management | Default | Evidence pending | Evidence pending | |
| PS-13 | Help and support | Default | Evidence pending | Evidence pending | |
| PS-14 | About | Default | Evidence pending | Evidence pending | |
| PS-15 | Terms | Default | Evidence pending | Evidence pending | |
| PS-16 | Privacy policy | Default | Evidence pending | Evidence pending | |
| PS-17 | Data export | Default, loading, success | Evidence pending | Evidence pending | |
| PS-18 | Account deletion | Confirmation flow | Evidence pending | Evidence pending | Destructive; multi-step required |
| PS-19 | Sign out | Confirmation dialog | Evidence pending | Evidence pending | |

---

## Summary counts

| Section | Screen count |
|---------|-------------|
| Launch and authentication | 15 |
| Home and library | 16 |
| Scan capture | 19 |
| Crop and enhancement | 17 |
| Document editor | 19 |
| Export and share | 17 |
| Backup, sync and security | 21 |
| Subscription | 18 |
| Profile and settings | 19 |
| **Total** | **161** |

Phone and tablet variants are required for every screen, resulting in a minimum of 322 Figma frames for screen coverage alone. State variants add to this total.
