# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

## Dependency baseline

- Latest stable release available locally: `0.15.0`.
- Latest published post-release snapshot observed on 2026-09-01:
  `0.15.1-SNAPSHOT`, build `0.15.1-20260901.101938-7`.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL and macOS ARM64 Metal.

## Findings

Five common-API gaps are confirmed below. The abandoned
`upstream/maplibre-compose` integration predates 0.15, so its other assumptions
continue to be re-evaluated against the snapshot before being attributed upstream.

The complete StreetComplete base style compiled against the snapshot with one
intentional source migration: symbol icon padding now uses MapLibre Compose's
typed `DpPadding` expression value. Runtime rendering and interaction may expose
additional findings that compilation cannot.

### Missing global style-transition configuration

The Android implementation sets MapLibre Native's global style transition to a
300ms duration multiplied by the system animator-duration scale, with placement
transitions enabled. The post-v0.15 Compose API exposes no common configuration
for the equivalent native transition options. The shared style therefore uses
backend defaults for now; `StreetCompleteMap` carries a TODO at the integration
point. A common, backend-neutral style-transition option would let the migration
preserve this behavior and respect reduced or disabled system animation.

### Missing volatile GeoJSON source option

The legacy downloaded-area, recorded-track, focused-geometry, geometry-marker,
selected-pin, clustered-pin, styleable-overlay, and current-location sources explicitly set
`GeoJsonSource.isVolatile = true`
because their geometry changes frequently. The snapshot's common
`GeoJsonOptions` exposes tiling, clustering, line metrics, and synchronous
updates, but not MapLibre Native's volatile-source flag.

The shared layers still update their GeoJSON data through
`rememberGeoJsonSource`, so the visualizations remain functional. What cannot
currently be preserved is the legacy cache/performance hint. MapLibre Compose
should expose this as a common GeoJSON source option on native-backed targets
and document browser behavior.

### Layer click handlers cannot configure hit radius

StreetComplete's Android overlay component queries rendered features in a box
around the tap using the device's finger radius. MapLibre Compose layer click
handlers currently issue a point query at the exact `DpOffset`; the handler API
does not expose a radius or query rectangle. The shared overlay still handles
clicks on rendered symbols, lines, and fills, but thin lines are less forgiving
until the common API accepts configurable hit geometry (or provides an async
dispatch contract that can preserve event fallthrough after a rectangle query).

### Gesture callbacks and thresholds are too coarse for parity

StreetComplete's Android map distinguishes pan begin from zoom, rotate, and tilt;
only a pan disables GPS following. It also configures a 5dp pan threshold, 1.5°
rotation threshold, 8dp tilt threshold, fling threshold/base time of 250/500, and
disables rotation while a scale gesture is active.

The snapshot exposes only the aggregate `CameraMoveReason.GESTURE`. Its common
`GestureOptions` can enable or disable gesture families and configure mouse click
slop, but the touch slops are fixed and it exposes neither gesture-specific begin
events nor the other native thresholds above. The shared controller therefore
detects gesture-driven camera-target changes as the narrowest available pan
signal and otherwise uses the standard common gesture configuration. A typed
gesture event (at least pan begin) plus common threshold/interlock fields would
remove both compromises.

### No post-layer unhandled map-click callback

StreetComplete treats a map click as a fallback: quest pins, edit pins, and
overlay features get the event first, and the raw map receives it only if no
interactive feature consumed it. MapLibre Compose invokes
`MapPresentationCallbacks.onClick` before layer handlers. Returning `Pass` lets
layers run, but there is no callback after layer dispatch to report that none of
them consumed the event.

The shared renderer preserves behavior by querying its complete set of
interactive layer IDs before invoking the raw-map callback. This duplicates
dispatch knowledge and an extra rendered-feature query. A common post-dispatch
`onUnhandledClick` callback, or an async dispatch result, would provide the
fallback contract directly.

## Resolved integration findings

### Map lifecycle lock inversion during style-source refresh

Snapshot build `0.15.1-20260831.102040-6` deterministically ANRed Android on
StreetComplete's first map load. The UI thread waited in
`MlnFfiGate.awaitUntilOpen` while reading native style sources; the map-owner and
render threads were both waiting on lifecycle locks. A cold-launch device loop
reproduced the three-thread cycle on every run.

MapLibre Compose commit `2e7114c5` moves source reads out of the lifecycle lock
and adds the focused `MapLifecycleCallbackRaceTest`. Snapshot build
`0.15.1-20260901.101938-7` includes the fix. The unchanged StreetComplete device
loop stays resumed, renders its first OpenGL frame, and produces no ANR. This is
resolved upstream and requires no application workaround.

### Negative symbol collision padding

The old exploratory integration omitted StreetComplete's asymmetric pin
collision box because the Compose API at that time rejected negative padding.
The current snapshot's typed `DpPadding` represents negative sides in style-spec
top/right/bottom/left order. The shared pin layer can therefore declare the exact
legacy values without the old workaround; live target validation is still needed.

## Integration constraints

- The current desktop runtime artifacts target Java 25. StreetComplete's future
  desktop distribution must package a Java 25 runtime even though its Kotlin JVM
  bytecode target remains 11.
- There is no published macOS x64 runtime. This does not block the current ARM64
  development host, but StreetComplete cannot claim macOS x64 support without an
  upstream runtime artifact.
