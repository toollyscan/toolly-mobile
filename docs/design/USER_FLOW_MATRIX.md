# User Flow Matrix

This document maps the primary user flows through the Toolly V1 application. Each flow traces
a user journey from an entry point to a completion state, identifying the screens involved and
the transitions between them.

Screen IDs reference [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md). Flows that require a
specific entitlement are marked accordingly.

---

## Flow conventions

| Symbol | Meaning |
|--------|---------|
| → | Forward navigation |
| ← | Back navigation |
| ⇒ | Redirect or programmatic navigation |
| [P] | Premium entitlement required |
| [G] | Gate: condition that must be met to proceed |
| [E] | Error path |

---

## UF-001 — New user sign-up

**Entry point:** App first launch (no account, not in guest mode)  
**Completion:** User reaches document library (authenticated)  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | AUTH-001 Welcome | Tap "Sign in / Create account" | AUTH-002 |
| 2 | AUTH-002 Phone Number Entry | Enter +91 number; tap Continue | [G] valid format → AUTH-003 |
| 2a | AUTH-002 | Invalid format | Show inline error; stay on AUTH-002 |
| 3 | AUTH-003 OTP Entry | Enter 6-digit OTP | [G] correct OTP → AUTH-005 |
| 3a | AUTH-003 | Wrong OTP | Show inline error; stay on AUTH-003 |
| 3b | AUTH-003 | Tap Resend | [G] not rate-limited → new OTP sent; stay on AUTH-003 |
| 3c | AUTH-003 | Rate-limited resend | ⇒ AUTH-004 |
| 4 | AUTH-005 Account Created | Tap "Save recovery codes" | AUTH-007 |
| 5 | AUTH-007 Recovery Codes — Display | Copy or note codes; tap Continue | AUTH-008 |
| 6 | AUTH-008 Recovery Codes — Acknowledge | Tap "I've saved them" | ⇒ LIB-001 |

---

## UF-002 — Returning user sign-in

**Entry point:** App launch (account exists; session expired)  
**Completion:** User reaches document library  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | AUTH-001 Welcome | Tap "Sign in" | AUTH-002 |
| 2 | AUTH-002 Phone Number Entry | Enter number; tap Continue | AUTH-003 |
| 3 | AUTH-003 OTP Entry | Enter OTP | ⇒ LIB-001 |
| 3a | AUTH-003 | Failed or expired OTP | Inline error; stay on AUTH-003 |

---

## UF-003 — Guest mode entry

**Entry point:** App launch (no account) or welcome screen  
**Completion:** User reaches document library (guest, no account)  
**Entitlement:** Free (no account-dependent features)

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | AUTH-001 Welcome | Tap "Continue as guest" | AUTH-006 |
| 2 | AUTH-006 Guest Mode Confirmation | Tap "Continue as guest" | ⇒ LIB-001 |
| 2a | AUTH-006 | Tap "Sign in instead" | AUTH-002 |

---

## UF-004 — Capture a new document

**Entry point:** LIB-001 Document Library (FAB) or anywhere capture is triggered  
**Completion:** Document saved and visible in library  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | LIB-001 Document Library | Tap capture FAB | [G] camera permission granted → CAP-001 |
| 1a | LIB-001 | No camera permission | ⇒ CAP-007 |
| 2 | CAP-001 Camera Viewfinder | Tap capture button | ⇒ CAP-002 |
| 3 | CAP-002 Edge Detection | Auto-detected or manually positioned; tap Use | CAP-003 |
| 3a | CAP-002 | Tap retake | ← CAP-001 |
| 4 | CAP-003 Manual Crop | Adjust corners; tap Apply | CAP-004 |
| 5 | CAP-004 Enhancement Preview | Select colour mode; tap Accept | CAP-005 |
| 6 | CAP-005 Multi-page Review | Tap "Add another page" | ← CAP-001 (new page) |
| 6a | CAP-005 | Tap Save | CAP-006 |
| 7 | CAP-006 Save Document | Enter name; select folder; tap Save | ⇒ LIB-001 (document visible) |
| 7a | CAP-006 | Low storage | ⇒ CAP-008 (warning shown before step 7) |

---

## UF-005 — View and browse documents

**Entry point:** LIB-001 Document Library  
**Completion:** User views a specific page in a document  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | LIB-001 Document Library | Tap folder | LIB-002 |
| 2 | LIB-002 Folder View | Tap document | LIB-003 |
| 3 | LIB-003 Document Detail | Swipe between pages | Stay on LIB-003 |
| 3a | LIB-003 | Tap Back | ← LIB-002 |

---

## UF-006 — Search documents

**Entry point:** LIB-001 Document Library (search bar)  
**Completion:** User taps a search result and views the document  
**Entitlement:** Free (title search); Premium (OCR text search)

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | LIB-001 Document Library | Tap search bar; type query | LIB-008 (if results) or LIB-009 (if no results) |
| 2 | LIB-008 Search Results | Tap result | LIB-003 |
| 2a | LIB-009 Search Empty State | Modify query | Back to search |

---

## UF-007 — Export and share a document

