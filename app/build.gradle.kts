import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.10"
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    signingConfigs {
        create("release") {
        }
    }

    compileSdk = 33
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete"
        minSdk = 21
        targetSdk = 33
        versionCode = 5500
        versionName = "55.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        all {
            isMinifyEnabled = true
            isShrinkResources = false
            // don't use proguard-android-optimize.txt, it is too aggressive, it is more trouble than it is worth
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        getByName("debug") {
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
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
    namespace = "de.westnordost.streetcomplete"
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

repositories {
    google()
    mavenCentral()
}

configurations {
    all {
        // it's already included in Android
        exclude(group = "net.sf.kxml", module = "kxml2")
        exclude(group = "xmlpull", module = "xmlpull")
    }
}

dependencies {
    val mockitoVersion = "3.12.4"

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // tests
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation(kotlin("test"))

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    androidTestImplementation(kotlin("test"))

    // dependency injection
    implementation("io.insert-koin:koin-android-compat:3.4.1")
    implementation("io.insert-koin:koin-androidx-workmanager:3.4.1")

    // Android stuff
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.viewpager:viewpager:1.0.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // photos
    implementation("androidx.exifinterface:exifinterface:1.3.6")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Date/time
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    // scheduling background jobs
    implementation("androidx.work:work-runtime:2.8.1")

    // finding in which country we are for country-specific logic
    implementation("de.westnordost:countryboundaries:2.1")
    // finding a name for a feature without a name tag
    implementation("de.westnordost:osmfeatures-android:5.2")
    // talking with the OSM API
    implementation("de.westnordost:osmapi-map:2.3")
    implementation("de.westnordost:osmapi-changesets:2.3")
    implementation("de.westnordost:osmapi-notes:2.0")
    implementation("de.westnordost:osmapi-traces:2.0")
    implementation("de.westnordost:osmapi-user:2.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("se.akerfeldt:okhttp-signpost:1.1.0")

    // widgets
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.grantland:autofittextview:0.2.1")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // box2d view
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")

    // sharing presets/settings via QR Code
    implementation("com.google.zxing:core:3.5.2")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.charleskorn.kaml:kaml:0.55.0")

    // map and location
    implementation("com.mapzen.tangram:tangram:0.17.1")

    // opening hours parser
    implementation("ch.poole:OpeningHoursParser:0.27.1")

    // image view that allows zoom and pan
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
}

/** Localizations that should be pulled from POEditor */
val bcp47ExportLanguages = setOf(
    "am", "ar", "ast", "be", "bg", "bs", "ca", "cs", "da", "de", "el",
    "en", "en-AU", "en-GB", "eo", "es", "eu", "fa", "fi", "fr", "gl", "he", "hr", "hu", "hy",
    "id", "it", "ja", "ko", "lt", "lv", "nb", "no", "nl", "nn", "pl", "pt", "pt-BR",
    "ro", "ru", "sk", "sl", "sr-cyrl", "sr-latn", "sv", "sw", "th", "tr", "uk",
    "zh", "zh-CN", "zh-HK", "zh-TW"
)

// see https://github.com/osmlab/name-suggestion-index/tags for latest version
val nsiVersion = "v6.0.20230925"
// see https://github.com/openstreetmap/id-tagging-schema/releases for latest version
val presetsVersion = "v6.4.1"

val poEditorProjectId = "97843"

tasks.register("updateAvailableLanguages") {
    group = "streetcomplete"
    doLast {
        val fileWriter = FileWriter("$projectDir/src/main/res/raw/languages.yml", false)
        fileWriter.write(bcp47ExportLanguages.joinToString("\n") { "- $it" })
        fileWriter.write("\n")
        fileWriter.close()
    }
}

tasks.register<GetTranslatorCreditsTask>("updateTranslatorCredits") {
    group = "streetcomplete"
    targetFile = "$projectDir/src/main/res/raw/credits_translators.yml"
    languageCodes = bcp47ExportLanguages
    cookie = properties["POEditorCookie"] as String
    phpsessid = properties["POEditorPHPSESSID"] as String
}

tasks.register<UpdatePresetsTask>("updatePresets") {
    group = "streetcomplete"
    version = presetsVersion
    languageCodes = bcp47ExportLanguages
    targetDir = "$projectDir/src/main/assets/osmfeatures/default"
}

tasks.register<UpdateNsiPresetsTask>("updateNsiPresets") {
    group = "streetcomplete"
    version = nsiVersion
    targetDir = "$projectDir/src/main/assets/osmfeatures/brands"
}

// tasks.register<DownloadBrandLogosTask>("downloadBrandLogos") {
//     group = "streetcomplete"
//     version = nsiVersion
//     targetDir = "$projectDir/src/main/assets/osmfeatures/brands"
// }

tasks.register<DownloadAndConvertPresetIconsTask>("downloadAndConvertPresetIcons") {
    group = "streetcomplete"
    version = presetsVersion
    targetDir = "$projectDir/src/main/res/drawable/"
    iconSize = 34
    transformName = { "ic_preset_" + it.replace('-', '_') }
    indexFile = "$projectDir/src/main/java/de/westnordost/streetcomplete/view/PresetIconIndex.kt"
}

tasks.register<UpdateAppTranslationsTask>("updateTranslations") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    apiToken = properties["POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { "$projectDir/src/main/res/values-$it/strings.xml" }
}

tasks.register<UpdateAppTranslationCompletenessTask>("updateTranslationCompleteness") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    mustIncludeLanguagePercentage = 90
    apiToken = properties["POEditorAPIToken"] as String
    projectId = poEditorProjectId
    targetFiles = { "$projectDir/src/main/res/values-$it/translation_info.xml" }
}

tasks.register<UpdateMapStyleTask>("updateMapStyle") {
    group = "streetcomplete"
    targetDir = "$projectDir/src/main/assets/map_theme/jawg"
    mapStyleBranch = "jawg"
}

tasks.register<GenerateMetadataByCountryTask>("generateMetadataByCountry") {
    group = "streetcomplete"
    sourceDir = "$rootDir/res/country_metadata"
    targetDir = "$projectDir/src/main/assets/country_metadata"
}

tasks.register("copyDefaultStringsToEnStrings") {
    doLast {
        File("$projectDir/src/main/res/values/strings.xml")
            .copyTo(File("$projectDir/src/main/res/values-en/strings.xml"), true)
    }
}
