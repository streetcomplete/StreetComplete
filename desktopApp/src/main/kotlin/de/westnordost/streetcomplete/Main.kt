package de.westnordost.streetcomplete

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.WindowScaffold
import dev.nucleusframework.window.material2.MaterialDecoratedWindow
import dev.nucleusframework.window.material2.MaterialTitleBar
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import streetcomplete.desktopapp.generated.resources.Res
import streetcomplete.desktopapp.generated.resources.app_icon

fun main(args: Array<String>) {
    // appId is the directory name within the system locations for FileKit.filesDir / cacheDir:
    // - macOS: ~/Library/Application Support/<appId> / ~/Library/Caches/<appId>
    // - Linux: $XDG_DATA_HOME/<appId> / $XDG_CACHE_HOME/<appId>
    //          (or ~/.local/share/<appId> / ~/.cache/<appId>)
    // - Windows: %APPDATA%\<appId> / %LOCALAPPDATA%\<appId>\Cache
    FileKit.init(appId = DESKTOP_APP_ID)
    val koin = startKoin { modules(desktopModule, commonModule) }.koin
    // Nucleus exits the process when the app is closed, so this can't be done after it returns
    Runtime.getRuntime().addShutdownHook(Thread { stopKoin() })
    koin.get<CrashReportsUncaughtExceptionHandler>().install()
    koin.get<ApplicationInitializer>().initialize()
    nucleusApplication(args) {
        // TODO desktop: the window decoration follows the system theme, not the theme chosen in
        //      the settings, as that is only read inside App (by AppViewModel)
        AppTheme(isSystemInDarkTheme()) {
            MaterialDecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(),
                title = "StreetComplete — Development",
                icon = painterResource(Res.drawable.app_icon),
                minimumSize = DpSize(320.dp, 320.dp),
            ) {
                WindowScaffold(titleBar = { MaterialTitleBar() }) {
                    // the map needs the window's GPU context, which Tao has after the first frame
                    val mapHost = rememberTaoComposeMapPresentationHost() ?: return@WindowScaffold
                    ProvideMapPresentationHost(mapHost) {
                        App(uri = null, onConsumedUri = {})
                    }
                }
            }
        }
    }
}
