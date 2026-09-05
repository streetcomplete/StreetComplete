# PR 7068 source audit

This is a static audit of pull request 7068, `iOS / KMP migration probe`.

- Base: `643f462831d0e27949e33ec22787a41661326b75`
- Head: `1e5b8062c7e25cdd45c14dfa261b677959d1e50e`
- Diff: `git diff 643f462831d0e27949e33ec22787a41661326b75...1e5b8062c7e25cdd45c14dfa261b677959d1e50e`
- Method: source, diff, deleted-source parity, build configuration, tests, workflows, and the other files in this directory
- Excluded: building, running, device testing, and re-validating the commands or videos recorded in `04-validation.md`

This document preserves the findings at the pinned head above. See
`03-maplibre-compose-upstream.md` for the current upstream status and resolved
snapshot behavior.

Current scope note, 2026-09-05: desktop release packaging and custom iOS crash
reporting have been removed from the probe. The App Store identity and camera
purpose text have since been updated. These changes do not rewrite the historical
findings below. See `02-needs-work.md` for current device observations and pending work.

The audit uses four blocker classes:

- **Internal**: StreetComplete can implement or correct it in this repository.
- **MapLibre Compose**: preserving the behavior cleanly needs a missing or defective upstream API.
- **Product or platform**: the next step needs a product decision, identity, entitlement, external application, or target runtime.
- **Validation**: implementation may exist, but the claimed behavior has no proportionate static test or continuous gate.

## Summary

The migration is not source-complete under its own contract. The audit found four high-impact functional gaps, seven other product/parity gaps, and four validation/build gaps that are not recorded as remaining work. The strongest findings are:

1. expired offline packs are never deleted;
2. raw map clicks are swallowed for overlay features whose handlers deliberately decline the click;
3. iOS cannot receive the HTTPS configuration URLs StreetComplete generates;
4. packaged desktop builds have no operating-system URL ingress;
5. desktop and iOS have no continuous build/test gate.

The branch also has honest, well-localized compromises. Gesture parity, exact overlay hit radius, global style transitions, volatile GeoJSON hints, and parts of the fixed-source workaround are genuinely waiting on MapLibre Compose. iOS AR, App Store identity, the iOS 15 deployment claim, and unsupported desktop runtime hosts depend on product or upstream artifacts. Those known limitations are inventoried later rather than mixed with the undisclosed gaps.

## Undisclosed or incorrectly completed functionality

### High: expired offline packs are never deleted

[`MapLibreMapTilesDownloader`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/maptiles/MapLibreMapTilesDownloader.kt) writes a creation timestamp into pack metadata at lines 33–37, but its only pack deletion path is `clear()` at lines 24–30, which deletes every pack. [`Cleaner.cleanOld`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/Cleaner.kt) deletes old downloaded-tile database rows at lines 29–50 without deleting their corresponding MapLibre packs.

The deleted Android `MapFragment` did the missing work: base lines 96–102 waited until startup settled, computed `DELETE_OLD_DATA_AFTER`, and called `deleteRegionsOlderThan`. The deleted offline helper read each pack's metadata and deleted only expired packs.

**Impact:** after the 14-day database record and downloaded-area hatch disappear, the actual base-map pack remains on disk. Storage and backend tile-count usage can grow with every download.

**Blocker:** internal. The timestamp already exists, and the offline manager exposes pack enumeration, metadata, and deletion.

**Documentation:** contradicted by [`01-going-well.md`](01-going-well.md), which says timestamp metadata and cleanup were retained. The tests cited in [`04-validation.md`](04-validation.md) exercise downloaded-tile DAO retention, not offline-pack retention.

### High: the raw-click workaround swallows clicks that should fall through

[`MainMap`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/MainMap.kt) lines 92–112 suppress the raw `onClickMap` callback whenever a rendered feature exists in any ID at lines 200–208. That set includes `overlay-fills-outline` and all overlay symbols.

This is broader than the real layer dispatch contract. [`overlayClickHandler`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/layers/StyleableOverlayLayers.kt) returns `Pass` for disabled or keyless features at lines 288–300. [`StyledElement`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/layers/StyledElement.kt) intentionally creates keyless polyline-label symbols at lines 116–125 so the click can fall through to the underlying line. The deleted Android overlay query excluded `overlay-fills-outline` and returned `false` for disabled or keyless hits.

