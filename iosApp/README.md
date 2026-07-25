# Toolly iOS host

This first-party SwiftUI host embeds the shared `ToollySharedUI` Compose Multiplatform framework.
Swift owns only the operating-system application surface; Toolly screens, accessibility semantics
and English/Hindi/Kannada resources remain in shared Kotlin.

## Automated simulator gate

`scripts/ios_simulator_smoke.sh`:

1. selects available iPhone and iPad simulators from the reviewed Xcode installation;
2. builds the debug host without distribution signing;
3. installs the resulting application bundle on both form factors;
4. launches `com.toollyscan.mobile` on both form factors; and
5. shuts down both simulators.

This gate requests no permission, makes no network call, contains no Firebase/AWS integration and
adds no dependency.

## Signing boundary

CI uses `CODE_SIGNING_ALLOWED=NO` only for the simulator smoke test. Physical iPhone/iPad testing,
TestFlight and App Store delivery require a separately controlled Apple Developer account,
registered bundle identifier, signing certificate and provisioning profile. Those credentials must
remain in protected CI secrets and must never be committed.
