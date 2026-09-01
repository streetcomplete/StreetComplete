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

## Styleable overlay map layers

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.StyledElementTest'` | 5 pass | Point, polygon, invisible, extrusion, side/center line, bridge, label, element-key, disabled, road-width, and color properties preserve the legacy feature contract. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests 'de.westnordost.streetcomplete.screens.main.map.layers.StyledElementTest'` | 5 pass | The same overlay conversion and generated drawable lookup execute on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | All four layer insertion groups, dynamic painters, styles, visibility, and exact-coordinate click handlers compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared styleable overlay layers compile beside the active legacy component. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Shared feature conversion, expressions, painters, source handles, and layers compile and link for iOS. |

MapLibre Compose's layer callback currently queries only the exact tap point, so
these checks do not claim parity with Android's finger-radius rendered-feature
query. That upstream API gap is documented and marked at the click handler.

## Quest-pin viewport source

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MapQuestPinsSourceTest'` | 4 pass | Zoom gating and viewport loading produce shared drawable pins, OSM and note quest keys round-trip through renderer properties, and malformed feature data is ignored safely. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MapQuestPinsSourceTest'` | 4 pass | The same viewport and click-key contracts execute on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The renderer-independent viewport cache, listeners, cancellation, and shared drawable pins compile for JVM. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common source compiles beside the active legacy quest manager during the transition. |
| iOS simulator library | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | The source and its concurrency primitives compile for Kotlin/Native. |

This commit migrates quest-pin data ownership, but does not claim live quest-pin
parity until the common map controller supplies its visible bounding box and
cluster camera behavior on all three targets.

## Edit-history pin source

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*EditHistoryPinsSourceTest'` | 3 pass | Initial history loading, deletion reloads, complete order reindexing, Compose drawable pins, all edit-key variants, and malformed-property handling work on JVM. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*EditHistoryPinsSourceTest'` | 3 pass | The same history and click-key contracts execute on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The listener-backed common source compiles for JVM. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common source compiles beside the active legacy history manager. |
| iOS simulator library | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | Listener ownership, coroutine reloads, edit models, and Compose resources compile for Kotlin/Native. |

The source deliberately reloads the whole edit history after additions,
deletions, or invalidation. This retains the legacy manager's ordering semantics
and avoids the stale-order bug in the older experimental incremental source.

## Styleable-overlay viewport source

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*StyleableOverlaySourceTest'` | 1 pass | A retained viewport stays dormant without an overlay, loads styled shared map data immediately after selection, and clears immediately after deselection. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*StyleableOverlaySourceTest'` | 1 pass | The same overlay-selection and viewport contract executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Viewport caching, map-data listeners, delta updates, cancellation, and shared styling compile for JVM. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common source compiles beside the active legacy overlay manager. |
| iOS simulator library | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | The common source, overlay model, synchronization, and styling compile for Kotlin/Native. |

The renderer's exact-coordinate click limitation remains separate from this data
source and is still tracked as a MapLibre Compose upstream gap.

## Downloaded-tile state source

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused test | `./gradlew :app:desktopTest --tests '*DownloadedTilesStateSourceTest'` | 1 pass | Initial and listener-triggered reloads publish tiles using the production 14-day retention cutoff. |
| Android host focused test | `./gradlew :app:testAndroidHostTest --tests '*DownloadedTilesStateSourceTest'` | 1 pass | The same retained-tile observation contract executes on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The common listener-backed state source compiles for JVM. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common source compiles beside the active Android downloaded-area manager. |
| iOS simulator library | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | The source compiles for Kotlin/Native. |

## Shared main-map data owner

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Koin constructs the common main-map view model and its downloaded-tile, quest-pin, edit-pin, and overlay sources for JVM. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The same graph compiles alongside the transition Android map. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The complete common data-owner graph and listener cleanup link into the iOS framework. |

This is the data boundary only. Camera ownership and live renderer composition
remain separate commits so their behavior can be reviewed independently.

## Shared main-map renderer

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused test | `./gradlew :app:desktopTest --tests '*MapBoundingBoxConversionTest'` | 1 pass | MapLibre longitude/latitude viewport bounds reach StreetComplete without an axis swap. |
| Android host focused test | `./gradlew :app:testAndroidHostTest --tests '*MapBoundingBoxConversionTest'` | 1 pass | The same viewport conversion executes on Android host. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The complete renderer and every migrated layer compose against the desktop snapshot API. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The renderer compiles for Android while the legacy fragment remains the active transition path. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The renderer, all style slots, shared resources, callbacks, and Metal runtime link into the iOS framework. |

These checks establish one cross-target composition path. They do not yet claim
camera-policy parity or live rendering because target entry points have not
switched to it.

## Shared main-map camera state

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MainMapCameraControllerTest' --tests '*ClusterCameraTest'` | 10 pass | First-fix zoom, 600ms following, track-bearing navigation, tilt reset, compass reset, gesture/pan discrimination, persistence, cluster margin/cap/duration, and antimeridian handling retain the legacy policy. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MainMapCameraControllerTest' --tests '*ClusterCameraTest'` | 10 pass | The same camera and cluster policy executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The common camera state and MapLibre presentation adapter compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common camera state compiles beside the active legacy fragment. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The shared camera state, persistence adapter, controller, and MapLibre animation API link into the iOS framework. |

The controller uses target movement during a generic user gesture as a pan
signal because MapLibre Compose does not expose gesture-specific begin events.
That upstream limitation is documented separately. Entry-point wiring and live
interaction evidence remain outstanding.
