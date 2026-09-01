import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import dev.mokkery.MockMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileWriter


/** App version name, code and flavor */
val appVersionName = "64.0-alpha1"
val appVersionCode = 6400

/** Localizations the app should be available in */
val bcp47ExportLanguages = setOf(
    "ar", "ast", "be", "bg", "bs", "ca", "cs", "cy", "da", "de", "el",
    "en", "en-AU", "en-CA", "en-GB", "eo", "es", "es-AR", "et", "eu",
    "fa", "fi", "fr", "ga", "gl", "he", "hr", "hu", "hy",
    "id", "it", "ja", "ko", "kw", "lt", "lv", "ml", "nb", "no", "nl", "nn", "pl", "pt", "pt-BR",
    "ro", "ru", "sk", "sl", "sr-cyrl", "sr-latn", "sv", "sw", "th", "tr", "uk", "vi",
    "zh", "zh-CN", "zh-TW"
)

/** Version of the iD presets to use
 *  see https://github.com/openstreetmap/id-tagging-schema/releases for latest version */
val presetsVersion = "v7.0.1"

/** Version of the Name Suggestion Index to use
 *  see https://github.com/osmlab/name-suggestion-index/tags for latest version (without leading "v"
 *  */
val nsiVersion = "7.2.20260530"

/** Project ID of the crowdsource translation platform (from where to pull translations from) */
val poEditorProjectId = "97843"

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.0"
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("com.codingfeline.buildkonfig") version "0.22.0"
    // keep in sync with Kotlin version! See https://mokkery.dev/docs/Setup/#compatibility
    id("dev.mokkery") version "3.4.2"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.4.0"
}

repositories {
    google()
    mavenCentral()
}

buildkonfig {
    packageName = "de.westnordost.streetcomplete"
    objectName = "BuildConfig"

    defaultConfigs {
        buildConfigField(STRING, "VERSION_NAME", appVersionName)
        buildConfigField(BOOLEAN, "DEBUG", properties["app.streetcomplete.debug"]!!.toString())
    }

    targetConfigs {
        create("android") {
            buildConfigField(STRING, "PLATFORM", "android")
        }
        for (ios in listOf("iosArm64", "iosSimulatorArm64")) {
            create(ios) {
                buildConfigField(STRING, "PLATFORM", "ios")
            }
        }
    }
}

// for mocking in tests
allOpen {
    annotation("de.westnordost.streetcomplete.util.Mockable")
}
mokkery {
    // mocks will return default values if not mocked otherwise
    defaultMockMode.set(MockMode.autofill)
    // to enable mocking of classes whose constructor parameters also need to be mocked (with concrete classes)
    stubs.allowConcreteClassInstantiation = true
}

