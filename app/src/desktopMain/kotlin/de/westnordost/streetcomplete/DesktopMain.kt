package de.westnordost.streetcomplete

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
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
    koin.get<ApplicationInitializer>().initialize()
    args.firstOrNull()?.let(koin.get<IncomingUriHandler>()::submit)

    application {
        Window(
            onCloseRequest = {
                applicationScope.cancel()
                stopKoin()
                exitApplication()
            },
            title = "StreetComplete",
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
        ) {
            ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                PreferenceAwareAppTheme { StreetCompleteApp() }
            }
        }
    }
}
