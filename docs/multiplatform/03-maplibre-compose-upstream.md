# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

The upstream audit below is dated 2026-09-04. The 2026-09-05 branch integration
updates the dependency to a publication of that audited commit, without a new
audit of later upstream changes. The synthetic scenario and timing hooks used
for earlier measurements have been removed. Those measurements remain historical
evidence, not coverage of the current real-device jank. The last pre-cleanup code
is available at `3be8406d6b0126781061aa68b766a4477ab76752`.

## Dependency baseline

- Dependency version: `0.15.1-SNAPSHOT`.
- Resolved publication rechecked on 2026-09-05:
  `0.15.1-20260904.102255-10`.
- The resolved publication was built from MapLibre Compose commit `71c5b258` by
  [the September 4 daily run](https://github.com/maplibre/maplibre-compose/actions/runs/33862529720).
- Latest MapLibre Compose `main` audited on 2026-09-04: `71c5b258`.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL, macOS ARM64 Metal, and Linux/Windows Vulkan for
  x64 and ARM64.
- The desktop artifacts require Java 25; Android and iOS keep their existing
  platform bytecode and native deployment targets.
- This branch intentionally follows the mutable snapshot version so it can test
  new MapLibre Compose publications without maintaining a timestamped artifact
  manifest. Update the resolved publication above after validating a new build.

The September 5 integration replaces `MapStyleScope` with `LocalMapState` in the
state-owned style, uses `DefaultMapRuntime.instance` for the optional default,
and uses the unified `MapRuntimeOptions` platform cache defaults. The app's
ordinary map and offline downloader still share the Koin-owned runtime.

## Pending on latest main

Seven integration gaps remain on MapLibre Compose `main` at `71c5b258`. Findings
fixed after the resolved snapshot belong in a separate section until
StreetComplete validates a snapshot that contains them. This audit found no
findings in that state.

The complete StreetComplete base style compiles against the snapshot with one
intentional source migration: symbol icon padding now uses MapLibre Compose's
typed `DpPadding` expression value. Cold production runs on Android, iOS, and
desktop exposed the additional runtime findings recorded here.

### Remembered dynamic sources need a stable public ID

StreetComplete's clustered pin layers refer to one source ID from several style
layers and use the matching source handle to request cluster leaves. The public
`rememberGeoJsonSource` API allocates an internal ID that its returned `Source`
does not expose. `MapStyleState.sources[id]` exposes a current handle publicly,
but callers need the generated ID to use it.
Supplying a fixed-ID custom `GeoJsonSource` preserves the layer graph and makes
the public lookup usable, while leaving the application responsible for tracking
handle generations.

Latest `main` keeps `Source.id` internal and gives `rememberGeoJsonSource` no ID
parameter, so the gap remains.

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
snapshot and reconstruction path. The current snapshot and latest MapLibre
Compose `main` both contain native-ffi `0.202608.3`. Draft
[maplibre-native-ffi PR #685](https://github.com/maplibre/maplibre-native-ffi/pull/685)
contains the measured pinning fix. The fix must ship in native-ffi and MapLibre
Compose must update its dependency before StreetComplete can remove this
cold-image limit without local dependency substitution.

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
make the caller-side JSON conversion asynchronous. Latest `main` retains this
call order.

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

Latest `main` still applies each changed property through
`LayerInstallation.update`. Each native `setLayerProperty` call waits for
`MlnFfiMapSession` owner-thread access before it returns.

### Layer click handlers cannot configure hit radius

StreetComplete's Android overlay component queries rendered features in a box
around the tap using the device's finger radius. MapLibre Compose layer click
handlers on latest `main` issue a point query at the exact `DpOffset`; the
handler API does not expose a radius or query rectangle. The shared overlay
still handles clicks on rendered symbols, lines, and fills, but thin lines are
less forgiving until the common API accepts configurable hit geometry (or
provides an async dispatch contract that can preserve event fallthrough after a
rectangle query).

### Gesture callbacks and thresholds are too coarse for parity

StreetComplete's Android map distinguishes pan begin from zoom, rotate, and tilt;
only a pan disables GPS following. It also configures a 5dp pan threshold, 1.5°
rotation threshold, 8dp tilt threshold, fling threshold/base time of 250/500, and
disables rotation while a scale gesture is active.

Latest `main` exposes only the aggregate `CameraMoveReason.GESTURE`. Its common
`GestureOptions` can enable or disable gesture families and configure mouse click
slop, but the touch slops are fixed and it exposes neither gesture-specific begin
events nor the other native thresholds above. The shared controller therefore
detects gesture-driven camera-target changes as the narrowest available pan
signal and otherwise uses the standard common gesture configuration. A typed
gesture event (at least pan begin) plus common threshold/interlock fields would
remove both compromises.

The newer `MapEvent.CameraMoveStarted` reports whether the movement is animated,
not whether a pan, zoom, rotation, or tilt started. It does not close this gap.

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

The newer `MapState.events` stream contains engine load, camera, and frame
events. It does not report input dispatch results, so this gap remains on latest
`main`.

## Resolved in the current snapshot

These findings are fixed in MapLibre Compose commit `c0e96909`, which produced
the snapshot that this branch resolves.

### Global style-transition configuration

StreetComplete's Android map sets a 300 ms global style transition adjusted by
the system animator-duration scale. It also enables placement transitions.
MapLibre Compose commit `cce9fe1e` adds both operations to the common imperative
style API through `MapStyleState.transition`. The resolved snapshot contains the
commit. StreetComplete now applies the scaled duration and enables placement
transitions each time the style reaches `StyleLoadState.Ready`.

### Declarative GeoJSON refresh keeps the Android map visible

StreetComplete reproduced a blank Android map when a fixed-ID declarative
`GeoJsonSource` changed after the first frame. Style reconciliation marked the
map as not presentable, so the Android host removed its platform surface while
it applied the replacement.

MapLibre Compose commit `3f8fe157` keeps the previous style presentable while a
replacement style or style revision loads. The commit adds a native composition
test that switches the base style and verifies that the load placeholder does
not cover the map. The resolved snapshot contains the commit. StreetComplete's
stable source-handle updates remain useful for performance, but they are no
longer required to prevent this blank-map failure.

### The public image boundary rejects zero-size painters

On Android, Compose loaded a drawable `<layer-list>` used for the location
shadow as a painter with zero intrinsic width and height. Older MapLibre Compose
builds accepted it, then crashed when `ImageManager` tried to allocate
`ImageBitmap(0, 0)`.

MapLibre Compose commit `28862c52` rejects a painter without positive intrinsic
or explicit dimensions when the application calls `image()`. The error tells
the caller to pass a positive size. The resolved snapshot contains the commit,
and StreetComplete still supplies explicit dimensions for its dynamic location
images.

### iOS location service checks run off the main thread

Older `IosLocationProvider.updates` collections called
`CLLocationManager.locationServicesEnabled()` on the main dispatcher. Core
Location warned that the call could make the UI unresponsive.

MapLibre Compose commit `c61804e4` removes the startup service query. The
provider now calls the static function only to classify a denied Core Location
error, and `readLocationServicesEnabled` runs that call on `Dispatchers.Default`.
The resolved snapshot contains the commit.

### Volatile local GeoJSON sources

The Android implementation set `GeoJsonSource.isVolatile = true` on its dynamic
local sources. MapLibre Native uses this option only for HTTP sources, so the
setting does not affect StreetComplete's inline GeoJSON data. The common API
does not need a volatile option for this migration. Latest `main` still omits
the option.

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
MapLibre Compose commit `4542c118` added typed `DpPadding` values that represent
negative sides in style-spec top/right/bottom/left order. The resolved snapshot
contains the commit. The shared pin layer can therefore declare the exact legacy
values without the old workaround; live target validation is still needed.

## Integration constraints

- The current desktop runtime artifacts target Java 25. StreetComplete's future
  desktop distribution must package a Java 25 runtime, and this branch compiles
  its desktop target to JVM 25 bytecode accordingly.
- There is no published macOS x64 runtime. This does not block the current ARM64
  development host, but StreetComplete cannot claim macOS x64 support without an
  upstream runtime artifact.
