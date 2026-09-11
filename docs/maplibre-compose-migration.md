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

## Phase 2: replace the production map

- Resolve view models and collect their flows in `MainScreen`. Wire map data and
  direct callbacks into the production screen, controls, and forms as the
  Activity/Fragment/Compose forwarding bridges are removed. Keep the existing
  controllers/sources and map source helpers: visible
  quests flow through `MapQuestPinsSource` and the view model to the screen, then
  `PinsLayers` converts pins to GeoJSON in the background. Forward viewport
  changes from the screen to the quest and overlay sources through the view model.
- Move production sheets, markers, history visibility, selection, and highlighted
  geometry to their composition owners in `MainScreen`. Save the minimal presentation state
  needed for restoration with `rememberSaveable`/`rememberSerializable`;
  reconstruct database-derived data from the existing sources. Keep view models
  exposing derived flows and operations. Remove obsolete bridge properties in
  `MainViewModel` as callers move. Verify selection and form restoration after
  activity recreation and process death.
- Use screen-owned `MapState` when wiring `MainScreen`, with the shared runtime
  in DI. Verify camera restoration after process death and a stable map lifetime
  across recomposition and ordinary production screen controls.

- Wire camera preferences and incoming `geo:` handling into the production screen,
  including moves before the map is attached. Connect the shared camera state to
  location/navigation controls, sheet padding and focus, pan gestures, and temporary
  sidebar changes. Verify follow/navigation restoration and camera preferences
  across these transitions.
- Feed the shared track state from lifecycle-controlled location/heading collection
  in the production screen. Keep survey checking on the raw location event stream,
  before display or accuracy filtering, and retain auto-sync's separate request.
  Restore the displayed location and pass recorded tracks into the saved note
  sheet payload. Verify recording and note attachment across permission changes
  and activity recreation.
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
  an older device below API 33; the API 26 emulator cannot initialize Vulkan.
  Check map-entry responsiveness, rendering correctness, and background/resume. Resolve
  demonstrated compatibility problems before production cutover.

## Before marking ready for review

- Delete the temporary root `AGENTS.md` containing this branch's working preferences.
