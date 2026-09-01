# MapLibre Compose upstream findings

This file records StreetComplete integration findings that should be fixed or
improved in MapLibre Compose. Entries must include a reproducer or precise missing
API before they are considered actionable.

## Dependency baseline

- Latest stable release available locally: `0.15.0`.
- Latest published post-release snapshot observed on 2026-09-01:
  `0.15.1-SNAPSHOT`, build `0.15.1-20260831.102040-6`.
- The snapshot includes the shared map artifact and platform runtime artifacts,
  including Android OpenGL and macOS ARM64 Metal.

## Findings

No confirmed upstream defect has been recorded yet. The abandoned
`upstream/maplibre-compose` integration predates 0.15 and will be re-evaluated
against the snapshot APIs before any limitation is attributed upstream.

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

The legacy downloaded-area source explicitly sets `GeoJsonSource.isVolatile =
true` because its world mask changes whenever downloaded tiles change. The
snapshot's common `GeoJsonOptions` exposes tiling, clustering, line metrics, and
synchronous updates, but not MapLibre Native's volatile-source flag.

The shared layer still updates its GeoJSON data through `rememberGeoJsonSource`,
so the visualization is functional. What cannot currently be preserved is the
legacy cache/performance hint. MapLibre Compose should expose this as a common
GeoJSON source option on native-backed targets and document browser behavior.

## Integration constraints

- The current desktop runtime artifacts target Java 25. StreetComplete's future
  desktop distribution must package a Java 25 runtime even though its Kotlin JVM
  bytecode target remains 11.
- There is no published macOS x64 runtime. This does not block the current ARM64
  development host, but StreetComplete cannot claim macOS x64 support without an
  upstream runtime artifact.
