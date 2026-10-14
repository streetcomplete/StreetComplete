import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "de.westnordost.streetcomplete.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "de.westnordost.streetcomplete"
        minSdk = 25
        targetSdk = 37
        versionCode = 6404
        versionName = "64.0-beta2"

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
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    debugImplementation(libs.androidx.compose.uiTooling)

    // Dependency Injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidxCompose)
    implementation(libs.koin.androidxWorkmanager)

    coreLibraryDesugaring(libs.desugar.jdkLibs)
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
