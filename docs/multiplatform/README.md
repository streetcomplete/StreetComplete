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

## Base branch integration on 2026-09-05

The probe now builds on StreetComplete's `maplibre-compose` branch at `05b04f1c6`,
instead of `master`. The previous probe tip was `08870b8a6`. All 85 probe commits
were replayed; overlapping implementations were reconciled rather than kept as
parallel map stacks.

| Area | Integration decision |
| --- | --- |
| Map state and viewport data | Keep the probe's shared navigation, camera controller, serialized viewport updates, and off-main prepared GeoJSON. The base branch still rebuilds feature collections in map composition. |
| Images and selection | Keep the probe's dynamic image registry, image-before-source ordering, and imperative selection animation. These implement the earlier measured performance fixes. |
| Base style | Keep the probe's legacy-map parity choices, including font sizing and corrected feature filters. Use the base branch's `ColorFilterPainter` to tint the non-SDF oneway image; `iconColor` does not tint a normal bitmap. |
| Scale bar | Use the base branch's MapLibre Compose scale bar. Remove the probe's obsolete platform measurement adapters and their test. |
| Offline maps | Keep the probe's runtime-owned manager and pixel ratio. Add the base branch's timestamp expiry and `Cleaner` call. One downloader implements both. |
| Shared types and resources | Keep one `Pin` in the base branch's separate file and reuse the existing quest `Marker`. Remove duplicate images and the unused halo shader path; the probe already supplies SDF icons through the image registry. |
| Debug map | Remove the base branch's settings-only preview. The ordinary main screen already runs the shared map on all targets. |
| Apple linkage | Remove the base branch's manual linker override. Its quoted `-framework Name` arguments fail the Swift app link; keep the probe's existing linkage. |

The snapshot coordinate remains mutable. Build validation also required adapting
to the newly resolved publication's `LocalMapState`, `DefaultMapRuntime`, and
platform-default runtime options. The resolved version belongs in
[the dependency baseline](03-maplibre-compose-upstream.md#dependency-baseline).
This integration does not replace the outstanding physical-iPhone validation.

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
