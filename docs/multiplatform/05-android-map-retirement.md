# Android map retirement inventory

This inventory is the deletion guard for the old `org.maplibre.android` map.
The production `MainActivity` already renders `MainScreen` and `MainMap` from
`commonMain`; no layout, manifest, Koin module, or source outside the legacy map
directory references `MainMapFragment` or its collaborators.

## Source replacements

| Android-only source | Shared replacement and retained behavior |
| --- | --- |
| `MainMapFragment.kt`, `MapFragment.kt` | `MainMap.kt`, `MainMapState.kt`, `MainMapContentState.kt`, `MainMapTrackState.kt`, and `MainMapDownloadArea.kt` own presentation, camera, location, interaction, selection, tracking, and download planning. |
| `QuestPinsManager.kt` | `MapQuestPinsSource.kt` retains viewport loading, quest deltas, ordering, cancellation, and typed keys. |
| `EditHistoryPinsManager.kt` | `EditHistoryPinsSource.kt` retains complete-history reloads, edit keys, and pin resources. |
| `StyleableOverlayManager.kt` | `StyleableOverlaySource.kt` retains viewport loading, selection changes, map-data deltas, cancellation, and feature conversion. |
| `DownloadedAreaManager.kt` | `DownloadedTilesStateSource.kt` retains the 14-day cutoff and update listening. |
| `MapIconBitmapCreator.kt` | `PinPainter.kt` and Compose resource painters construct quest, edit, and selected-pin imagery. |
| `CurrentLocationMapComponent.kt` | `CurrentLocationLayers.kt` retains accuracy, bearing, shadow, dot, April 1 artwork, and animation. |
| `DownloadedAreaMapComponent.kt` | `DownloadedAreaLayer.kt` retains the inverse mask and hatched visualization. |
| `FocusGeometryMapComponent.kt` | `FocusedGeometryLayers.kt` retains point, line, polygon, multipolygon, and breathing highlight rendering. |
| `GeometryMarkersMapComponent.kt` | `GeometryMarkersLayers.kt` retains optional icons, titles, lines, polygons, and resource registration. |
| `PinsMapComponent.kt` | `PinsLayers.kt`, `PinPainter.kt`, and `MainMapViewModel.kt` retain clustering, collision geometry, ordering, clicks, and leaf lookup. |
| `SelectedPinsMapComponent.kt` | `SelectedPinsLayer.kt` retains pin composition and the overshoot selection animation. |
| `StyleableOverlayMapComponent.kt` | `StyleableOverlayLayers.kt` retains fills, outlines, extrusions, strokes, dashes, symbols, labels, ordering, disabled state, and clicks. |
| `TracksMapComponent.kt` | `TracksLayer.kt` and `MainMapTrackState.kt` retain recording segments, styling, animation, chunking, and restoration. |
| `SceneMapComponent.kt` | `MapStyle.kt` and `StreetCompleteMap.kt` contain the complete light/night base style and four data-layer insertion seams. |
| `maplibre/CameraUtils.kt`, `Position.kt`, and `Bearing.kt` | `MainMapState.kt`, `GeometryUtils.kt`, and `AnimationUtils.kt` retain camera fitting, coordinate conversion, and shortest-turn/antimeridian math. |
| `maplibre/Expression.kt` | `ExpressionUtils.kt` and typed MapLibre Compose expressions retain interpolation and style conversions. |
| `maplibre/MapImages.kt` | MapLibre Compose resource registration in the shared layer files replaces imperative style-image ownership. |
| `maplibre/MapLibreMap.kt`, `MapView.kt` | `StreetCompleteMap.kt` owns the common MapLibre Compose map and target presentation host. |
| `maplibre/OfflineManager.kt` | `MapLibreMapTilesDownloader.kt` uses the shared offline-pack API and the same target runtime/cache as rendering. |

The directory contains 24 Kotlin files and 3,599 lines. Its only references from
outside the directory are three explanatory comments naming the former managers;
there are no production call sites.

## Dependency and asset replacements

- Remove the direct `org.maplibre.gl:android-sdk-opengl:13.3.1` dependency.
  Android keeps the MapLibre Compose OpenGL runtime dependency, which supplies
  the renderer used by the shared map.
- Remove `androidMain/assets/map_theme`: 512 glyph ranges duplicated by
  `commonMain/composeResources/files/glyphs`, two legacy JSON styles, and four
  sprite PNG/JSON files. The shared style is declarative and registers its own
  Compose image resources.
- Remove the obsolete `UpdateMapStyleTask`, its `updateMapStyle` registration,
  and its `updateStreetCompleteData` dependency. Updating Android JSON cannot
  update the shared declarative style and would recreate deleted assets.
- Remove the now-empty `androidMain/assets/authors.txt`; its only attribution was
  for the retired `map_theme` directory.

## Deletion gates

After deletion, all of the following must hold:

1. No source or build file refers to `org.maplibre.android`, the retired map
   classes, `UpdateMapStyleTask`, or `androidMain/assets/map_theme`.
2. Android, desktop, and the iOS simulator framework compile together.
3. The Android APK packages the 512 shared glyph ranges and cold-launches into
   a first MapLibre Compose OpenGL frame without a crash or ANR.
4. Focused common map tests still pass on the desktop and Android host runners.
5. Desktop and iOS continue to package and render from the same shared source;
   the deletion must not introduce an Android-specific replacement.

## Result

The reference, cross-target compile, glyph, dependency, APK-content, focused
test, and Android cold-launch gates pass. The 21 common map test classes execute
142 tests on both desktop and Android host with no failures. The broad desktop
suite also reproduced only its six documented baseline locale/date failures.
Desktop packaging/live Metal and iOS bundle/live Metal checks remain part of the
final three-target demo and validation pass rather than being inferred from this
Android-only deletion.
