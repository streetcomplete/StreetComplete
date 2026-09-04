# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

## Dependency baseline

- Dependency version: `0.15.1-SNAPSHOT`.
- Resolved publication rechecked on 2026-09-04:
  `0.15.1-20260903.101931-9`.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL, macOS ARM64 Metal, and Linux/Windows Vulkan for
  x64 and ARM64.
- The desktop artifacts require Java 25; Android and iOS keep their existing
  platform bytecode and native deployment targets.
- This branch intentionally follows the mutable snapshot version so it can test
  new MapLibre Compose publications without maintaining a timestamped artifact
  manifest. Update the resolved publication above after validating a new build.

## Findings

Eleven unresolved integration gaps are confirmed below. The abandoned
`upstream/maplibre-compose` integration predates 0.15, so its other assumptions
continue to be re-evaluated against the snapshot before being attributed upstream.

The complete StreetComplete base style compiles against the snapshot with one
intentional source migration: symbol icon padding now uses MapLibre Compose's
typed `DpPadding` expression value. Cold production runs on Android, iOS, and
desktop exposed the additional runtime findings recorded here.

### Missing global style-transition configuration

The Android implementation sets MapLibre Native's global style transition to a
300ms duration multiplied by the system animator-duration scale, with placement
transitions enabled. The post-v0.15 Compose API exposes no common configuration
for the equivalent native transition options. The shared style therefore uses
backend defaults for now; `StreetCompleteMap` carries a TODO at the integration
point. A common, backend-neutral style-transition option would let the migration
preserve this behavior and respect reduced or disabled system animation.

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

### Native style-image upload copies dominate cold-image stalls

The Android map creates bitmaps for every newly encountered quest icon, then
installs all color images with one `Style.addImages` call. Its cache repeats this
work only after the loaded style changes.

The common `StyleImages` API accepts one `ImageBitmap` per `add` call. Each call
captures the bitmap into an `IntArray`, reconstructs an `ImageBitmap` from that
array in the Compose native binding, and converts the result to premultiplied
RGBA8. The native-ffi binding then copied the resulting `ByteArray` through a
`UByteArray` and `toCValues` before its synchronous C call. For a 213 by 213
pixel iPhone pin, each full pixel buffer is about 181 KB.

The shared map keeps the same loaded-style cache and installs only new icons
before it updates the stable pin source. It moves upload work off the UI thread
and starts at most one new icon per display frame. A synchronized two-second
process sample during an eight-marker upload attributed 678 samples to
`MlnFfiStyleBinding.addImage`, 676 to `MapHandle.setStyleImage`, and 675 to
`PremultipliedRgba8Image` conversion through `toCValues`. Only one sample reached
the C `mln_map_set_style_image` call. The dominant cost was therefore proven to
be Kotlin/Native pixel marshalling, not MapLibre Native's image insertion.

An A/B build against the exact native-ffi `0.202608.3` source replaced those
transit copies with a scoped pin of the binding's already-owned pixel snapshot.
The same deterministic iPhone 17 simulator scenario reduced 37 image calls from
1.49 seconds to 62 ms, eight geometry-marker calls from 1.37 seconds to 36 ms,
and a 45-image style reload from 2.52 seconds to 181 ms. Cluster expansion,
overlay loading, selection animation, geometry markers, and far-pan data loads
then completed with 16.67 ms maximum display-frame intervals. The scoped pin is
safe under the current C contract because every image entry point copies the
pixels synchronously before returning; the public Kotlin pixel getter remains a
defensive copy.

The pinning fix is required before a batch API can materially help this case. A
batch operation is still desirable for parity with Android's `Style.addImages`,
atomic preflight/reservation, and one render request per group. It should accept
captured image data so callers do not repeat the Compose-side `ImageBitmap`
snapshot and reconstruction path. The current snapshot still contains native-ffi
`0.202608.3`; the measured fix must be released in native-ffi and consumed by a
new MapLibre Compose snapshot before StreetComplete can remove this cold-image
limit without local dependency substitution.

