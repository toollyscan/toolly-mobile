# Responsive Layouts

Documents the adaptive layout behaviour of Toolly across device sizes, orientations and postures.

---

## Breakpoints

Toolly follows the Material Design 3 adaptive breakpoints mapped to the platform conventions used by Android and iOS.

| Class | Width range | Typical device |
|-------|-------------|----------------|
| Compact | 0 – 599 dp | Phone portrait |
| Medium | 600 – 839 dp | Phone landscape, small tablet, foldable inner |
| Expanded | 840 dp + | Tablet, large tablet, foldable outer with keyboard |

All screens must be functional at compact width. Medium and expanded widths must provide improved use of available space and must not simply stretch compact layouts.

---

## Navigation model per class

### Compact (phone portrait)

- **Primary navigation:** BottomNavBar with 4 items and a prominent ScanFab.
- **Secondary navigation:** Back button in TopAppBar.
- **Immersive flows:** BottomNavBar hidden; TopAppBar replaced by immersive control bar.
- **Sheets:** Modal bottom sheets for contextual actions and filters.

### Medium (phone landscape, small tablet)

- **Primary navigation:** BottomNavBar or NavigationRail depending on available height.
- When height is insufficient for BottomNavBar (< 480 dp), switch to NavigationRail.
- **ScanFab:** Moves to NavigationRail or remains visible in accessible position.
- **Sheets:** May expand to larger bottom sheets; full-screen dialogs acceptable.

### Expanded (tablet, large tablet)

- **Primary navigation:** NavigationRail (always visible).
- **ScanFab:** Integrated into NavigationRail as a prominent item.
- **Content pane:** Two-pane layout where appropriate (see per-workflow table below).
- **Sheets:** Replaced by side panels where contextually appropriate.

---

## Per-workflow layout specification

### Launch and authentication

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Single-column, centred | Centred card with max width 480 dp; background fill |
| Navigation | None (no nav during auth) | None |
| Modal behaviour | Full-screen sheets | Centred dialog |
| Max content width | Full width | 480 dp |
| Immersive mode | Not applicable | Not applicable |

### Home and library

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Single-pane list or grid | Two-pane: library list (left) + document preview (right) |
| Navigation | BottomNavBar | NavigationRail |
| Pane behaviour | Full-width | Library pane ≥ 320 dp; preview pane fills remainder |
| Max content width | Full width | Library pane: 360 dp; no max for preview |
| Filter/sort | Bottom sheet | Popover or side panel |
| Selection mode | Bottom action bar | Toolbar in library pane |
| Immersive mode | Not applicable | Not applicable |

### Scan capture

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Full-screen camera preview | Full-screen camera preview |
| Navigation | BottomNavBar hidden; immersive | NavigationRail hidden; immersive |
| Pane behaviour | Single pane; controls overlaid | Single pane; controls overlaid; capture button repositioned |
| Max content width | Full screen | Full screen |
| Immersive mode | Full; status bar adjusted | Full |
| Landscape | Preview rotates; controls reposition | Preview rotates; controls reposition |

### Crop and enhancement

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Crop canvas + bottom control bar | Wider canvas; control panel may move to side |
| Navigation | Hidden during crop | Hidden during crop |
| Canvas size | Full available height minus controls | Larger canvas; controls may be in rail |
| Immersive mode | Yes | Yes |
| Landscape | Canvas reflows; controls reposition below or beside | Wide canvas mode active |

### Document editor

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Page preview + horizontal thumbnail strip at bottom | Page preview (main) + vertical thumbnail strip (side) |
| Navigation | Reduced; back and save visible | NavigationRail visible; back and save in TopAppBar |
| Page thumbnail strip | Horizontal scroll, bottom | Vertical scroll, leading edge |
| OCR panel | Bottom sheet | Side panel |
| Immersive mode | Full-screen preview option | Full-screen preview option |

### Export and share

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Bottom sheet or full-screen options | Options in a side panel or centred card |
| Navigation | Standard | Standard |
| Max content width | Full width | 600 dp centred card |
| Modal behaviour | Bottom sheet | Dialog |

### Backup, sync and security

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Single-column settings-style | Single-column; max content width 720 dp |
| Navigation | Standard settings back navigation | NavigationRail; back in TopAppBar |
| Recovery phrase | Full-screen | Centred card |

### Subscription

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Vertical plan cards; comparison scroll | Side-by-side plan cards |
| Navigation | Standard | Standard |
| Max content width | Full width | 720 dp |
| Modal behaviour | Full screen | Dialog or centred card |

### Profile and settings

| Aspect | Phone | Tablet |
|--------|-------|--------|
| Composition | Single-column list | Two-pane: category list (left) + detail (right) |
| Navigation | Standard | NavigationRail; category pane always visible |
| Max content width | Full width | 1200 dp total; category pane ≤ 360 dp |

---

## Landscape orientation

All screens must be functional in landscape orientation on phone and tablet.

| Screen category | Landscape behaviour |
|----------------|---------------------|
| Auth and onboarding | Single-column scrollable; centred card |
| Home and library | Existing layout; BottomNavBar may become NavigationRail if height < 480 dp |
| Scan capture | Camera preview fills landscape viewport; capture button repositioned |
| Crop and enhancement | Wider canvas preferred; controls move below or beside canvas |
| Document editor | Thumbnail strip moves or becomes collapsible |
| Export, settings, subscription | Standard scrollable content |

---

## Split-screen and multi-window

On Android, Toolly must function in split-screen mode at any pane width ≥ 320 dp.

On iPad, Toolly must function in Split View and Slide Over.

| Pane width | Expected behaviour |
|------------|--------------------|
| < 320 dp | Graceful degradation: core content visible; secondary panels may collapse |
| 320 – 599 dp | Compact layout |
| 600 – 839 dp | Medium layout |
| ≥ 840 dp | Expanded layout |

---

## Large text and accessibility scaling

All layouts must remain functional when system text size is increased to 200%.

Requirements:

- No text truncation in interactive elements (buttons, labels) at 200% text scale.
- Minimum touch targets remain ≥ 48 × 48 dp at all text scales.
- Bottom sheets and dialogs must scroll if content overflows.
- Component height must scale with text size; fixed-height containers that clip text are not permitted.
- Verify with the largest Hindi and Kannada glyph sizes on representative devices.

---

## Foldable devices

For foldable Android devices (e.g., inner display in book posture):

| Posture | Behaviour |
|---------|-----------|
| Folded (outer) | Compact layout |
| Half-open (tabletop) | UI should not place interactive controls at the physical fold |
| Open (inner, flat) | Medium or expanded layout; avoid placing controls at the hinge |

Foldable support is non-blocking for initial launch but must be documented and tested before general availability.

---

## Maximum content width

To prevent uncomfortable line lengths on very large displays:

| Content type | Maximum width |
|-------------|--------------|
| Body text | 680 dp |
| Form inputs | 480 dp |
| Auth centred card | 480 dp |
| Settings detail pane | 720 dp |
| Document editor main canvas | Full available width |
| Subscription plan comparison | 720 dp |
