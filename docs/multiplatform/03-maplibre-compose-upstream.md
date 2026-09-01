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

## Integration constraints

- The current desktop runtime artifacts target Java 25. StreetComplete's future
  desktop distribution must package a Java 25 runtime even though its Kotlin JVM
  bytecode target remains 11.
- There is no published macOS x64 runtime. This does not block the current ARM64
  development host, but StreetComplete cannot claim macOS x64 support without an
  upstream runtime artifact.
