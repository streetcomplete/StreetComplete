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

## Phase 2: finish offline integration

- Verify that areas downloaded with the bundled minimal style render offline
  with the shared map's packaged glyphs and images, without the hosted style
  repository. Keep old-pack cleanup in `Cleaner`; do not port the fragment's
  duplicate cleanup.

- **Blocked on MapLibre Compose: headless download completion.** Adopt an upstream
  fix that publishes offline progress without a visible composition. In v0.16,
  native downloads finish but `snapshotFlow` can remain suspended until snapshot
  apply notifications are sent. Verify the download worker completes after a cold
  start without an activity or visible map.
- **Blocked on MapLibre Compose: initial offline-pack loading.** Await an upstream
  readiness API before `deleteOld` and `clear` read `OfflineManager.packs`. In
  v0.16, its initially empty list cannot distinguish loading from an empty cache,
  so cold-start cleanup can miss saved packs. Verify `Cleaner.cleanOld` and
  `cleanAll` include existing packs when started by an Android worker.
- **Confirmed: clearing can swallow cancellation.** Propagate cancellation from
  `MapLibreMapTilesDownloader.clear()`. Make download cancellation pause the pack,
  and preserve the original failure if pausing also fails. Test cancellation and
  partial failures during download, deletion, and cache clearing.
- Verify cleanup of interrupted downloads against the actual runtime cache.

## Phase 3: retire the legacy map

After the production validation below passes:

- Remove the legacy map fragments, components, managers, camera/style/image
  helpers, and Android map downloader. Handle tasks saved with legacy fragments
  before removing their classes; the new host currently removes restored fragments
  after `super.onCreate`.
- Remove the direct Android SDK dependency and map assets/glyphs used only by the
  legacy renderer. Remove `updateMapStyle`, `UpdateMapStyleTask`, and the root
  update task's reference once their Android JSON style files are retired. Retain
  resource-copy helpers still used by the shared implementation or other screens.
- Delete `CopyIconsTask` and its task wiring when the legacy map's icon consumers
  are gone. Check remaining `R.string.` references before removing
  `CopyStringsTask` and its wiring. Remove the generated Android Kotlin source
  directory registration once no generated-source consumers remain.
- Delete `ShowMapScreen` and its debug-settings/navigation entry after production
  map validation no longer needs it.
- Review the remaining `TODO maplibre-compose` markers after cutover. Rebase
  `PointerPinButton` and `AttributionButton` on the library controls where
  compatible with the existing Material 2 UI, and review `CompassButton` inputs
  against the shared camera state. Reassess `LocationIndicatorLayer` while
  preserving track-endpoint animation synchronization. Remove obsolete adapters
  and resolved TODOs; keep unresolved upstream dependencies explicitly tracked.
- Verify no production, DI, layout, or build references remain to the retired
  code. Recheck Android release assembly, packaged glyphs/images/native libraries,
  first map frame, and offline restart after removal.

## Production validation still required

- Compare the shared map with the legacy map for layer order, road/bridge
  overlays, multipolygons/holes, pin collision padding, selected-pin animation,
  location/track synchronization, labels, font scale, language, theme, and system
  animation settings. Fix unintended differences; validate StreetComplete behavior
  with checks proportional to the existing tests. Do not require pixel parity for
  deliberate changes such as painter halos or removal of enlarged hit areas.
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
  Opening `ShowMapScreen` reportedly freezes the UI for about a second. Profile
  cold and repeated entry, distinguish UI-thread blocking from renderer
  initialization and tile loading, and verify navigation stays responsive.
- Validate the selected Vulkan backend on supported Android devices, including
  an older device below API 33; the API 26 emulator cannot initialize Vulkan.
  Check map-entry responsiveness, rendering correctness, and background/resume. Resolve
  demonstrated compatibility problems before production cutover.

## Before marking ready for review

- Delete the temporary root `AGENTS.md` containing this branch's working preferences.
