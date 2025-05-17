import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
    id("com.android.application") version "8.7.3"
    //id("org.jetbrains.compose") version "1.8.0" apply false
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
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            val mockitoVersion = "5.17.0"

            // tests
            testImplementation("org.mockito:mockito-core:$mockitoVersion")
            testImplementation(kotlin("test"))

            androidTestImplementation("androidx.test:runner:1.6.2")
            androidTestImplementation("androidx.test:rules:1.6.1")
            androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
            androidTestImplementation(kotlin("test"))

            // dependency injection
            implementation(platform("io.insert-koin:koin-bom:4.0.4"))
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
            implementation("androidx.fragment:fragment-ktx:1.8.6")
            implementation("androidx.recyclerview:recyclerview:1.4.0")
            implementation("androidx.viewpager:viewpager:1.1.0")
            implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

            // Jetpack Compose
            val composeBom = platform("androidx.compose:compose-bom:2025.04.01")
            implementation(composeBom)
            androidTestImplementation(composeBom)
            implementation("androidx.compose.material:material")
            implementation("androidx.activity:activity-compose")
            // Jetpack Compose Previews
            implementation("androidx.compose.ui:ui-tooling-preview")
            debugImplementation("androidx.compose.ui:ui-tooling")

            implementation("androidx.navigation:navigation-compose:2.8.9")

            implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

            // reorderable lists (raw Compose API is pretty complicated)
            implementation("sh.calvin.reorderable:reorderable:2.4.3")

            // multiplatform webview (for login via OAuth)
            implementation("io.github.kevinnzou:compose-webview-multiplatform-android:1.9.40")

            // photos
            implementation("androidx.exifinterface:exifinterface:1.4.1")

            // settings
            implementation("com.russhwolf:multiplatform-settings:1.3.0")

            // Kotlin
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")

            // Date/time
            api("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

            // scheduling background jobs
            implementation("androidx.work:work-runtime-ktx:2.10.1")

            // HTTP Client
            implementation("io.ktor:ktor-client-core:3.1.3")
            implementation("io.ktor:ktor-client-android:3.1.3")
            implementation("io.ktor:ktor-client-encoding:3.1.3")
            testImplementation("io.ktor:ktor-client-mock:3.1.3")

            // finding in which country we are for country-specific logic
            implementation("de.westnordost:countryboundaries:3.0.0")
            // finding a name for a feature without a name tag
            implementation("de.westnordost:osmfeatures:7.0")

            // widgets
            implementation("androidx.viewpager2:viewpager2:1.1.0")
            implementation("me.grantland:autofittextview:0.2.1")
            implementation("com.google.android.flexbox:flexbox:3.0.0")

            // sharing presets/settings via QR Code
            implementation("io.github.alexzhirkevich:qrose:1.0.1")
            // for encoding information for the URL configuration (QR code)
            implementation("com.ionspin.kotlin:bignum:0.3.10")

            // serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-io:1.8.1")
            implementation("com.charleskorn.kaml:kaml:0.77.1")
            implementation("io.github.pdvrieze.xmlutil:core:0.91.0")
            implementation("io.github.pdvrieze.xmlutil:core-io:0.91.0")

            // map and location
            implementation("org.maplibre.gl:android-sdk:11.8.7")

            // opening hours parser
            implementation("de.westnordost:osm-opening-hours:0.2.0")

            // image view that allows zoom and pan
            implementation("com.github.chrisbanes:PhotoView:2.3.0")
        }
        androidTest.dependencies {
            // TODO
        }
        commonMain.dependencies {
            // TODO
        }
        commonTest.dependencies {
            // TODO
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
        versionCode = 6101
        versionName = "61.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
}

composeCompiler {
    enableStrongSkippingMode = true
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
val nsiVersion = "v6.0.20250504"
// see https://github.com/openstreetmap/id-tagging-schema/releases for latest version
val presetsVersion = "v6.10.0"

val poEditorProjectId = "97843"

tasks.register("updateAvailableLanguages") {
    group = "streetcomplete"
    doLast {
        val fileWriter = FileWriter("$projectDir/src/androidMain/res/raw/languages.yml", false)
        fileWriter.write(bcp47ExportLanguages.joinToString("\n") { "- $it" })
        fileWriter.write("\n")
        fileWriter.close()
    }
}

tasks.register<GetTranslatorCreditsTask>("updateTranslatorCredits") {
    group = "streetcomplete"
    targetFile = "$projectDir/src/androidMain/res/raw/credits_translators.yml"
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
    targetFile = "$projectDir/src/androidMain/res/raw/changelog.html"
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