**Impact:** tapping a disabled overlay element, a keyless polyline label, or an area outline can do nothing. The layer declines the click, but the independent pre-query has already suppressed the raw map/form callback.

**Blocker:** the concrete bug is internal: filter the pre-query with the same consumability rules as the layer handlers. Removing the duplicated dispatch logic is separately blocked on MapLibre Compose exposing a post-layer/unhandled-click callback.

**Documentation:** `01-going-well.md` and `02-needs-work.md` describe the pre-query as preserving fallback behavior. `03-maplibre-compose-upstream.md` records the upstream API gap but not this local semantic break.

### High: iOS cannot receive the configuration URLs the app generates

[`createConfigUrl`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/urlconfig/UrlConfig.kt) always emits `https://streetcomplete.app/s?...` at lines 19 and 82–128. [`Info.plist`](../../iosApp/iosApp/Info.plist) registers only the `streetcomplete` and `geo` custom schemes at lines 18–31. The iOS project has no Associated Domains entitlement for `applinks:streetcomplete.app`.

**Impact:** a normally generated, shared, tapped, or scanned configuration URL does not enter `IncomingUriHandler` on iOS. The parser supports `streetcomplete://s?...`, but the generator never produces it.

**Blocker:** integration choice plus internal wiring. Either add the Associated Domains entitlement and the website's AASA declaration, or generate/use a target-appropriate custom-scheme fallback. This is unrelated to MapLibre.

**Documentation:** `01-going-well.md` broadly says StreetComplete/configuration URLs enter the common ingress on every target. `04-validation.md` validates only `geo:` delivery on iOS.

### High: packaged desktop has no operating-system URL ingress

[`DesktopMain`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/DesktopMain.kt) consumes only `args.firstOrNull()` at lines 20–27. The desktop package block in [`app/build.gradle.kts`](../../app/build.gradle.kts) lines 377–400 declares formats, resources, and a bundle ID, but no URL/file association or running-instance handoff.

**Impact:** the recorded desktop `geo:` behavior works only when a caller manually supplies a command-line argument. Clicking a `geo:`, `streetcomplete:`, or HTTPS configuration link does not route into the packaged app. A second invocation also has no path to hand a URL to an already-running process.

**Blocker:** internal per-OS packaging and instance-routing work.

**Documentation:** `01-going-well.md` overgeneralizes desktop URL ingress. The narrower entry in `04-validation.md` correctly proves only an executable argument.

### Medium: the legacy 10,000-tile offline limit is no longer configured

[`MapLibreMapTilesDownloader`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/maptiles/MapLibreMapTilesDownloader.kt) lines 22–66 handles a `TileLimitExceeded` result but never configures StreetComplete's limit. The deleted `MapFragment` base lines 91–94 called `setOfflineMapboxTileCountLimit(10000)` before using the offline manager.

**Impact:** downloads accepted by StreetComplete's 12 km² planning rule can fail against a lower backend default. Indefinitely retained old packs make that failure more likely.

**Blocker:** MapLibre Compose. The reviewed common `OfflineManager` can report `TileLimitExceeded` but exposes no tile-count-limit setter. Restoring the old limit cleanly needs a common upstream API; a target-specific native escape hatch would undermine the shared downloader.

**Documentation:** absent from the parity inventory.

### Medium: desktop and iOS language selection is applied after strings are read

[`PreferenceAwareAppTheme`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/ui/theme/PreferenceAwareAppTheme.kt) invokes the platform language effect and then immediately composes keyed resource consumers at lines 34–44. The desktop actual changes `Locale.setDefault` inside a `DisposableEffect` at [`PreferenceAwareAppTheme.desktop.kt`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/ui/theme/PreferenceAwareAppTheme.desktop.kt) lines 15–20. The iOS actual changes `AppleLanguages` inside a `DisposableEffect` at [`PreferenceAwareAppTheme.ios.kt`](../../app/src/iosMain/kotlin/de/westnordost/streetcomplete/ui/theme/PreferenceAwareAppTheme.ios.kt) lines 18–25.

Compose runs those effects after the composition that reads resources, and neither platform mutation publishes state that guarantees another resource pass.

**Impact:** an initial persisted language or in-app language change can leave the current desktop/iOS UI in the previous language until an unrelated recomposition or relaunch.

**Blocker:** internal composition/lifecycle ordering.