kotlin {
    android {
        namespace = "de.westnordost.streetcomplete"
        compileSdk = 37
        minSdk = 25

        androidResources {
            enable = true
        }

        withHostTest {
            isIncludeAndroidResources = true
        }

        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        lint {
            disable += listOf(
                "MissingTranslation", // crowd-contributed translations are incomplete all the time
            )
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "StreetComplete"
            isStatic = true
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                // Kotlin
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

                // Atomics, Locks, Synchronization
                // Aparently only necessary as long as https://github.com/Kotlin/kotlinx-atomicfu/issues/145 is not solved
                implementation("org.jetbrains.kotlinx:atomicfu:0.33.0")

                // Dependency injection
                implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.2.2"))
                implementation("io.insert-koin:koin-core")
                implementation("io.insert-koin:koin-compose")
                implementation("io.insert-koin:koin-compose-viewmodel")
                implementation("io.insert-koin:koin-androidx-compose-navigation")

                // Logging
                implementation("co.touchlab:kermit:2.1.0")

                // settings
                implementation("com.russhwolf:multiplatform-settings:1.3.0")

                // I/O
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")

                // location
                implementation("org.maplibre.compose:location:0.15.0")

                // SQLite
                implementation("androidx.sqlite:sqlite:2.7.0")
                implementation("androidx.sqlite:sqlite-bundled:2.7.0")

                // HTTP client
                implementation("io.ktor:ktor-client-core:3.5.1")
                implementation("io.ktor:ktor-client-encoding:3.5.1")
                // SHA256 hashing, used during OAuth authentication
                implementation("org.kotlincrypto.hash:sha2:0.8.0")

                // XML
                implementation("io.github.pdvrieze.xmlutil:core:1.0.1")
                implementation("io.github.pdvrieze.xmlutil:core-io:1.0.1")

                // YAML
                implementation("com.charleskorn.kaml:kaml:0.104.0")

                // JSON
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.11.0")

                // Date / time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")

                // finding in which country we are for country-specific logic
                implementation("de.westnordost:countryboundaries:3.0.0")

                // finding OSM features
                implementation("de.westnordost:osmfeatures:8.0.0")

                // opening hours parser
                implementation("de.westnordost:osm-opening-hours:0.4.0")

                // UI (Compose)
                implementation("org.jetbrains.compose.runtime:runtime:1.12.0")
                implementation("org.jetbrains.compose.foundation:foundation:1.12.0")
                implementation("org.jetbrains.compose.material:material:1.12.0")
                implementation("org.jetbrains.compose.ui:ui:1.12.0")
                implementation("org.jetbrains.compose.components:components-resources:1.12.0")
                implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.12.0")

                // UI Navigation
                implementation("org.jetbrains.compose.ui:ui-backhandler:1.12.0")
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")

                // UI ViewModel
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

                // UI widgets

                // Map
                implementation("org.maplibre.compose:maplibre-compose:0.14.0")

                // non-lazy grid
                // NOTE: might replace with
                // https://developer.android.com/develop/ui/compose/layouts/adaptive/grid
                // when that API is not experimental anymore
                implementation("com.cheonjaeung.compose.grid:grid:2.8.0")

                // reorderable lists (raw Compose API is pretty complicated)
                implementation("sh.calvin.reorderable:reorderable:3.1.0")

                // multiplatform webview (for login via OAuth)
                implementation("io.github.kevinnzou:compose-webview-multiplatform:2.0.3")

                // sharing presets/settings via QR Code
                implementation("io.github.alexzhirkevich:qrose:1.1.2")

                // for encoding information for the URL configuration (QR code)
                implementation("com.ionspin.kotlin:bignum:0.3.10")

                // taking a photo (, picking an image from gallery, ...)
                implementation("io.github.vinceglb:filekit-dialogs-compose:0.14.1")
            }
        }
        androidMain {
            kotlin.srcDirs(layout.buildDirectory.dir("generated/androidMain/kotlin"))
            dependencies {
                // Dependency injection
                implementation("io.insert-koin:koin-android")
                implementation("io.insert-koin:koin-androidx-workmanager")

                // Android stuff
                implementation("com.google.android.material:material:1.14.0")
                implementation("androidx.appcompat:appcompat:1.7.1")

                // Compose
                implementation("androidx.activity:activity-compose:1.13.0")

                // Kotlin
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

                // scheduling background jobs
                implementation("androidx.work:work-runtime-ktx:2.11.2")

                // HTTP Client
                implementation("io.ktor:ktor-client-android:3.5.1")

                // map and location
                //implementation("org.maplibre.gl:android-sdk-opengl:13.3.1")

                // required to @Preview composables in Android Studio
                runtimeOnly("androidx.compose.ui:ui-tooling:1.10.0")
            }
        }
        iosMain {
            dependencies {
                // HTTP client
                implementation("io.ktor:ktor-client-darwin:3.5.1")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))

                implementation("io.ktor:ktor-client-mock:3.5.1")
                implementation("androidx.sqlite:sqlite-bundled:2.7.0")
            }
        }
        getByName("androidHostTest") {
            dependencies {
                // without it, :app:testAndroidHostTest throws java.lang.UnsatisfiedLinkError for sqliteJni
                implementation("androidx.sqlite:sqlite-bundled-jvm:2.7.0")
            }
        }
    }
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "de.westnordost.streetcomplete.resources"
    }
}

dependencies {
    androidRuntimeClasspath("org.jetbrains.compose.ui:ui-tooling:1.12.0")
    // see comment in android.compileOptions.isCoreLibraryDesugaringEnabled
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}

