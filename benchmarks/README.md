# Toolly Benchmark Workspace

This directory contains versioned benchmark contracts, consent-safe corpus metadata and privacy-safe evidence.

## Layout

- `contracts/metrics.v1.json` — metric IDs, units and direction.
- `corpus/manifest.v1.json` — required cohorts and registered corpus items.
- `templates/run.template.json` — non-evidence example of the run contract.
- `evidence/` — immutable run manifests and raw measurements.

Do not commit personal documents, OCR text, user filenames, device serials, credentials, tokens, keys or production traces.

## Validate

```bash
python3 scripts/validate_benchmark_evidence.py --self-test
python3 scripts/validate_benchmark_evidence.py
```

An empty evidence directory means no benchmark claim exists. It is valid during contract setup but cannot satisfy any Production Gate.
