# Compose Multiplatform migration (#7097)

Integration baseline and implementation notes, 2026-09-15. The baseline audit and implementation validation are recorded separately below.

## Integration baseline

Branch: `sargunv/compose-multiplatform`, based on upstream master `e996a7fb4`.

| Included PR | Head | Local merge |
| --- | --- | --- |
| #7088: Complete MapLibre Compose migration (includes #6352) | `ce570b799` | `e7097d270` |
| #7123: iOS download/upload controllers | `3801986b9` | `0472028b0` |
| #7124: iOS periodic cleanup | `8e52fbdd4` | `0b22aedf7` |

The experimental #7068 probe is excluded. All three heads, current upstream master, and upstream `maplibre-compose` are ancestors of this baseline. Nothing has been pushed.

Merge resolutions preserve master's move of AutoSyncer/FeedsUpdater/PeriodicCleaner ownership to MainViewModel, #7088's direct location updates and offline-map cleanup, and #7124's cancellable cleanup and singleton iOS scheduler. The removed iOS map-tile stub stays removed.

Validation on this merged baseline passed with JDK 21 and `TZ=UTC`:

```text
./gradlew :app:testAndroidHostTest :androidApp:compileDebugKotlin :app:compileKotlinIosSimulatorArm64
```

Android host results: 2,448 tests reported, zero failures/errors, one skipped. Android app and iOS simulator Kotlin compilation passed. No app launch, iOS framework link, device acceptance, or release/R8 validation was performed in this audit.

## Application shell implementation

The remaining shell work is now implemented on this branch:

- `App` observes theme, language, and keep-awake preferences through `AppViewModel`.
- `MainNavHost` owns one navigation stack for the map, settings, about/logs, and login/profile. Main remains the root; log filters retain their parent graph ViewModel. Full in-memory tracks survive navigation, while process-death state remains bounded.
- Android uses one `ComponentActivity`. Incoming map/configuration links and `ACTION_MANAGE_NETWORK_USAGE` reach the shared navigation host. Saved pending requests are consumed once.
- iOS mounts the same production app and forwards SwiftUI URL events to it.
- Platform environment adapters apply locale, layout direction, native appearance, and keep-awake changes without recreating navigation. Retained formatters and dictionary language lists refresh with the language preference. System-default formatting preserves native defaults on iOS.
- Map colors follow the selected app theme. Name/location labels refresh with the active Compose resource environment.
- Obsolete activities, the iOS screen launcher, unused Android helpers/colors, and direct AppCompat/Material Components dependencies are removed. Android retains native launch/window themes and required platform integrations.
- Stale Gradle references to nonexistent app/test ProGuard files are removed; optimized Android default rules remain.

The obsolete `CopyStringsTask` was already removed in #7088. The useful `copyDefaultStringsToEnStrings` translation task remains.

Source entry points: [App](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/App.kt), [MainNavHost](../../app/src/commonMain/kotlin/de/westnordost/streetcomplete/screens/MainNavHost.kt), [MainActivity](../../androidApp/src/main/kotlin/de/westnordost/streetcomplete/screens/main/MainActivity.kt), and [MainViewController](../../app/src/iosMain/kotlin/de/westnordost/streetcomplete/MainViewController.kt).

## Validation

Validation uses JDK 21 and `TZ=UTC`. `AppViewModelTest` covers live preference changes, listener cleanup, pending request restoration/consumption, and repeated incoming URLs after consumption.

Final implementation checks passed:

```text
./gradlew :app:testAndroidHostTest :androidApp:assembleDebug \
  :androidApp:minifyReleaseWithR8 :androidApp:lintVitalRelease \
  :app:compileKotlinIosSimulatorArm64
```

Host results: 2,450 tests reported, zero failures/errors, one skipped. Android debug packaging, release R8, release vital lint, and iOS simulator Kotlin compilation pass. R8 reports the existing xmlutil serialization service warnings. The complete iOS simulator app also links and builds through Xcode.

Runtime checks on an Android API 34 emulator and an iPhone 17 / iOS 26.5 simulator cover the production map launch, settings navigation, immediate dark-theme and German-language changes, switching back to system language, and Arabic RTL layout. Android additionally covers incoming `geo:` links while settings is open, `ACTION_MANAGE_NETWORK_USAGE`, reactive keep-awake window flags, and about/logs/filter Back navigation. iOS also covers switching back to the system theme and opening the OSM authorization WebView, then returning to the map. Android restores the language-selection destination and preferences after backgrounding, killing the process, and reopening its task.

### Release blocker found during minimum-SDK validation

On API 25, the app installs and launches, but granting location permission crashes MapLibre Compose 0.17.0's `AndroidLocationProvider` with:

```text
java.lang.AbstractMethodError:
  android.location.LocationListener.onStatusChanged(String, int, Bundle)
```

This dependency defect is fixed upstream by [MapLibre Compose #1418](https://github.com/maplibre/maplibre-compose/pull/1418), commit `57fb154a7`. Maven Central still lists 0.17.0 as the latest published location artifact at validation time. Consume a release containing that fix and repeat the API 25 location/map check before shipping. The shared shell does not implement or replace this dependency's location listener.

### Remaining acceptance coverage

No physical-device or authenticated OSM login/upload validation was performed. The iOS URL bridge compiles, but runtime URL dispatch was not verified: the simulator has another StreetComplete app registered for the same schemes and dispatched the test URL to that app. No other installed app was removed. Release signing requires a local keystore and is not configured in this checkout.

This completes the migration's application shell; separate iOS-port issues, such as external AR measurement availability, remain outside this change.
