# Design Tokens

Defines the approved design token system for Toolly.

Tokens must be referenced by semantic name in Figma and in production design-system components. Hardcoded hex values must not appear in component code; use token names.

> **Source of truth note:** the primary/surface/outline/text values below are aligned to the
> exported "Toolly Scan — Low-Fidelity Product Flows" Figma file, GitHub issue #52 (TLY-013), and
> the values actually implemented in `shared-ui/src/commonMain/kotlin/com/toolly/shared/ui/ToollyTheme.kt`
> (`ToollyColors`). This table previously listed an earlier placeholder palette (`#2563EB` primary)
> that predates the Figma export and was never implemented; it has been corrected to match.

## Primitive colour tokens

Primitive tokens are the raw values. They must not be used directly in components — use semantic tokens instead.

| Token name | Value | Notes |
|------------|-------|-------|
| `color.primitive.blue.600` | `#2961F2` | Toolly Blue — primary brand |
| `color.primitive.blue.100` | `#E5ECFF` | Primary container |
| `color.primitive.cyan.600` | `#0891B2` | Scan Cyan — capture accent |
| `color.primitive.gray.900` | `#1C2129` | Ink Primary |
| `color.primitive.gray.600` | `#616B78` | Ink Secondary |
| `color.primitive.gray.300` | `#C7CFD9` | Outline |
| `color.primitive.gray.50` | `#F5F7FA` | Surface |
| `color.primitive.white` | `#FFFFFF` | Surface Elevated |
| `color.primitive.green.600` | `#047857` | Success |
| `color.primitive.amber.800` | `#92400E` | Warning |
| `color.primitive.red.700` | `#B91C1C` | Error |

---

## Semantic colour tokens

Semantic tokens communicate intent. Use these in all design and implementation work.

### Brand

| Token name | Primitive | Usage |
|------------|-----------|-------|
| `color.brand.primary` | `color.primitive.blue.600` | Primary actions, active states |
| `color.brand.capture` | `color.primitive.cyan.600` | Capture and scan UI |

### Text

| Token name | Primitive | Usage |
|------------|-----------|-------|
| `color.text.primary` | `color.primitive.gray.900` | Body text, headings |
| `color.text.secondary` | `color.primitive.gray.600` | Supporting text, metadata, hints |
| `color.text.on-primary` | `color.primitive.white` | Text on primary-coloured backgrounds |
| `color.text.on-error` | `color.primitive.white` | Text on error backgrounds |
| `color.text.disabled` | `color.primitive.gray.600` @ 38% opacity | Disabled text |

### Surface

| Token name | Primitive | Usage |
|------------|-----------|-------|
| `color.surface.default` | `color.primitive.gray.50` | Screen backgrounds |
| `color.surface.elevated` | `color.primitive.white` | Cards, sheets, dialogs |
| `color.surface.overlay` | `color.primitive.gray.900` @ 60% opacity | Scrim behind modal content |
| `color.border.outline` | `color.primitive.gray.300` | Card/input/chip borders, dividers |

### Interactive

| Token name | Primitive | Usage |
|------------|-----------|-------|
| `color.interactive.primary` | `color.brand.primary` | Primary button background |
| `color.interactive.primary-pressed` | `color.primitive.blue.600` @ 85% | Primary button press state |
| `color.interactive.focus-ring` | `color.brand.primary` | Focus indicator outline |
| `color.interactive.destructive` | `color.primitive.red.700` | Destructive buttons and actions |

### Status

| Token name | Primitive | Usage |
|------------|-----------|-------|
| `color.status.success` | `color.primitive.green.600` | Success states and icons |
| `color.status.warning` | `color.primitive.amber.800` | Warning states and icons |
| `color.status.error` | `color.primitive.red.700` | Error states, inline errors |

> **Reminder:** Status colours must never be the only indicator of state. Every status token usage must be paired with an icon and/or text label (see COMPONENT_STATE_MATRIX.md).

---

## Spacing tokens

Spacing tokens define padding, margin and gap values. All values are in density-independent pixels (dp on Android; logical pixels on iOS).

| Token name | Value |
|------------|-------|
| `spacing.1` | 4 dp |
| `spacing.2` | 8 dp |
| `spacing.3` | 12 dp |
| `spacing.4` | 16 dp |
| `spacing.5` | 20 dp |
| `spacing.6` | 24 dp |
| `spacing.8` | 32 dp |
| `spacing.10` | 40 dp |
| `spacing.12` | 48 dp |

