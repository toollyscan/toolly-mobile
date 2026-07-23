# Design Tokens

This document defines the design token specification for Toolly V1. Design tokens are the
single source of truth for visual decisions: colour, typography, spacing, elevation, radius
and motion. All tokens must be implemented in Figma (as styles or variables) and in the
engineering implementation (as a shared token file).

Actual Figma implementation of these tokens is **evidence-pending**; see
[FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

Token names use the format `{category}/{role}` or `{category}/{scale-step}`.

---

## Colour tokens

### Brand colours

| Token | Light mode value | Dark mode value | Usage |
|-------|-----------------|-----------------|-------|
| `color/brand/primary` | `#1A6BFF` | `#4D8FFF` | Primary actions, FAB, active tabs |
| `color/brand/primary-container` | `#E3EDFF` | `#0D3A80` | Button backgrounds, selection states |
| `color/brand/on-primary` | `#FFFFFF` | `#FFFFFF` | Text/icons on primary |
| `color/brand/on-primary-container` | `#0D3A80` | `#E3EDFF` | Text/icons on primary-container |
| `color/brand/premium` | `#F5A623` | `#F5A623` | Premium badge, premium CTAs |
| `color/brand/on-premium` | `#1A1A1A` | `#1A1A1A` | Text on premium colour |

### Semantic colours

| Token | Light mode value | Dark mode value | Usage |
|-------|-----------------|-----------------|-------|
| `color/semantic/success` | `#1E7A3A` | `#4CAF71` | Success states, backup complete |
| `color/semantic/on-success` | `#FFFFFF` | `#1A1A1A` | Text/icons on success |
| `color/semantic/error` | `#C62828` | `#EF5350` | Error states, inline errors |
| `color/semantic/on-error` | `#FFFFFF` | `#1A1A1A` | Text/icons on error |
| `color/semantic/warning` | `#E65100` | `#FF8A65` | Warning banners, expiry notices |
| `color/semantic/on-warning` | `#FFFFFF` | `#1A1A1A` | Text/icons on warning |
| `color/semantic/info` | `#0277BD` | `#4FC3F7` | Informational banners |
| `color/semantic/on-info` | `#FFFFFF` | `#1A1A1A` | Text/icons on info |
| `color/semantic/destructive` | `#C62828` | `#EF5350` | Destructive buttons and icons |
| `color/semantic/on-destructive` | `#FFFFFF` | `#1A1A1A` | Text/icons on destructive |

### Neutral colours

| Token | Light mode value | Dark mode value | Usage |
|-------|-----------------|-----------------|-------|
| `color/neutral/background` | `#F8F9FA` | `#121212` | Screen background |
| `color/neutral/surface` | `#FFFFFF` | `#1E1E1E` | Card, sheet and dialog surfaces |
| `color/neutral/surface-variant` | `#F1F3F4` | `#2A2A2A` | Muted surface (settings rows) |
| `color/neutral/outline` | `#C2C7CC` | `#4A4A4A` | Input field borders, dividers |
| `color/neutral/outline-variant` | `#E0E3E7` | `#3A3A3A` | Subtle dividers |
| `color/neutral/on-background` | `#1A1A1A` | `#E8E8E8` | Body text on background |
| `color/neutral/on-surface` | `#1A1A1A` | `#E8E8E8` | Body text on surface |
| `color/neutral/on-surface-variant` | `#5F6368` | `#9E9E9E` | Secondary/helper text |
| `color/neutral/disabled` | `#9E9E9E` | `#555555` | Disabled text and icons |
| `color/neutral/disabled-container` | `#E0E3E7` | `#2A2A2A` | Disabled component background |
| `color/neutral/scrim` | `#000000 / 32 %` | `#000000 / 60 %` | Modal overlay |

> **Dark mode note:** Dark mode colour values are indicative placeholders. Final dark mode
> colours require a full contrast audit. Dark mode is required before GA (DA-006) but does not
> gate the phone launch.

---

## Typography tokens

All type styles use the platform system font (Roboto on Android, SF Pro on iOS) for Latin
characters. Noto Sans Devanagari and Noto Sans Kannada are used for Hindi and Kannada
respectively.

| Token | Size (sp/pt) | Weight | Line height | Usage |
|-------|-------------|--------|-------------|-------|
| `type/display/large` | 32 | Regular | 40 | Splash headline |
| `type/display/medium` | 28 | Regular | 36 | Section headers |
| `type/headline/large` | 24 | Medium | 32 | Screen titles |
| `type/headline/medium` | 20 | Medium | 28 | Card titles, dialog titles |
| `type/headline/small` | 18 | Medium | 26 | Subsection headings |
| `type/body/large` | 16 | Regular | 24 | Primary body text |
| `type/body/medium` | 14 | Regular | 20 | Secondary body text |
| `type/body/small` | 12 | Regular | 18 | Helper text, captions |
| `type/label/large` | 14 | Medium | 20 | Button labels |
| `type/label/medium` | 12 | Medium | 16 | Chip labels, tab labels |
| `type/label/small` | 11 | Medium | 14 | Badge labels |

Line heights must accommodate Devanagari and Kannada script ascenders. Verify rendering with
Hindi and Kannada text at each style before finalising.

---

## Spacing tokens

Base grid: 4 dp / pt.

| Token | Value | Usage |
|-------|-------|-------|
| `spacing/1` | 4 dp | Tight internal padding |
| `spacing/2` | 8 dp | Small internal padding |
| `spacing/3` | 12 dp | Medium internal padding |
| `spacing/4` | 16 dp | Standard component padding, margin |
| `spacing/5` | 20 dp | |
| `spacing/6` | 24 dp | Section padding, dialog padding |
| `spacing/8` | 32 dp | Large section spacing |
| `spacing/10` | 40 dp | Screen-level padding (top/bottom) |
| `spacing/12` | 48 dp | FAB bottom margin |
| `spacing/16` | 64 dp | |

---

## Radius tokens

| Token | Value | Usage |
|-------|-------|-------|
| `radius/none` | 0 dp | No rounding (dividers) |
| `radius/xs` | 4 dp | Chips, badges |
| `radius/sm` | 8 dp | Input fields, small cards |
| `radius/md` | 12 dp | Standard cards |
| `radius/lg` | 16 dp | Bottom sheets, dialogs |
| `radius/xl` | 24 dp | Large modals |
| `radius/full` | 9999 dp | Pills, FAB, circular buttons |

---

## Elevation / shadow tokens

| Token | Android dp | iOS shadow | Usage |
|-------|-----------|-----------|-------|
| `elevation/none` | 0 | None | Flat surfaces |
| `elevation/sm` | 1 dp | opacity 0.1, offset (0,1), blur 2 | Subtle card lift |
| `elevation/md` | 4 dp | opacity 0.15, offset (0,2), blur 8 | Cards, input fields |
| `elevation/lg` | 8 dp | opacity 0.2, offset (0,4), blur 16 | Bottom sheets, FAB |
| `elevation/xl` | 16 dp | opacity 0.25, offset (0,8), blur 24 | Dialogs, modals |

---

## Motion tokens

| Token | Duration | Easing | Usage |
|-------|----------|--------|-------|
| `motion/duration/short` | 100 ms | Standard | Icon state change |
| `motion/duration/medium` | 200 ms | Standard | Button state change, tab switch |
| `motion/duration/long` | 300 ms | Emphasized | Screen transition, bottom sheet open |
| `motion/duration/extra-long` | 500 ms | Emphasized decelerate | Complex transitions |
| `motion/easing/standard` | — | cubic-bezier(0.2, 0, 0, 1) | General transitions |
| `motion/easing/emphasized` | — | cubic-bezier(0.2, 0, 0, 1) with decelerate out | Navigation |
| `motion/easing/decelerate` | — | cubic-bezier(0, 0, 0, 1) | Elements entering |
| `motion/easing/accelerate` | — | cubic-bezier(0.3, 0, 1, 1) | Elements leaving |

All motion must respect the system reduced-motion setting. See
[ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md).

---

## Icon size tokens

| Token | Value | Usage |
|-------|-------|-------|
| `icon/size/sm` | 16 dp | Inline icons in labels and chips |
| `icon/size/md` | 24 dp | Standard icon buttons and navigation |
| `icon/size/lg` | 32 dp | Feature icons in empty states |
| `icon/size/xl` | 48 dp | Illustration-scale icons |

---

## Implementation note

These tokens represent the specification. Final values must be reviewed against contrast
requirements (see [ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md)) before
they are frozen. Values marked with indicative placeholders must be confirmed in Figma
before engineering implements them.
