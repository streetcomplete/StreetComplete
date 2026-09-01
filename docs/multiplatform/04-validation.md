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
