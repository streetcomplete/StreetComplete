# What still needs work

## Target and entry-point parity

- [x] Add a JVM desktop library target.
- [x] Add a Compose Desktop application that enters the real shared app flow.
- [x] Replace the temporary iOS launcher with the real shared app entry point.
- [x] Replace Android activity-to-activity navigation with the same shared
  navigation graph used by desktop and iOS.

## Map parity

- [x] Move the Android-only `Fragment` and `org.maplibre.android` map stack to a
  shared MapLibre Compose implementation and retire the unused legacy source,
  dependency, updater, and assets after a guarded parity inventory.
- [x] Run Android's real main screen on the shared MapLibre Compose renderer.
- [x] Port the complete light/night base map style and offline glyph resources to
  shared MapLibre Compose declarations.
- [x] Port shared quest/edit pin clustering, rendering, resource registration,
  ordering, visibility, clicks, and cluster-leaf lookup to MapLibre Compose.
- [x] Move the quest and edit-history pin data managers to common code and wire
  typed pin and cluster-leaf callbacks into the shared renderer.
- [x] Wire antimeridian-safe cluster camera fitting, breathing room, zoom cap,
  and distance-scaled animation into the shared map controller.
- [x] Move persisted camera position, zoom controls, compass reset, GPS following,
  navigation tilt/bearing, and user-pan detection into shared map state.
- [ ] Replace target-delta pan detection with an explicit pan-begin callback when
  MapLibre Compose exposes gesture-specific camera events.
- [ ] Restore the legacy pan, rotate, tilt, fling, and rotate-while-scaling gesture
  configuration when MapLibre Compose exposes those controls in common code.
- [x] Move map projection, long press, raw map click, interactive-feature
  suppression, and finger-radius measurement into the shared renderer/state.
- [ ] Replace the shared renderer's explicit interactive-layer pre-query when
  MapLibre Compose exposes an unhandled/post-layer map click callback.
- [x] Port styleable overlay geometry, colors, strokes, bridge ordering,
  extrusions, icons, labels, disabled state, visibility, and element clicks.
- [x] Move the styleable overlay data manager to common code and connect it to
  the shared camera/viewport flow.
- [ ] Restore the legacy finger-radius overlay hit area when MapLibre Compose
  layer click handlers expose configurable rendered-feature query geometry.
- [x] Port the downloaded-area mask and hatching to a shared MapLibre Compose
  layer.
- [ ] Restore the downloaded-area GeoJSON source's volatile cache hint when
  MapLibre Compose exposes it as a common source option.
- [x] Port recorded-track geometry, styling, animation, and bounded-segment
  rendering to shared MapLibre Compose layers.
- [x] Move track accumulation, accuracy filtering, timed segment breaks,
  recording start/stop handoff, renderer chunking, and saveable restoration to
  common state.
- [ ] Restore the three recorded-track GeoJSON sources' volatile cache hints
  when MapLibre Compose exposes them as common source options.
- [x] Port focused point, line, polygon, and multipolygon rendering and its
  breathing highlight animation to shared MapLibre Compose.
- [x] Port focused-geometry camera framing, form-aware padding, zoom margin/cap,
  and return-to-previous-camera behavior into shared map state.
- [x] Port quest-form geometry marker icons, labels, points, lines, and polygons
  to shared MapLibre Compose layers.
- [x] Port selected-pin icon composition, placement, and overshoot animation to
  a shared MapLibre Compose layer.
- [x] Port current-location accuracy, bearing, shadow, dot, April 1 artwork, and
  motion animation to shared MapLibre Compose layers.
- [ ] Restore the current-location GeoJSON source's volatile cache hint when
  MapLibre Compose exposes it as a common source option.
- [ ] Restore the clustered-pin GeoJSON source's volatile cache hint when
  MapLibre Compose exposes it as a common source option.
- [ ] Restore the styleable-overlay GeoJSON source's volatile cache hint when
  MapLibre Compose exposes it as a common source option.
- [x] Wire the shared map into the real main screen on iOS.
- [x] Wire the shared map into the real main screen on desktop.
- [x] Remove the legacy Android assets and map stack after the guarded parity
  inventory demonstrates that no live functionality depends on them.
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
- [ ] Eliminate the duplicate SQLite symbols reported while linking the iOS app.
  The current framework includes both `sqlite3.c.o` and `sqlite3.o`; the app runs,
  but carrying two bundled SQLite copies is not an acceptable final state.
- [ ] Audit iOS behavior that compiles but is still placeholder-level, including
  email launching, background lifecycle, and crash handling. Foreground
  application startup now uses the shared production initializer.
- [ ] Remove the Core Location main-thread performance diagnostic emitted while
  the shared lifecycle attaches location-aware synchronization on iOS. The app
  stays live and renders, but authorization state should be consumed from
  `locationManagerDidChangeAuthorization` rather than queried synchronously.
- [x] Add real desktop implementations for storage paths, settings, database,
  HTTP, location, external-app launching, sound, connectivity, background work,
  and platform formatting.
- [ ] Replace the desktop connection monitor's ordinary Ethernet/Wi-Fi
  unmetered assumption with host-specific metered-network status where the OS
  exposes it.
- [ ] Replace the desktop camera-launcher fallback if FileKit adds camera capture;
  desktop currently reports that no camera capture integration is available and
  therefore never presents the mobile-only camera action.
- [ ] Replace opening an exported log file with a native desktop share sheet when
  Compose Desktop exposes a portable share API.
- [ ] Make desktop distribution fail with a targeted explanation on hosts for
  which MapLibre Compose publishes no runtime. The current published matrix
  covers macOS ARM64 plus Linux and Windows x64/ARM64, but not macOS x64.

## Completeness safeguards

- [x] Inventory Android-only production classes before deleting them and map each
  one to a shared or platform implementation.
- [ ] Run the common test suite and target-specific tests on every target.
- [ ] Make the inherited common tests compile for Kotlin/Native. The current suite
  contains a JVM `Thread` call and test names that Kotlin/Native rejects.
- [ ] Make six existing locale/date-sensitive common tests deterministic. Both the
  Android host and new desktop runner currently execute 2,449 tests with the same
  six failures on this machine.
- [ ] Exercise production flows on Android, desktop, and iOS and record demo videos.
- [x] Verify shared glyph resources in a built and running iOS application bundle.
- [ ] Obtain independent adversarial reviews of functionality parity, architecture,
  commit structure, and target evidence; fix all confirmed findings.
