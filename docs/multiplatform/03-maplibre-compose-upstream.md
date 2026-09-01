# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

## Dependency baseline

- Latest stable release available locally: `0.15.0`.
- Latest published post-release snapshot observed on 2026-09-01:
  `0.15.1-SNAPSHOT`, build `0.15.1-20260901.101938-7`.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL, macOS ARM64 Metal, and Linux/Windows Vulkan for
  x64 and ARM64.

## Findings

Ten integration gaps are confirmed below. The abandoned
`upstream/maplibre-compose` integration predates 0.15, so its other assumptions
continue to be re-evaluated against the snapshot before being attributed upstream.

The complete StreetComplete base style compiles against the snapshot with one
intentional source migration: symbol icon padding now uses MapLibre Compose's
typed `DpPadding` expression value. Cold production runs on Android, iOS, and
desktop exposed the additional runtime findings recorded here.

### Snapshot metadata cannot be consumed as an immutable version directly

The Sonatype snapshot repository stores build `20260901.101938-7` under
timestamped `.module`, `.jar`, `.aar`, `.klib`, source, and resource names, but
the Gradle module metadata inside that build names its variant files with
`0.15.1-SNAPSHOT`. Resolving the timestamp as an ordinary immutable version
therefore cannot fetch those logical file names.

StreetComplete preserves the published variant graph with a component-metadata
rule and replaces each selected variant's files from a committed manifest. This
is reproducible, but it requires listing every target/runtime file. A release-like
nightly version whose module metadata refers to its own timestamped files would
make reviewed snapshot pins ordinary Gradle dependencies and eliminate this
consumer workaround.

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

Most shared layers update their GeoJSON data through `rememberGeoJsonSource`.
Clustered pins and the styleable overlay require stable public source IDs and
cluster-leaf access, so they retain stable source definitions and update the
current generation's handle directly. In both cases the visualizations remain
functional. What cannot currently be preserved is the legacy cache/performance
hint. MapLibre Compose should expose this as a common GeoJSON source option on
native-backed targets and document browser behavior.

### Remembered dynamic sources need a stable public ID

StreetComplete's clustered pin layers refer to one source ID from several style
layers and use the matching source handle to request cluster leaves. The public
`rememberGeoJsonSource` API allocates an internal ID that its returned `Source`
does not expose. `MapStyleState.sources` and `MapStyleState.source(id)` do expose
current handles publicly, but callers need the generated ID to use them.
Supplying a fixed-ID custom `GeoJsonSource` preserves the layer graph and makes
the public lookup usable, while leaving the application responsible for tracking
handle generations.

A supported stable-ID parameter on `rememberGeoJsonSource`, or a public overload
that resolves the remembered `Source` to its current handle, would remove this
custom-source seam while retaining declarative data updates and cluster
inspection.

### Declarative GeoJSON refresh can remove the Android render surface

After the first Android frame, changing a fixed-ID declarative `GeoJsonSource`
causes `MlnFfiMapSession.reconcileStyleRevision` to set
`hasLoadedFirstStyle = false` while it prepares the replacement source. The
Android presentation then receives `presentFrames = false`, removes its platform
surface, and logs `Host surface lost`; Compose controls remain visible over a
blank map. StreetComplete reproduced this with both Surface and Texture modes.

The application workaround keeps each fixed-ID source definition stable and
updates the current generation's `GeoJsonSourceHandle`. A style-generation race
restarts the effect with the new handle; only the three exact stale-handle
exceptions are treated as recoverable. Upstream should keep the first-style flag
monotonic after the first successful load, as its lifecycle comment describes,
or decouple reconciliation progress from platform-surface visibility.

### Zero-size painters fail deep inside runtime image registration

On Android, Compose loaded a drawable `<layer-list>` used for the location shadow
as a painter with zero intrinsic width and height. MapLibre Compose accepted it,
then `ImageManager` attempted to allocate `ImageBitmap(0, 0)` and crashed during
style preparation. Converting the resource to a sized vector and declaring
explicit dimensions for all dynamic location images fixes the application.

The existing `image(Painter, DpSize, ...)` overload supplies an explicit caller
size and StreetComplete now uses it. Image registration should still reject a
non-positive raster size at the public boundary with the resource/source name
and an actionable message, and document when callers must provide that explicit
size rather than relying on painter intrinsic dimensions.

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

### iOS location collection synchronously queries service availability

Collecting `IosLocationProvider.updates` on an iOS 26.5 simulator emits Core
Location's performance diagnostic that `locationServicesEnabled()` can make the
UI unresponsive when called on the main thread. StreetComplete sees it twice
when the main screen and lifecycle-driven auto-sync location consumers attach.

The provider currently begins its main-dispatcher `callbackFlow` by calling
`CLLocationManager.locationServicesEnabled()`. Core Location recommends waiting
for `locationManagerDidChangeAuthorization` and inspecting the manager's
authorization status. The provider should derive startup permission/service
state from its existing delegate-driven requester and reserve the static service
query for a non-main execution context only if it remains necessary. The app
stays responsive and receives a map frame, so this is a measured performance and
lifecycle defect rather than a launch blocker.

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