tasks.register<UpdateContributorStatisticsTask>("updateContributorStatistics") {
    group = "streetcomplete"
    skipCommits = setOf(
        "ae7a244dd60ccfc91cf2dc01bf9e60c8d6a81616", // some weird force-merge or something
        "f3bc67328c3be989835e44eb33e769f49da479e1", // just a large re-import of orchard-produce images
        "9c6d3e25216d06a2c5afa71086949e1e195de926", // mechanical linting
        "1908fc930397c17739e60c8da67f968361f52e89", // mechanical linting
        "74b6424d3310f62a5c0f7b0071ee81c2308db4f6", // mechanically optimized all graphics in the repo back then
        "4282c1e812764a2bb46c17bbdb0fd98aee598e83", // deletion of adding too many files prior
        "a64d57efc3d8d51c564365088772fdac528ab069", // deletion of adding too many files prior
        "7fb216b8360ee85d84b36ad3fb0b0ea0ebf9977d", // mechanical linting
        "21aa1deabae7a563ba1475094f372590fb33d784", // mechanical linting
        "fef6877852d6a19a7b85e6f3ed3b09ea7c6538ec", // mostly just moving a lot of packages around
        "7a7d725154eb38d53936d154fc8011355679a8ae", // just moving packages around
    )
    val skipWords = listOf("lint", "linter", "reorder imports", "organize imports")
    skipCommitRegex = Regex(".*\\b(${skipWords.joinToString("|")})\\b.*", RegexOption.IGNORE_CASE)
    targetFile = projectDir.resolve("src/commonMain/composeResources/files/credits_contributors.yml")
    // gradle, py, bat, java and mjs don't exist anymore in this repo but they used to
    codeFileRegex = Regex(".*\\.(java|kt|kts|py|gradle|bat|mjs)$")
    /* photos, illustrations, sounds ... but not YAML, JSON, ... because most of these are updated
       via gradle tasks */
    assetFileRegex = Regex(".*\\.(jpe?g|png|svg|webp|wav)$", RegexOption.IGNORE_CASE)
    /* drawable XMLs, layout XMLs, animation XMLs ... but not strings because they are updated
       via gradle tasks */
    interfaceMarkupRegex = Regex(".*(anim|color|drawable|layout|menu|mipmap).*\\.xml$")
    githubApiToken = properties["app.streetcomplete.GithubApiToken"] as String
}

tasks.register("updateAvailableLanguages") {
    group = "streetcomplete"
    outputs.file(projectDir.resolve("src/commonMain/composeResources/files/languages.yml"))
    doLast {
        val fileWriter = FileWriter(outputs.files.singleFile, false)
        fileWriter.write(bcp47ExportLanguages.joinToString("\n") { "- $it" })
        fileWriter.write("\n")
        fileWriter.close()
    }
}

tasks.register<GetTranslatorCreditsTask>("updateTranslatorCredits") {
    group = "streetcomplete"
    targetFile = projectDir.resolve("src/commonMain/composeResources/files/credits_translators.yml")
    languageCodes = bcp47ExportLanguages
    cookie = properties["app.streetcomplete.POEditorCookie"] as String
    phpsessid = properties["app.streetcomplete.POEditorPHPSESSID"] as String
}

tasks.register<UpdatePresetsTask>("updatePresets") {
    group = "streetcomplete"
    version = presetsVersion
    languageCodes = bcp47ExportLanguages
    targetDir = projectDir.resolve("src/commonMain/composeResources/files/osmfeatures/default")
}

tasks.register<UpdateNsiPresetsTask>("updateNsiPresets") {
    group = "streetcomplete"
    version = nsiVersion
    targetDir = projectDir.resolve("src/commonMain/composeResources/files/osmfeatures/brands")
}

// tasks.register<DownloadBrandLogosTask>("downloadBrandLogos") {
//     group = "streetcomplete"
//     version = nsiVersion
//     targetDir = projectDir.resolve("src/commonMain/composeResources/files/osmfeatures/brands")
// }

tasks.register<DownloadAndConvertPresetIconsTask>("downloadAndConvertPresetIcons") {
    group = "streetcomplete"
    version = presetsVersion
    targetDir = projectDir.resolve("src/commonMain/composeResources/drawable")
    iconSize = 34
    transformName = { "preset_" + it.replace('-', '_') }
}

tasks.register<UpdateAppTranslationsTask>("updateTranslations") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    apiToken = properties["app.streetcomplete.POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { projectDir.resolve("/src/commonMain/composeResources/values-$it/strings.xml") }
}

tasks.register<UpdateAppTranslationCompletenessTask>("updateTranslationCompleteness") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    mustIncludeLanguagePercentage = 90
    apiToken = properties["app.streetcomplete.POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { projectDir.resolve("src/commonMain/composeResources/values-$it/translation_info.xml") }
}

tasks.register<UpdateIosAppTranslationsTask>("updateIosTranslations") {
    group = "streetcomplete"
    projectId = poEditorProjectId
    apiToken = properties["app.streetcomplete.POEditorAPIToken"] as String
    targetFile = projectDir.resolve("../iosApp/iosApp/InfoPlist.xcstrings")
    languageCodes = bcp47ExportLanguages
    strings = mapOf(
        "NSLocationWhenInUseUsageDescription" to "no_location_permission_warning"
    )
}

