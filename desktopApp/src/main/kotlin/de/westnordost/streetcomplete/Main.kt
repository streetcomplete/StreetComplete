package de.westnordost.streetcomplete

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.v2.Window
import androidx.compose.ui.window.v2.rememberWindowState
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import org.maplibre.compose.desktop.rememberAwtComposeMapPresentationHost
import streetcomplete.desktopapp.generated.resources.Res
import streetcomplete.desktopapp.generated.resources.app_icon

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // appId is the directory name within the system locations for FileKit.filesDir / cacheDir:
    // - macOS: ~/Library/Application Support/<appId> / ~/Library/Caches/<appId>
    // - Linux: $XDG_DATA_HOME/<appId> / $XDG_CACHE_HOME/<appId>
    //          (or ~/.local/share/<appId> / ~/.cache/<appId>)
    // - Windows: %APPDATA%\<appId> / %LOCALAPPDATA%\<appId>\Cache
    FileKit.init(appId = DESKTOP_APP_ID)
    val koin = startKoin { modules(desktopModule, commonModule) }.koin
    koin.get<CrashReportsUncaughtExceptionHandler>().install()
    koin.get<ApplicationInitializer>().initialize()
    try {
        application(exitProcessOnExit = false) {
            Window(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(),
                title = "StreetComplete — Development",
                icon = painterResource(Res.drawable.app_icon),
                minSize = DpSize(320.dp, 320.dp),
            ) {
                ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                    App(uri = null, onConsumedUri = {})
                }
            }
        }
    } finally {
        stopKoin()
    }
}
