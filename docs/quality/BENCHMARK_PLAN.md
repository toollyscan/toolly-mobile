# Benchmark Plan

This document coordinates Toolly's TLY-006 benchmark program. It defines planned evidence; it does not claim any implementation meets a target.

## Program sequence

| Slice | Scope | Completion evidence |
|-------|-------|---------------------|
| TLY-006A | Governance, corpus, devices, metrics and evidence contracts | Validators and reviewed contracts |
| TLY-006B | KMP camera boundary and buffer transfer | Physical-device raw runs and ADR-0001 update |
| TLY-006C | Geometry and image enhancement | Accuracy/performance evidence and engine decision |
| TLY-006D | PDF generation and export | Reliability, fidelity, privacy and performance evidence |
| TLY-006E | OCR comparison | Per-language/cohort accuracy, latency, memory and size |
| TLY-006F | Encrypted local vault | Performance, tamper, interruption, corruption and recovery evidence |
| TLY-006G | Representative matrix and decision review | Accepted/blocked decisions linked to raw runs |

TLY-007 approval is required before a new third-party implementation dependency is introduced in a spike.

## Binding contracts

- [Benchmark Governance](BENCHMARK_GOVERNANCE.md)
- [Corpus Policy](CORPUS_POLICY.md)
- [Device Matrix](DEVICE_MATRIX.md)
- [Metric Contracts](BENCHMARK_METRICS.md)
- [Evidence Format](BENCHMARK_EVIDENCE.md)
- `benchmarks/contracts/metrics.v1.json`
- `benchmarks/corpus/manifest.v1.json`

## Corpus readiness

The corpus is currently `definition_only`. Required cohorts and minimum counts are in the machine-readable manifest. No benchmark depending on document images or ground truth becomes a decision candidate until:

- synthetic/licensed items are registered with SHA-256 digests;
- English, Hindi and Kannada minimums are met for the applicable cohort;
- OCR or geometry ground truth is independently sampled and reviewed;
- privacy and licence statuses are approved;
- the exact corpus version is frozen for the comparison.

No real personal or production document may be committed or uploaded as benchmark evidence.

## Device readiness

The active matrix is selected by capability tier close to execution. Required applicable tiers are:

- Android low, mid, high and tablet;
- iPhone low, mid and high;
- iPad/tablet.

Exact model, OS, memory, architecture, power and thermal conditions are recorded per run. Emulator, simulator and host data remain supplemental.

## Planning hypotheses

The following values are initial product hypotheses inherited from the baseline. They are not approved release thresholds.

| ID | Metric | Initial hypothesis | Validation scope |
|----|--------|--------------------|------------------|
| H-BENCH-001 | `camera.first_frame` | p95 below 200 ms | Supported physical phone tiers |
| H-BENCH-002 | `geometry.pipeline_latency` plus enhancement | p95 below 2 s per page | Applicable low/mid phone tiers |
| H-BENCH-003 | `vault.write_latency` | p95 below 500 ms for one reference page | Low-tier physical devices |
| H-BENCH-004 | `vault.read_latency` | p95 below 300 ms for one reference page | Low-tier physical devices |
| H-BENCH-005 | `vault.open_latency` | p95 below 1 s | Cold process, supported tiers |
| H-BENCH-006 | Capture pipeline peak memory | Below 200 MiB | Constrained supported tier |
| H-BENCH-007 | Steady-state application memory | Below 80 MiB after protocol workload | Constrained supported tier |

Startup targets are deferred until application scaffolding defines stable startup boundaries.

OCR hypotheses are evaluated as ranges rather than launch promises:

| Language/cohort | Initial character-accuracy hypothesis |
|-----------------|---------------------------------------|
| English printed A4 | At least 0.97 |
| English printed compact/card-like | At least 0.95 |
| Hindi printed A4 | At least 0.90 |
| Kannada printed A4 | At least 0.85 |
| Handwriting | Report separately; no release threshold approved |

Accuracy equals `1 - character_error_rate` under the approved normalization contract. Aggregate accuracy cannot hide language, cohort or device regressions.

## Run requirements

Each candidate implementation follows a versioned protocol that specifies:

- hypothesis and decision to inform;
- implementation ID and exact commit;
- corpus cohorts and exclusions;
- device tiers and build variant;
- warm-up, measured iteration, timeout and cooldown;
- failure injection and stopping rules;
- metrics and grouping dimensions;
- artifact collection and privacy checks;
- comparison and acceptance authority.

At least three independent physical-device sessions per required tier are expected unless the reviewed protocol justifies another sample design.

## Analysis

- Retain individual samples.
- Report sample counts and failed/timed-out/excluded counts.
- Use median, p90 and p95 for latency; include min/max and distribution plot when useful.
- Report confidence intervals or repeated-run variance when selecting close candidates.
- Separate cold/warm, language, cohort, device tier and thermal state.
- Do not drop an outlier without a recorded rule and reason.
- Do not compare incompatible protocol, corpus or metric versions in one aggregate.

## Completion

TLY-006 is complete only when:

- required child slices have candidate evidence;
- raw artifacts validate and remain accessible;
- representative device tiers are executed or explicitly blocked;
- ADRs record accepted claim and non-claims;
- security, privacy and dependency reviews are complete;
- the product owner records accepted, blocked or follow-up status.

Copied tables, screenshots or self-certified summaries do not satisfy this gate.
