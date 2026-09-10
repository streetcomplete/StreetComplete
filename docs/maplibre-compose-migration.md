# Remaining MapLibre Compose migration work

Finish the production MapLibre Compose migration on top of
[StreetComplete #6352](https://github.com/streetcomplete/StreetComplete/pull/6352),
preserving Android behavior and keeping the map implementation shared for future
iOS work. Full iOS UI/platform-service migration, desktop support, and unrelated
activity/navigation cleanup are outside this plan.

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

## Working boundary

The initial audit is against `ccb6dd7d7` on `maplibre-compose`. Production still
uses `MainMapFragment`; the shared `MainMap` is reached through `ShowMapScreen`.
Layer implementations and data sources exist, but dynamic images, changing data,
interaction parity, production wiring, and offline integration remain incomplete.

Use [#7068](https://github.com/streetcomplete/StreetComplete/pull/7068) as a source
of candidate implementations, test cases, and the legacy deletion inventory.
Review each piece against production behavior and current upstream APIs before
adopting it. Its broader multiplatform architecture is outside this branch's scope.

Keep the stack based on upstream `maplibre-compose` while #6352 is open. Integrate
parent changes and remove requirements they satisfy. Keep each implementation
commit focused on one behavior, with its tests and corresponding plan deletions.

## Phase 1: make the shared map complete and testable

### Establish the current dependency and ownership model

- Keep StreetComplete's follow, focus, selection, and tracking policy in small
  shared components. Give view models stable observable data.
- Establish runtime ownership for rendering and offline access, including
  background cleanup. Select and validate the Android renderer: the branch uses
  the Vulkan runtime while the production legacy map uses OpenGL.

The old PR blocker list needs updating against current APIs. Dynamic image issue
[#468](https://github.com/maplibre/maplibre-compose/issues/468) and geolocation
issue [#6310](https://github.com/streetcomplete/StreetComplete/issues/6310) are
closed; offline access is available. Do not reintroduce historical probe
workarounds without evidence that the selected release needs them.

### Correct data publication and lifecycle

Source paths below are under
`app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/`.

- **Confirmed: background conversions do not follow input changes.** Seven
  `produceState` calls lack input keys: `MainMap.kt`, `layers/PinsLayers.kt`,
  `layers/GeometryMarkersLayers.kt`, `layers/SelectedPinsLayer.kt`,
  `layers/DownloadedAreaLayer.kt`, and both persistent geometries in
  `layers/TracksLayer.kt`. Keep expensive work off the UI dispatcher while
  restarting it for new inputs and cancelling superseded work. Verify initially
  empty data becomes visible, subsequent updates replace it, and clearing removes it.
- **Confirmed: overlay data events are never subscribed to.**
  `sources/StyleableOverlaySource.kt` defines `mapDataWithEditsListener` without
  registering it. Attach and detach it with the source's active lifetime. Verify
  downloads, edits, replacements, deletions, and clearing update the current view.
- **Confirmed: edit-history flow identity and snapshots are unstable.**
  `MainMapViewModel.kt` creates `stateIn` inside the `editHistoryPins` getter.
  `sources/EditHistoryPinsSource.kt` publishes a mutable map-values view and
  does not emit after invalidation reloads. Own one stable flow, publish immutable
  snapshots, and verify additions, deletions, invalidation, ordering, and keys.
- **Needs validation: source lifetime and concurrent updates.** Quest and overlay
  sources own scopes/listeners independently of visible layers. Define
  activation/visibility ownership and validate viewport readiness and size changes.
  Verify superseded loads and deltas cannot publish stale data after a newer viewport, clear, or disposal;
  verify reactivation reloads missed changes without duplicate listeners.

### Finish rendering and interaction parity

- **Confirmed: dynamic images are not registered.** Pins reference
  `pin_<resource-id>`; markers and overlays reference resource IDs, but the shared
  style never receives those images. Connect the painter/resource path to style
  image registration. Preserve image-before-source publication, replay after
  style replacement, density, pin geometry, and overlay tint/halo behavior.
  Eager registration matches the legacy map's policy; validate its cost and
  correctness rather than treating it as an upstream workaround.
- **Confirmed: cluster behavior differs.** `PinsLayers.kt` asks only for expansion
  zoom, and `MainMap.kt` keeps the current camera center. Restore leaf fitting,
  recentering, the quarter-zoom margin, maximum zoom 19, and distance-dependent
  animation from `androidMain/.../components/PinsMapComponent.kt`. Restore the
  cluster/dot/pin filtering behavior around zoom 13–15. Test off-center clusters,
  overlapping pins, and clusters spanning the antimeridian.
- **Confirmed: visibility and selection inputs are incomplete.** Preserve
  `Overlay.hidesLayers`, including address-overlay suppression of base house
  numbers. Represent selected edits and overlay elements, create-mode pin
  suppression, and form-driven markers as well as quest selection. Compare with
  `MainActivity` and `MainMapFragment`; `shownBottomSheet` alone does not represent
  all these states.
- **Confirmed: click routing is incomplete.** Add the background-map click path
  after feature handlers decline an event. Preserve overlay finger hit radius,
  disabled-feature pass-through, typed quest/edit/element callbacks, and long
  presses. Keep asynchronous cluster queries safe across style replacement and
  disposal without swallowing unrelated failures or coroutine cancellation.
- **Needs validation: visual and animation parity.** Compare layer order, road
  and bridge overlays, multipolygons/holes, pin collision padding, selected-pin
  animation, location/track synchronization, labels, font scale, language, theme,
  and system animation settings with the legacy map. Verify painter dimensions
  and glyph loading on Android and iOS. Distinguish intended differences from
  defects before changing production behavior.
- **Confirmed: the debug screen cannot exercise most of this.** `ShowMapScreen`
  supplies no location, markers, or selection; empty tracks; and no-op clicks.
  Add representative changing inputs and actionable callbacks using the same map
  code intended for production. Include real loaded data and transitions between
  quests, history, overlays, and focused geometry.

Phase 1 gate: all layer/data requirements above pass focused regression checks
and Android device inspection through the debug map, including updates after the
first frame and leaving/re-entering the screen. Compilation alone is insufficient.

## Phase 2: replace the production map

- Transfer camera initialization/persistence, incoming `geo:` handling, pending
  moves before readiness, follow/navigation modes, and focus fitting/restoration
  from `MapFragment`, `MainMapFragment`, and their camera helpers. Preserve sheet
  padding and restoration of follow/navigation after temporary sidebar changes.
- Move track recording/segmentation, accuracy filtering, time gaps, bounded
  restoration, and note attachment into shared map behavior. Preserve every
  location fix needed by survey checking; avoid losing events through conflated
  display state. Stop foreground location/heading collection with the appropriate
  presentation lifecycle while preserving separately scoped background needs.
- Route existing `MainActivity`/`MainScreen` controls and callbacks through the
  shared map: gestures, zoom/compass/location controls, projection for quest and
  solved-pin UI, crosshair/create actions, forms, history, and download-area
  calculation. Preserve gesture thresholds, momentum, and the distinction between
  panning (which stops following) and other camera input.
- Mount the shared map in production through the existing Android host. Keep one
  stable presentation across ordinary screen/state changes. Retain the native map
  as a temporary selectable fallback until production acceptance passes. Do not
  couple this cutover to a full activity/navigation or iOS application rewrite.

### Finish offline integration

- **Confirmed: the shared downloader is not used.** Android DI still selects
  `MapTilesDownloaderAndroid`; iOS selects its existing downloader. Wire
  `data/maptiles/MaplibreMapTilesDownloader.kt` to the rendering runtime's offline
  manager.
- **Confirmed: clearing can swallow cancellation.** The shared downloader catches
  all `Exception`s in `clear()`. Propagate cancellation and define partial-failure
  behavior. Verify cancelling a download reliably pauses work and preserves the
  original failure if cleanup also fails.
- **Needs implementation/validation: persistence and migration.** Establish how
  existing Android offline packs/cache remain usable after the renderer change.
  Verify downloaded resource URLs match rendering, expiry metadata and old-pack
  deletion, ambient-cache clearing, interrupted-download cleanup, restart, and
  background cleanup without requiring a visible map.

Phase 2 gate: exercise the production UI on a physical Android device with dense
quests/clusters, edit history, all overlay/form modes, GPS/navigation/recording,
airplane-mode use after download and restart, download cancellation/deletion,
background/resume, activity/process recreation, and theme/language/font changes.
Check release packaging and supported Android ABIs. Keep iOS shared-code
compilation and a map-rendering smoke test as protection for the shared boundary;
full iOS application parity is outside this gate.

## Phase 3: retire the legacy map

Only after the production acceptance gate passes:

- Remove legacy map fragments, components, managers, camera/style/image helpers,
  the temporary fallback, and the obsolete Android map downloader.
- Remove the direct Android SDK dependency, duplicated legacy map assets/glyphs,
  and obsolete generation tasks. Audit remaining call sites before deleting
  resource-copy helpers also used elsewhere. Use the probe's
  `docs/multiplatform/05-android-map-retirement.md` as a candidate inventory.
- Verify no production, DI, layout, or build references remain to the retired
  map. Recheck release assembly, packaged shared glyphs/images and native libraries,
  first map frame, and offline restart after deletion. Update lasting map-style
  maintenance instructions so future updates reach the shared style.

## Validation and upstream follow-up still required

- Establish targeted regression coverage for the remaining requirements above;
  do not equate the existing suite with map parity or iOS compilation with
  framework/app rendering.
- Existing host-test CI runs on `master` pushes; APK building is manual. Ensure
  each reviewed migration revision has reproducible checks and explicit device
  evidence. Use JDK 21/UTC to avoid the date-format and timezone failures observed
  under the local JDK 25/default timezone during the audit.
- **Upstream API gap to reassess during integration:** the probe's
  `StyleLifecycle.kt` classifies stale/not-ready style operations by exception
  messages. 0.16.0 still uses generic `IllegalStateException` checks for these
  lifecycle failures. Prefer typed classification when available; isolate any
  necessary compatibility handling and preserve cancellation/unrelated exceptions.
  This is a source-observed API limitation, not a reproduced 0.16.0 runtime failure
  or an established hard blocker.
