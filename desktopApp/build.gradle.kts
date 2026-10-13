plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
    id("dev.nucleusframework")
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
val mapBackend = if (hostOs == "macos") "metal" else "vulkan"
// a development build of Nucleus 3.0, as it fixes some issues with maplibre-compose. Update to 3.0
// once released, see https://github.com/maplibre/maplibre-compose/pull/1808
// (keep in sync with the plugin version in the root build.gradle.kts)
val nucleusVersion = "3.0.0-dev-202610121021"

dependencies {
    implementation(project(":app"))
    implementation(compose.desktop.currentOs)

    // Dependency injection
    implementation("io.insert-koin:koin-core:4.2.2")

    // Compose
    implementation("org.jetbrains.compose.components:components-resources:1.12.1")

    // File access
    implementation("io.github.vinceglb:filekit-core:0.16.0")

    // Map
    implementation("org.maplibre.compose:maplibre-compose:0.20.0")
    implementation("dev.nucleusframework:nucleus.nucleus-application:$nucleusVersion")
    implementation("dev.nucleusframework:nucleus.decorated-window-tao:$nucleusVersion")
    implementation("dev.nucleusframework:nucleus.decorated-window-material2:$nucleusVersion")
    runtimeOnly("org.maplibre.compose:maplibre-compose-runtime-$mapBackend-$hostOs-$hostArch:0.20.0")
}

nucleus.application {
    mainClass = "de.westnordost.streetcomplete.MainKt"
    // Nucleus already enables native access, which MapLibre needs, see
    // https://maplibre-compose.pages.dev/maplibre-compose/platform-setup/#java-and-native-access
    val resourcesDir = rootProject.file("app/src/commonMain/composeResources/files")
    jvmArgs += "-Dstreetcomplete.resources=$resourcesDir"
    if (hostOs == "macos") {
        // Tao records the wrong UI thread without this flag, see
        // https://github.com/NucleusFramework/Nucleus/issues/829
        jvmArgs += "-XstartOnFirstThread"
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
            // TODO macOS: these strings are copied from no_location_permission_warning in
            //      strings.xml, and only in English. Translations are in values-*/strings.xml,
            //      but would need to be added to the .app bundle as
            //      <lang>.lproj/InfoPlist.strings, which Nucleus doesn't support yet, see
            //      https://github.com/NucleusFramework/Nucleus/issues/833
            //      (For iOS, see UpdateIosAppTranslationsTask)
            val locationDescription = "Precise location permission is needed to show your " +
                "position on the map and download data in your vicinity."
            infoPlist {
                extraKeysRawXml = """
                    <key>NSLocationWhenInUseUsageDescription</key>
                    <string>$locationDescription</string>
                    <key>NSLocationUsageDescription</key>
                    <string>$locationDescription</string>
                """.trimIndent()
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
