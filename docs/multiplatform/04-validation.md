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
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MainMapCameraControllerTest' --tests '*ClusterCameraTest' --tests '*FocusCameraTest'` | 16 pass | First-fix zoom, 600ms following, track-bearing navigation, tilt reset, compass reset, gesture/pan discrimination, pre-presentation and ordinary explicit moves, persistence, cluster behavior, focus padding/margin/cap, and return/clear behavior retain the legacy policy. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MainMapCameraControllerTest' --tests '*ClusterCameraTest' --tests '*FocusCameraTest'` | 16 pass | The same camera, explicit movement, cluster, and focused-geometry policy executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The common camera state and MapLibre presentation adapter compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common camera state compiles beside the active legacy fragment. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The shared camera state, persistence adapter, controller, and MapLibre animation API link into the iOS framework. |

The controller uses target movement during a generic user gesture as a pan
signal because MapLibre Compose does not expose gesture-specific begin events.
That upstream limitation is documented separately. Desktop and iOS entry-point
wiring and live interaction evidence remain outstanding.

## Android shared main-screen entry point

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Android APK | `./gradlew :androidApp:assembleDebug --refresh-dependencies` | Pass | The live activity, complete shared screen, MapLibre OpenGL runtime, and snapshot build `0.15.1-20260901.101938-7` package together. |
| Android cold launch | Install the debug APK, force-stop the test and app packages, cold-launch `MainActivity`, and observe for 15 seconds | Pass | The activity stays resumed, MapLibre renders its first OpenGL frame, and the prior style-source lock-inversion ANR does not recur. |
| Android onboarding | Complete all four tutorial pages and the location-permission step | Pass | Shared tutorial state, permission request, and transition into the real main map remain interactive. |
| Android live map | Inspect the post-onboarding screen and accessibility tree | Pass | The Compose hierarchy contains MapLibre's platform surface plus the shared stars, overlay, menu, location, attribution, and scale controls; there is no fragment container in the live hierarchy. |
| Desktop and Android camera tests | `./gradlew :app:desktopTest --tests '*MainMapCameraControllerTest' :app:testAndroidHostTest --tests '*MainMapCameraControllerTest'` | Pass | A geo move consumed before presentation is retained until the map attaches on both JVM runners. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The complete shared screen and direct map ownership compile for desktop. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The same screen, location/heading integration, projection, and map ownership link through Kotlin/Native and the Metal runtime. |

This section records the first live Android switch. Later sections record iOS
and desktop entry-point evidence; guarded deletion of Android legacy source and
assets remains outstanding.

## Android shared application navigation

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Android application | `./gradlew :androidApp:assembleDebug` | Pass | The activity-hosted shared root graph and its real settings/about/user destinations package with the app. |
| Android cold launch | Reinstall the debug APK, force-stop it, and cold-launch `MainActivity` | Pass | The app remains resumed after 15 seconds and MapLibre renders its first OpenGL frame using the injected runtime. No fatal exception or ANR is logged. |
| Main to settings | Open Menu, select Settings, then inspect the task and accessibility hierarchy | Pass | The real Settings screen is shown while `MainActivity` remains the task's only activity; no `SettingsActivity` is launched. |
| Settings to main | Invoke system back and inspect the accessibility hierarchy | Pass | The shared graph returns to the existing live map with Overlays, Menu, and Follow me controls present. |

The host now listens for language changes because navigation no longer
backgrounds `MainActivity`, so `BaseActivity.onRestart` cannot provide the old
locale-recreation behavior. Theme changes still flow through the application
delegate, while returning to main reapplies the keep-screen-on preference.

## Shared map interaction boundary

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Logical-offset projection, long press, interactive-layer pre-query, and raw map callbacks compile against the desktop runtime. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The same shared interaction boundary compiles beside the active fragment. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Projection, async feature queries, presentation-detach handling, and callbacks link into the iOS framework. |

These checks do not claim live pointer behavior. The target demos must verify
callback ordering and the 14dp projected ground radius after entry-point wiring.

## Shared GPS track state

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MainMapTrackStateTest'` | 6 pass | Accuracy rejection, stable timestamps, elevation, timed segmentation, recording handoff/recovery, renderer chunking, and bounded restoration retain the production data contract. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MainMapTrackStateTest'` | 6 pass | The same track ownership and restoration contract executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Saveable track ownership and renderer wiring compile for desktop. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The shared track state compiles beside the transition fragment. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Serializable location measurements, track state, saver, and renderer wiring link through Kotlin/Native. |

