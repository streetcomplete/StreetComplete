package de.westnordost.streetcomplete

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.Preloader
import de.westnordost.streetcomplete.data.quest.AutoSyncer
import de.westnordost.streetcomplete.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import org.maplibre.compose.desktop.rememberAwtComposeMapPresentationHost

fun main() {
    startKoin { modules(desktopModule, commonModule) }
    val koin = GlobalContext.get()
    val applicationScope = koin.get<CoroutineScope>(named("ApplicationScope"))

    koin.get<de.westnordost.streetcomplete.util.error_reporting.CrashReportHolder>()
    applicationScope.launch { koin.get<Preloader>().preload() }
    koin.get<FeedsUpdater>().updateNow()

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
            val lifecycleOwner = LocalLifecycleOwner.current
            val autoSyncer = koin.get<AutoSyncer>()
            DisposableEffect(lifecycleOwner, autoSyncer) {
                lifecycleOwner.lifecycle.addObserver(autoSyncer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(autoSyncer) }
            }

            ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                AppTheme { StreetCompleteApp() }
            }
        }
    }
}
