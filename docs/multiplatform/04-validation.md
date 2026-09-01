# Validation evidence

Validation claims in this file distinguish compilation, automated tests, and
interactive runtime evidence. A compile result is not treated as proof of feature
parity.

## Baseline: upstream master at `c778f48b1`

Environment: macOS 26.6.2 ARM64, Temurin 25.0.4, Gradle 9.7.1.

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Common | `./gradlew :app:compileCommonMainKotlinMetadata` | Pass | Shared source compiles as metadata. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | Existing Android library source compiles. |
| Android app | `./gradlew :androidApp:assembleDebug` | Pass | Existing Android debug APK assembles. |
| iOS simulator | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | Existing iOS source compiles; the product app is still only a temporary launcher. |
| Desktop | n/a | Missing | No desktop target or application exists at baseline. |

The baseline Gradle configuration also warns that `androidUnitTest` is declared
but not attached to a compilation. This must be corrected before final validation.

## JVM foundation

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Common UI and desktop/JVM actuals compile. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | Moving Java actuals to `javaMain` preserves Android compilation. |
| Android app | `./gradlew :androidApp:assembleDebug` | Pass | The existing Android APK still assembles. |
| iOS simulator | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | Explicit custom hierarchy retains the Native and iOS actuals. |
| Desktop tests | `./gradlew :app:desktopTest` | 2,443 pass, 6 fail, 1 skip | The full common suite executes on desktop; six locale/date-sensitive failures match Android host results. |
| Android host tests | `./gradlew :app:testAndroidHostTest` | 2,443 pass, 6 fail, 1 skip | Existing host-test failures are reproduced independently of the new target. |

The desktop runner initially exposed two real portability issues: an Android-only
Koin navigation artifact was declared in `commonMain`, and FileKit camera/share
APIs were referenced directly from common code despite having no JVM variants.
Both dependency-shape problems are fixed in this layer.

## iOS sync foundation

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.data.sync.CoroutineSyncControllersTest'` | 4 pass | Unique upload work, automatic-download retention, user-download replacement, and delayed changeset rescheduling behave as designed. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The real common Koin graph, coroutine controllers, and Apple Network framework monitor compile and link together for iOS. |
| iOS simulator tests | `./gradlew :app:compileTestKotlinIosSimulatorArm64` | Blocked by inherited tests | The new controller tests are common code, but the existing suite has one JVM `Thread` use and several test names that Kotlin/Native rejects before tests can link. |

Compilation does not prove Apple background execution. The current iOS sync
controllers run in the application process; registering and exercising
`BGProcessingTask` handlers remains explicit follow-up work.

## MapLibre Compose snapshot foundation

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Shared source compiles against the snapshot's JVM APIs. |
| Desktop location tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.data.location.RecentLocationsTest'` | 7 pass | Recent-location ordering, spacing, and expiry use real measurement instants. |
| Desktop runtime | `./gradlew :app:dependencyInsight --dependency maplibre-compose-runtime-metal-macos-arm64 --configuration desktopRuntimeClasspath` | Pass | macOS ARM64 resolves snapshot build `0.15.1-20260831.102040-6` and its Metal runtime. |
| Android app | `./gradlew :androidApp:assembleDebug` | Pass | The legacy Android map and new Compose OpenGL runtime can coexist in one transition APK. |
| Android runtime | `./gradlew :app:dependencyInsight --dependency maplibre-compose-runtime-opengl-android --configuration androidRuntimeClasspath` | Pass | Android resolves snapshot build `0.15.1-20260831.102040-6` as an AAR runtime. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The snapshot's iOS API and transitive Metal native archive link into StreetComplete. |
| Desktop and Android host tests | `./gradlew :app:desktopTest :app:testAndroidHostTest --continue` | 2,453 tests on each runner; same 6 fail and 1 skip | The four new sync tests run and the complete inherited suite has no failures beyond the baseline locale/date set. |

These checks validate dependency integration and the location/heading API
migration. They do not yet validate rendering; the shared map composition has
not been introduced in this layer.

