# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

The current audit targets released [MapLibre Compose v0.16.0](https://github.com/maplibre/maplibre-compose/releases/tag/v0.16.0),
commit `c95a0afbf`, on 2026-09-09. Historical timing measurements below are not
coverage of current real-device performance.

## Dependency baseline

- Core, location, resource, and platform runtime artifacts use `0.16.0` from Maven Central.
- Snapshot repositories and Maven Local wiring are absent.
- The release includes the image restoration fixes and queued style writes that
  previously required local publications (`9cd93ad` and `e3d246b9`).
- Desktop still requires Java 25 and has no macOS x64 runtime artifact.
- Rendering and offline downloads continue to share the Koin-owned runtime.

## Release cleanup

- Move gesture bindings into `MapUiOptions`, retaining the existing StreetComplete
  thresholds, momentum, and follow-mode callbacks in their respective builders.
- Use declarative `baseStyle` and remove the unused standalone tile-LOD parameter
  from the application map wrapper. The release configures LOD through `RenderOptions`.
- Use `textOffset` for em-based label offsets, preserve nullable image expressions,
  and omit a solid road casing's dash array with Kotlin `null`.
- Remove application-side transition scaling and its obsolete tests. MapLibre
  applies Android's animator duration scale to the supplied 300 ms duration;
  multiplying it in StreetComplete would apply the scale twice. The effect still
  reapplies the transition when the system setting changes.

## Idiomatic usage sweep

The September 9 sweep covers every production file importing MapLibre Compose,
plus the map's data builders, animation helpers, and tests.

| Surface | Result and rationale |
| --- | --- |
| Map runtime, presentation, and state ownership | Keep the shared runtime for rendering/offline storage, `rememberMapState` ownership, and the style-input bridge. The map's independent style composition observes those inputs; it must not depend on a captured initial screen value. Desktop shutdown closes the runtime after UI disposal. Mobile runtimes live for the process. |
| Camera and viewport | Keep the application controller's follow/navigation policy, latest pending move, focus restoration, zoom margins, and antimeridian-aware fitting. These reproduce StreetComplete behavior rather than substitute for missing map APIs. Queries use current `MapState`/viewport values. |
| Input and clicks | Keep explicit gesture policy, typed layer hit padding, and `onUnhandled`. Cluster queries acquire a current source handle for each click; the remembered callback now keys on its source. |
| Focused geometry | Replace the retained-handle frame loop and feature-state expression with a Compose infinite animation and declarative paint properties. Disable native transitions for those properties so animation frames are not interpolated twice. No feature ID is needed. |
| Current location marker | Animate accuracy and rotation directly as layer properties. Remove the GeoJSON property transport and equator-radius adjustment; latitude goes directly into the existing meter conversion. Keep explicit icon sizes and April 1 styling. |
| Other sources and layers | Keep `rememberGeoJsonSource`, ordinary declarative visibility/properties, stable layer identity, and the overlay insertion points. Tracks, downloaded areas, and overlays pass their typed geometry directly instead of serializing an already-built tree. Heavy domain conversion remains off the UI dispatcher. |
| Pin publication | Keep `PinSnapshot` reuse and direct JSON writing: this hot path avoids building a second feature-object tree, unlike the removed tree-to-string conversions. Existing tests cover reserved keys, duplicate properties, and escaping. |
| Images | Keep eager registration, SDF conversion, background rasterization, installed-generation tracking, and image-before-source publication. This is master's icon policy, not a workaround. Fixed resource images use declarative `image(painterResource(...))`. |
| Expressions | Remove unchecked casts for localized names and house numbers. Use string conversion with defined missing-value behavior. Keep typed filters, zoom interpolation, and StreetComplete's meter/latitude conversion. |
| Location providers | Foreground heading and location collection now stop below STARTED. Keep direct location events because survey checking must consume every fix, rather than observe conflated `LocationState`. AutoSyncer retains its separately scoped, lower-frequency location request. Tutorial permission requests remain explicit. |
| Offline data | Keep the runtime-owned manager, pixel ratio, expiry metadata, and snapshot-state progress observation. Cache clearing now propagates cancellation instead of logging and swallowing it. |
| Scale bar and overlays | Keep the app's Material theme adapter over the upstream scale bar and its system measurement defaults. No platform map escape hatch is used. |

The sweep also removes redundant `distinctUntilChanged` after `snapshotFlow` and
keys location forwarding on the owning map state. Existing application behavior
is retained rather than replaced with generic defaults.

## Remaining API wart

### Stale-style error classification

`isStyleHandleRace()` still compares three `IllegalStateException` message strings
in cluster queries, image installation, and transition setup. The classifier
lives in `StyleLifecycle.kt`, separately from pin rendering. The release still throws these exact strings from `MapStyleState`,
`MapState.requireStyleBinding`, and `StyleBinding.requireCurrent`.

No source handle is retained across animation frames. An asynchronous cluster
query or image command can still be overtaken by style replacement before it completes.
StreetComplete must distinguish this expected lifecycle race from a programming
error without swallowing unrelated exceptions. An upstream typed stale/not-ready
exception or a typed operation result would remove this message matching.
`StyleHandleException` exists but does not classify these lifecycle checks.

This is an API ergonomics finding from source inspection, not a newly reproduced
0.16.0 failure. Invalidating old handles is correct; classifying that outcome by
exception message text is the awkward part.

## Retained application behavior and resolved findings

Eager image registration matches master. `DynamicStyleImageRegistry` and
`rememberImageBackedGeoJsonSource` preserve that policy, including image-before-source
ordering and replay on style reload; they are not migration workarounds.

The physical-iPhone jank was resolved with the preceding snapshots, as confirmed
by the user on 2026-09-09. The release update does not reopen that finding merely
because no new physical-device run was performed.

Desktop Java 25 is the supported baseline for a new target, not an iOS-port gap
or a workaround to remove. Desktop architecture availability is outside this
migration's remaining-wart list.

## Previously resolved integration findings

The following records explain why the older source, gesture, click, and layer
workarounds were removed. All referenced fixes are included in 0.16.0.

### Typed handles for remembered sources

MapLibre Compose PR
[#1324](https://github.com/maplibre/maplibre-compose/pull/1324) lets
`MapStyleState.sources` resolve a remembered `Source` directly. Cluster-leaf
lookup now uses `mapState.style.sources[source]`; StreetComplete no longer owns
fixed source IDs or a generation-bound GeoJSON update helper.

### Native image copies and dynamic installation

MapLibre Compose PR
[#1291](https://github.com/maplibre/maplibre-compose/pull/1291) updates
native-ffi to `0.202609.0`, which contains the scoped pixel-copy repair measured
in native-ffi PR
[#685](https://github.com/maplibre/maplibre-native-ffi/pull/685). StreetComplete
eagerly supplies quest, selection, overlay, and marker icons through
`MapStyleState.images`. This matches master's bounded, known-working-set model
and keeps these images outside MapLibre Native's on-demand image cache. A
`StyleLoaded` event starts a fresh installed-ID generation, and image-backed
sources remain empty until their required images are present. Installation no
longer waits one display frame per image.

### Off-owner GeoJSON preparation

MapLibre Compose PR
[#1285](https://github.com/maplibre/maplibre-compose/pull/1285) moves native
GeoJSON preparation off the owner thread. All dynamic map sources now use
`rememberGeoJsonSource`. Image-backed sources start empty and publish changing
data through Compose after eager image installation. Their loaded handles cannot
mutate the definition because Compose owns these sources.

### Non-blocking declarative style revisions

MapLibre Compose PR
[#1313](https://github.com/maplibre/maplibre-compose/pull/1313) reconciles
per-frame style revisions without blocking the UI dispatcher. Pin and overlay
visibility, house-number visibility, track color, and selected-pin icon size are
again ordinary declarative layer properties. The application-owned layer-ID
lists, generation retries, and imperative property effects are gone.

### Layer hit padding

MapLibre Compose PR
[#1290](https://github.com/maplibre/maplibre-compose/pull/1290) adds typed layer
hit padding. StreetComplete applies its 14dp finger radius to interactive
overlay fill, line, and symbol layers.

### Gesture-specific starts and parity controls

PRs [#1290](https://github.com/maplibre/maplibre-compose/pull/1290),
[#1318](https://github.com/maplibre/maplibre-compose/pull/1318), and
[#1323](https://github.com/maplibre/maplibre-compose/pull/1323) provide
component start callbacks and configurable bindings. The shared map now stops
location following only on pan, records other camera input without disabling
follow, and restores the legacy 5dp pan, 1.5 degree rotation, 8dp tilt,
250/500 pan-momentum, and rotate-during-zoom settings.

### Post-layer unhandled clicks

The interaction API from PR
[#1290](https://github.com/maplibre/maplibre-compose/pull/1290) provides
`click.onUnhandled`. Raw-map clicks now run after feature handlers decline the
event, without duplicating the interactive layer list or issuing a pre-query.

### Rejected: offline tile-count configuration

The source audit classified the missing common setter for StreetComplete's old
10,000-tile limit as an upstream gap. That premise was incorrect. MapLibre Native
applies the setting only to canonical-provider resources; StreetComplete's direct
Jawg URL never used it. StreetComplete PR
[#7069](https://github.com/streetcomplete/StreetComplete/pull/7069) removed the
ineffective legacy call. MapLibre Compose does not need an API for this migration.

## Resolved in the previously validated snapshot

These findings were already fixed in MapLibre Compose commit `c0e96909`, which
produced the last repository snapshot validated by this branch.

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
not cover the map. The release contains the commit. StreetComplete now uses
declarative GeoJSON updates without the old source-handle workaround.

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
