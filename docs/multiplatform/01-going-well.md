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

## Desktop application

- The JVM target now produces a real Compose Desktop application and native
  distributable that enter the same onboarding, main map, settings, about, and
  user flows as Android and iOS.
- Desktop has persistent platform paths, bundled SQLite, observable Java
  preferences, Ktor's Java HTTP engine, native MapLibre location, browser/map and
  mail launchers, WAV playback, crash-report persistence, network observation,
  and in-process upload/download/changeset controllers.
- Desktop's renderer and offline downloader share one MapLibre runtime. The
  macOS ARM64 distributable packages the Metal and Skiko native libraries plus
  the complete 43 MB application resource tree used by metadata, presets,
  sounds, glyphs, and the base style.

## iOS sync foundation

- iOS now registers the real `commonModule` dependency graph rather than a
  three-screen demonstration graph.
- Upload, download, and changeset auto-closing use tested shared coroutine
  controllers while the iOS process is alive. User downloads replace automatic
  downloads; repeated automatic work stays unique.
- Apple Network framework path monitoring now feeds the shared connectivity API,
  including satisfied-path and expensive-path state.
- The iOS application now enters the same shared top-level navigation graph as
  the other targets. Its real onboarding, main map, settings, quest settings,
  about, profile, and login flows replace the temporary three-screen launcher.
- iOS email composition now builds encoded `mailto:` URLs with Foundation query
  items and the current `UIApplication` API; subject and body are no longer lost
  through impossible Kotlin-to-`NSString` casts.
- Persisted element edits now use an explicit polymorphic action serializer.
  Saving and restoring every supported edit action therefore works on
  Kotlin/Native instead of depending on JVM-only runtime serializer discovery.
- iOS production and database tests now use `NativeSQLiteDriver`, while bundled
  SQLite remains scoped to JVM targets. A clean Xcode application link no longer
  combines two SQLite implementations.
- iOS installs a Kotlin/Native unhandled-exception hook before application
  initialization. A crash report is persisted in Application Support and
  consumed once by the same shared recovery dialog used on other targets.
- The Swift host registers a network-constrained Apple processing task. It runs
  pending automatic uploads and changeset closure through the production Koin
  graph, cancels on expiration, reports completion, and reschedules itself.
- iOS now reads the user's Foundation measurement system for its scale bar,
  declares the camera purpose required by the shared photo flow, and presents
  the external-map chooser from the invoking Compose scene with an iPad popover anchor.

## Shared application lifecycle

- Android, iOS, and desktop now run the same process initializer for logging,
  metadata preloading, old edit-history cleanup, feed refresh, resurvey interval
  updates, stored-version migration, and downloaded-tile invalidation.
- The shared root composition reference-counts active scenes through one
  process-owned `AutoSyncer`. A scene closing cannot cancel synchronization for
  another iPad window, while Android and desktop retain their single-root behavior.
- The process coroutine scope is part of the common dependency graph, so the
  target upload, download, network, startup, and changeset controllers use one
  supervised application lifetime rather than independently shaped scopes.
- External `geo:` and StreetComplete URLs now enter a buffered common ingress.
  Android and desktop own one process ingress; each iOS scene owns its own so the
  scene that receives a URL both navigates and consumes it. Cold-start URLs are
  retained without leaving sticky navigation state after recreation.
- Android now has one application activity. Settings, quest settings, About,
  profile, and login are destinations in the shared Compose navigation graph;
  the exported network-usage intent enters that graph through `MainActivity`.
- The Android source/resource audit finds no fragments, XML layouts, navigation
  graphs, or menus. Obsolete Fragment, View, Bitmap, and density helpers and the
  direct Fragment dependency have been removed; Android-only source is limited
  to the Compose host and platform integrations.
- External URLs now also carry a shared navigation signal. A warm `geo:` or
  configuration link returns Settings/About/Profile to the main map before the
  buffered payload moves the camera or opens configuration UI.
- Theme, language, and keep-screen-on preferences are observed at the shared
  Compose root. Android localizes its plain `ComponentActivity`; desktop applies
  JVM locale plus native sleep inhibition; iOS applies its Apple language domain
  and idle-timer policy.
- Every target runs expired-data cleanup at process initialization. Android keeps
  its daily WorkManager job; desktop and iOS schedule daily cleanup in the shared
  application scope. Cleanup is suspend/structured work, and desktop first
  disposes Compose, then cancels and joins that scope, then lets Koin close SQLite
  and the native map runtime. Foreground upload/download and delayed changeset
  failures are contained instead of escaping a root native coroutine.

## MapLibre Compose snapshot foundation

- All targets compile against the post-v0.15 `0.15.1-SNAPSHOT` series. This
  validation branch follows new publications through Sonatype's Maven snapshot
  repository; the currently validated publication is recorded in
  `03-maplibre-compose-upstream.md`.
- Android packages the OpenGL runtime, iOS links the transitive Metal runtime,
  and the current macOS ARM64 host selects the desktop Metal runtime. Simulated
  Gradle host resolution also selects the Vulkan plus native-location artifacts
  for Linux and Windows x64/ARM64 rather than nonexistent OpenGL modules.
- StreetComplete now consumes the snapshot's `LocationMeasurement`,
  `LocationEvent.Update`, and `HeadingMeasurement` APIs. Recorded tracks use the
  measurement's real wall-clock instant instead of accidentally serializing a
  monotonic location age as an epoch timestamp.
- The main map, onboarding tutorial, and automatic sync consume MapLibre
  Compose's common location API. Android intentionally splits ownership: the
  visible UI uses an Activity-bound provider that can request and refresh runtime
  permission, while process-owned automatic sync uses an application-safe backend
  and never retains the Activity. Desktop and iOS use the process provider.
- Automatic sync reconnects a completed Android permission-denied location flow
  when the Activity resumes from the system permission sheet. Its last position
  is atomic across lifecycle, connectivity, and download callbacks.

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
  `MainMap` directly. After a committed source-by-source replacement inventory,
  the unused 24-file fragment stack, direct MapLibre Android SDK dependency,
  obsolete JSON-style updater, and 518 duplicate Android map assets were
  removed. The APK now contains only the shared Compose map and glyph resources.
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
  leaf lookup through generation-bound source handles. Like the Android map, it
  keeps one source, installs only newly encountered pin images for each loaded
  style, then updates the source data imperatively.
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
- Quest-pin and styleable-overlay viewport scheduling is serialized across
  repository callbacks, Compose viewport updates, and shutdown. Child-context
  cancellation and generation checks prevent a non-suspending old database load
  from publishing over a newer viewport; focused supersession tests cover both.
- Overlay-declared base-layer suppression is live again: the address overlay
  hides `labels-housenumbers` while its own address labels are visible, avoiding
  the duplicate labels that the retired Android style manager prevented.
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
- Android's main menu now opens settings, quest settings, about, profile, and
  login inside the shared root navigation graph rather than launching separate
  activities. Returning to the map keeps the same activity and refreshes its
  keep-screen-on policy; language changes recreate the host explicitly.
- The shared screen also owns the quest and map-data invalidation listeners that
  close forms when their backing data disappears. Listener lifetime now follows
  composition on Android, iOS, and desktop instead of an Android activity.
- Styleable overlays now have shared feature conversion and declarative layers
  for areas, outlines, extrusions, center and side strokes, bridge ordering,
  dashes, SDF/color icons, labels, disabled elements, and element clicks. One
  source is safely shared across all four base-style insertion points.