In a stronger run that continuously animates the camera during installation,
the paced application path keeps map intervals below 32.80 ms for 37 quest
images and 26.21 ms for eight overlay images. One cold quest-load UI frame still
reaches 54.95 ms. The upstream fix remains important for the measured 1.5-second
CPU cost and delayed icon availability. That work is a plausible source of the
reported device heat, but the paced path prevents it from becoming the
multi-hundred-millisecond visible freeze seen before the application workaround.

### GeoJSON preparation runs synchronously on the caller thread

`GeoJsonSourceHandle.setData` calls `prepareGeoJsonUpdate` before it dispatches
the update to the map owner thread. On native targets, that preparation creates
the native GeoJSON source data synchronously on the calling thread. The default
`synchronousUpdate = false` makes native tiling asynchronous, but it does not
make the caller-side JSON conversion asynchronous.

StreetComplete calls `setData` from `Dispatchers.Default` for its large pin
snapshots. This prevents source preparation from blocking Compose's UI thread.
The binding already supports preparing data before the map-owner call. A future
API can make this preparation asynchronous by default, as the implementation's
existing TODO proposes.

### Declarative transient layer properties block the iOS UI thread

MapLibre Compose reconciles changed layer properties from the composable's
dispatcher. The iOS binding then calls the synchronous map-owner mutation for
each property. Opening a StreetComplete quest changed visibility on three pin
layers and eleven overlay layers. The map callbacks remained below 22 ms, but
the Compose UI paused for 171.8 ms. Closing the quest paused it for 131.3 ms.

StreetComplete now keeps the layer definitions stable and applies transient
visibility through the current `LayerHandle` objects on `Dispatchers.Default`.
The published-snapshot simulator run reduces quest open, selected panning, and
quest close to 16.67 ms maximum UI intervals and 22.83 ms or lower maximum map
intervals. Track-recording color changes use the same method and remove a
separate 122.1 ms UI pause during track stop.

An upstream property-update transaction could apply several changes with one
map-owner handoff and one render request. At minimum, the declarative
reconciler should not make synchronous native waits on the UI dispatcher. The
imperative API is usable for this migration, but every application must list
the retained layer IDs, replay values after style reload, and handle stale
generation errors.

### Declarative GeoJSON refresh can remove the Android render surface

After the first Android frame, changing a fixed-ID declarative `GeoJsonSource`
causes `MlnFfiMapSession.reconcileStyleRevision` to set
`hasLoadedFirstStyle = false` while it prepares the replacement source. The
Android presentation then receives `presentFrames = false`, removes its platform
surface, and logs `Host surface lost`; Compose controls remain visible over a
blank map. StreetComplete reproduced this with both Surface and Texture modes.

The application workaround keeps each fixed-ID source definition stable and
updates the current generation's `GeoJsonSourceHandle`. The update effect
observes the load state and current data, then looks up the current handle before
each publication. Only the three exact stale-handle exceptions are recoverable.
Upstream should keep the first-style flag monotonic after the first successful
load, as its lifecycle comment describes, or decouple reconciliation progress
from platform-surface visibility.

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
the map-level `onClick` callback before layer handlers. Returning `Pass` lets
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

### Volatile local GeoJSON sources

The Android implementation set `GeoJsonSource.isVolatile = true` on its dynamic
local sources. MapLibre Native uses this option only for HTTP sources, so the
setting does not affect StreetComplete's inline GeoJSON data. The common API
does not need a volatile option for this migration.

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
  desktop distribution must package a Java 25 runtime, and this branch compiles
  its desktop target to JVM 25 bytecode accordingly.
- There is no published macOS x64 runtime. This does not block the current ARM64
  development host, but StreetComplete cannot claim macOS x64 support without an
  upstream runtime artifact.
