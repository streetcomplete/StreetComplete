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

- All targets now compile against the post-v0.15 `0.15.1-SNAPSHOT`; dependency
  resolution selected build `0.15.1-20260831.102040-6`.
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
- The existing Android map remains active while shared data layers are migrated,
  so this intermediate layer does not remove working map functionality.
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
