import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("com.android.application") version "8.9.3"
    id("org.jetbrains.compose") version "1.8.1"
}

repositories {
    google()
    mavenCentral()
    // for com.github.chrisbaines:PhotoView
    maven { url = uri("https://www.jitpack.io") }
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

                // settings
                implementation("com.russhwolf:multiplatform-settings:1.3.0")

                // I/O
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")

                // HTTP client
                implementation("io.ktor:ktor-client-core:3.1.3")
                implementation("io.ktor:ktor-client-encoding:3.1.3")
                // SHA256 hashing, used during OAuth authentication
                implementation("org.kotlincrypto.hash:sha2:0.7.0")

                // XML
                implementation("io.github.pdvrieze.xmlutil:core:0.91.0")
                implementation("io.github.pdvrieze.xmlutil:core-io:0.91.0")

                // YAML
                implementation("com.charleskorn.kaml:kaml:0.77.1")

                // JSON
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.8.1")

                // Date / time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

                // finding in which country we are for country-specific logic
                implementation("de.westnordost:countryboundaries:3.0.0")

                // finding OSM features
                implementation("de.westnordost:osmfeatures:7.0")

                // opening hours parser
                implementation("de.westnordost:osm-opening-hours:0.2.0")

                // UI (Compose)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // UI Navigation
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.0-beta01")

                // UI ViewModel
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")

                // UI widgets

                // reorderable lists (raw Compose API is pretty complicated)
                implementation("sh.calvin.reorderable:reorderable:2.4.3")

                // multiplatform webview (for login via OAuth)
                implementation("io.github.kevinnzou:compose-webview-multiplatform-android:1.9.40")

                // sharing presets/settings via QR Code
                implementation("io.github.alexzhirkevich:qrose:1.0.1")
                // for encoding information for the URL configuration (QR code)
                implementation("com.ionspin.kotlin:bignum:0.3.10")
            }
        }
        androidMain {
            dependencies {
                // dependency injection
                implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.0.4"))
                implementation("io.insert-koin:koin-core")
                implementation("io.insert-koin:koin-android")
                implementation("io.insert-koin:koin-androidx-workmanager")
                implementation("io.insert-koin:koin-androidx-compose")

                // Android stuff
                implementation("com.google.android.material:material:1.12.0")
                implementation("androidx.core:core-ktx:1.16.0")
                implementation("androidx.appcompat:appcompat:1.7.0")
                implementation("androidx.constraintlayout:constraintlayout:2.2.1")
                implementation("androidx.annotation:annotation:1.9.1")
                implementation("androidx.fragment:fragment-ktx:1.8.7")
                implementation("androidx.recyclerview:recyclerview:1.4.0")
                implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

                // Compose
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.10.1")

                // photos
                implementation("androidx.exifinterface:exifinterface:1.4.1")

                // Kotlin
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

                // scheduling background jobs
                implementation("androidx.work:work-runtime-ktx:2.10.1")

                // HTTP Client
                implementation("io.ktor:ktor-client-android:3.1.3")

                // widgets
                implementation("androidx.viewpager2:viewpager2:1.1.0")
                implementation("me.grantland:autofittextview:0.2.1")
                implementation("com.google.android.flexbox:flexbox:3.0.0")
                implementation("com.github.chrisbanes:PhotoView:2.3.0")

                // map and location
                implementation("org.maplibre.gl:android-sdk:11.8.8")
            }
        }
        iosMain {
            dependencies {
                // HTTP client
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))

                implementation("io.ktor:ktor-client-mock:3.1.3")
            }
        }
        androidUnitTest {
            dependencies {
                implementation("org.mockito:mockito-core:5.17.0")
                implementation(kotlin("test"))
            }
        }
        androidInstrumentedTest {
            dependencies {
                implementation(kotlin("test"))
                // android tests
                implementation("androidx.test:runner:1.6.2")
                implementation("androidx.test:rules:1.6.1")
            }
        }
    }
}

