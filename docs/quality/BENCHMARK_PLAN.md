# Benchmark Plan

This document defines the benchmark corpus, representative device matrix and performance targets for Toolly.

---

## Purpose

Benchmarks must be run on representative devices before any production feature is merged. Results must be attached to the relevant pull request or linked from the Production Gate.

---

## Representative device matrix

### Android

| Device | Category | Android version | RAM | Notes |
|--------|----------|----------------|-----|-------|
| Samsung Galaxy A14 | Entry-level phone | Android 13 | 4 GB | India volume leader |
| Redmi 12 | Entry-level phone | Android 13 | 4 GB | India volume leader |
| Samsung Galaxy S23 | Flagship phone | Android 14 | 8 GB | Upper bound |
| Xiaomi Pad 6 | Tablet | Android 13 | 6 GB | Tablet baseline |
| Samsung Galaxy Tab S8 | Flagship tablet | Android 14 | 8 GB | Tablet upper bound |

### iOS

| Device | Category | iOS version | Notes |
|--------|----------|------------|-------|
| iPhone SE (3rd gen) | Entry-level phone | iOS 17 | Smallest supported screen |
| iPhone 14 | Mid-range phone | iOS 17 | Volume baseline |
| iPhone 15 Pro | Flagship phone | iOS 17 | Upper bound |
| iPad (10th gen) | Entry-level tablet | iOS 17 | iPad baseline |
| iPad Pro 12.9 (6th gen) | Flagship tablet | iOS 17 | Tablet upper bound |

---

## Benchmark corpus

The benchmark corpus is a set of representative documents used to measure accuracy and performance.

| Category | Count | Description |
|----------|-------|-------------|
| Aadhaar card | 5 | Front and back; standard and worn |
| PAN card | 5 | Standard and worn |
| Indian passport | 5 | Data page |
| Utility bill | 10 | English and Hindi |
| Handwritten text | 10 | English, Hindi and Kannada |
| Multi-page A4 document | 5 | English, 5–10 pages |
| Low-light capture | 5 | Mixed document types |
| Skewed capture (>15°) | 5 | Deskewing required |

All benchmark documents must be synthetic or redacted before inclusion in the repository. No real personal documents may be committed.

---

## Performance targets

### Camera and capture

| Metric | Target | Measurement method |
|--------|--------|-------------------|
| Camera preview latency | < 200 ms to first frame | Measured from screen tap to preview visible |
| Auto-capture trigger time | < 500 ms after document detected | Measured from detection to shutter |
| Page processing time | < 2 s per page | From capture to enhanced preview |

### Vault operations

| Metric | Target | Measurement method |
|--------|--------|-------------------|
| Document write (1 page, encrypted) | < 500 ms | Measured on entry-level devices |
| Document read (1 page, decrypted) | < 300 ms | Measured on entry-level devices |
| Vault open (cold start) | < 1 s | Measured on entry-level devices |

### Application startup

| Metric | Target | Measurement method |
|--------|--------|-------------------|
| Cold start to interactive | < 3 s | Measured on entry-level devices |
| Warm start to interactive | < 1 s | Measured on entry-level devices |

### Memory

| Metric | Target | Measurement method |
|--------|--------|-------------------|
| Peak heap during capture | < 200 MB | Measured on 4 GB RAM devices |
| Steady-state heap | < 80 MB | Measured on 4 GB RAM devices after 10 captures |

---

## OCR accuracy targets

| Language | Document type | Target accuracy |
|----------|--------------|----------------|
| English | Printed (A4) | ≥ 97 % |
| English | Printed (ID card) | ≥ 95 % |
| Hindi | Printed (A4) | ≥ 90 % |
| Kannada | Printed (A4) | ≥ 85 % |
| Any | Handwritten | ≥ 70 % |

Accuracy is measured as character-level accuracy on the benchmark corpus.

---

## Accessibility targets

| Requirement | Target |
|-------------|--------|
| WCAG level | 2.1 AA |
| TalkBack (Android) | All interactive elements labelled; no unlabelled images |
| VoiceOver (iOS) | All interactive elements labelled; no unlabelled images |
| Minimum touch target | 48 × 48 dp (Android) / 44 × 44 pt (iOS) |
| Minimum contrast ratio | 4.5:1 (normal text) |

---

## How to run benchmarks

Benchmark procedures will be documented in the relevant feature pull requests. Results must be attached as artefacts or linked from the PR before the Definition of Done is signed off.

No benchmark result may be self-certified. Raw measurement output must be attached.