The provider-outage test deliberately locks in a repair: logical recording was
already retained by Android, but its cleared renderer silently lost the red
recording style. Shared state keeps those two states consistent when GPS fixes resume.

## iOS shared application entry point

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| iOS Xcode bundle | `DEVELOPER_DIR=/Applications/Xcode-26.5.0.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,id=4AE0B7AD-8A31-457E-B505-A46F3644E43D' -derivedDataPath build/ios-derived CODE_SIGNING_ALLOWED=NO clean build` | Pass | The Kotlin framework, Compose resources, shared root graph, and Metal runtime package into a real iOS simulator application. |
| iOS cold launch | Install, cold-launch, and observe the app on the iOS 26.5 simulator | Pass | The real common Koin graph starts without the former changeset-manager recursion or a missing map-tile service. |
| iOS onboarding | Launch with a fresh app preference domain | Pass | The first shared screen is StreetComplete's real onboarding flow rather than the former changelog/credits/privacy development launcher. |
| iOS live map | Mark the tutorial complete, cold-launch, and observe the resulting screen | Pass | MapLibre renders the real shared StreetComplete style through Metal with the downloaded-area hatch, stars, overlay, menu, attribution, and location controls. Shared glyphs load from the built app bundle. |

The clean Xcode link after the database-driver split succeeds without the former
duplicate SQLite diagnostic. Apple production and tests use the framework-backed
`NativeSQLiteDriver`; the bundled SQLite artifact is now confined to JVM source
sets. The linked Kotlin framework exports one `sqlite3_open` and one
`sqlite3_close` definition set, supplied alongside MapLibre Native rather than a
second bundled SQLite compilation.

## Desktop shared application entry point

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | The complete desktop platform graph and shared application compile for JVM. |
| Desktop development run | `./gradlew :app:run` | Pass | Prepared file resources, bundled SQLite, native Skiko, application services, the AWT presentation host, and MapLibre Metal start together; MapLibre reports a first 1200x772 logical / 2400x1544 physical frame at scale 2.0. |
| macOS app image | `./gradlew :app:createDistributable` | Pass | The jlink image includes `java.net.http`, `java.prefs`, `jdk.unsupported`, the macOS ARM64 native libraries, and 43 MB of external application resources. |
| Packaged onboarding | Launch `StreetComplete.app` with a fresh desktop preference node | Pass | The real shared OpenStreetMap onboarding renders in the packaged native window. |
| Packaged live map | Mark the tutorial complete, relaunch the app image, and inspect the native window | Pass | The shared world map renders through Metal with StreetComplete styling, downloaded-area hatching, stars, overlays, menu, follow, attribution, scale, and shared glyph resources. |

The first app-image attempt exposed a missing `java.net.http` jlink module; the
committed distribution module list fixes that packaging-only failure. Automated
computer-use inspection could read the Compose accessibility tree and capture
the window, but input delivery to this JVM window closed the automation pipe, so
menu navigation still needs separate desktop demo evidence rather than being
claimed from the screenshots alone.

