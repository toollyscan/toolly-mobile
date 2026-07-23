# Benchmark Evidence Format

## Directory convention

```text
benchmarks/
  contracts/
  corpus/
  evidence/
    <benchmark-id>/
      <run-id>/
        run.json
        measurements.jsonl
        summary.json
        README.md
```

`run.json` and `measurements.jsonl` are the evidence source. `summary.json` is generated and must be reproducible. Large traces or binaries may live outside Git only when the manifest records an immutable digest, approved location and retention.

## Run identifier

Use `<UTC-date>-<implementation>-<device-alias>-<sequence>`, for example:

`20260725-camera-boundary-a-low-01`

Aliases must not contain a person's name, device serial, account identifier or other personal data.

## Required run manifest

The validator requires:

- schema and protocol versions;
- run, benchmark and implementation IDs;
- full Git commit SHA and build variant;
- start/end UTC timestamps and status;
- evidence maturity (`supplemental` or `candidate`);
- operator role, not personal contact data;
- physical/emulator/simulator/host classification;
- platform, OS, model alias, architecture, RAM, storage, power and thermal context;
- corpus version and selected cohort IDs;
- warm-up, measured iterations, timeout and cooldown;
- artifacts with SHA-256 digest and personal-data declaration;
- notes for deviations, aborts and exclusions.

A candidate run must use a physical device, complete all required fields and include raw measurements.

## Raw measurements

`measurements.jsonl` contains one JSON object per sample. Required fields:

- `schema_version`;
- `run_id`, `benchmark_id` and registered `metric_id`;
- `case_id`, `cohort_id` and iteration;
- numeric value and exact registered unit;
- sample status;
- monotonic start/end timestamps when latency is measured;
- allowlisted dimensions such as language, device tier or algorithm candidate.

Do not write document text, filenames, paths, OCR output, phone/email data, hardware identifiers, tokens, key material or exception payloads to measurement records.

## Summary

Generated summaries group by benchmark, metric, implementation, device tier, language and cohort as applicable. They include:

- total, successful, failed, timed-out and excluded sample counts;
- median, p90, p95, min and max;
- exclusion reasons;
- links/digests for raw data and generation code;
- protocol, corpus and metric-registry versions.

Overall scores are optional and never replace cohort results.

## Validation

Run:

```bash
python3 scripts/validate_benchmark_evidence.py --self-test
python3 scripts/validate_benchmark_evidence.py
```

The first command validates the validator against known valid and invalid in-memory records. The second validates contracts, corpus manifests and every checked-in evidence run.

Validation does not prove a result is scientifically sufficient or security-compliant. It proves required structure and basic invariants are present.

## Review record

The pull request or ADR accepting evidence records:

- reviewed run IDs;
- accepted claim and explicit non-claims;
- missing tiers/cohorts and follow-up issues;
- security/privacy/dependency reviews;
- reviewer and product-owner decision;
- expiry or re-benchmark trigger.
