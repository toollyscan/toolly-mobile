# Component Inventory

Defines all required UI component families for Toolly.

Component names in this document are canonical and must map cleanly to production design-system component names.

---

## Status key

> All components are currently **Evidence pending** — the live Figma component library has not been audited.

---

## Navigation

### BottomNavBar

Purpose: Primary navigation for phone in non-immersive screens.

Tabs: Home, Documents, Tools, Profile. Prominent Scan action (floating or elevated centre tab).

Variants: Default, icon-only (compact height), badge active.

Platform: Android phone, iPhone.

### NavigationRail

Purpose: Primary navigation for tablet in non-immersive screens.

Items: Home, Documents, Tools, Profile.

Variants: Default, collapsed (icon-only), expanded (icon + label).

Platform: Android tablet, iPad.

### TopAppBar

Purpose: Screen title, back navigation, contextual actions.

Variants: Default (title), large title, search active, selection mode (count + actions).

### BottomAppBar

Purpose: Contextual actions in document editor and selection mode.

Variants: Default, selection active.

### TabBar

Purpose: Segmented views within a screen (e.g., document tabs, filter tabs).

Variants: Default, scrollable.

---

## Buttons

### PrimaryButton

Purpose: Primary call to action.

Variants: Default, loading, disabled, destructive.

Minimum size: 48 dp height.

### SecondaryButton

Purpose: Secondary action, outlined style.

Variants: Default, loading, disabled.

### TextButton

Purpose: Tertiary action, text only.

Variants: Default, disabled.

### DestructiveButton

Purpose: Irreversible actions (delete, account removal).

Variants: Default, loading, disabled.

---

## Icon buttons

### IconButton

Purpose: Single-icon actions in toolbars and cards.

Variants: Default, pressed, focused, disabled.

Minimum size: 48 × 48 dp.

### ToggleIconButton

Purpose: Stateful icon button (e.g., favourite, grid/list toggle).

Variants: Off, on, disabled.

---

## Floating scan action

### ScanFab

Purpose: Primary scan entry point; prominent on home and library screens.

Variants: Expanded (icon + label), collapsed (icon only).

Behaviour: Hidden during immersive flows (capture, crop, editor).

---

## Inputs

### TextField

Purpose: General text input.

Variants: Default, focused, filled, error, disabled, read-only.

### PasswordField

Purpose: Password entry with visibility toggle.

Variants: Default, focused, error, disabled.

### SearchField

Purpose: Document search; inline in screen or in search bar.

Variants: Default, active, filled, results, no-results.

---

## OTP fields

### OtpField

Purpose: Six-digit OTP entry.

Variants: Empty, entering (digit filled), complete, error, expired.

Accessibility: Each cell must be individually focusable with descriptive label.

---

## Search

### SearchBar

Purpose: Full-width search entry with clear and back.

Variants: Inactive, active, filled.

### SearchSuggestionRow

Purpose: Individual suggestion or recent search item.

Variants: Default, highlighted.

---

## Filters

### FilterChip

Purpose: Active filter indicator; tappable to edit or remove.

Variants: Default, selected, disabled.

### FilterSheet

Purpose: Bottom sheet with full filter options.

Variants: Default, active filter applied.

### SortSheet

Purpose: Bottom sheet for sort-order selection.

Variants: Default, option selected.

---

## Cards

### DocumentCard

Purpose: Document entry in list or grid view.

Variants: List (thumbnail + metadata), grid (thumbnail dominant), selection active, offline, error.

### FolderCard

Purpose: Folder entry in library.

Variants: Default, empty, selection active.

---

## Document thumbnails

### DocumentThumbnail

Purpose: Thumbnail image for a multi-page document.

Variants: Loading (skeleton), loaded, error, corrupted.

### PageThumbnail

Purpose: Individual page thumbnail in editor page strip.

Variants: Default, selected, drag active, drop target, error.

---

## Status chips

### StatusChip

Purpose: Inline status indicator for document or backup state.

Variants: Success, warning, error, info, syncing, offline, premium.

Accessibility: Must not use colour alone to convey status.

---

## Banners

### InfoBanner

Purpose: Informational notice at top of screen.

Variants: Info, warning, error, offline, storage pressure.

### ActionBanner

Purpose: Banner with a primary action button (e.g., backup enabled, upgrade prompt).

Variants: Info, warning.

---

## Dialogs

### AlertDialog

Purpose: Confirmation or information dialogs requiring user acknowledgement.

Variants: Informational, confirmation, destructive confirmation.