## Shared base map

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The complete declarative base style and shared map shell compile against the snapshot API. |
| Shared resources | `./gradlew :app:verifySharedMapGlyphResources` | Pass | The source set contains exactly 512 expected, non-empty Roboto glyph ranges. |
| Desktop resource URI | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.MapStyleResourceUriTest'` | 2 pass | Percent-encoded desktop/iOS and Android asset file URIs retain MapLibre's font-stack and range placeholders. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared map source compiles while the production legacy Android map remains present. |
| Android app resources | `./gradlew :androidApp:verifyDebugMapGlyphAssets` | Pass | The transition APK assembles and contains exactly 512 non-empty shared glyph entries in addition to the legacy map assets. |
| Android host resource URI | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.MapStyleResourceUriTest'` | 2 pass | Android retains the `file:///android_asset/` URI that the snapshot's resource reader handles. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The shared base-map code and Metal runtime compile and link into the framework; this does not prove resources are copied into an iOS app bundle. |
| Desktop and Android host suites | `./gradlew :app:desktopTest :app:testAndroidHostTest --continue` | 2,455 tests per runner; same 6 baseline failures and 1 skip | Both complete runners reproduced only the inherited locale/date-sensitive baseline failures. No failure was added by the shared base-map layer. |

The layer has not replaced the running Android map or the temporary iOS
launcher, so these are composition, packaging, and link checks—not rendering or
feature-parity evidence. Interactive evidence will be recorded only after the
shared map is connected to target entry points.

## Downloaded-area map layer

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayerTest'` | 2 pass | The world mask and clockwise tile holes preserve the legacy geometry. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayerTest'` | 2 pass | The same geometry behavior executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The hatch resource and declarative fill layer compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared layer compiles alongside the still-active legacy implementation. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The shared layer, geometry, and hatch resource API compile and link for iOS. |

## Recorded-track map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.TrackGeometryTest'` | 4 pass | Line filtering, coordinate order, and shortest-path antimeridian interpolation preserve the track contract. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.TrackGeometryTest'` | 4 pass | The same track geometry behavior executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Track sources, styles, animation, and image patterns compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared track layer compiles alongside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Track geometry, Compose animation, and resource APIs compile and link for iOS. |

## Focused-geometry map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.ElementGeometryConversionTest' --tests 'de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryStyleTest'` | 5 pass | Point/line/polygon conversion, multipolygon hole allocation, and highlight ranges match the legacy component. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.ElementGeometryConversionTest' --tests 'de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryStyleTest'` | 5 pass | The same conversion and style behavior executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Declarative focused-geometry sources, layers, and animation compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared focused-geometry layer compiles beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Shared geometry conversion and highlight animation compile and link for iOS. |

## Geometry-marker map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayersTest'` | 4 pass | Default/custom icon IDs, center symbols, labels, and geometry feature splitting match the legacy component. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayersTest'` | 4 pass | The same marker feature contract and generated resource lookup execute on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Dynamic painter registration, SDF coloring, labels, and geometry layers compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared marker layers compile beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Marker resource lookup, image registration, and layers compile and link for iOS. |

## Selected-pin map layer

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayerTest'` | 2 pass | Selected positions become point features and the 300ms easing matches Android's default overshoot interpolator. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayerTest'` | 2 pass | The same point and animation contract executes on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Shared pin composition, painter registration, animation, and symbol placement compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared selected-pin layer compiles beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Shared pin resources, painter composition, and animation compile and link for iOS. |

## Current-location map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.CurrentLocationAnimationTest'` | 4 pass | Antimeridian movement, shortest-turn bearing normalization, and latitude-aware meter scaling preserve or improve the legacy behavior. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.CurrentLocationAnimationTest'` | 4 pass | The same motion and scaling contract executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Shared accuracy, bearing, shadow, dot, April 1 artwork, and animations compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared current-location layers compile beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Shared current-location artwork, expressions, and animations compile and link for iOS. |

The legacy Android evaluator can jump the long way around the globe when a
measurement crosses the antimeridian. The shared animation intentionally uses
the shortest longitude delta, consistent with the already-migrated track layer.

## Clustered pin map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.PinsLayersTest'` | 3 pass | Pin point geometry, drawable IDs, sort keys, caller properties, and click-property decoding retain the legacy data contract. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.PinsLayersTest'` | 3 pass | The same pin feature contract and generated drawable lookup execute on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Clustering, painter selection, exact collision padding, visibility, pin clicks, and cluster-handle queries compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared clustered-pin layer compiles beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Shared clustering, painter expressions, and generation-bound source-handle APIs compile and link for iOS. |

The layer returns every cluster leaf position to its caller, which preserves the
input needed by Android's existing fit-to-cluster camera behavior. The shared
camera controller and live rendering are not yet wired, so these checks do not
claim interactive camera parity.