**Documentation:** `01-going-well.md` says the desktop JVM locale and iOS Apple language domain are applied, without disclosing that the current resource composition is not ordered after the mutation.

### Medium: desktop has no image-attachment path

[`PhotosUtils.desktop.kt`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/ui/util/photo/PhotosUtils.desktop.kt) lines 8–21 reports no camera and returns a cancellation-like `null`. [`NoteForm`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/quests/note_comments/NoteForm.kt) lines 63 and 124–138 exposes only the camera action; it has no gallery or file-picker action.

**Impact:** desktop users cannot attach an existing image to a note. The source comment that gallery/file picking remains available is false.

**Blocker:** desktop camera capture is an honest platform difference. Existing-image selection is internal UI/adapter work and does not need to wait for FileKit camera capture.

**Documentation:** `02-needs-work.md` misclassifies the whole missing capability as a FileKit camera blocker.

### Medium: localized iOS camera privacy text is an action label, not a purpose explanation

The default `NSCameraUsageDescription` in [`Info.plist`](../../iosApp/iosApp/Info.plist) lines 43–46 explains the need for access. The localized values in [`InfoPlist.xcstrings`](../../iosApp/iosApp/InfoPlist.xcstrings), such as German `Foto anhängen` around lines 338–345, are recycled UI labels that merely say “attach photo.”

**Impact:** non-default locales receive a privacy prompt that does not explain why StreetComplete needs camera access. This weakens consent UX and is an App Review risk.

**Blocker:** internal localization/content work.

**Documentation:** `04-validation.md` lines 491–494 explicitly call these localized purpose strings accurate and cite the German action label as evidence.

### Medium: desktop connectivity treats any active interface as Internet

[`DesktopActiveNetworkConnection`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/data/connection/DesktopActiveNetworkConnection.kt) lines 31–45 reports `hasInternet=true` whenever any non-loopback, non-virtual interface is up, always reports unmetered, and converts every inspection failure to offline.

**Impact:** a LAN-only, captive, or non-routing interface can enable sync as if Internet were available. VPN and metered semantics can also be wrong.

**Blocker:** the JVM lacks a portable metered/reachability API, but OS-specific adapters or an explicit reachability probe are internal options. This is a soft portability constraint, not a hard blocker.

**Documentation:** `02-needs-work.md` discloses only the unmetered assumption; `01-going-well.md` calls this real network observation without the reachability caveat.

### Medium: desktop `mailto:` construction uses form encoding

[`DesktopExternalAppLaunchers.kt`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/screens/main/DesktopExternalAppLaunchers.kt) lines 24–36 and 54 applies `URLEncoder` to the address, subject, and body. Form encoding turns spaces into `+`, while `mailto:` URI components need URI percent encoding.

**Impact:** the standard error-report subject and multiline report body can reach desktop mail clients with literal plus signs or otherwise malformed address/query semantics.

**Blocker:** internal adapter bug. The Foundation-based iOS implementation already demonstrates the required component encoding.

**Documentation:** desktop mail launching is listed as functional, but there is no desktop URL-semantics test comparable to the iOS check.

### Low/medium: desktop audio failures escape into ordinary actions

[`DesktopSoundEffectPlayer`](../../app/src/desktopMain/kotlin/de/westnordost/streetcomplete/util/sound/DesktopSoundEffectPlayer.kt) lines 14–18 synchronously loads and starts a Java Sound `Clip`; lines 27–35 rethrow missing-resource, decode, mixer, and line failures. Callers such as solved-quest animation do not contain those failures.

**Impact:** a machine without a usable Java Sound line can turn a routine quest sound into an uncaught application failure instead of disabling sound.

**Blocker:** internal adapter hardening.

**Documentation:** undisclosed; WAV playback is listed as a completed desktop service.

## Validation and delivery gaps

### High: the new target matrix is absent from CI

[`unit-test.yml`](../../.github/workflows/unit-test.yml) lines 9–24 still runs only `:app:testAndroidHostTest` on Ubuntu/JDK 21. [`build-debug-apk.yml`](../../.github/workflows/build-debug-apk.yml) lines 9–31 is Android-only and manual. No workflow compiles or tests desktop or iOS.

**Impact:** Kotlin/Native, Apple framework/link, desktop compilation/packaging, source-set wiring, and target-specific tests can regress while every continuous check stays green.

**Blocker:** internal CI work. iOS needs a macOS job; desktop compilation/tests can be gated on an ordinary supported runner.

