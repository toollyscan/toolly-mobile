# Low-Cost CI Operating Policy

## Purpose

Toolly keeps continuous validation secure and reproducible without running paid
Android and macOS capacity for every implementation commit.

## Development mode

All repository workflows are manual (`workflow_dispatch`) during active feature
development. A commit or pull-request update must not automatically allocate a
GitHub-hosted runner.

Developers and coding agents must batch coherent changes and avoid repeated
cloud-agent or code-review sessions. GitHub Copilot cloud agents and agentic
code review consume GitHub Actions minutes in private repositories and are not
a zero-cost replacement for CI.

## Required milestone gates

| Change or milestone | Required manual workflow |
| --- | --- |
| Documentation, policy, localization or dependency metadata | Documentation CI |
| Android feature milestone or installable APK request | Android CI |
| Shared Android code milestone | Multiplatform CI with `android` |
| iOS feature milestone | Multiplatform CI with `ios` |
| Cross-platform release candidate | Multiplatform CI with `all`, then Android CI and Documentation CI |

Security-sensitive changes to capture, permissions, encryption, vault
persistence, export, sharing, authentication or backend boundaries require the
relevant platform workflow and physical-device evidence before merge.

## Release gates

Pre-production and production candidates require:

1. Consolidated documentation, secret, dependency, Firebase and CI trust checks.
2. Android build, lint, unit tests, androidTest APK compilation and installable
   APK generation.
3. Shared Android and iOS tests, framework links and first-party iOS simulator
   launch.
4. Physical Android test evidence and iPhone/iPad evidence appropriate to the
   release scope.
5. Dependency verification, immutable action pins and fail-closed security
   behavior with no bypass.

## Cost controls

- Android and documentation validation use Linux runners.
- macOS is selected explicitly and is never the default target.
- Documentation and governance checks share one Linux job because GitHub bills
  each job separately and rounds partial minutes.
- Concurrency cancels obsolete runs for the same workflow and ref.
- Debug APK artifacts are retained for three days.
- Failed runs are inspected before any retry; no blind reruns.
- The development Actions budget remains a hard-stop budget and is increased
  only for an approved pre-production or production validation window.

## Branch protection

Required-check rules must match the milestone-driven model. During active
development, draft PRs may remain unmerged without automatic checks. Before
merging a feature or release candidate, run the required manual workflows on
the exact reviewed commit and confirm their successful check results.
