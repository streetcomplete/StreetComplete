import java.io.FileInputStream
import java.util.Properties

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
        versionCode = 6308
        versionName = "64.0-alpha1"

        // no x86: the MapLibre Compose runtime has no x86 build, and other native libraries must
        // not make the app installable where the map cannot run
        ndk { abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64") }
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

    // Android / UI
    implementation("androidx.activity:activity-compose:1.13.0")

    // Compose
    implementation("org.jetbrains.compose.runtime:runtime:1.12.0")
    implementation("org.jetbrains.compose.ui:ui:1.12.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.10.0")

    // Dependency Injection
    implementation("io.insert-koin:koin-android:4.2.2")
    implementation("io.insert-koin:koin-androidx-compose:4.2.2")
    implementation("io.insert-koin:koin-androidx-workmanager:4.2.2")

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
