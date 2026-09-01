# What still needs work

## Target and entry-point parity

- [ ] Add a JVM desktop target and a Compose Desktop application.
- [ ] Replace the temporary iOS launcher, which exposes only Changelog, Credits,
  and Privacy Statement, with the real shared app entry point.
- [ ] Replace Android activity-to-activity navigation with the same shared
  navigation graph used by desktop and iOS.

## Map parity

- [ ] Move the Android-only `Fragment` and `org.maplibre.android` map stack to a
  shared MapLibre Compose implementation.
- [ ] Preserve every current map data source, layer, selection flow, camera
  behavior, gesture, offline-area visualization, quest pin interaction, edit
  history pin, track, overlay, and location behavior.
- [ ] Use the current post-v0.15 snapshot if it remains buildable. The snapshot
  repository currently publishes `org.maplibre.compose:maplibre-compose:0.15.1-SNAPSHOT`.

## Platform services

- [ ] Implement the existing iOS TODOs for upload scheduling, download scheduling,
  changeset auto-closing, active-network inspection, and App Store identity.
- [ ] Audit iOS behavior that compiles but is still placeholder-level, including
  email launching, background lifecycle, crash handling, and application startup.
- [ ] Add real desktop implementations for storage paths, settings, database,
  HTTP, location, external-app launching, sound, connectivity, background work,
  and platform formatting.

## Completeness safeguards

- [ ] Inventory Android-only production classes before deleting them and map each
  one to a shared or platform implementation.
- [ ] Run the common test suite and target-specific tests on every target.
- [ ] Exercise production flows on Android, desktop, and iOS and record demo videos.
- [ ] Obtain independent adversarial reviews of functionality parity, architecture,
  commit structure, and target evidence; fix all confirmed findings.