tasks.register<UpdateChangelogTask>("updateChangelog") {
    group = "streetcomplete"
    sourceFile = rootDir.resolve("CHANGELOG.md")
    targetFile = projectDir.resolve("src/commonMain/composeResources/files/changelog.html")
}

tasks.register<UpdateMapStyleTask>("updateMapStyle") {
    group = "streetcomplete"
    targetDir = projectDir.resolve("src/androidMain/assets/map_theme")
    apiKey = "mL9X4SwxfsAGfojvGiion9hPKuGLKxPbogLyMbtakA2gJ3X88gcVlTSQ7OD6OfbZ"
    mapStyleBranch = "master"
}

tasks.register<GenerateMetadataByCountryTask>("generateMetadataByCountry") {
    group = "streetcomplete"
    dependsOn(
        ":updateAtmOperators",
        ":updateChargingStationOperators",
        ":updateClothesContainerOperators",
        ":updateParcelLockerBrand",
    )
    sourceDir = rootDir.resolve("res/country_metadata")
    targetDir = projectDir.resolve("src/commonMain/composeResources/files/country_metadata")
}

tasks.register("copyDefaultStringsToEnStrings") {
    group = "streetcomplete"
    inputs.file(projectDir.resolve("src/commonMain/composeResources/values/strings.xml"))
    outputs.file(projectDir.resolve("src/commonMain/composeResources/values-en/strings.xml"))
    doLast {
        inputs.files.singleFile.copyTo(outputs.files.singleFile, true)
    }
}

// necessary as long as map hasn't been converted to compose yet
tasks.register<CopyIconsTask>("copyIconsToAndroid") {
    group = "streetcomplete"
    sourceDir = projectDir.resolve("src/commonMain/composeResources/drawable")
    targetDir = projectDir.resolve("build/generated/androidMain/res/drawable")
    filter = {
        // quest pins, icons for overlays
        it.startsWith("quest_") ||
        it.startsWith("building_") ||
        it.startsWith("preset_") ||
        it == "sport_volleyball.xml" ||
        it == "religion_christian.xml" ||
        it == "religion_jewish.xml" ||
        it == "religion_muslim.xml" ||
        it == "address_dot.xml" ||
        it == "none.png" ||
        // icons for base map
        it == "pin_shadow.png" ||
        it == "location_nyan.png" ||
        it == "scissors_cut.xml" ||
        it == "scissors.xml" ||
        it == "track_nyan.png" ||
        it == "track_nyan_record.png" ||
        it == "downloaded_area_hatching.xml" ||
        it == "location_shadow.xml" ||
        it == "location_view_direction.xml" ||
        it == "pin.xml" ||
        it == "pin_circle.xml"
    }
    indexFile = projectDir.resolve("build/generated/androidMain/kotlin/de/westnordost/streetcomplete/view/IconIndex.kt")
}

tasks.register<CopyStringsTask>("copyStringsToAndroid") {
    group = "streetcomplete"
    sourceDir = projectDir.resolve("src/commonMain/composeResources")
    targetDir = projectDir.resolve("build/generated/androidMain/res")
}

project.afterEvaluate {
    tasks.named("androidPreBuild") {
        dependsOn(tasks.named("copyIconsToAndroid"))
        dependsOn(tasks.named("copyStringsToAndroid"))
    }
}

tasks.register<JavaExec>("printQuestFiltersAsOverpassQL") {
    group = "utils"

    val testTask = tasks.named<Test>("testAndroidHostTest")
    dependsOn(testTask.map { it.classpath })
    classpath = testTask.get().classpath

    mainClass.set("de.westnordost.streetcomplete.PrintQuestFiltersAsOverpassQLKt")
}

tasks.register<JavaExec>("openingHoursParsingStatistics") {
    group = "utils"

    val testTask = tasks.named<Test>("testAndroidHostTest")
    dependsOn(testTask.map { it.classpath })
    classpath = testTask.get().classpath

    mainClass.set("de.westnordost.streetcomplete.OpeningHoursParsingStatisticsKt")
}

androidComponents {
    onVariants { variant ->
        variant.sources.res?.addStaticSourceDirectory(layout.buildDirectory.dir("generated/androidMain/res").get().asFile.absolutePath)
    }
}
