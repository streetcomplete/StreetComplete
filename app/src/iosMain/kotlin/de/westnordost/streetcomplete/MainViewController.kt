package de.westnordost.streetcomplete

import androidx.compose.ui.window.ComposeUIViewController
import de.westnordost.streetcomplete.screens.main.IosMapAppLauncher
import de.westnordost.streetcomplete.ui.theme.PreferenceAwareAppTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    lateinit var controller: UIViewController
    val mapAppLauncher = IosMapAppLauncher { controller }
    controller = ComposeUIViewController {
        PreferenceAwareAppTheme {
            IosApp(mapAppLauncher)
        }
    }
    return controller
}
