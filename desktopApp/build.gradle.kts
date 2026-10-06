plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
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

dependencies {
    // the webview used for login needs KCEF, which is not on Maven Central; login is unavailable here anyway
    implementation(project(":app")) { exclude(group = "dev.datlag") }
    implementation(compose.desktop.currentOs)
    implementation("io.insert-koin:koin-core:4.2.2")
    implementation("org.maplibre.compose:maplibre-compose:0.19.0")
    implementation("io.github.vinceglb:filekit-core:0.16.0")
    runtimeOnly("org.maplibre.compose:maplibre-compose-runtime-$mapBackend-$hostOs-$hostArch:0.19.0")
}

compose.desktop {
    application {
        mainClass = "de.westnordost.streetcomplete.MainKt"
        jvmArgs += listOf(
            "--enable-native-access=ALL-UNNAMED",
            "-Dstreetcomplete.resources=${rootProject.file("app/src/commonMain/composeResources/files")}",
        )
        if (hostOs == "macos") {
            // Ad-hoc signing changes native library hashes in the local app bundle.
            jvmArgs += "-Dorg.lwjgl.util.NoHashChecks=true"
        }
        nativeDistributions {
            packageName = "StreetCompleteDev"
            packageVersion = "1.0.0"
            modules("java.prefs", "jdk.unsupported")
            macOS {
                bundleID = "de.westnordost.streetcomplete.desktop"
                entitlementsFile.set(file("entitlements.plist"))
                runtimeEntitlementsFile.set(file("entitlements.plist"))
                infoPlist {
                    extraKeysRawXml = """
                        <key>NSLocationWhenInUseUsageDescription</key>
                        <string>StreetComplete uses your location to display nearby quests.</string>
                        <key>NSLocationUsageDescription</key>
                        <string>StreetComplete uses your location to display nearby quests.</string>
                    """.trimIndent()
                }
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
