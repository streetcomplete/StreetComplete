package de.westnordost.streetcomplete

import androidx.compose.ui.window.ComposeUIViewController
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import kotlin.native.ref.WeakReference
import platform.UIKit.UIViewController

fun MainViewController(incomingUriHandler: IncomingUriHandler): UIViewController {
    val presentationHost = IosPresentationHost()
    val mapAppLauncher = IosMapAppLauncher(presentationHost::currentController)
    val controller = ComposeUIViewController {
        PreferenceAwareAppTheme {
            IosApp(mapAppLauncher, incomingUriHandler)
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