## Shared application lifecycle

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Cross-target compilation | `./gradlew :app:compileKotlinDesktop :androidApp:compileDebugKotlin :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The common process initializer, application scope, and root lifecycle observer resolve through all three production dependency graphs. |
| Android cold launch | Install the rebuilt debug APK, force-stop it, cold-launch `MainActivity`, and inspect activity/log state after eight seconds | Pass | The shared initializer completes, `MainActivity` stays resumed, and MapLibre renders the first OpenGL frame without a fatal exception or ANR. |
| iOS bundle and cold launch | Build with Xcode 26.5, install on the iOS 26.5 simulator, cold-launch, inspect the process, and capture the screen | Pass with diagnostic | The shared initializer and lifecycle attachment keep the app alive and the full StreetComplete map renders through Metal. Core Location emits a main-thread authorization-status diagnostic that is tracked for correction. |
| Desktop app image | `./gradlew :app:createDistributable`, then launch the packaged executable | Pass with warnings | Shared preloading completes and MapLibre renders the first 1200x772 logical Metal frame. LWJGL reports a Java/native version warning, but the packaged app remains live; no functionality claim depends on suppressing that diagnostic. |

These checks prove foreground process startup and lifecycle attachment. The
separate iOS background-processing section records the registered task boundary
and the remaining device-only execution evidence.

## Single Android Compose application host

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Source and manifest gate | Search `androidApp` and `app` for `AboutActivity`, `SettingsActivity`, and `UserActivity` after removing their source and manifest entries | No match | Android no longer retains parallel activity-owned copies of shared About, Settings, or user UI. |
| Cross-target compilation | `./gradlew :androidApp:compileDebugKotlin :app:compileKotlinDesktop :app:compileKotlinIosSimulatorArm64` | Pass | The destination type and shared navigation request boundary compile for every production target. |
| Cold Android network-usage intent | Force-stop the installed debug app, then start `android.intent.action.MANAGE_NETWORK_USAGE` with the default category | Pass | Android resolves the exported system intent to `MainActivity`; the first rendered hierarchy is the shared Settings screen with quest, preset, communication, and display controls. |
| Warm Android network-usage intent | Cold-launch `MainActivity`, then deliver the same system intent while it is the top `singleTop` activity | Pass | Android reports that the intent was delivered to the running instance, and the shared navigation graph renders Settings without creating a second activity. |

The system intent starts at Settings on a cold process and becomes a consumed
navigation request on a warm process. Ordinary launcher, deep-link, profile,
login, About, and settings navigation therefore retain one activity and one
Compose Multiplatform root.

## Android legacy map retirement

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Reference gate | Search production source and build files for `org.maplibre.android`, all retired class names, `UpdateMapStyleTask`, and `androidMain/assets/map_theme` | Pass | No live call site, direct Android SDK use, or updater can recreate the retired implementation. |
| Cross-target compilation | `./gradlew :app:compileKotlinDesktop :androidApp:compileDebugKotlin :app:linkDebugFrameworkIosSimulatorArm64` | Pass | Removing 24 Android map files and the direct SDK leaves the shared desktop, Android, and iOS products intact. |
| Shared resource gates | `./gradlew :app:verifySharedMapGlyphResources :androidApp:verifyDebugMapGlyphAssets` | Pass | Source and APK still contain exactly the 512 non-empty shared glyph ranges after deleting the 512 Android duplicates. |
| Map tests | Run the 21 common map test classes on desktop and Android host | 142 pass on each runner | Camera, animation, geometry, layer styling, content, download planning, track state, resources, and renderer-independent sources retain their tested contracts. |
| Full desktop suite | `./gradlew :app:desktopTest` | 2,518 pass, 6 fail, 1 skip | The retirement adds no failure; the same six documented locale/date-sensitive baseline failures remain. |
| Android dependency | `./gradlew :app:dependencyInsight --dependency org.maplibre.gl:android-sdk-opengl --configuration androidRuntimeClasspath` | No match | The direct legacy SDK is absent from Android's runtime graph; MapLibre Compose's OpenGL runtime remains. |
| Android APK assets | Inspect the rebuilt APK for `assets/map_theme`, JSON styles, and sprite sheets | None present | The package cannot silently fall back to the retired imperative style or its duplicate glyph tree. |
| Android cold launch | Reinstall the rebuilt APK, force-stop it, cold-launch `MainActivity`, and inspect activity/log state after ten seconds | Pass | `MainActivity` remains resumed and the only map runtime renders its first OpenGL frame without a fatal exception or ANR. |

The source-to-source replacement mapping and deletion gates are retained in
`05-android-map-retirement.md` so later parity review can audit why each file was
safe to remove instead of relying on a successful compile alone.

## Shared incoming URL ingress

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Desktop and Android host tests | `./gradlew :app:desktopTest --tests '*IncomingUriHandlerTest' :app:testAndroidHostTest --tests '*IncomingUriHandlerTest'` | 1 pass on each runner | URLs arriving before collection are buffered and delivered in order on both JVM runners. |
| Android application | Rebuild and reinstall the APK, force-stop it, launch `MainActivity` with `geo:37.7749,-122.4194?z=16`, then inspect process, logs, and the rendered map | Pass after runtime repair | The real intent enters the common handler, the app stays resumed without a fatal exception or ANR, and the map renders at the requested San Francisco position and zoom. |
| Desktop app image | Build the distributable and launch its executable with `geo:37.7749,-122.4194?z=16` as the first argument | Pass | The packaged application renders its first Metal frame and persists latitude `37.7749`, longitude `-122.4194`, and zoom `16.0` through the shared map state. |
| iOS framework and Swift host | Link the simulator framework and build the complete Xcode application | Pass | The generated Kotlin bridge and SwiftUI `onOpenURL` callback compile together in the production host. |
| iOS URL registration | Ask the booted simulator to open `geo:37.7749,-122.4194?z=16` | Partial runtime evidence | iOS resolves StreetComplete and presents its system-owned “Open in StreetComplete?” confirmation. The locked host prevented UI automation from accepting the prompt, so final callback/camera evidence remains part of the iOS demo run. |

The first Android live run found a constructor-order regression: the buffered
URL was consumed from the view model's first `init` block before its URL state
flows had been initialized, producing a deterministic main-thread null-pointer
failure. The collector now starts only after both state flows exist. The same
force-stop and real-intent loop is the runtime regression gate; the view model's
23 service dependencies make a narrower constructor test a misleading seam.

## iOS external application integration

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Foundation URL construction | Construct the production component shape with subject `A & B` and a multiline body using `xcrun swift` | `mailto:test@example.com?subject=A%20%26%20B&body=one%0Atwo` | Foundation produces a valid opaque mail URL and percent-encodes reserved characters and line breaks. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The Foundation components/query-item implementation compiles and links through Kotlin/Native without the former impossible-cast diagnostics. |
| iOS Xcode bundle | Build the complete simulator app with Xcode 26.5 | Pass | The fixed launcher and nullable App Store review destination integrate with the Swift host. |

StreetComplete does not yet have an iOS App Store product identifier. The
rating destination therefore returns `null` and carries an explicit TODO until
that external product record exists; it no longer reaches a runtime `TODO()` and
crashes when the About screen asks whether rating is available.

## Kotlin/Native database and edit persistence

| Target | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| iOS test compilation | `./gradlew :app:compileTestKotlinIosSimulatorArm64` | Pass | Common tests no longer contain a JVM thread call, Native-illegal test names, or colliding utility `main()` functions. |
| iOS DAO suite | `./gradlew :app:iosSimulatorArm64Test --tests '*DaoTest'` | 216 tests execute; 200 pass before the edit fix | Concrete setup/teardown lifecycle overrides open, initialize, close, and delete the Native SQLite test database. The 16 remaining failures isolated one production serialization defect. |
| Persisted element edits | Run `ElementEditsDaoTest` on iOS simulator, desktop, and Android host | 16 pass on each target | Every supported edit-action subtype round-trips through the production database with the explicit polymorphic serializer. |
| Downloaded-tile timestamps | Run `DownloadedTilesDaoTest` on iOS simulator, desktop, and Android host | 11 pass on each target | The DAO's injected clock makes old/new tile retention deterministic without a JVM-only sleep. |

The first Native DAO run failed because Kotlin/Native did not execute lifecycle
methods declared only on the inherited test base. Explicit concrete overrides
are intentionally repetitive: they are the cross-runner contract and preserve
the same database setup order on Native and JUnit. Once the harness reached the
edit table, it also showed that reified interface serializer discovery was
JVM-specific. `PolymorphicSerializer(ElementEditAction::class)` retains the
registered subtype and wire format while making the production path portable.

## Cross-target test portability and Apple SQLite ownership

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| iOS simulator full suite | `./gradlew :app:iosSimulatorArm64Test` | 2,493 pass | All portable common tests execute through Kotlin/Native, including foreground-sync failure containment, buffered external-link delivery, stale-viewport rejection, production DAOs with `NativeSQLiteDriver`, edit serialization, locale parsing, Apple measurement-system mapping, crash persistence, and file-backed photo fixtures. |
| Desktop full suite | `./gradlew :app:desktopTest` | 2,531 pass, 1 skip | The portable suite and six JVM-only live HTTP integration classes execute together against the desktop production implementations. |
| Android host full suite | `./gradlew :app:testAndroidHostTest` | 2,531 pass, 1 skip | The final complete run is clean, including the live OSM development-server classes. Earlier complete runs intermittently failed live requests with `Unexpected end of file from server` or a reset connection; assertions and transport behavior were left intact. |
| iOS application link | `DEVELOPER_DIR=/Applications/Xcode-26.5.0.app/Contents/Developer xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,id=4AE0B7AD-8A31-457E-B505-A46F3644E43D' -derivedDataPath build/ios-derived CODE_SIGNING_ALLOWED=NO clean build` | Pass with warning | The production framework and Swift host link after removing bundled SQLite from Apple source sets; symbol inspection finds one exported `sqlite3_open`/`sqlite3_close` definition set. The linker separately reports Skiko's ICU object as built for iOS Simulator 18.5 while the app declares 15.0. |

