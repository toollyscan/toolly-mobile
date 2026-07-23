# Design Audit

This document tracks design decisions that require evidence before production implementation.

---

## Purpose

Each item below represents an area where a design assumption has been made but evidence has not yet been gathered. The Production Gate requires all blocking items to be resolved.

---

## Blocking audit items

### DA-001 — Compose Multiplatform production readiness

**Status:** Open — evidence required before adopting CMP for production UI.

**Questions:**

- What is the current Compose Multiplatform rendering parity between Android and iOS?
- Which UIKit APIs (camera, document picker, haptics) lack CMP equivalents?
- What is the binary-size impact on the iOS target?
- Is GPU-accelerated image processing available via CMP?

**Evidence required:** Benchmark results on representative Android and iOS devices comparing native rendering with CMP rendering for document-scan preview and result views.

---

### DA-002 — Camera and processing API coverage in KMP

**Status:** Open.

**Questions:**

- Which camera APIs are shared vs. platform-specific?
- Can a KMP expect/actual boundary cleanly separate capture logic from processing?

**Evidence required:** Prototype demonstrating capture pipeline with KMP boundary as described in ADR-0001.

---

### DA-003 — SQLCipher vs. SQLite with platform encryption

**Status:** Open.

**Questions:**

- What is the binary-size cost of SQLCipher on Android and iOS?
- Does SQLCipher integrate cleanly with SQLDelight or Room?
- Is platform-native encryption (Android Keystore + iOS Secure Enclave) sufficient without SQLCipher?

**Evidence required:** Security review decision and benchmark of encrypted read/write throughput on representative devices.

---

### DA-004 — OCR engine selection

**Status:** Open — no OCR SDK may be integrated without an approved ADR.

**Questions:**

- Which on-device OCR engines (ML Kit, Tesseract, Vision framework) meet accuracy and latency requirements for Indian documents?
- What are the licence and binary-size impacts?
- Is a commercial engine required for Hindi and Kannada accuracy?

**Evidence required:** Accuracy and latency benchmarks on the benchmark corpus defined in BENCHMARK_PLAN.md, plus a completed dependency analysis.

---

### DA-005 — Accessibility compliance

**Status:** Open.

**Questions:**

- What is the target WCAG level (2.1 AA recommended)?
- Are Hindi and Kannada fonts rendered correctly on all representative devices?
- Are TalkBack (Android) and VoiceOver (iOS) flows validated?

**Evidence required:** Accessibility audit report before first beta release.

---

### DA-010 — Figma design specifications complete

**Status:** Open — design specifications written; Figma implementation evidence pending.

**Questions:**

- Is the Toolly Figma file created and organised per
  [FIGMA_INFORMATION_ARCHITECTURE.md](../design/FIGMA_INFORMATION_ARCHITECTURE.md)?
- Are all 52 screens in [SCREEN_INVENTORY.md](../design/SCREEN_INVENTORY.md) designed and
  approved?
- Are all 50 components in [COMPONENT_INVENTORY.md](../design/COMPONENT_INVENTORY.md)
  designed with all required states?
- Are design tokens implemented in Figma per [DESIGN_TOKENS.md](../design/DESIGN_TOKENS.md)?
- Have the [FIGMA_COMPLETION_GATE.md](../design/FIGMA_COMPLETION_GATE.md) global gate items
  been passed?

**Evidence required:** Figma file URL, screen-count verification and Figma Completion Gate
sign-off by `@shivayogih` before engineering implementation of any screen begins. See
[FIGMA_AUDIT_REPORT.md](../design/FIGMA_AUDIT_REPORT.md).

---

## Non-blocking audit items

| ID | Title | Notes |
|----|-------|-------|
| DA-006 | Dark mode | Required before general availability; not a launch blocker. |
| DA-007 | Tablet layout (Android) | Required before general availability. |
| DA-008 | iPad layout | Required before general availability. |
| DA-009 | Widget support | Post-launch enhancement. |
