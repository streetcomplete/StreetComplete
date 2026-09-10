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

### Correct data publication and lifecycle

- **Confirmed: background conversions do not follow input changes.** Key the
  seven `produceState` calls in `MainMapContent.kt`, `layers/PinsLayers.kt`,
  `layers/GeometryMarkersLayers.kt`, `layers/SelectedPinsLayer.kt`,
  `layers/DownloadedAreaLayer.kt`, and both persistent geometries in
  `layers/TracksLayer.kt` to their inputs. Cancel superseded conversions and keep
  expensive work off the UI dispatcher. Test empty-to-populated, replacement,
  and clearing updates after the first composition.
- **Confirmed: overlay data events are never subscribed to.** Register
  `sources/StyleableOverlaySource.kt`'s `mapDataWithEditsListener` while the source
  is active and unregister it on disposal. Test downloads, edits, replacements,
  deletions, and clearing against the visible overlay.
- **Confirmed: edit-history flow identity and snapshots are unstable.** Move
  `MainMapViewModel.kt`'s `editHistoryPins.stateIn` out of the getter so the view
  model owns one flow. In `sources/EditHistoryPinsSource.kt`, publish immutable
  snapshots and emit the reloaded data after invalidation. Test additions,
  deletions, invalidation, ordering, and keys.
- **Needs implementation/validation: source activation and update ordering.**
  Quest and overlay sources remain subscribed independently of layer visibility.
  Stop unnecessary loading while their layers are hidden and reload on
  reactivation. Verify viewport readiness and resizing trigger the required
  loads, leaving the map releases listeners/jobs, and older loads cannot replace
  newer viewport data or repopulate cleared data. Fix any races exposed by these
  tests, including updates arriving during initial subscription.

### Finish rendering and interaction parity

- **Confirmed: dynamic images are not registered.** Register the pin, marker,
  and overlay images referenced by feature properties, using the existing
  painter/resource helpers and MapLibre Compose's image APIs. Make the images
  available before their features render and after style replacement. Preserve
  density, pin dimensions/anchors, and overlay tint/halo behavior.
- **Confirmed: cluster behavior differs.** Replace the expansion-zoom-only path
  in `layers/PinsLayers.kt` and `MainMapViewModel.kt` with leaf fitting and recentering.
  Preserve the quarter-zoom margin, maximum zoom 19, and zoom-difference-based
  animation duration from `androidMain/.../components/PinsMapComponent.kt`.
  Restore its cluster/dot/pin filters around zoom 13–15. Test off-center clusters,
  overlapping pins, and clusters spanning the antimeridian. Test clicks followed
  by style replacement or map disposal; cancel obsolete queries without
  swallowing unrelated failures.
- **Confirmed: visibility and selection inputs are incomplete.** Apply
  `Overlay.hidesLayers`, including address-overlay suppression of house numbers.
  Add selected-edit and selected-overlay-element highlighting, create-mode pin
  suppression, and form-driven markers to the shared map inputs. Match the
  behavior currently wired by `MainActivity` and `MainMapFragment`.
- **Confirmed: click routing is incomplete.** Add a background-map click callback
  after feature handlers decline the event. Make disabled or unrecognized overlay
  features pass the event through instead of consuming it. Preserve the legacy
  overlay hit radius, quest/edit/element callbacks, and long presses. Test handler
  priority and pass-through behavior.
- **Confirmed: the debug screen cannot exercise these states.** Extend
  `screens/settings/debug/ShowMapScreen.kt`, which currently leaves location,
  markers, selection, and tracks empty and uses no-op click callbacks. Add controls
  for changing these inputs, switching quests/history/overlays, and exercising
  focus and selection with real loaded data. Use the production `MainMap`.

Before replacing the Android host, exercise these updates and interactions through
`ShowMapScreen`, including leaving and re-entering it.

## Phase 2: replace the production map

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
  Preserve the existing gesture thresholds and momentum behavior.
- Replace the production fragment map with `MainMap` through the existing Android
  host. Keep the map mounted across ordinary control, form, and sidebar changes;
  verify these changes do not recreate its presentation or reset its camera.

### Finish offline integration

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
- Verify no production, DI, layout, or build references remain to the retired
  code. Recheck Android release assembly, packaged glyphs/images/native libraries,
  first map frame, and offline restart after removal.

## Production validation still required

- Compare the shared map with the legacy map for layer order, road/bridge
  overlays, multipolygons/holes, pin collision padding, selected-pin animation,
  location/track synchronization, labels, font scale, language, theme, and system
  animation settings. Fix unintended differences and cover the affected behavior
  with regression tests.
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