The seven tests that contact live OSM or ban-list endpoints live in `javaTest`.
They still run on both JVM targets, but not in the bare Kotlin/Native test
executable: both Ktor Darwin and a direct `NSURLSession` probe failed TLS in that
host even though Safari in the same simulator completed TLS 1.3 to the endpoint.
Portable mock-client, parser, persistence, and production-link coverage remains
in `commonTest`; live iOS networking must be validated from the real application
host rather than by weakening transport security in the Native test binary.

## iOS crash recovery and background sync

| Boundary | Command or action | Result | What it proves |
| --- | --- | --- | --- |
| Native crash persistence | `./gradlew :app:iosSimulatorArm64Test --tests '*IosCrashReportHolderTest'` | 1 pass | The iOS holder writes an `ErrorReportBuilder` report, returns it once, deletes the file, and returns `null` after consumption. |
| Kotlin/Native framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The unhandled-exception hook, cancellable background-sync handle, production uploader, changeset closure, network policy, and Swift callback bridge link together. |
| Xcode application | Build the iOS 26.5 simulator app with Xcode 26.5 | Pass | Swift registers the processing launch handler, calls the Kotlin work boundary, handles expiration/completion, and packages both required Info.plist declarations. |
| Built bundle declarations | Inspect the built app's Info.plist with `plutil` | Pass | `BGTaskSchedulerPermittedIdentifiers` contains `de.westnordost.streetcomplete.background-sync`, and `UIBackgroundModes` contains `processing`. |
| Simulator cold launch | Install and launch the built app while capturing its console | Registration passes; scheduling returns `BGTaskSchedulerErrorDomain` code 1 | The app remains live and does not hit the registration precondition. Apple's SDK defines code 1 as expected when Simulator does not support background processing, so actual execution remains device-only evidence. |

