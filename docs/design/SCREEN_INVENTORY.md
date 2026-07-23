# Screen Inventory

Complete inventory of all screens required for the Toolly V1 application. This inventory is
derived from the product scope in [PRODUCT_SCOPE.md](../product/PRODUCT_SCOPE.md) and the
information architecture in
[FIGMA_INFORMATION_ARCHITECTURE.md](FIGMA_INFORMATION_ARCHITECTURE.md).

Each screen must be designed in Figma and pass the
[FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md) before engineering implementation begins.
Figma design status is evidence-pending; see [FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

---

## Status legend

| Status | Meaning |
|--------|---------|
| Pending | Screen definition written; Figma design not yet started. |
| In design | Figma frame in progress; not complete. |
| Design complete | Figma frame complete; handoff not yet started. |
| In review | Design review in progress. |
| Approved | Design approved; ready for engineering handoff. |
| Implemented | Engineering implementation complete. |

---

## Authentication screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| AUTH-001 | Welcome / Splash | Android, iOS | Initial launch screen; Toolly logo and tagline; sign-in and guest-mode entry points. | Free | Pending |
| AUTH-002 | Phone Number Entry | Android, iOS | Input for Indian mobile number (+91 prefix); validation inline. | Free | Pending |
| AUTH-003 | OTP Entry | Android, iOS | 6-digit OTP input; countdown timer; resend button. | Free | Pending |
| AUTH-004 | OTP Rate Limited | Android, iOS | Message shown when OTP resend is blocked; wait-period countdown. | Free | Pending |
| AUTH-005 | Account Created | Android, iOS | Success confirmation after first sign-in; prompt to save recovery codes. | Free | Pending |
| AUTH-006 | Guest Mode Confirmation | Android, iOS | Explains guest-mode limitations; confirm or sign in instead. | Free | Pending |
| AUTH-007 | Recovery Codes — Display | Android, iOS | Shows generated recovery codes; copy and save prompts. | Free | Pending |
| AUTH-008 | Recovery Codes — Acknowledge | Android, iOS | Confirmation that user has saved recovery codes. | Free | Pending |

---

## Document capture screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| CAP-001 | Camera Viewfinder | Android, iOS | Live camera preview; capture button; page counter; flash toggle. | Free | Pending |
| CAP-002 | Edge Detection Active | Android, iOS | Viewfinder with edge-detection overlay highlighting detected document boundary. | Free | Pending |
| CAP-003 | Manual Crop | Android, iOS | Captured image with draggable corner handles for manual crop adjustment. | Free | Pending |
| CAP-004 | Enhancement Preview | Android, iOS | Side-by-side or toggle preview of original vs. enhanced image; colour/greyscale/B&W selector. | Free | Pending |
| CAP-005 | Multi-page Review | Android, iOS | Thumbnail grid of captured pages; reorder, delete and add-more actions. | Free | Pending |
| CAP-006 | Save Document | Android, iOS | Text field for document name; folder selector; save action. | Free | Pending |
| CAP-007 | Camera Permission Error | Android, iOS | Explains why camera permission is required; link to system settings. | Free | Pending |
| CAP-008 | Low Storage Warning | Android, iOS | Warns that device storage is low; option to proceed or cancel. | Free | Pending |

---

## Document library screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| LIB-001 | Document Library | Android, iOS | List of all documents and folders; FAB for new capture; search bar. | Free | Pending |
| LIB-002 | Folder View | Android, iOS | Contents of a selected folder; breadcrumb navigation. | Free | Pending |
| LIB-003 | Document Detail | Android, iOS | Full-screen page viewer; page navigation; export, share and rename actions. | Free | Pending |
| LIB-004 | Page Reorder | Android, iOS | Drag-to-reorder page thumbnails within a document. | Free | Pending |
| LIB-005 | Document Rename | Android, iOS | Inline rename field; confirm and cancel actions. | Free | Pending |
| LIB-006 | Move to Folder | Android, iOS | Folder picker for moving a document. | Free | Pending |
| LIB-007 | Delete Confirmation | Android, iOS | Confirmation dialog before deleting a document or folder; explains action is irreversible. | Free | Pending |
| LIB-008 | Search Results | Android, iOS | List of documents matching the search query; highlights matching title text. | Free | Pending |
| LIB-009 | Search Empty State | Android, iOS | No results illustration and message for a query that returned no matches. | Free | Pending |
| LIB-010 | Library Empty State | Android, iOS | Empty state when no documents have been captured yet; prompt to scan first document. | Free | Pending |

---

## Export and sharing screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| EXP-001 | Export Options | Android, iOS | Bottom sheet with export format options (PDF, JPEG per page); advanced options for premium. | Free / Premium | Pending |
| EXP-002 | Export Progress | Android, iOS | Progress indicator while PDF is being generated. | Free / Premium | Pending |
| EXP-003 | Share Sheet Trigger | Android, iOS | Triggers the native OS share sheet with the exported file. (System UI — design annotation only.) | Free | Pending |
| EXP-004 | Export Error | Android, iOS | Error message with retry option when export fails. | Free / Premium | Pending |

---

## Subscription and paywall screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| SUB-001 | Paywall — Feature Gate | Android, iOS | Shown when a free user attempts a premium feature; lists premium benefits; CTA to subscribe. | Free | Pending |
| SUB-002 | Subscription Options | Android, iOS | Monthly and annual subscription cards with pricing (INR); subscribe CTA. | Free | Pending |
| SUB-003 | Purchase In Progress | Android, iOS | Loading state while store transaction is being processed. | Free | Pending |
| SUB-004 | Purchase Success | Android, iOS | Confirmation that subscription is active; premium features now accessible. | Premium | Pending |
| SUB-005 | Purchase Error | Android, iOS | Error message with retry and cancel options. | Free | Pending |
| SUB-006 | Subscription Management | Android, iOS | Current plan details; link to manage or cancel via the platform store. | Premium | Pending |
| SUB-007 | Restore Purchase | Android, iOS | UI to restore a purchase on a new device or after reinstall. | Free | Pending |
| SUB-008 | Expiry Grace Notice | Android, iOS | Notification that subscription has expired; prompt to renew. | Premium → Free | Pending |

---

## Settings screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| SET-001 | Settings Root | Android, iOS | Top-level settings list: account, language, backup, privacy, about. | Free | Pending |
| SET-002 | Account Settings | Android, iOS | Phone number display (masked); trusted devices; sign out. | Free | Pending |
| SET-003 | Language Settings | Android, iOS | Language picker: English, Hindi, Kannada. | Free | Pending |
| SET-004 | Cloud Backup Settings | Android, iOS | Backup toggle (opt-in); last backup timestamp; storage usage indicator (premium). | Premium | Pending |
| SET-005 | Privacy and Data | Android, iOS | Links to privacy policy; data deletion request; grievance contact. | Free | Pending |
| SET-006 | Delete Account | Android, iOS | Confirmation flow for account and data deletion; explains what will be deleted. | Free | Pending |
| SET-007 | About | Android, iOS | App version, build number, licences, contact and support links. | Free | Pending |

---

## Cloud backup screens

| Screen ID | Screen name | Platform | Description | Entitlement | Design status |
|-----------|-------------|----------|-------------|-------------|---------------|
| BCK-001 | Backup Setup / Opt-in | Android, iOS | Explains what is backed up, that it is encrypted, and that it is opt-in; enable button. | Premium | Pending |
| BCK-002 | Backup In Progress | Android, iOS | Progress indicator; documents being uploaded; cancel option. | Premium | Pending |
| BCK-003 | Backup Complete | Android, iOS | Confirmation with timestamp; storage used. | Premium | Pending |
| BCK-004 | Restore In Progress | Android, iOS | Progress indicator for restore from cloud backup. | Premium | Pending |
| BCK-005 | Restore Complete | Android, iOS | Confirmation that restore is complete; document count. | Premium | Pending |
| BCK-006 | Backup Error | Android, iOS | Error message with retry option; details of what failed. | Premium | Pending |
| BCK-007 | Backup Paused — Expired | Android, iOS | Explains backup is paused because subscription expired; CTA to renew. | Premium → Free | Pending |

---

## Summary

| Section | Screen count |
|---------|-------------|
| Authentication | 8 |
| Document capture | 8 |
| Document library | 10 |
| Export and sharing | 4 |
| Subscription and paywall | 8 |
| Settings | 7 |
| Cloud backup | 7 |
| **Total** | **52** |

All 52 screens must be designed, reviewed and approved in Figma before engineering
implementation begins. Current design status for all screens is **Pending**. See
[FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md) for the current audit state.
