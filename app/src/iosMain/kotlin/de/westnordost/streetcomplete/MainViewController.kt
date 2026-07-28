package de.westnordost.streetcomplete

import androidx.compose.ui.window.ComposeUIViewController
import de.westnordost.streetcomplete.ui.theme.AppTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    AppTheme {
        IosApp()
    }
}
