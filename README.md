# PackMate — Navigation 3 Compose sample

PackMate is a small, realistic offline packing-list app built to practise **Jetpack Compose Navigation 3** without turning the project into a toy navigation demo.

The app is intentionally useful enough to polish and publish: users can create trips, build packing checklists, track progress, reuse templates, add demo data, and keep everything locally on-device.

> This branch is a standalone Android project. Clone this branch directly, or use it as the source for a new dedicated repository.

## What the app demonstrates

- Strongly typed, serializable `NavKey` destinations.
- `NavDisplay` and `entryProvider` instead of a `NavHost` graph.
- Three independent saved back stacks: **Trips**, **Templates**, and **Settings**.
- Route arguments for trip and template detail screens.
- Navigation-managed dialog destinations for adding items and confirming deletion.
- Back-stack restoration across configuration changes and process recreation.
- “Exit through Trips” behavior and pop-to-root when a selected tab is tapped again.
- State-driven Compose UI with lifecycle-aware collection.

## Real functionality

- Create a trip with destination and travel dates.
- Add, pack, unpack, and delete checklist items.
- See per-trip and overall packing progress.
- Start from Weekend, Business, Beach, or Hiking templates.
- Persist data locally with Preferences DataStore and kotlinx.serialization.
- Reset local data and load a demo trip.
- Dynamic color, dark theme, adaptive launcher icon, release shrinking, unit tests, and CI.

## Tech stack

- Kotlin 2.3.21
- Jetpack Compose BOM 2026.08.00
- Navigation 3 1.1.6
- Material 3
- Lifecycle 2.11.0
- Preferences DataStore 1.2.1
- kotlinx.serialization 1.11.0
- AGP 9.2.1 / Gradle 9.4.1 / JDK 17

## Open and run

1. Clone this branch:

   ```bash
   git clone --branch navigation3-packmate --single-branch https://github.com/toollyscan/toolly-mobile.git PackMateNav3
   cd PackMateNav3
   ```

2. Open the folder in a current Android Studio version.
3. Install Android SDK 37 when prompted.
4. Run the `app` configuration on an emulator or device with Android 6.0 or newer.

Command line:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

The wrapper JAR is stored as reviewed base64 text and decoded into `.gradle-bootstrap/` on first use. This keeps the repository compatible with the browser-based GitHub connector used to create it.

## Navigation map

```text
Trips stack
Trips → Create trip
Trips → Trip details → Add item dialog
Trips → Trip details → Delete trip dialog

Templates stack
Templates → Template details → Create trip

Settings stack
Settings → About
Settings → Reset-data dialog
```

See [`docs/NAVIGATION3_GUIDE.md`](docs/NAVIGATION3_GUIDE.md) for the code walkthrough.

## Architecture

```text
Compose screens
      ↓ events / immutable UI state
PackMateViewModel
      ↓ suspend functions / Flow
TripRepository
      ↓ atomic JSON updates
Preferences DataStore
```

Navigation state is separate from business state. Routes only carry stable identifiers such as `tripId`; the destination reads current data from the shared UI state.

## Play Store readiness

This is a **publishable starter**, not a submitted Play Store listing. Before production release, choose a final unique app name and application ID, create store graphics/screenshots, host the privacy policy, complete the Data safety form, test backup/restore, add a signing configuration, and build a signed App Bundle.

The app requests no permissions and contains no ads, account system, analytics SDK, or network code. Review [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md) and [`docs/PLAY_STORE_CHECKLIST.md`](docs/PLAY_STORE_CHECKLIST.md).

## License

Apache License 2.0. The Navigation 3 multi-back-stack state holder is adapted from the Android Open Source Project recipe and retains attribution in source.
