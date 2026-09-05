# Multiplatform migration notes

These notes record the findings from [probe PR #7068](https://github.com/streetcomplete/StreetComplete/pull/7068).
The branch is a runnable MapLibre Compose integration probe, not a merge candidate.
Android and iOS are the application targets. Desktop remains available for local development,
without release-distribution packaging.

- [What is going well](01-going-well.md)
- [What still needs work](02-needs-work.md)
- [MapLibre Compose upstream findings](03-maplibre-compose-upstream.md)
- [Validation evidence](04-validation.md)
- [Android map retirement](05-android-map-retirement.md)
- [Historical source audit](06-source-audit.md)

## Cleanup on 2026-09-05

- Removed the custom iOS crash reporter. iOS uses master's empty report holder;
  Android's crash reporting is unchanged.
- Removed the deterministic performance scenario, its simulator scripts, and
  validation-only timing, frame callbacks, and publication tracking from the map.
  The scenario covered earlier stalls, not the remaining real-device jank.
  Historical measurements remain in the docs and the removed code remains in Git
  at `3be8406d6b0126781061aa68b766a4477ab76752`.
- Kept map code that serves application behavior, including cached pin data,
  image-installation ordering, retained layers, and off-main source updates.
- Removed desktop installer release targets. `mise run run:desktop` uses a local
  `.app` on macOS, following the MapLibre Compose demo. The app image retains
  resource staging, location purpose strings, and entitlements required for local
  development. Other hosts use the plain Gradle launcher.
- Left iOS background sync and the process-local cleanup timer unchanged pending
  a separate decision. Neither is evidence of validated iOS background execution.

The old validation reports and source audit describe their recorded revisions.
Their test counts and completeness claims do not establish current parity.
Likewise, a successful test on this probe does not verify the corresponding
implementation on master.

## Local development settings

`mise.toml` contains shared commands and the Java version. Ignored `mise.local.toml`
contains machine-specific settings under `[env]`:

- `ANDROID_HOME` and `_.path` for the Android SDK tools.
- `STREETCOMPLETE_AVD` for `mise run emulator`.
- `DEVELOPER_DIR` for an Xcode override, if the selected system Xcode is unsuitable.
- `STREETCOMPLETE_IOS_DEVICE`, `STREETCOMPLETE_IOS_TEAM`, and
  `STREETCOMPLETE_IOS_BUNDLE_ID` for `mise run run:ios`.

The iOS task builds, installs, and launches on the configured device. It requires
the three iOS values above before it starts the build. No global Gradle setup is needed.