android {
    namespace = "de.westnordost.streetcomplete"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete"
        minSdk = 25
        targetSdk = 35
        versionCode = 6102
        versionName = "61.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    signingConfigs {
        create("release") {
        }
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    buildTypes {
        all {
            isMinifyEnabled = true
            isShrinkResources = false
            // don't use proguard-android-optimize.txt, it is too aggressive, it is more trouble than it is worth
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            testProguardFile("test-proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        create("releaseGooglePlay") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "true")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    bundle {
        language {
            enableSplit = false // because language is selectable in-app
        }
    }

    lint {
        disable += listOf(
            "MissingTranslation", // crowd-contributed translations are incomplete all the time
            "UseCompatLoadingForDrawables" // doesn't make sense for minSdk >= 21
        )
        abortOnError = false
    }

    dependencies {
        debugImplementation("androidx.compose.ui:ui-tooling:1.8.2")
    }
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "de.westnordost.streetcomplete.resources"
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    val props = Properties()
    props.load(FileInputStream(keystorePropertiesFile))
    val releaseSigningConfig = android.signingConfigs.getByName("release")
    releaseSigningConfig.storeFile = file(props.getProperty("storeFile"))
    releaseSigningConfig.storePassword = props.getProperty("storePassword")
    releaseSigningConfig.keyAlias = props.getProperty("keyAlias")
    releaseSigningConfig.keyPassword = props.getProperty("keyPassword")
}

/** Localizations that should be pulled from POEditor */
val bcp47ExportLanguages = setOf(
    "ar", "ast", "be", "bg", "bs", "ca", "cs", "da", "de", "el",
    "en", "en-AU", "en-GB", "eo", "es", "es-AR", "et", "eu",
    "fa", "fi", "fr", "gl", "he", "hr", "hu", "hy",
    "id", "it", "ja", "ko", "lt", "lv", "nb", "no", "nl", "nn", "pl", "pt", "pt-BR",
    "ro", "ru", "sk", "sl", "sr-cyrl", "sr-latn", "sv", "sw", "th", "tr", "uk",
    "zh", "zh-CN", "zh-HK", "zh-TW"
)

// see https://github.com/osmlab/name-suggestion-index/tags for latest version
val nsiVersion = "v6.0.20250525"
// see https://github.com/openstreetmap/id-tagging-schema/releases for latest version
val presetsVersion = "v6.11.0"

val poEditorProjectId = "97843"

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
    targetFile = "$projectDir/src/commonMain/composeResources/files/credits_contributors.yml"
    // gradle, py, bat, java and mjs don't exist anymore in this repo but they used to
    codeFileRegex = Regex(".*\\.(java|kt|kts|py|gradle|bat|mjs)$")
    /* photos, illustrations, sounds ... but not yml, json, ... because most of these are updated
       via gradle tasks */
    assetFileRegex = Regex(".*\\.(jpe?g|png|svg|webp|wav)$", RegexOption.IGNORE_CASE)
    /* drawable xmls, layout xmls, animation xmls ... but not strings because they are updated
       via gradle tasks */
    interfaceMarkupRegex = Regex(".*(anim|color|drawable|layout|menu|mipmap).*\\.xml$")
    githubApiToken = properties["GithubApiToken"] as String
}

tasks.register("updateAvailableLanguages") {
    group = "streetcomplete"
    doLast {
        val fileWriter = FileWriter("$projectDir/src/commonMain/composeResources/files/languages.yml", false)
        fileWriter.write(bcp47ExportLanguages.joinToString("\n") { "- $it" })
        fileWriter.write("\n")
        fileWriter.close()
    }
}

tasks.register<GetTranslatorCreditsTask>("updateTranslatorCredits") {
    group = "streetcomplete"
    targetFile = "$projectDir/src/commonMain/composeResources/files/credits_translators.yml"
    languageCodes = bcp47ExportLanguages
    cookie = properties["POEditorCookie"] as String
    phpsessid = properties["POEditorPHPSESSID"] as String
}

tasks.register<UpdatePresetsTask>("updatePresets") {
    group = "streetcomplete"
    version = presetsVersion
    languageCodes = bcp47ExportLanguages
    targetDir = "$projectDir/src/androidMain/assets/osmfeatures/default"
}

tasks.register<UpdateNsiPresetsTask>("updateNsiPresets") {
    group = "streetcomplete"
    version = nsiVersion
    targetDir = "$projectDir/src/androidMain/assets/osmfeatures/brands"
}

// tasks.register<DownloadBrandLogosTask>("downloadBrandLogos") {
//     group = "streetcomplete"
//     version = nsiVersion
//     targetDir = "$projectDir/src/androidMain/assets/osmfeatures/brands"
// }

tasks.register<DownloadAndConvertPresetIconsTask>("downloadAndConvertPresetIcons") {
    group = "streetcomplete"
    version = presetsVersion
    targetDir = "$projectDir/src/androidMain/res/drawable/"
    iconSize = 34
    transformName = { "ic_preset_" + it.replace('-', '_') }
    indexFile = "$projectDir/src/androidMain/kotlin/de/westnordost/streetcomplete/view/PresetIconIndex.kt"
}

tasks.register<UpdateAppTranslationsTask>("updateTranslations") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    apiToken = properties["POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { "$projectDir/src/androidMain/res/values-$it/strings.xml" }
}

tasks.register<UpdateAppTranslationCompletenessTask>("updateTranslationCompleteness") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    mustIncludeLanguagePercentage = 90
    apiToken = properties["POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { "$projectDir/src/androidMain/res/values-$it/translation_info.xml" }
}

tasks.register<UpdateChangelogTask>("updateChangelog") {
    group = "streetcomplete"
    sourceFile = "$rootDir/CHANGELOG.md"
    targetFile = "$projectDir/src/commonMain/composeResources/files/changelog.html"
}

tasks.register<UpdateMapStyleTask>("updateMapStyle") {
    group = "streetcomplete"
    targetDir = "$projectDir/src/androidMain/assets/map_theme"
    apiKey = "mL9X4SwxfsAGfojvGiion9hPKuGLKxPbogLyMbtakA2gJ3X88gcVlTSQ7OD6OfbZ"
    mapStyleBranch = "master"
}

tasks.register<GenerateMetadataByCountryTask>("generateMetadataByCountry") {
    group = "streetcomplete"
    sourceDir = "$rootDir/res/country_metadata"
    targetDir = "$projectDir/src/androidMain/assets/country_metadata"
}

tasks.register("copyDefaultStringsToEnStrings") {
    doLast {
        File("$projectDir/src/androidMain/res/values/strings.xml")
            .copyTo(File("$projectDir/src/androidMain/res/values-en/strings.xml"), true)
    }
}
