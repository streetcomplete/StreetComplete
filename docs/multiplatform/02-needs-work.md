# What still needs work

Checked implementation items below are not full parity claims. Historical test
counts and demonstrations describe their recorded revisions in `04-validation.md`.

## Real-device findings from 2026-09-04

The [iPhone walk report](https://github.com/streetcomplete/StreetComplete/pull/7068#issuecomment-5547402966)
confirms usable mapping, completed quests, notes with photos, and location tracking
on this probe. Follow-up status for its other observations:

- [ ] Intermittent jank and freezes, including navigation to Settings.
- [ ] Broken map-to-menu transition backgrounds.
- [x] Correct the inverted location-indicator bearing. The shared screen now uses
  the clockwise angle from north to the heading, then subtracts camera bearing.
  Regression tests cover direction and camera rotation; an iPhone recheck remains.
- [x] Preserve automatic time formatting in opening-hours controls. Automatic app
  language passes `null` through to the native formatter; an explicit language
  remains explicit. The quest's country locale still controls weekday and month
  labels. Native formatter tests pass with en-US and the 24-hour override enabled.
  Refreshing an already-open picker after a system preference change remains unimplemented.
- [ ] Quest forms lack an obvious close control on iOS. Closing must preserve the
  form's existing discard-confirmation flow.
- [ ] One download failed and appeared to succeed on retry; cause unknown.
- [x] Remove the custom iOS crash-report dialog path, as requested in the PR thread.
  This removal does not diagnose whether the reported repeated dialog represented
  repeated crashes or repeated presentation of an old report.

The old synthetic performance scenario was removed on 2026-09-05. It does not
cover this remaining jank. These probe observations do not verify fixes on master.

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
- [ ] Establish runtime parity for map behavior. Shared implementations and focused
  tests exist, but the device findings above and the historical source audit
  prevent a completeness claim.
- [x] Follow the post-v0.15 MapLibre Compose snapshot series on this validation
  branch. Core, location, target, resource, and runtime artifacts resolve through
  the standard Maven snapshot repository; the validated publication is recorded
  in `03-maplibre-compose-upstream.md`.

## Platform services

- [x] Replace the iOS upload, download, changeset auto-close, and network-state
  stubs with functional in-process implementations.
- [x] Register an Apple processing task for pending uploads and changeset closure.
- [ ] Validate iOS background-sync policy and actual task execution. Registration
  and linkage alone do not prove suspended-time uploads or changeset closure.
- [ ] Implement OS-scheduled iOS cleanup through `IosPeriodicCleaner`. The current
  daily coroutine in `ApplicationInitializer` runs only while its process can execute.
- [ ] Add background map-data downloads only if StreetComplete adopts an
  Apple-approved background-location mode. The current when-in-use authorization
  cannot provide a current vicinity after iOS suspends the foreground scene.
- [x] Use the App Store identity from master, `6808344816`.
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
  iOS leaves crash reporting to the platform, and eligible sync work has an Apple
  background-processing adapter whose execution remains unvalidated. The scale bar now
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

Desktop installer releases are out of scope for this probe. The macOS development
launcher still needs a local app image and its resources for Core Location.

- [x] Resolve the macOS local-launch location requirement. `mise run run:desktop`
  uses `:app:runDistributable` with location purpose strings and entitlements,
  following the MapLibre Compose demo. The 2026-09-05 check rendered the map and
  received location updates that triggered a successful nearby quest download.

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
- [ ] Resolve or explicitly defer the remaining source-audit and device findings.
  Earlier review and test results do not establish complete migration parity.
