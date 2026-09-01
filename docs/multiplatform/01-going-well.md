# What is going well

## Baseline on 2026-09-01

- The `app` module is already a Kotlin Multiplatform library with Android,
  `iosArm64`, and `iosSimulatorArm64` targets.
- 1,237 Kotlin source files are already in `app/src/commonMain`; only 68 are in
  `androidMain` and 30 are in `iosMain`.
- The shared source set already contains the quest model, persistence, networking,
  most platform-independent services, and 151 Compose screen files.
- Compose resources, Koin, Ktor, kotlinx.serialization, kotlinx-datetime,
  multiplatform settings, and bundled SQLite are already shared dependencies.
- The clean baseline compiles common metadata, Android, and the iOS simulator
  framework. The Android debug application assembles successfully.
- A previous `upstream/maplibre-compose` branch contains useful exploratory work
  for shared map layers and sources. It is being used as design input, not treated
  as verified production code.

## JVM foundation

- The shared library now has a JVM desktop target.
- Android and desktop share their Java implementations of date, time, number,
  currency, locale, and access-ordered map behavior through `javaMain`.
- Shared photo capture and file sharing no longer expose APIs that exist only in
  FileKit's Android and iOS variants. Narrow platform launchers preserve mobile
  behavior while allowing the common UI to compile for desktop.
- Desktop compilation, Android compilation and APK assembly, and iOS simulator
  compilation all pass together after adding the target.

## iOS sync foundation

- iOS now registers the real `commonModule` dependency graph rather than a
  three-screen demonstration graph.
- Upload, download, and changeset auto-closing use tested shared coroutine
  controllers while the iOS process is alive. User downloads replace automatic
  downloads; repeated automatic work stays unique.
- Apple Network framework path monitoring now feeds the shared connectivity API,
  including satisfied-path and expensive-path state.