**Documentation:** not listed in `02-needs-work.md`. A one-time local matrix in `04-validation.md` is evidence for the pinned revision, not an ongoing safeguard.

### Medium: Java 25 desktop packaging is documented but not enforced

[`app/build.gradle.kts`](../../app/build.gradle.kts) lines 160–163 targets JVM 11 bytecode, while lines 377–400 package with the JDK that happens to launch Gradle. There is no Java 25 toolchain, launcher, vendor, or version check. Existing CI selects JDK 21.

**Impact:** desktop package reproducibility depends on developer environment even though `03-maplibre-compose-upstream.md` says the runtime artifacts require a packaged Java 25 runtime.

**Blocker:** the upstream runtime establishes the Java requirement; enforcing it is internal build work.

**Documentation:** the requirement is recorded only as evidence/integration context, not as unfinished build wiring.

### Medium: iOS background-sync policy has no deterministic test

[`IosBackgroundSyncController`](../../app/src/iosMain/kotlin/de/westnordost/streetcomplete/data/sync/IosBackgroundSyncController.kt) lines 37–91 implements login, autosync `ON`/`WIFI`/`OFF`, a five-second network wait, unsynced-count gating, upload, changeset closure, cancellation, success aggregation, and completion callback behavior. No iOS or common test references the controller.

`04-validation.md` proves framework linkage, declarations, registration, and an expected simulator scheduling failure. It explicitly does not prove task execution.

**Impact:** the main background-processing contract can regress while all reported tests pass.

**Blocker:** physical background-task scheduling is device validation. Unit-testing the controller policy is internal and not device-blocked.

**Documentation:** `02-needs-work.md` marks the behavior complete more strongly than the evidence supports.

### Medium: offline download tests cover only definition construction

The production lifecycle in [`MapLibreMapTilesDownloader`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/data/maptiles/MapLibreMapTilesDownloader.kt) lines 24–65 includes pack deletion, create/resume, terminal progress, backend error handling, and pause on cancellation/failure. [`MapLibreMapTilesDownloaderTest`](../../app/src/commonTest/kotlin/de/westnordost/streetcomplete/data/maptiles/MapLibreMapTilesDownloaderTest.kt) lines 7–21 tests only the pure `OfflinePackDefinition` fields.

**Impact:** cancellation, cleanup, terminal-state handling, failure propagation, the tile limit, and expiry can all regress without the cited test failing. The missing expiry and tile-limit behavior demonstrate that this is not theoretical.

**Blocker:** internal testability/integration work. A narrow offline-manager seam can make the state machine deterministic; live runtime validation remains a separate gate.

**Documentation:** `04-validation.md` lines 546–561 says cancellation, error handling, and cleanup are claimed from shared contract tests, but the only contract test does not execute them. The representative product videos also omit download/deletion.

## Known compromises and what they are actually blocked on