---

## Radius tokens

Corner radius tokens define the rounding of containers, cards, buttons and chips.

| Token name | Value | Typical usage |
|------------|-------|---------------|
| `radius.sm` | 8 dp | Chips, small components |
| `radius.md` | 12 dp | Cards, inputs, bottom sheets (handle area) |
| `radius.lg` | 16 dp | Dialogs, buttons |
| `radius.xl` | 24 dp | Bottom sheets, large cards |

---

## Typography tokens

Typography tokens define the text styles used across the application.

| Token name | Weight | Size | Line height | Usage |
|------------|--------|------|-------------|-------|
| `type.display.large` | 700 | 32 sp | 40 sp | Splash, hero screens |
| `type.display.medium` | 700 | 28 sp | 36 sp | Screen headers |
| `type.heading.large` | 600 | 22 sp | 28 sp | Section headers |
| `type.heading.medium` | 600 | 18 sp | 24 sp | Card titles, dialog titles |
| `type.heading.small` | 600 | 16 sp | 22 sp | Subsection headers |
| `type.body.large` | 400 | 16 sp | 24 sp | Primary body text |
| `type.body.medium` | 400 | 14 sp | 20 sp | Supporting body text |
| `type.body.small` | 400 | 12 sp | 16 sp | Metadata, captions |
| `type.label.large` | 500 | 14 sp | 20 sp | Button labels |
| `type.label.medium` | 500 | 12 sp | 16 sp | Chip labels, tab labels |
| `type.label.small` | 500 | 11 sp | 14 sp | Status labels, badges |

All sizes are in scalable pixels (sp) to support system text scaling on both Android and iOS.

---

## Elevation / shadow tokens

| Token name | Value | Usage |
|------------|-------|-------|
| `elevation.none` | No shadow | Flat surfaces |
| `elevation.low` | 2 dp shadow, 4% opacity | Cards on surface |
| `elevation.medium` | 4 dp shadow, 8% opacity | Elevated cards, tooltips |
| `elevation.high` | 8 dp shadow, 12% opacity | Bottom sheets (resting), FAB |
| `elevation.modal` | 16 dp shadow, 20% opacity | Dialogs, modals |

---

## Motion tokens

| Token name | Duration | Easing | Usage |
|------------|----------|--------|-------|
| `motion.duration.short1` | 50 ms | Standard | Icon state change |
| `motion.duration.short2` | 100 ms | Standard | Small component transitions |
| `motion.duration.medium1` | 200 ms | Emphasized decelerate | Screen entry |
| `motion.duration.medium2` | 300 ms | Emphasized | Sheet expansion |
| `motion.duration.long1` | 450 ms | Emphasized | Full-screen transition |

**Easing curves:**

| Name | Cubic bezier | Usage |
|------|-------------|-------|
| Standard | `cubic-bezier(0.2, 0.0, 0, 1.0)` | Most transitions |
| Emphasized decelerate | `cubic-bezier(0.05, 0.7, 0.1, 1.0)` | Elements entering screen |
| Emphasized accelerate | `cubic-bezier(0.3, 0.0, 0.8, 0.15)` | Elements leaving screen |

When the system "Reduce Motion" preference is active, replace translate/scale animations with opacity transitions using `motion.duration.short2`.

---

## Interaction tokens

| Token name | Value | Usage |
|------------|-------|-------|
| `interaction.min-touch-target` | 48 × 48 dp | Minimum tap area for all interactive elements |
| `interaction.focus-ring-width` | 2 dp | Visible focus indicator stroke |
| `interaction.focus-ring-offset` | 2 dp | Gap between component and focus ring |

---

## Token naming conventions

Tokens follow the pattern:

```text
{category}.{group}.{variant}
```

Examples:

- `color.brand.primary`
- `color.status.error`
- `spacing.4`
- `radius.md`
- `type.body.large`
- `motion.duration.medium1`

Semantic token names must not reference feature-specific contexts (e.g., `color.scan.button-background` is not a token — use `color.interactive.primary`).

---

## Token usage rules

1. All design and code must reference semantic token names, not primitive values.
2. Primitive values are reserved for token definition only.
3. New tokens must be added to this document before use in Figma or code.
4. A token must not be created for a one-off value — reuse existing tokens.
5. Tokens must be reviewed when OS design system updates (Material Design 3, iOS HIG updates) affect the values.
