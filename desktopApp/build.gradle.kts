plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(25)
}

val hostOs = when {
    System.getProperty("os.name").startsWith("Mac") -> "macos"
    System.getProperty("os.name").startsWith("Windows") -> "windows"
    else -> "linux"
}
val hostArch = when (System.getProperty("os.arch")) {
    "aarch64", "arm64" -> "arm64"
    "amd64", "x86_64" -> "x64"
    else -> error("Unsupported desktop architecture: ${System.getProperty("os.arch")}")
}
val mapRuntime = when ("$hostOs-$hostArch") {
    "macos-arm64" -> libs.maplibre.runtime.macosArm64
    "macos-x64" -> libs.maplibre.runtime.macosX64
    "windows-arm64" -> libs.maplibre.runtime.windowsArm64
    "windows-x64" -> libs.maplibre.runtime.windowsX64
    "linux-arm64" -> libs.maplibre.runtime.linuxArm64
    "linux-x64" -> libs.maplibre.runtime.linuxX64
    else -> error("Unsupported desktop platform: $hostOs-$hostArch")
}

dependencies {
    implementation(project(":app"))
    implementation(compose.desktop.currentOs)

    // Dependency injection
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)

    // Compose
    implementation(libs.compose.resources)

    // File access
    implementation(libs.filekit.core)

    // Map
    implementation(libs.maplibre.compose)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decoratedWindowTao)
    runtimeOnly(mapRuntime)
}

nucleus.application {
    mainClass = "de.westnordost.streetcomplete.MainKt"
    // Nucleus already enables native access, which MapLibre needs, see
    // https://maplibre-compose.pages.dev/maplibre-compose/platform-setup/#java-and-native-access
    val resourcesDir = rootProject.file("app/src/commonMain/composeResources/files")
    jvmArgs += "-Dstreetcomplete.resources=$resourcesDir"
    if (hostOs == "macos") {
        // the .app bundle (see below) is signed, which changes LWJGL's native libraries, see
        // https://maplibre-compose.pages.dev/maplibre-compose/platform-setup/#signed-packages
        jvmArgs += "-Dorg.lwjgl.util.NoHashChecks=true"
    }
    nativeDistributions {
        packageName = "StreetCompleteDev"
        // TODO desktop: sync with versionName in androidApp and MARKETING_VERSION in iosApp
        packageVersion = "1.0.0"
        modules("java.prefs", "jdk.unsupported")
        macOS {
            // keep in sync with DESKTOP_APP_ID in DesktopModule.kt
            bundleID = "de.westnordost.streetcomplete.desktop"
            entitlementsFile.set(file("entitlements.plist"))
            runtimeEntitlementsFile.set(file("entitlements.plist"))
            // copied from no_location_permission_warning in strings.xml. Translations are in
            // macosLocalizations, see the app:updateMacosTranslations task
            val locationDescription = "Precise location permission is needed to show your " +
                "position on the map and download data in your vicinity."
            infoPlist {
                extraKeysRawXml = """
                    <key>NSLocationWhenInUseUsageDescription</key>
                    <string>$locationDescription</string>
                    <key>NSLocationUsageDescription</key>
                    <string>$locationDescription</string>
                """.trimIndent()
                localizationsDir = file("macosLocalizations")
            }
        }
    }
}

// Launch a local .app on macOS, as Core Location usage descriptions are read from the app bundle.
if (hostOs == "macos") {
    tasks.configureEach {
        if (name == "run") {
            enabled = false
            dependsOn("runDistributable")
        }
    }
}
