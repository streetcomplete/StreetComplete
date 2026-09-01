# What still needs work

## Target and entry-point parity

- [x] Add a JVM desktop library target.
- [ ] Add a Compose Desktop application that enters the real shared app flow.
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
- [ ] Replace the desktop camera-launcher fallback if FileKit adds camera capture;
  desktop currently reports that no camera capture integration is available and
  therefore never presents the mobile-only camera action.
- [ ] Replace opening an exported log file with a native desktop share sheet when
  Compose Desktop exposes a portable share API.

## Completeness safeguards

- [ ] Inventory Android-only production classes before deleting them and map each
  one to a shared or platform implementation.
- [ ] Run the common test suite and target-specific tests on every target.
- [ ] Make six existing locale/date-sensitive common tests deterministic. Both the
  Android host and new desktop runner currently execute 2,449 tests with the same
  six failures on this machine.
- [ ] Exercise production flows on Android, desktop, and iOS and record demo videos.
- [ ] Obtain independent adversarial reviews of functionality parity, architecture,
  commit structure, and target evidence; fix all confirmed findings.
