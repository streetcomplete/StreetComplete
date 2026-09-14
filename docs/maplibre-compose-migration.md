# Remaining MapLibre Compose migration work

Finish the production MapLibre Compose migration on top of
[StreetComplete #6352](https://github.com/streetcomplete/StreetComplete/pull/6352),
preserving Android behavior and keeping the map implementation shared for future
iOS work. Full iOS UI/platform-service migration, desktop support, and unrelated
activity/navigation cleanup are outside this plan.

[#7068](https://github.com/streetcomplete/StreetComplete/pull/7068)
is a reference for individual migration implementations, not additional scope.

## Maintaining this plan

- This document contains remaining work, not a progress log.
- Delete a defect, task, or acceptance item when its implementation and relevant
  validation are complete. For partially completed work, remove the completed
  portion and retain the remaining requirement.
- Delete empty sections. Do not retain checked boxes, completed sections,
  resolutions, dated updates, or a history of previous approaches.
- Add entries only when a new defect or gap is discovered. Include the affected
  behavior, source or reproducer, and the evidence needed to close it. Distinguish
  confirmed defects from behavior that still needs investigation or validation.
- Refine existing entries in place when evidence changes. Keep implementation
  and validation evidence in tests, commits, and PR descriptions rather than
  appending it here. A code change alone does not close a device-validation gap.
- Delete this document when no migration work remains. Put any lasting maintenance
  instructions in the appropriate existing project documentation.

## Upstream releases still required

- **Offline completion and initial pack loading:** adopt a release containing
  [MapLibre Compose #1405](https://github.com/maplibre/maplibre-compose/pull/1405).
  Collect `downloadProgress` directly and read `packs.value` after the manager's
  initial load. In v0.16, headless `snapshotFlow` can remain suspended after a
  download finishes, and cold-start cleanup can miss saved packs. Verify the
  download worker completes without an activity and that `Cleaner.cleanOld` and
  `cleanAll` include existing packs after a cold start.
- **Focus and cluster camera parity:** adopt `cameraForBounds` from a release
  containing [#1400](https://github.com/maplibre/maplibre-compose/pull/1400).
  Restore the 0.75 focus / 0.25 cluster zoom margins, maximum zoom 19,
  zoom-dependent durations, and the 0.5 focus zoom threshold. Check point, line,
  and polygon focus and dense clusters with sheet padding, bearing, and tilt.
- **Android shader halos:** adopt a release containing
  [#1394](https://github.com/maplibre/maplibre-compose/pull/1394), then verify pin
  halos on API 33+ and keep the no-halo fallback below API 33.
- **Permission changes:** remove `updatesWithPermissionChanges` after adopting a
  release containing [#1393](https://github.com/maplibre/maplibre-compose/pull/1393).
  Verify deny/grant/revoke and return from settings during foreground tracking.
- **Style replacement:** remove the background/road layer ID workaround when the
  runtime includes the fix for
  [native-ffi #709](https://github.com/maplibre/maplibre-native-ffi/issues/709).
  Verify light/dark style changes and overlay replacement preserve their colors.

## Production validation still required

- Compare the shared map with the legacy map for layer order, road/bridge
  overlays, multipolygons/holes, pin collision padding, selected-pin animation,
  location/track synchronization, labels, font scale, language, theme, and system
  animation settings. Fix unintended differences; validate StreetComplete behavior
  with checks proportional to the existing tests. Do not require pixel parity for
  deliberate changes such as removal of enlarged hit areas.
- Exercise the Android production UI on a physical device with dense quests and
  clusters, edit history, overlay/form/create modes, GPS/navigation/recording,
  airplane-mode use after download and restart, download cancellation/deletion,
  background/resume, and activity/process recreation. Verify release packaging
  includes the required resources and native libraries for supported Android ABIs.
- Build the iOS host and exercise the shared map with changing data, registered
  images, gestures, style replacement, and background/resume. Verify painter
  dimensions and glyph loading. This validates the shared map, not the full iOS
  application migration.
- **Needs investigation: map-entry responsiveness on the Moto G 5G (2024).**
  The former debug map entry reportedly froze the UI for about a second. Profile
  cold startup and repeated entry to the production map, distinguish UI-thread
  blocking from renderer initialization and tile loading, and verify navigation
  stays responsive. Candidates for UI-thread work on first display: adding the
  ~60 style layers, and rasterizing every distinct pin, marker and overlay icon
  (`PinPainter`, `WithHaloPainter`) when their layers first compose.
- Validate the selected Vulkan backend on supported Android devices, including
  an older device below API 33; the API 26 emulator cannot initialize Vulkan.
  Check map-entry responsiveness, rendering correctness, and background/resume. Resolve
  demonstrated compatibility problems before production cutover.
