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
- [x] Port recorded-track geometry, styling, animation, and bounded-segment
  rendering to shared MapLibre Compose layers.
- [x] Move track accumulation, accuracy filtering, timed segment breaks,
  recording start/stop handoff, renderer chunking, and saveable restoration to
  common state.
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
- [x] Wire the shared map into the real main screen on iOS.
- [x] Wire the shared map into the real main screen on desktop.
- [x] Remove the legacy Android assets and map stack after the guarded parity
  inventory demonstrates that no live functionality depends on them.
- [x] Restore the legacy 300 ms, system-animation-scale-aware global style
  transition through the common imperative style API.
- [x] Account for every current map data source, layer, selection flow, camera
  behavior, gesture, offline-area visualization, quest/edit pin interaction,
  track, overlay, and location behavior. Preserved behavior is implemented and
  tested; the exact MapLibre Compose API gaps above each have code TODOs and
  corresponding upstream notes rather than silent omissions.
- [x] Follow the post-v0.15 MapLibre Compose snapshot series on this validation
  branch. Core, location, target, resource, and runtime artifacts resolve through
  the standard Maven snapshot repository; the validated publication is recorded
  in `03-maplibre-compose-upstream.md`.

## Platform services

- [x] Replace the iOS upload, download, changeset auto-close, and network-state
  stubs with functional in-process implementations.
- [x] Register Apple background processing so pending edits upload and inactive
  changesets close after iOS suspends or relaunches the process. The task is
  network-constrained, cancellation-aware, rescheduled after every launch, and
  reports completion to `BGTaskScheduler`.
- [ ] Add background map-data downloads only if StreetComplete adopts an
  Apple-approved background-location mode. The current when-in-use authorization
  cannot provide a current vicinity after iOS suspends the foreground scene.
- [ ] Configure the real App Store identity once the iOS product record exists.
- [ ] Validate the promised iOS 15 minimum on an iOS 15 device/runtime after
  Compose/Skiko republishes its ICU data object with a compatible deployment
  target. The current Skiko Native cache member `libicu.icudtl_dat.o` declares
  iOS Simulator 18.5, so the successful iOS 26.5 build is not evidence for iOS 15.
- [x] Eliminate the duplicate SQLite implementation reported while linking the
  iOS app. Apple targets now use `NativeSQLiteDriver`; the bundled driver remains
  confined to JVM targets, and a clean Xcode application link succeeds with one
  exported SQLite symbol set.
- [x] Audit iOS behavior that compiled but was placeholder-level. Foreground
  startup uses the production initializer, email launching is functional,
  unhandled Kotlin exceptions persist for the shared crash dialog, and eligible
  sync work has a real Apple background-processing boundary. The scale bar now
  consumes Foundation's measurement system, the photo flow declares camera use,
  and the map chooser uses the invoking Compose scene, a weak host reference, and
  an iPad-safe presentation anchor.
- [ ] Enable AR measurement on iOS if StreetMeasure publishes a compatible
  launch/result protocol. The current feature is an external-app contract, and
  StreetMeasure has no iOS application or protocol to call; the hidden adapter
  carries an explicit `TODO(multiplatform)` rather than pretending to measure.
- [x] Move the iOS Core Location service-state query off the main thread through
  the resolved MapLibre Compose location provider.
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
- [x] Verify the final Android presentation surface has one Compose host and no
  fragments, XML layouts, navigation resources, menus, Android imports in
  `commonMain`, or orphaned View-era extension helpers.
- [x] Run the common test suite and target-specific tests on every target. iOS
  passes cleanly, desktop has one intentional skip, and the final Android run is
  clean. Earlier Android runs exposed intermittent live OSM development-server
  connection failures without weakening those integration tests.
- [x] Make the inherited common tests compile for Kotlin/Native by replacing the
  real-time `Thread` delay, using Native-safe test names, and keeping JVM utility
  entry points in the Android-host test source set.
- [x] Make the locale/date-sensitive common tests deterministic across JVM and
  Foundation formatting data, time zones, calendars, and non-ASCII decimal digits.
  The iOS simulator now passes all 2,498 Native tests; desktop and Android each
  pass 2,537 tests with one intentional skip.
- [x] Exercise representative production flows on Android, desktop, and iOS and
  record revision-pinned demo videos. Android covers a live map, quest pins,
  location, pan, long press, and an overlay update; desktop covers the packaged
  Metal application and incoming `geo:` camera position; iOS covers live style
  rendering, incoming `geo:` camera changes, and light/dark adaptation.
- [x] Verify shared glyph resources in a built and running iOS application bundle.
- [x] Obtain independent adversarial reviews of functionality parity, architecture,
  commit structure, and target evidence; fix all confirmed findings. Three fresh
  reviewers cleared the rewritten source, evidence, and layered history after the
  final findings were corrected.
