package de.westnordost.streetcomplete

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import de.westnordost.streetcomplete.data.DesktopPeriodicCleaner
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.ui.theme.rememberAppTheme
import de.westnordost.streetcomplete.util.error_reporting.CrashReportsUncaughtExceptionHandler
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.TitleBar
import dev.nucleusframework.window.WindowAppearance
import dev.nucleusframework.window.WindowAppearanceMode
import dev.nucleusframework.window.WindowScaffold
import dev.nucleusframework.window.styling.LocalTitleBarStyle
import io.github.vinceglb.filekit.FileKit
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import streetcomplete.desktopapp.generated.resources.Res
import streetcomplete.desktopapp.generated.resources.app_icon
import kotlin.system.exitProcess

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
    // the OS may start the app just to run a scheduled task, then the UI is not shown
    if (koin.get<DesktopPeriodicCleaner>().runIfScheduled(args)) exitProcess(0)
    nucleusApplication(args) {
        val theme = rememberAppTheme()
        NucleusDecoratedWindowTheme(isDark = theme.isDark) {
            // TODO desktop: use native window decorations when supported, see
            //      https://github.com/NucleusFramework/Nucleus/issues/841
            DecoratedWindow(
                onCloseRequest = ::exitApplication,
                state = rememberWindowState(),
                title = "StreetComplete — Development",
                icon = painterResource(Res.drawable.app_icon),
                minimumSize = DpSize(320.dp, 320.dp),
            ) {
                WindowAppearance(when (theme) {
                    Theme.LIGHT -> WindowAppearanceMode.Light
                    Theme.DARK -> WindowAppearanceMode.Dark
                    Theme.SYSTEM -> WindowAppearanceMode.System
                })
                WindowScaffold(titleBar = {
                    val style = LocalTitleBarStyle.current
                    TitleBar(style = style.copy(metrics = style.metrics.copy(height = 28.dp)))
                }) {
                    // the map needs the window's GPU context, which Tao has after the first frame
                    val mapHost = rememberTaoComposeMapPresentationHost() ?: return@WindowScaffold
                    ProvideMapPresentationHost(mapHost) {
                        App(uri = null, onConsumedUri = {}, theme = theme)
                    }
                }
            }
        }
    }
}
