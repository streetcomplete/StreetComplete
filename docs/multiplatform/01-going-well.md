# What is going well

## Baseline on 2026-09-01

- The `app` module is already a Kotlin Multiplatform library with Android,
  `iosArm64`, and `iosSimulatorArm64` targets.
- 1,237 Kotlin source files are already in `app/src/commonMain`; only 68 are in
  `androidMain` and 30 are in `iosMain`.
- The shared source set already contains the quest model, persistence, networking,
  most platform-independent services, and 151 Compose screen files.
- Compose resources, Koin, Ktor, kotlinx.serialization, kotlinx-datetime,
  multiplatform settings, and bundled SQLite are already shared dependencies.
- The clean baseline compiles common metadata, Android, and the iOS simulator
  framework. The Android debug application assembles successfully.
- A previous `upstream/maplibre-compose` branch contains useful exploratory work
  for shared map layers and sources. It is being used as design input, not treated
  as verified production code.

## JVM foundation

- The shared library now has a JVM desktop target.
- Android and desktop share their Java implementations of date, time, number,
  currency, locale, and access-ordered map behavior through `javaMain`.
- Shared photo capture and file sharing no longer expose APIs that exist only in
  FileKit's Android and iOS variants. Narrow platform launchers preserve mobile
  behavior while allowing the common UI to compile for desktop.
- Desktop compilation, Android compilation and APK assembly, and iOS simulator
  compilation all pass together after adding the target.

## iOS sync foundation

- iOS now registers the real `commonModule` dependency graph rather than a
  three-screen demonstration graph.
- Upload, download, and changeset auto-closing use tested shared coroutine
  controllers while the iOS process is alive. User downloads replace automatic
  downloads; repeated automatic work stays unique.
- Apple Network framework path monitoring now feeds the shared connectivity API,
  including satisfied-path and expensive-path state.

## MapLibre Compose snapshot foundation

- All targets now compile against the post-v0.15 `0.15.1-SNAPSHOT`; the latest
  dependency refresh selected build `0.15.1-20260901.101938-7`.
- Android packages the OpenGL runtime, iOS links the transitive Metal runtime,
  and the current macOS ARM64 host selects the desktop Metal runtime.
- StreetComplete now consumes the snapshot's `LocationMeasurement`,
  `LocationEvent.Update`, and `HeadingMeasurement` APIs. Recorded tracks use the
  measurement's real wall-clock instant instead of accidentally serializing a
  monotonic location age as an epoch timestamp.

## Shared base map

- StreetComplete's full Jawg vector-tile base style now lives in `commonMain` as
  declarative MapLibre Compose layers, including light and night palettes,
  localized labels, roads, bridges, tunnels, buildings, water, land use,
  boundaries, railways, hillshade, and one-way arrows.
- All 512 offline Roboto glyph ranges are available as Compose resources for
  Android, iOS, and desktop. Verification checks the exact non-empty source set,
  URI templates, and the shared glyph entries packaged in the Android APK.
- The shared map shell exposes the four intentional layer-ordering seams needed
  by StreetComplete data: below roads, below bridge roads, below labels, and
  above labels. This keeps quest and overlay state out of the base-style module.
- Android's production `MainActivity` now presents the shared `MainScreen` and
  `MainMap` directly. The fragment is no longer in the live view hierarchy; the
  old sources and assets remain temporarily so parity can be audited before
  deleting them in a separate commit.
- The downloaded-area visualization is now a shared MapLibre Compose layer. It
  preserves the existing 60%-opaque hatch outside downloaded zoom-16 tiles and
  uses the same Compose vector resource on Android, iOS, and desktop.
- Recorded tracks now have shared current, animated, and completed-segment
  layers. The 600ms motion crosses the antimeridian by the shortest path, and
  the recording color, opacity, dash, line width, and April 1 pattern are kept.
- Focused element geometry now converts to shared GeoJSON and renders as the
  same orange fill, round line, or circle with the legacy breathing size and
  opacity ranges. Multipolygon holes are assigned to the containing outer ring.
- Quest-form geometry markers now render from the existing shared `Marker`
  model, including center-anchored optional icons and titles plus line/polygon
  geometry. Dynamic Compose resources are registered as SDF or color images.
- Selected quest pins now use a shared painter that reproduces the legacy
  71dp shadow, pin, and 48dp quest-icon composition. Their 300ms scale animation
  uses the same overshoot curve and restarts whenever the selection changes.
- Current location now has shared accuracy, bearing, shadow, dot, and April 1
  layers using the existing cross-platform artwork. Position and accuracy retain
  the legacy 600ms timing, bearing retains its 200ms shortest-turn timing, and
  position movement now also takes the shortest path across the antimeridian.
