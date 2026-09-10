package de.westnordost.streetcomplete

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import de.westnordost.streetcomplete.screens.main.DesktopMapAppLauncher
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import org.maplibre.compose.desktop.rememberAwtComposeMapPresentationHost

fun main(args: Array<String>) {
    startKoin { modules(desktopModule, commonModule) }
    val koin = GlobalContext.get()
    val applicationScope = koin.get<CoroutineScope>(named("ApplicationScope"))

    koin.get<de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder>()
    koin.get<ApplicationInitializer>().initialize(schedulePeriodicCleanup = true)
    args.firstOrNull()?.let(koin.get<IncomingUriHandler>()::submit)

    try {
        application {
            Window(
                onCloseRequest = ::exitApplication,
                title = "StreetComplete",
                state = rememberWindowState(width = 1200.dp, height = 800.dp),
            ) {
                ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                    PreferenceAwareAppTheme {
                        StreetCompleteApp(mapAppLauncher = DesktopMapAppLauncher)
                    }
                }
            }
        }
    } finally {
        // Compose disposes the window and all map presentations before native/database owners close.
        runBlocking { applicationScope.coroutineContext.job.cancelAndJoin() }
        stopKoin()
    }
}