| Remaining behavior | Source evidence | Classification | Audit assessment |
| --- | --- | --- | --- |
| Exact pan-begin detection | `MainMapState.kt:424-439` | MapLibre Compose | Honest blocker. Aggregate `GESTURE` cannot distinguish pan from zoom/rotate/tilt. |
| Legacy gesture thresholds, fling, and rotate-while-scaling interlock | `MainMap.kt:48-50` | MapLibre Compose | Honest blocker. Common gesture options do not expose the required controls. |
| Post-layer raw-click callback | `MainMap.kt:92-112` | MapLibre Compose | Honest upstream gap, but it does not excuse the local fallthrough bug above. |
| Finger-radius overlay hit geometry | `StyleableOverlayLayers.kt:288-300` | MapLibre Compose or larger local dispatcher | Honest API gap. Thin lines remain harder to select. |
| StreetComplete's 10,000-tile offline limit | `MapLibreMapTilesDownloader.kt:22-66` | MapLibre Compose | Newly identified upstream gap. The common manager reports limit failures but cannot configure the limit. |
| 300 ms, system-scale-aware global style transition | `StreetCompleteMap.kt:42-44` | MapLibre Compose | Honest blocker. Backend defaults do not preserve reduced-animation semantics. |
| Volatile GeoJSON hints | eight layer files listed below | MapLibre Compose | Visual behavior is wired; the missing cache/performance hint is honest. |
| Stable dynamic source IDs and Android surface-loss workaround | `PinsLayers.kt:86-99`; `StyleableOverlayLayers.kt:73-85` | MapLibre Compose | Functionality is wired but depends on imperative handles, generation retries, and exact stale-handle exceptions. |
| Core Location main-thread diagnostic | `IosModule.kt:123-125` | MapLibre Compose location provider | Honest measured upstream defect, not a launch blocker. |
| iOS AR measurement | iOS `ArSupportChecker` and launcher TODOs | External StreetMeasure product/protocol | Honest blocker; the action is hidden rather than represented as working. |
| iOS App Store rating destination | `IosAppStoreInfo.kt:3-8` | Product identity | Honest blocker; `null` is safer than a crashing placeholder. |
| iOS 15 support claim | `02-needs-work.md:96-99` | Skiko artifact plus device/runtime validation | Honest release blocker. The successful iOS 26.5 simulator result is not iOS 15 evidence. |
| Desktop native share sheet | `FileShareLauncher.desktop.kt:11-21` | Portable API or OS-specific implementation | Soft blocker. Opening the file is disclosed degraded behavior; narrow OS adapters remain possible. |
| Unsupported desktop runtime hosts, including macOS x64 | `app/build.gradle.kts:14-32` | MapLibre Compose runtime publication | Honest artifact gap. The current build should still fail explicitly instead of silently omitting a package. |
| Desktop camera capture | `PhotosUtils.desktop.kt:8-21` | FileKit or OS-specific implementation | Honest for camera capture only, not for selecting an existing image. |
| Desktop metered status | `DesktopActiveNetworkConnection.kt:31-45` | Host APIs or explicit probe | Soft portability constraint, not an absolute blocker. |
| iOS background map download | `IosBackgroundSyncController.kt:22-27` | Product semantics and possibly Apple entitlement | Fresh suspended-time location needs an approved background-location mode. A persisted, age-bounded last foreground vicinity is an internal alternative if product semantics allow it. |

The volatile-source TODO affects all eight legacy categories:

- downloaded area;
- recorded tracks;
- focused geometry;
- geometry markers;
- selected pins;
- current location;
- clustered pins;
- styleable overlay.

`03-maplibre-compose-upstream.md` lists all eight. `02-needs-work.md` omits focused geometry, geometry markers, and selected pins, then incorrectly says every exact API gap appears above its completion statement.

## Standards axis

These are maintainability findings, not missing product behavior.

### Documented standard: avoidable view-model inheritance

[`MainMapViewModel.kt`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/MainMapViewModel.kt) lines 17–55 introduces an abstract base whose implementation mostly forwards four flows and three methods to owned sources. `CONTRIBUTING.md` says to avoid inheritance/class hierarchies and prefer helper extraction. A concrete view model can own the sources directly.

### Judgement call: duplicated viewport publication state machines

[`MapQuestPinsSource`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/sources/MapQuestPinsSource.kt) and [`StyleableOverlaySource`](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/main/map/sources/StyleableOverlaySource.kt) independently implement the same zoom-16 tile gate, 32-tile cap, child-job replacement, locking, generation guard, clearing, and close machinery. The final hardening commits had to change both paths. A non-inheritance helper/coordinator would reduce cancellation drift.

## Evidence-document corrections needed

The existing ledger should be corrected even if the probe branch remains intentionally disposable:

1. Add offline-pack expiry, the 10,000-tile limit, raw-click fallthrough, iOS generated config-link ingress, desktop OS link ingress, language ordering, desktop image attachment, and target CI as open internal work.
2. Replace the offline downloader's “shared contract tests” claim with the narrower definition-only coverage actually present.
3. Mark iOS background task execution as unvalidated and separately add deterministic controller tests.
4. Correct the localized camera-purpose claim and replace the recycled action labels.
5. Expand desktop connectivity disclosure from metering to reachability and error semantics.
6. List all eight volatile GeoJSON source categories in `02-needs-work.md`.
7. Weaken `README.md`'s statement that every remaining limitation has an actionable TODO. The reachability shortcut, iOS 15 artifact/device gate, CI, and several evidence gaps do not.

## Static-audit limit

This document identifies source-visible omissions, mismatches, and unproven contracts. It does not establish runtime severity on any target. In particular, language-resource refresh, desktop mail-client interpretation, Java Sound failure behavior, URL association behavior after future packaging changes, and the exact performance effect of non-volatile sources still need focused target validation after implementation.