- Quest and edit-history pins now share one clustered MapLibre Compose layer.
  It preserves the legacy zoom thresholds, cluster sizing and labels, full pin
  painter, collision box, ordering, visibility, pin clicks, and complete cluster
  leaf lookup through generation-bound source handles.
- Quest-pin loading now also has a renderer-independent common source. It keeps
  the zoom-16 viewport cache, 32-tile guard, multi-marker edge retention,
  superseded-fetch cancellation, live quest deltas, user quest ordering, and
  typed click-key round trips without retaining an Android map or lifecycle.
- Edit-history pin loading is now common as well. It observes the shared edit
  history, reloads and reindexes the complete list after structural changes,
  uses Compose drawable resources directly, and safely decodes all four edit
  key variants from map feature properties.
- Styleable-overlay loading no longer retains an Android map. The common source
  reacts to overlay selection and map-data deltas, reuses the same zoom-16 and
  32-tile viewport guard, cancels superseded fetches, and converts live shared
  map data into the already-migrated `StyledElement` model.
- Downloaded-tile observation is now common and applies the same 14-day retained
  data cutoff before feeding the shared downloaded-area mask on every target.
- Base-map downloads now use MapLibre Compose's shared offline-pack API on
  Android and iOS. Both the renderer and downloader share one target-configured
  runtime and cache, while retaining the production style, zoom 0 through 16,
  display-density scaling, timestamp metadata, completion wait, and cleanup.
- A common `MainMapViewModel` now owns all four renderer-independent sources,
  forwards one shared viewport to quest and overlay loaders, decodes pin clicks,
  and closes every listener-backed source with the view-model lifecycle. Koin
  constructs the same graph on Android, iOS, and desktop.
- The complete main-map renderer is now one common composable. It places overlay
  side strokes below ordinary and bridge roads, downloaded areas/overlay bodies/
  tracks below labels, and overlay labels/markers/focus/location/pins above
  labels. It feeds MapLibre's immutable viewport back to the common data owner
  and exposes typed quest, edit, overlay, cluster, and raw map callbacks.
- Shared `MainMapState` now owns persisted camera restoration, zoom controls,
  compass reset, GPS following, navigation tilt and track bearing, the legacy
  first-fix zoom rule, and user-movement state. Camera policy is tested without a
  platform map view and drives the same durable MapLibre state on all targets.
- Cluster clicks now calculate their camera from the immutable rendered viewport,
  retain the legacy 0.25-level breathing room and zoom-19 cap, scale animation
  duration with zoom distance, and center correctly across the antimeridian.
- Focused geometry now uses the same shared viewport math with the legacy
  0.75-level margin, zoom-19 cap, 0.5-level zoom threshold, adaptive form
  padding, and reversible previous-camera capture. Clearing a selection can
  explicitly discard the return camera, matching edit-history behavior.
- Map projection is now exposed through logical `DpOffset` and shared `LatLon`
  values. Shared callbacks preserve consuming long presses, suppress raw map
  clicks behind interactive pins/overlays, and derive the legacy 14dp finger
  radius in ground meters from the rendered projection on every target.
- GPS track accumulation and recording now live in saveable common state. The
  20m accuracy filter, 60-second non-recording segment break, elevation and
  wall-clock capture, 50/100-point renderer chunking, recording handoff, and
  1,000-point restoration cap are shared. A provider outage now consistently
  keeps both recording intent and the red recording style when capture resumes.
- Main-map highlight and visibility state is now common too. Quest/edit pin
  mode, focused geometry, quest-form markers, selected pins, and overlay/pin
  visibility no longer need to be retained by the Android map fragment.
- The shared state now exposes the rendered scale, visible download bounds,
  projected coordinates, and animated target/zoom/padding moves needed by the
  HUD, geo links, context menu, and quest forms.
- Download-area planning is renderer-independent. It retains zoom-16 tile
  alignment, the 12 km2 rejection limit, and the 0.1 km2 minimum expansion
  around the camera target on every platform.
- The snapshot accepts negative `DpPadding` sides, so StreetComplete can express
  the pin's asymmetric collision box directly; the pre-0.15 workaround is gone.
- Geo links received before MapLibre publishes a presentation are retained and
  applied on attachment instead of being silently consumed.
- Android now keeps location and heading collection, map projection, quest/edit/
  overlay selection, contextual highlighting, download planning, GPS controls,
  track recording, bottom-sheet focus, context-menu actions, and solved-quest
  animation inside the shared Compose screen rather than bridging them through
  an Android activity and fragment.
- The shared screen also owns the quest and map-data invalidation listeners that
  close forms when their backing data disappears. Listener lifetime now follows
  composition on Android, iOS, and desktop instead of an Android activity.
- Styleable overlays now have shared feature conversion and declarative layers
  for areas, outlines, extrusions, center and side strokes, bridge ordering,
  dashes, SDF/color icons, labels, disabled elements, and element clicks. One
  source is safely shared across all four base-style insertion points.