Destructive confirmation: Must not use a preselected destructive option.

### InputDialog

Purpose: Dialog requiring text input (e.g., rename document).

Variants: Default, error.

---

## Bottom sheets

### ModalBottomSheet

Purpose: Contextual action sheet.

Variants: Default, scrollable content, drag handle visible.

### OptionsSheet

Purpose: List of selectable options.

Variants: Default, option selected.

---

## Snackbars

### Snackbar

Purpose: Brief contextual feedback; dismissible.

Variants: Default, action (e.g., undo), error.

Duration: Short (2 s) for informational; persistent for error with action.

---

## Progress indicators

### LinearProgressBar

Purpose: Determinate progress for export, backup and restore.

Variants: Determinate (with percentage), indeterminate.

### CircularProgressIndicator

Purpose: Loading spinner for async operations.

Variants: Default (indeterminate), determinate.

### SkeletonLoader

Purpose: Placeholder layout while content loads.

Variants: Document list skeleton, page thumbnail skeleton, home skeleton.

---

## Empty states

### EmptyState

Purpose: Screen or section has no content yet.

Variants: Empty home (first use), empty search, empty folder, empty library.

Each variant requires: illustration, headline, supporting text, optional action button.

Illustrations must be original (not copied from competitor apps).

---

## Error states

### ErrorState

Purpose: Full-screen or inline error when content cannot be loaded.

Variants: Network error, storage error, auth error, corruption error.

Each variant requires: icon (not colour-only), headline, supporting text, action button (retry / contact support).

### InlineError

Purpose: Error text below an input field.

Variants: Default.

---

## Offline indicators

### OfflineIndicator

Purpose: Persistent indication that the device is offline.

Variants: Banner, chip, icon-only (for toolbar).

---

## Sync indicators

### SyncIndicator

Purpose: Indicates backup/sync progress or last-synced time.

Variants: Syncing (animated), synced, error, paused, offline.

---

## Subscription indicators

### PremiumLockBadge

Purpose: Indicates a feature requires premium subscription.

Variants: Default (lock icon + label), compact (icon only).

### SubscriptionStatusChip

Purpose: Current subscription status in settings.

Variants: Free, premium active, trial, grace period, expired, cancelled, revoked.

---

## Camera controls

### CaptureButton

Purpose: Primary shutter button.

Variants: Default, processing.

### FlashToggle

Purpose: Flash mode selection.

Variants: Off, auto, on, torch.

### BatchCounter

Purpose: Shows number of pages captured in batch mode.

Variants: Default (0–n pages).

### GalleryImportButton

Purpose: Import from device gallery.

Variants: Default.

### CaptureWarningOverlay

Purpose: Warning overlaid on camera preview.

Variants: Blur, glare, low-light, edge-detection failure.

---

## Crop handles

### CropHandle

Purpose: Corner and edge handles for manual crop.

Variants: Default, active (drag state).

Minimum touch target: 48 × 48 dp including invisible tap area.

### MagnifiedHandleInset

Purpose: Zoomed inset showing precise handle position.

Variants: Active.

### CropOverlay

Purpose: Translucent mask outside the crop region.

Variants: Default, handle active.

---

## Enhancement controls

### EnhancementModeSelector

Purpose: Toggle between enhancement modes (auto, colour, greyscale, B&W).

Variants: Default, selected.

### RotationDial

Purpose: Rotate image by degree.

Variants: Default, active.

### EnhancementProcessingOverlay

Purpose: Progress overlay during enhancement processing.

Variants: Default.

---

## Editor controls

### PageReorderHandle

Purpose: Drag handle for page reorder in thumbnail strip.

Variants: Default, dragging.

Accessibility: Non-drag keyboard/accessibility alternative required (move up, move down actions).

### PageActionMenu

Purpose: Contextual menu for individual page actions.

Variants: Default.

### OcrStatusIndicator

Purpose: OCR availability and progress.

Variants: Pending, success, partial failure, unavailable, premium locked.

### AutosaveIndicator

Purpose: Document save status.

Variants: Saved, saving, unsaved changes, error.

---

## Security and trusted-device controls

### TrustedDeviceCard

Purpose: Represents an approved device.

Variants: Default, current device, remove action.

### RecoveryPhraseDisplay

Purpose: Shows recovery phrase words.

Variants: Masked, revealed.

Accessibility: Do not use screenshot-sensitive content as accessible text; prompt user not to screenshot.

### RecoveryPhraseInput

Purpose: Verification entry for recovery phrase.

Variants: Empty, filling, complete, error.
