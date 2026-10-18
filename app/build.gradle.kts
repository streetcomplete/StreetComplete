import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import dev.mokkery.MockMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import java.io.FileWriter


/** App version name */
val appVersionName = "64.0"

/** Localizations the app should be available in */
val bcp47ExportLanguages = setOf(
    "ar", "ast", "be", "bg", "bs", "ca", "cs", "cy", "da", "de", "el",
    "en", "en-AU", "en-CA", "en-GB", "eo", "es", "es-AR", "et", "eu",
    "fa", "fi", "fr", "ga", "gl", "he", "hr", "hu", "hy",
    "id", "it", "ja", "ko", "kw", "lt", "lv", "ml", "nb", "no", "nl", "nn", "pl", "pt", "pt-BR",
    "ro", "ru", "sk", "sl", "sr-Cyrl", "sr-Latn", "sv", "sw", "th", "tr", "uk", "vi",
    "zh-Hans", "zh-Hant"
)

/** Version of the iD presets to use
 *  see https://github.com/openstreetmap/id-tagging-schema/releases for latest version */
val presetsVersion = "v7.3.0"

/** Version of the Name Suggestion Index to use
 *  see https://github.com/osmlab/name-suggestion-index/tags for latest version (without leading "v"
 *  */
val nsiVersion = "7.2.20260530"

/** Project ID of the crowdsource translation platform (from where to pull translations from) */
val poEditorProjectId = "97843"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.multiplatformLibrary)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.kotlin.allopen)
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
        create("desktop") {
            buildConfigField(STRING, "PLATFORM", "desktop")
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

    jvm("desktop") {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val jvmAndroidMain = create("jvmAndroidMain") { dependsOn(commonMain.get()) }
        val nonAndroidMain = create("nonAndroidMain") { dependsOn(commonMain.get()) }
        val mobileMain = create("mobileMain") { dependsOn(commonMain.get()) }

        commonMain {
            dependencies {
                // Kotlin
                implementation(libs.kotlinx.coroutines.core)

                // Atomics, Locks, Synchronization
                // Apparently only necessary as long as https://github.com/Kotlin/kotlinx-atomicfu/issues/145 is not solved
                implementation(libs.kotlinx.atomicfu)

                // Dependency injection
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.composeViewmodel)

                // Logging
                implementation(libs.kermit)

                // settings
                implementation(libs.multiplatform.settings)

                // I/O
                implementation(libs.kotlinx.io)

                // location
                implementation(libs.maplibre.location)

                // SQLite
                implementation(libs.androidx.sqlite)
                implementation(libs.androidx.sqliteBundled)

                // HTTP client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.encoding)
                // SHA256 hashing, used during OAuth authentication
                implementation(libs.kotlincrypto.sha2)

                // XML
                implementation(libs.xmlutil.core)
                implementation(libs.xmlutil.coreIo)

                // YAML
                implementation(libs.kaml)

                // JSON
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.jsonIo)

                // Date / time
                implementation(libs.kotlinx.datetime)

                // finding in which country we are for country-specific logic
                implementation(libs.countryboundaries)

                // finding OSM features
                implementation(libs.osmfeatures)

                // opening hours parser
                implementation(libs.osm.openingHours)

                // UI (Compose)
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material)
                implementation(libs.compose.ui)
                implementation(libs.compose.resources)
                implementation(libs.compose.uiToolingPreview)

                // UI Navigation
                implementation(libs.androidx.navigationevent.compose)
                implementation(libs.androidx.navigation3.ui)
                implementation(libs.androidx.lifecycle.viewmodelNavigation3)

                // UI ViewModel
                implementation(libs.androidx.lifecycle.viewmodelCompose)

                // UI widgets

                // Map
                implementation(libs.maplibre.compose)

                // non-lazy grid
                // NOTE: might replace with
                // https://developer.android.com/develop/ui/compose/layouts/adaptive/grid
                // when that API is not experimental anymore
                implementation(libs.compose.grid)

                // reorderable lists (raw Compose API is pretty complicated)
                implementation(libs.reorderable)

                // sharing presets/settings via QR Code
                implementation(libs.qrose)

                // for encoding information for the URL configuration (QR code)
                implementation(libs.bignum)

                // taking a photo (, picking an image from gallery, ...)
                implementation(libs.filekit.dialogsCompose)
            }
        }
        mobileMain.dependencies {
            // multiplatform webview (for login via OAuth)
            implementation(libs.compose.webview)
        }
        androidMain {
            dependsOn(jvmAndroidMain)
            dependsOn(mobileMain)
            dependencies {
                // Dependency injection
                implementation(libs.koin.android)
                implementation(libs.koin.androidxWorkmanager)

                // Compose
                implementation(libs.androidx.activity.compose)

                // Kotlin
                implementation(libs.kotlinx.coroutines.android)

                // scheduling background jobs
                implementation(libs.androidx.work.runtime)

                // HTTP Client
                // cio, not android, because of https://youtrack.jetbrains.com/issue/KTOR-9848
                implementation(libs.ktor.client.cio)

                // map
                implementation(libs.maplibre.runtime.android)

                // required to @Preview composables in Android Studio
                runtimeOnly(libs.androidx.compose.uiTooling)
            }
        }
        iosMain {
            dependsOn(nonAndroidMain)
            dependsOn(mobileMain)
            dependencies {
                // HTTP client
                implementation(libs.ktor.client.darwin)
            }
        }
        getByName("desktopMain") {
            dependsOn(jvmAndroidMain)
            dependsOn(nonAndroidMain)
            dependencies {
                // HTTP client
                implementation(libs.ktor.client.cio)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)

                implementation(libs.ktor.client.mock)
                implementation(libs.androidx.sqliteBundled)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                // without it, :app:testAndroidHostTest throws java.lang.UnsatisfiedLinkError for sqliteJni
                implementation(libs.androidx.sqliteBundledJvm)
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
    androidRuntimeClasspath(libs.compose.uiTooling)
    // see comment in android.compileOptions.isCoreLibraryDesugaringEnabled
    coreLibraryDesugaring(libs.desugar.jdkLibs)
}

// run tests in a fixed time zone so that results don't depend on the host's time zone
tasks.withType<Test>().configureEach {
    systemProperty("user.timezone", "UTC")
}
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    // simctl passes SIMCTL_CHILD_-prefixed variables on to the simulated process
    environment("SIMCTL_CHILD_TZ", "UTC")
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
    targetFiles = { projectDir.resolve("src/commonMain/composeResources/values-$it/strings.xml") }
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
        "NSCameraUsageDescription" to "camera_permission_description",
        "NSLocationWhenInUseUsageDescription" to "no_location_permission_warning",
    )
}

tasks.register<UpdateChangelogTask>("updateChangelog") {
    group = "streetcomplete"
    sourceFile = rootDir.resolve("CHANGELOG.md")
    targetFile = projectDir.resolve("src/commonMain/composeResources/files/changelog.html")
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
