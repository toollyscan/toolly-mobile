# Benchmark Governance

Toolly benchmarks are decision evidence, not marketing material. This document defines who may produce, review and accept evidence for TLY-006.

## Evidence principles

1. A benchmark claim links to raw measurements, a run manifest, the exact Git commit, corpus version and environment.
2. Targets in planning documents are hypotheses until an accountable reviewer accepts measured evidence.
3. Emulator, simulator and host results are useful for regression detection but cannot satisfy a real-device gate.
4. Failed, aborted and excluded runs remain visible. They are never silently removed from an aggregate.
5. Different languages, document cohorts, device tiers and thermal states are reported separately before an overall result.
6. A summary is regenerated from raw data; manually entered summary values are not evidence.
7. Benchmark code, fixtures and reports must not contain personal documents, identifiers, credentials or production data.
8. A faster implementation does not override security, privacy, accessibility, licence or maintainability requirements.

## Evidence maturity

| Status | Meaning | May support an ADR? |
|--------|---------|---------------------|
| `planned` | Protocol or corpus is defined; no run exists | No |
| `supplemental` | Host, emulator, simulator or exploratory physical-device run | Context only |
| `candidate` | Complete physical-device run satisfying the protocol | Yes, with review |
| `accepted` | Candidate evidence reviewed and linked from the decision | Yes |
| `rejected` | Invalid, non-reproducible or outside approved protocol | No |
| `superseded` | Replaced by a newer protocol, implementation or corpus | Historical only |

Only an accountable reviewer may change candidate evidence to accepted. The raw run remains immutable; acceptance is recorded in a review document or ADR.

## Roles

| Role | Responsibility |
|------|----------------|
| Benchmark owner | Defines protocol, implementation candidates and stopping rules |
| Run operator | Records environment, executes the protocol and uploads raw artifacts |
| Reviewer | Verifies provenance, exclusions, calculations and reproducibility |
| Security/privacy reviewer | Reviews sensitive pipelines, fixtures, logs and crypto evidence |
| Product owner | Accepts, blocks or requests follow-up for a product/architecture decision |

The run operator and reviewer should be different people for release-blocking evidence when staffing permits. A founder-run benchmark requires an explicit independent-review gate.

## Required review questions

- Is the tested implementation the exact commit identified in the manifest?
- Is the corpus consent-safe and does its manifest match the run?
- Are device tier, OS, architecture, memory, power and thermal conditions recorded?
- Are warm-up, measured iterations, timeout and cooldown consistent with the protocol?
- Are all failures, retries, outliers and exclusions visible with reasons?
- Are units and metric definitions identical to the registered contract?
- Can the summary be regenerated from checked-in raw measurements?
- Does the evidence support only the stated decision, without extrapolating to untested devices?
- Are privacy, security, licence, binary-size and removal risks evaluated separately?

## Change control

Changes to a metric definition, corpus normalization, protocol, device tier or aggregation rule create a new version. Results from incompatible versions are not pooled.

A pull request that changes benchmark behavior must update:

- the relevant contract or protocol version;
- the metric registry when units or semantics change;
- the corpus manifest when items or ground truth change;
- the decision register or ADR when an accepted conclusion changes.

## Storage and retention

- Small text manifests and privacy-safe raw measurements may be committed.
- Large images, traces and binary artifacts use an approved artifact store with digest and retention metadata in the run manifest.
- Raw evidence supporting a shipped decision is retained for the supported product lifetime plus the approved engineering retention period.
- Personal or production documents are prohibited even when Git history is private.

## Invalid evidence

Evidence is rejected when it:

- lacks raw measurements or provenance;
- uses summary-only screenshots;
- mixes units or incompatible protocol versions;
- claims a physical-device result from an emulator, simulator or host;
- omits failed runs or unexplained exclusions;
- uses real unredacted identity, financial, health or family documents;
- reports only a blended OCR score while hiding a language or cohort regression;
- selects cryptography solely by throughput;
- cannot be reproduced from the identified commit and inputs.

## Related contracts

- [Benchmark Plan](BENCHMARK_PLAN.md)
- [Corpus Policy](CORPUS_POLICY.md)
- [Device Matrix](DEVICE_MATRIX.md)
- [Metric Contracts](BENCHMARK_METRICS.md)
- [Evidence Format](BENCHMARK_EVIDENCE.md)