The processing task deliberately does not request a fresh location or download
map data. StreetComplete currently asks for when-in-use location only; claiming a
current vicinity after foreground suspension would be false. The code carries a
TODO at that boundary pending an Apple-approved background-location product
decision. Pending uploads and stale changesets require no background location
and now run through the real production services.

## Final platform-surface audit

| Boundary | Command or inspection | Result | What it proves |
| --- | --- | --- | --- |
| Shared source | Search `app/src/commonMain` for `import android.` | No match | Common production code has no Android API dependency. |
| Android presentation | Search Android Kotlin/XML plus `layout`, `navigation`, and `menu` resource directories for fragments and View-owned screens | One plain `ComponentActivity`; no fragments or UI resources | Android retains a platform lifecycle host and native service adapters, but `setContent` presents all application screens and navigation from the shared Compose tree. AppCompat and Material Components are absent as direct dependencies and no source relies on their transitive Koin presence. |
| Android location authorization | Clear app data, launch the production APK, and advance onboarding to its location page | Pass | The system precise/approximate location dialog is presented. The shared UI uses an Activity-bound MapLibre provider for permission while process-owned automatic sync retains an application-safe provider and no Activity reference. |
| Android cleanup | `./gradlew :androidApp:compileDebugKotlin --rerun-tasks` | Pass | Removing the direct Fragment dependency, layout-capable base-activity constructor, and orphaned Activity/Fragment/View/Bitmap/density helpers does not remove a live caller. |
| Apple measurement policy | Run `ScaleBarMeasureTest` on iOS simulator, desktop, and Android host | 1 pass on each target | Foundation's `Metric`, `U.S.`, and `U.K.` values map to the same shared scale-bar units used by the other targets. |
| Apple product build | Clean Xcode 26.5 simulator build | Pass | The weak scene-local map chooser, scene-owned URL ingress, iPad action-sheet anchor, camera-purpose declaration, Foundation measurement lookup, Kotlin framework, and Swift host compile and link as one app. |
| Built Apple declarations | Inspect the built app's `Info.plist` and localized `InfoPlist.strings` with `plutil` | Pass; 55 locale files | The product bundle contains accurate photo-attachment and precise-location purpose strings. German camera text is `Foto anhängen`; FileKit camera capture no longer reaches an undeclared or misleading privacy API. |
| iOS simulator launch | Install and cold-launch the rebuilt app on iOS 26.5 | Pass | The process stays live and renders the shared MapLibre Compose map after the final adapter changes. |

