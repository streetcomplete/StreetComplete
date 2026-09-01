import java.io.FileInputStream
import java.util.Properties
import java.util.zip.ZipFile

val appVersionName = "63.4"
val appVersionCode = 6308

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "de.westnordost.streetcomplete.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete"
        minSdk = 25
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        create("release") {
        }
    }

    buildTypes {
        all {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            testProguardFile("test-proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(project(":app"))

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // Android / UI
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.fragment:fragment-ktx:1.8.6")

    // Compose
    implementation("org.jetbrains.compose.runtime:runtime:1.12.0")
    implementation("org.jetbrains.compose.ui:ui:1.12.0")
    implementation("org.jetbrains.compose.material:material:1.12.0")
    implementation("org.jetbrains.compose.components:components-resources:1.12.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.10.0")

    // location
    implementation("org.maplibre.compose:location:0.15.0")

    // Dependency Injection
    implementation("io.insert-koin:koin-android:4.2.2")
    implementation("io.insert-koin:koin-compose:4.2.2")
    implementation("io.insert-koin:koin-androidx-workmanager:4.2.2")

    // Settings
    implementation("com.russhwolf:multiplatform-settings:1.3.0")

    // Database / IO
    implementation("androidx.sqlite:sqlite-bundled:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.9.1")

    // HTTP Client
    implementation("io.ktor:ktor-client-android:3.5.1")

    // finding OSM features
    implementation("de.westnordost:osmfeatures:8.0.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
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

tasks.register("verifyDebugMapGlyphAssets") {
    group = "verification"
    description = "Verifies all shared MapLibre glyphs are packaged in the debug APK."
    dependsOn("assembleDebug")
    val apk = layout.buildDirectory.file("outputs/apk/debug/androidApp-debug.apk")
    inputs.file(apk)
    doLast {
        val prefix = "assets/composeResources/de.westnordost.streetcomplete.resources/files/glyphs/"
        ZipFile(apk.get().asFile).use { zip ->
            val glyphs = zip.entries().asSequence()
                .filter { !it.isDirectory && it.name.startsWith(prefix) && it.name.endsWith(".pbf") }
                .toList()
            check(glyphs.size == 512) { "Expected 512 shared glyph assets, found ${glyphs.size}" }
            check(glyphs.all { it.size > 0L }) { "Shared glyph assets in the APK must not be empty" }
        }
    }
}
