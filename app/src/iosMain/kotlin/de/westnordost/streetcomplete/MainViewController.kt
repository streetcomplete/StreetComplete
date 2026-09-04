package de.westnordost.streetcomplete

import androidx.compose.ui.window.ComposeUIViewController
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.screens.main.map.MapPerformanceScenario
import de.westnordost.streetcomplete.screens.main.map.MapPerformanceDiagnostics
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import kotlin.native.ref.WeakReference
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIViewController

fun MainViewController(incomingUriHandler: IncomingUriHandler): UIViewController {
    val presentationHost = IosPresentationHost()
    val mapAppLauncher = IosMapAppLauncher(presentationHost::currentController)
    val runMapPerformanceScenario =
        NSProcessInfo.processInfo.environment["STREETCOMPLETE_MAP_PERFORMANCE"] == "1"
    MapPerformanceDiagnostics.enabled = runMapPerformanceScenario
    val controller = ComposeUIViewController {
        PreferenceAwareAppTheme {
            if (runMapPerformanceScenario) {
                MapPerformanceScenario()
            } else {
                IosApp(mapAppLauncher, incomingUriHandler)
            }
        }
    }
    presentationHost.controller = controller
    return controller
}

/** Keeps presentation host lookup scene-local without retaining the Compose controller. */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
private class IosPresentationHost {
    private var reference: WeakReference<UIViewController>? = null

    fun currentController(): UIViewController? = reference?.get()

    var controller: UIViewController?
        get() = currentController()
        set(value) {
            reference = value?.let(::WeakReference)
        }
}