AR measurement is the remaining intentional mobile capability boundary. The
existing feature launches StreetMeasure and consumes its result contract;
StreetMeasure has no iOS app or compatible protocol. iOS therefore hides that
action and carries an explicit `TODO(multiplatform)`. This is an external-product
blocker, not a placeholder represented as working functionality.

## Final production build matrix

| Product | Command | Result | What it proves |
| --- | --- | --- | --- |
| Android debug APK | `./gradlew :androidApp:assembleDebug` | Pass | The single-activity Compose host, OpenGL MapLibre runtime, shared resources, WorkManager services, and platform adapters package together. |
| Desktop app image | `./gradlew :app:createDistributable` | Pass | The shared UI and Metal map runtime package into the macOS ARM64 jlink application image with the full resource tree. |
| iOS simulator app | Clean Xcode 26.5 build for the iPhone 17 / iOS 26.5 simulator | Pass | The Kotlin framework, SwiftUI host, Metal runtime, Compose resources, background-task declarations, and privacy strings link into a validated app bundle. |
| Immutable MapLibre Compose batch | Clean `--refresh-dependencies` compilation plus dependency insight for desktop, Android runtime, and iOS simulator compile configurations | Pass; every selected module is `0.15.1-20260901.101938-7` | Core, location, target variants, iOS resource archives, Android OpenGL, macOS location, and desktop Metal runtime retain Gradle variant metadata but fetch timestamped immutable files listed in `gradle/maplibre-compose-snapshot-files.tsv`. Resolution rules align all transitive edges to the same reviewed publication. |
| Cross-host desktop runtime resolution | Run `dependencyInsight` with simulated Linux x64 and Windows ARM64 JVM host properties | Pass | Linux resolves the exact Vulkan x64 and `location-runtime-linux` artifacts; Windows resolves the exact Vulkan ARM64 and `location-runtime-windows` artifacts from the committed timestamp manifest. |
| Superseded viewport regression | `./gradlew :app:desktopTest --tests '*MapQuestPinsSourceTest*' --tests '*StyleableOverlaySourceTest*'` | Pass | A first non-suspending repository load triggers a newer viewport before returning; only the newer quest pins and overlay elements may publish. |
| Cleared overlay regression | `./gradlew :app:desktopTest --tests '*StyleableOverlaySourceTest*'` | Pass | A map-data clear increments the source generation and rechecks cancellation after locking, so an in-flight viewport cannot republish cleared overlay elements. |
| Permission-flow reconnection | `./gradlew :app:desktopTest --tests '*AutoSyncerTest*'` | Pass | A completed permission-denied location flow is collected again on resume without requiring the only app scene to stop and restart. |
| Shared sync root failures | `./gradlew :app:desktopTest --tests '*CoroutineSyncControllersTest*'` | Pass | Upload, download, and delayed changeset-close failures do not escape their caller-owned application scope; cancellation is still rethrown. |
| Production target matrix | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64 :androidApp:assembleDebug :app:createDistributable` | Pass | The exact pinned stack produces the iOS framework, Android APK, and native desktop application together after lifecycle and host-ownership fixes. |

The clean iOS result is runtime evidence only for the installed iOS 26.5
simulator. Inspecting Compose/Skiko's Native cache archive identifies
`libicu.icudtl_dat.o` with `LC_BUILD_VERSION minos 18.5`; it is not part of the
MapLibre archive. Until Skiko republishes that object for the declared minimum
and the app runs on an iOS 15 device/runtime, this ledger does not claim iOS 15
compatibility.

## Shared main-map content state

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MainMapContentStateTest'` | 4 pass | Quest/edit pin mode, highlight retention, independent visibility, selected pins, and clear behavior execute in common code. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MainMapContentStateTest'` | 4 pass | The same transient presentation contract executes on the Android host runner. |
| Desktop and Android library | `./gradlew :app:desktopTest --tests '*MainMapContentStateTest' :app:compileAndroidMain` | Pass | The renderer reads the common content owner on both production targets. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | The common content owner and renderer link through Kotlin/Native. |