**Entry point:** LIB-003 Document Detail  
**Completion:** Native share sheet opened with exported file  
**Entitlement:** Free (PDF and JPEG); Premium (advanced export options)

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | LIB-003 Document Detail | Tap Export / Share | EXP-001 |
| 2 | EXP-001 Export Options | Select format; tap Export | EXP-002 |
| 3 | EXP-002 Export Progress | Wait for export | EXP-003 |
| 4 | EXP-003 Share Sheet | Share or save | Return to LIB-003 |
| 3a | EXP-002 | Export fails | [E] EXP-004 |
| 4a | EXP-004 Export Error | Tap Retry | EXP-002 |

---

## UF-008 — Subscribe to premium

**Entry point:** SUB-001 Paywall (triggered by attempting a premium feature)  
**Completion:** Premium subscription active; user returned to the feature they wanted  
**Entitlement:** Free → Premium

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SUB-001 Paywall | Tap "See plans" or "Subscribe" | SUB-002 |
| 2 | SUB-002 Subscription Options | Select monthly or annual; tap Subscribe | SUB-003 |
| 3 | SUB-003 Purchase In Progress | Wait for store confirmation | SUB-004 |
| 4 | SUB-004 Purchase Success | Tap Continue | ⇒ Feature screen (where paywall was triggered) |
| 3a | SUB-003 | Purchase fails or cancelled | [E] SUB-005 |
| 5a | SUB-005 Purchase Error | Tap Retry | SUB-003 |
| 5b | SUB-005 Purchase Error | Tap Cancel | ← SUB-001 |

---

## UF-009 — Restore purchase

**Entry point:** AUTH-001 Welcome (new device) or SET-001 Settings  
**Completion:** Premium subscription restored  
**Entitlement:** Premium

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SUB-002 or SET-001 | Tap "Restore purchase" | SUB-007 |
| 2 | SUB-007 Restore Purchase | Restore confirmed | ⇒ LIB-001 with premium active |
| 2a | SUB-007 | No purchase found | Show message; stay on SUB-007 |

---

## UF-010 — Manage subscription

**Entry point:** SET-001 Settings → Subscription  
**Completion:** User taken to platform store subscription management (system UI)  
**Entitlement:** Premium

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SET-001 Settings | Tap "Subscription" | SUB-006 |
| 2 | SUB-006 Subscription Management | Tap "Manage" or "Cancel" | ⇒ Platform store (system) |

---

## UF-011 — Enable cloud backup

**Entry point:** SET-004 Cloud Backup Settings  
**Completion:** Backup enabled and first backup initiated  
**Entitlement:** Premium

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SET-001 Settings | Tap "Cloud backup" | SET-004 |
| 1a | SET-004 | User is on free tier | ⇒ SUB-001 Paywall |
| 2 | SET-004 (premium) | Tap "Enable backup" | BCK-001 |
| 3 | BCK-001 Backup Setup | Review consent; tap Enable | BCK-002 |
| 4 | BCK-002 Backup In Progress | Wait | BCK-003 |
| 5 | BCK-003 Backup Complete | Tap Done | ← SET-004 |

---

## UF-012 — Restore from cloud backup

**Entry point:** AUTH-001 Welcome (after sign-in on new device)  
**Completion:** Documents restored from backup  
**Entitlement:** Premium

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | AUTH-005 Account Created (returning) | Tap "Restore from backup" | BCK-004 |
| 2 | BCK-004 Restore In Progress | Wait | BCK-005 |
| 3 | BCK-005 Restore Complete | Tap Go to library | ⇒ LIB-001 |
| 2a | BCK-004 | Restore fails | [E] BCK-006 |

---

## UF-013 — Delete account

**Entry point:** SET-005 Privacy and Data  
**Completion:** Account and cloud data deleted; user signed out  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SET-005 Privacy and Data | Tap "Delete account" | SET-006 |
| 2 | SET-006 Delete Account | Review deletion details; tap Delete | [G] OTP re-verification → account deleted |
| 3 | Deletion complete | | ⇒ AUTH-001 Welcome |

---

## UF-014 — Change language

**Entry point:** SET-001 Settings  
**Completion:** App language changed  
**Entitlement:** Free

| Step | Screen | Action | Next |
|------|--------|--------|------|
| 1 | SET-001 Settings | Tap "Language" | SET-003 |
| 2 | SET-003 Language Settings | Select English / Hindi / Kannada | App restarts with new locale |

---

## Flow coverage summary

| Flow ID | Flow name | Premium | Authenticated only |
|---------|-----------|---------|-------------------|
| UF-001 | New user sign-up | No | — |
| UF-002 | Returning user sign-in | No | — |
| UF-003 | Guest mode entry | No | No |
| UF-004 | Capture a new document | No | No |
| UF-005 | View and browse documents | No | No |
| UF-006 | Search documents | Partial | No |
| UF-007 | Export and share | Partial | No |
| UF-008 | Subscribe to premium | — | Yes |
| UF-009 | Restore purchase | Yes | Yes |
| UF-010 | Manage subscription | Yes | Yes |
| UF-011 | Enable cloud backup | Yes | Yes |
| UF-012 | Restore from cloud backup | Yes | Yes |
| UF-013 | Delete account | No | Yes |
| UF-014 | Change language | No | No |
