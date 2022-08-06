import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.7.10"
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    signingConfigs {
        create("release") {
        }
    }

    compileSdk = 31
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete.expert"
        minSdk = 21
        targetSdk = 31
        versionCode = 4504
        versionName = "45.2_ee"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        all {
            isMinifyEnabled = true
            isShrinkResources = true
            // don't use proguard-android-optimize.txt, it is too aggressive, it is more trouble than it is worth
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            buildConfigField("boolean", "IS_GOOGLE_PLAY", "false")
        }
        getByName("debug") {
            isMinifyEnabled = true
            isShrinkResources = true
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
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    lint {
        disable += listOf(
            "MissingTranslation",
            "UseCompatLoadingForDrawables" // doesn't make sense for minSdk >= 21
        )
        abortOnError = false
    }
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
    maven {
        url = uri("https://jcenter.bintray.com/")
        content {
            includeGroup("org.sufficientlysecure")
        }
    }
}

configurations {
    all {
        // it's already included in Android
        exclude(group = "net.sf.kxml", module = "kxml2")

        // TODO remove substitution when `kaml` dependency uses newer version of `org.snakeyaml:snakeyaml-engine`
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.snakeyaml:snakeyaml-engine:2.3"))
                .using(module("org.bitbucket.snakeyaml:snakeyaml-engine:8209bb9484"))
                .because("https://github.com/streetcomplete/StreetComplete/issues/3889")
        }
    }
}

dependencies {
    val kotlinVersion = "1.7.10"
    val mockitoVersion = "3.12.4"
    val kotlinxCoroutinesVersion = "1.6.4"
    val koinVersion = "3.2.0"

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-inline:$mockitoVersion")
    testImplementation("org.assertj:assertj-core:3.23.1")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    androidTestImplementation("org.assertj:assertj-core:3.23.1")

    // dependency injection
    implementation("io.insert-koin:koin-android-compat:$koinVersion")
    implementation("io.insert-koin:koin-androidx-workmanager:$koinVersion")

    // Android stuff
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.annotation:annotation:1.4.0")
    implementation("androidx.fragment:fragment-ktx:1.5.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.viewpager:viewpager:1.0.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // photos
    implementation("androidx.exifinterface:exifinterface:1.3.3")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinxCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxCoroutinesVersion")

    // scheduling background jobs
    implementation("androidx.work:work-runtime:2.7.1")

    // finding in which country we are for country-specific logic
    implementation("de.westnordost:countryboundaries:1.5")
    // finding a name for a feature without a name tag
    implementation("de.westnordost:osmfeatures-android:5.0")
    // talking with the OSM API
    implementation("de.westnordost:osmapi-map:2.0")
    implementation("de.westnordost:osmapi-changesets:2.0")
    implementation("de.westnordost:osmapi-notes:2.0")
    implementation("de.westnordost:osmapi-traces:2.0")
    implementation("de.westnordost:osmapi-user:2.0")
    implementation("com.squareup.okhttp3:okhttp:3.14.9")
    implementation("se.akerfeldt:okhttp-signpost:1.1.0")

    // widgets
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("me.grantland:autofittextview:0.2.1")
    // html-textview not maintained anymore, only available on jcenter - should be replaced in the long term
    implementation("org.sufficientlysecure:html-textview:3.9")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // box2d view
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")

    // serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("com.charleskorn.kaml:kaml:0.45.0")

    // map and location
    implementation("com.mapzen.tangram:tangram:0.17.1")

    // opening hours parser
    implementation("ch.poole:OpeningHoursParser:0.27.0")

    // faster sqlite library (additional capapilities like R*-tree or json1 not used)
    // performance comparison: https://github.com/streetcomplete/StreetComplete/issues/3609#issuecomment-1031177576
    implementation("com.github.requery:sqlite-android:3.36.0")
    implementation("androidx.sqlite:sqlite:2.1.0")

    // sunset-sunrise parser for lit quests
    implementation("com.luckycatlabs:SunriseSunsetCalculator:1.2")

    // measuring distance with AR
    implementation("com.google.ar:core:1.32.0")
    implementation("com.google.ar.sceneform:core:1.17.1")
}

/** Localizations that should be pulled from POEditor */
val bcp47ExportLanguages = setOf(
    "am", "ar", "ast", "bg", "bs", "ca", "cs", "da", "de", "el",
    "en", "en-AU", "en-GB", "eo", "es", "eu", "fa", "fi", "fr", "gl", "hr", "hu", "hy",
    "id", "it", "ja", "ko", "lt", "lv", "ml", "nb", "no", "nl", "nn", "pl", "pt", "pt-BR",
    "ro", "ru", "sk", "sr-cyrl", "sv", "th", "tr", "uk", "zh", "zh-CN", "zh-HK", "zh-TW"
)

// see https://github.com/osmlab/name-suggestion-index/tags for latest version
val nsiVersion = "v6.0.20220711"
// see https://github.com/openstreetmap/id-tagging-schema/releases for latest version
val presetsVersion = "v3.4.2"

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

tasks.register<UpdateAppTranslationsTask>("updateTranslations") {
    group = "streetcomplete"
    languageCodes = bcp47ExportLanguages
    apiToken = properties["POEditorAPIToken"] as String
    targetFiles = { "$projectDir/src/main/res/values-$it/strings.xml" }
}

tasks.register<UpdateAppTranslationCompletenessTask>("updateTranslationCompleteness") {
    group = "streetcomplete"
    apiToken = properties["POEditorAPIToken"] as String
    targetFiles = { "$projectDir/src/main/res/values-$it/translation_info.xml" }
}

tasks.register<UpdateMapStyleTask>("updateMapStyle") {
    group = "streetcomplete"
    targetDir = "$projectDir/src/main/assets/map_theme/jawg"
    mapStyleBranch = "jawg"
}

tasks.register<GenerateMetadataByCountry>("generateMetadataByCountry") {
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
