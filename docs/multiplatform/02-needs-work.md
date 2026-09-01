# What still needs work

## Target and entry-point parity

- [x] Add a JVM desktop library target.
- [ ] Add a Compose Desktop application that enters the real shared app flow.
- [ ] Replace the temporary iOS launcher, which exposes only Changelog, Credits,
  and Privacy Statement, with the real shared app entry point.
- [ ] Replace Android activity-to-activity navigation with the same shared
  navigation graph used by desktop and iOS.

## Map parity

- [ ] Move the Android-only `Fragment` and `org.maplibre.android` map stack to a
  shared MapLibre Compose implementation.
- [x] Port the complete light/night base map style and offline glyph resources to
  shared MapLibre Compose declarations.
- [ ] Port StreetComplete's quest pin, edit-history pin, overlay,
  focused-geometry, marker, selection, and current-location layers.
- [x] Port the downloaded-area mask and hatching to a shared MapLibre Compose
  layer.
- [ ] Restore the downloaded-area GeoJSON source's volatile cache hint when
  MapLibre Compose exposes it as a common source option.
- [x] Port recorded-track geometry, styling, animation, and bounded-segment
  rendering to shared MapLibre Compose layers.
- [ ] Restore the three recorded-track GeoJSON sources' volatile cache hints
  when MapLibre Compose exposes them as common source options.
- [ ] Wire the shared map into the real main screen on each target, then remove
  the legacy Android assets and map stack only after parity is demonstrated.
- [ ] Restore the legacy 300ms, system-animation-scale-aware global style
  transition when MapLibre Compose exposes common transition configuration.
- [ ] Preserve every current map data source, layer, selection flow, camera
  behavior, gesture, offline-area visualization, quest pin interaction, edit
  history pin, track, overlay, and location behavior.
- [x] Use the current post-v0.15 snapshot. The snapshot
  repository currently publishes `org.maplibre.compose:maplibre-compose:0.15.1-SNAPSHOT`.

## Platform services

- [x] Replace the iOS upload, download, changeset auto-close, and network-state
  stubs with functional in-process implementations.
- [ ] Register Apple background-processing tasks so automatic sync survives iOS
  process suspension and relaunch; the in-process controllers cannot guarantee
  execution after the OS suspends the app.
- [ ] Configure the real App Store identity once the iOS product record exists.
- [ ] Audit iOS behavior that compiles but is still placeholder-level, including
  email launching, background lifecycle, crash handling, and application startup.
- [ ] Add real desktop implementations for storage paths, settings, database,
  HTTP, location, external-app launching, sound, connectivity, background work,
  and platform formatting.
- [ ] Replace the desktop camera-launcher fallback if FileKit adds camera capture;
  desktop currently reports that no camera capture integration is available and
  therefore never presents the mobile-only camera action.
- [ ] Replace opening an exported log file with a native desktop share sheet when
  Compose Desktop exposes a portable share API.
- [ ] Make desktop distribution fail with a targeted explanation on hosts for
  which MapLibre Compose publishes no runtime. The current published matrix
  covers macOS ARM64 plus Linux and Windows x64/ARM64, but not macOS x64.

## Completeness safeguards

- [ ] Inventory Android-only production classes before deleting them and map each
  one to a shared or platform implementation.
- [ ] Run the common test suite and target-specific tests on every target.
- [ ] Make the inherited common tests compile for Kotlin/Native. The current suite
  contains a JVM `Thread` call and test names that Kotlin/Native rejects.
- [ ] Make six existing locale/date-sensitive common tests deterministic. Both the
  Android host and new desktop runner currently execute 2,449 tests with the same
  six failures on this machine.
- [ ] Exercise production flows on Android, desktop, and iOS and record demo videos.
- [ ] Verify shared glyph resources in a built iOS application bundle; linking the
  framework alone does not copy Compose resources into an app.
- [ ] Obtain independent adversarial reviews of functionality parity, architecture,
  commit structure, and target evidence; fix all confirmed findings.
