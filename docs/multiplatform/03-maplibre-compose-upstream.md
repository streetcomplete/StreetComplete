# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

The upstream audit was refreshed on 2026-09-07 against exact MapLibre Compose
`main` commit `9717fc6f`. The application cleanup was compiled against a local
publication of that commit, now available from the normal snapshot repository.
The subsequent iPhone image fix was validated against local `9cd93ad`; its normal
snapshot publication is still pending. Historical timing measurements
remain evidence for the old workarounds, not coverage of current real-device
jank. The last pre-cleanup performance harness is available at
`3be8406d6b0126781061aa68b766a4477ab76752`.

## Dependency baseline

- Dependency version: `0.15.1-SNAPSHOT`.
- Last repository publication validated on 2026-09-05:
  `0.15.1-20260904.102255-10`.
- The resolved publication was built from MapLibre Compose commit `71c5b258` by
  [the September 4 daily run](https://github.com/maplibre/maplibre-compose/actions/runs/33862529720).
- The newest repository publication observed on 2026-09-08 is
  `0.15.1-20260907.101928-13`, built from `9717fc6f` by
  [the September 7 daily run](https://github.com/maplibre/maplibre-compose/actions/runs/34110763852).
  It contains the API cleanup baseline, but predates the missing-image fix
  `9cd93ad` used for the successful physical-iPhone validation.
- Latest MapLibre Compose `main` audited on 2026-09-07: `9717fc6f`.
- Latest physical-iPhone validation coordinate:
  `0.15.1-local.9cd93ad-SNAPSHOT`. This is a local Maven publication only and
  is not a dependency that the branch will commit.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL, macOS ARM64 Metal, and Linux/Windows Vulkan for
  x64 and ARM64.
- The desktop artifacts require Java 25; Android and iOS keep their existing
  platform bytecode and native deployment targets.
- This branch intentionally follows the mutable snapshot version so it can test
  new MapLibre Compose publications without maintaining a timestamped artifact
  manifest. Update the resolved publication above after validating a new build.

The September 7 cleanup also adopts `initialBaseStyle`, `Viewport.visibleBounds`,
the non-generic feature-state expression, and `LocationPermission.Unknown` from
the latest API. The ordinary map and offline downloader continue to share the
Koin-owned runtime.

## Remaining findings on latest main

No StreetComplete-specific MapLibre Compose API gap from the previous audits
remains at `9717fc6f`. Three integration gates remain:

### Publication and desktop artifact gates

- A normal `0.15.1-SNAPSHOT` publication containing `9717fc6f` is available.
  Publication of `9cd93ad`, used in the latest device validation, remains pending.
  The branch keeps its ordinary mutable snapshot coordinate without `mavenLocal()`.
- Desktop runtime artifacts still require Java 25.
- There is still no macOS x64 runtime artifact. The current ARM64 development
  host works, but the probe cannot claim macOS x64 support.

The physical-iPhone jank reported on 2026-09-04 also remains a validation finding.
Removing the measured workaround paths does not prove that the device issue is
gone; it needs a new device run after the upstream snapshot is available.

## Resolved on latest main, awaiting snapshot validation

The seven findings that were pending at `71c5b258` are resolved in locally
validated `9717fc6f`:

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