Clearing a focused interaction deliberately restores pins and overlays without
changing quest/edit mode, matching the legacy fragment's two independent state
transitions. Entry-point wiring remains a separate commit.

## Shared download-area planning

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused tests | `./gradlew :app:desktopTest --tests '*MainMapDownloadAreaTest'` | 4 pass | Unavailable projection, oversized rejection, minimum expansion, and tile-aligned ordinary bounds retain the production rules. |
| Android host focused tests | `./gradlew :app:testAndroidHostTest --tests '*MainMapDownloadAreaTest'` | 4 pass | The same download planning executes on the Android host runner. |
| Android library | `./gradlew :app:compileAndroidMain` | Pass | The common download planner compiles for the Android migration path. |
| iOS simulator library | `./gradlew :app:compileKotlinIosSimulatorArm64` | Pass | Tile math and geographic expansion compile through Kotlin/Native. |

## Shared offline base-map storage

| Target | Command | Result | What it proves |
| --- | --- | --- | --- |
| Desktop focused test | `./gradlew :app:desktopTest --tests '*MapLibreMapTilesDownloaderTest'` | 1 pass | The StreetComplete style, longitude/latitude bounds, zoom 0 through 16, and display pixel ratio produce the intended MapLibre offline tile pyramid. |
| Android host focused test | `./gradlew :app:testAndroidHostTest --tests '*MapLibreMapTilesDownloaderTest'` | 1 pass | The same offline definition contract executes on the Android host runner. |
| Desktop library | `./gradlew :app:compileKotlinDesktop` | Pass | Shared offline pack creation, progress observation, cancellation, and cleanup compile for JVM; a desktop runtime binding remains a separate entry-point layer. |
| Android application | `./gradlew :androidApp:compileDebugKotlin` | Pass | Android supplies one application MapLibre runtime to both the renderer and the common offline downloader. |
| iOS simulator framework | `./gradlew :app:linkDebugFrameworkIosSimulatorArm64` | Pass | iOS supplies one cache-backed Metal runtime to the renderer and common offline downloader. |

The downloader waits for a terminal MapLibre progress state before reporting
success. Cancellation pauses the pack, and an error is propagated to the shared
download coordinator rather than recording tiles as downloaded when base-map
storage failed. Live download and deletion still require target demo evidence.
