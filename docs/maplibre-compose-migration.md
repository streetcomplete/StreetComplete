# Remaining MapLibre Compose migration work

Finish the production MapLibre Compose migration on top of
[StreetComplete #6352](https://github.com/streetcomplete/StreetComplete/pull/6352),
preserving Android behavior and keeping the map implementation shared for future
iOS work. Full iOS UI/platform-service migration, desktop support, and unrelated
activity/navigation cleanup are outside this plan.

Production currently uses `MainMapFragment`; the shared `MainMap` is reached
through `ShowMapScreen`. [#7068](https://github.com/streetcomplete/StreetComplete/pull/7068)
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

## Phase 1: make the shared map complete and testable

Source paths in this phase are under
`app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/`
unless another source set or screen is named.

### Enable shader halos on Android

- Android map icons currently omit halos because MapLibre Compose rasterizes map
  images on a software canvas. Adopt MapLibre Compose hardware rasterization
  support when available and verify shader halos on Android API 33+. Resolve
  halo support below API 33, where `RuntimeShader` is unavailable. Verify icon
  halos and overlaps on Android and iOS.

### Validate the shared map through the debug screen

Before replacing the Android host, finish these checks through
`screens/settings/debug/ShowMapScreen.kt` using the production `MainMap`:

- Verify downloads, edits, replacements, deletions, and clearing update the
  visible quest/overlay/history data.
- Verify pin, marker, and overlay dimensions/anchors across densities and
  selected-pin animation. Verify map icons on an older Android device with Vulkan
  support; the API 26 emulator cannot initialize Vulkan (no physical devices).
- Verify feature-handler priority and disabled-overlay pass-through. Click clusters
  immediately before style replacement or leaving the map; obsolete queries must
  cancel without hiding unrelated failures.

## Phase 2: replace the production map

- Remove the Activity/Fragment/Compose communication bridges as the map moves
  into `MainScreen`. Pass presentation inputs and callbacks directly between the
  map, controls, and forms; remove mirrored state and forwarding effects rather
  than reproducing them in `MainMapViewModel`. In particular, move shown sheets,
  markers, history visibility, selection, and highlighted geometry to their
  composition owners. Use `rememberSaveable`/`rememberSerializable` where UI
  state needs restoration, and keep view models focused on persisted data and
  operations. Remove obsolete bridge properties in `MainViewModel` as callers
  move. Verify selection and form restoration after recreation.
- Reconsider `MapState` ownership explicitly before production wiring. This
  branch currently keeps it in `MainMapViewModel`; assess
  composition ownership against the library's lifetime and restoration APIs
  before changing that decision. Verify camera restoration and stable map
  lifetime under the chosen ownership.

- Transfer camera initialization/persistence, incoming `geo:` handling, pending
  moves before readiness, follow/navigation modes, and focus fitting/restoration
  from `MapFragment`, `MainMapFragment`, and their camera helpers to the shared map.
  Preserve sheet padding, stopping follow on pan, and restoring follow/navigation
  after temporary sidebar changes. Test these state transitions.
- Move track recording/segmentation, accuracy filtering, time gaps, bounded
  restoration, and note attachment out of `MainMapFragment` into shared behavior.
  Keep survey checking on the location event stream so display-state conflation
  cannot drop fixes. Preserve lifecycle-controlled location/heading collection
  and auto-sync's separate request. Test recording and restoration across gaps,
  permission changes, and activity recreation.
- Wire existing `MainActivity`/`MainScreen` controls and callbacks to the shared
  map: zoom/compass/location buttons, quest and solved-pin projection, crosshair
  and create actions, form markers, history, and download-area calculation.
  Preserve intended gesture behavior without carrying over the legacy finger-size
  calculation or enlarged feature-query area.
- Replace the production fragment map with `MainMap` through the existing Android
  host. Keep the map mounted across ordinary control, form, and sidebar changes;
  verify these changes do not recreate its presentation or reset its camera.

### Finish offline integration

- Replace the hosted `streetcomplete.app/map-jawg/streetcomplete.json` URL in
  `MapLibreMapTilesDownloader` with a bundled minimal style definition. Reference
  the same tile, glyph, and image resources used by the shared map, and verify
  that downloaded areas render offline without the hosted style repository.
  Keep old-pack cleanup in `Cleaner`; do not port the fragment's duplicate cleanup.

- **Confirmed: the shared downloader is not used.** Bind
  `MapLibreMapTilesDownloader` to the rendering runtime's `offlineManager` and
  the platform pixel ratio. Replace Android's `MapTilesDownloaderAndroid` binding
  and iOS's no-op `IosMapTilesDownloader` binding. Verify the existing download
  flow and `Cleaner.cleanOld`/`cleanAll` reach the shared implementation, including
  Android worker startup without an activity or visible map.
- **Confirmed: clearing can swallow cancellation.** Propagate cancellation from
  `MapLibreMapTilesDownloader.clear()`. Make download cancellation pause the pack,
  and preserve the original failure if pausing also fails. Test cancellation and
  partial failures during download, deletion, and cache clearing.
- **Needs implementation/validation: offline persistence across the switch.**
  Handle the existing Android offline packs/cache when moving to the Compose
  runtime. Verify downloaded resource URLs match the shared style, and that
  downloaded areas still render offline after upgrade and process restart.
  Verify expiry metadata, old-pack deletion, ambient-cache clearing, and cleanup
  of interrupted downloads against the actual runtime cache.

## Phase 3: retire the legacy map

After the production validation below passes:

- Remove the legacy map fragments, components, managers, camera/style/image
  helpers, and Android map downloader after their callers have moved to the
  shared implementation.
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
  map-entry responsiveness, rendering correctness, and background/resume. Resolve
  demonstrated compatibility problems before production cutover.

## Before marking ready for review

- Delete the temporary root `AGENTS.md` containing this branch's working preferences.
