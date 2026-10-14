package de.westnordost.streetcomplete

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Gives Swift a simple API to update Compose state without exposing Compose types.
 *  Holds an incoming URL even when SwiftUI receives it before the composition is ready. */
class IosAppLinks {
    internal var uri by mutableStateOf<String?>(null)
        private set

    fun openUri(uri: String) { this.uri = uri }
    internal fun consume() { uri = null }
}

fun MainViewController(links: IosAppLinks): UIViewController = ComposeUIViewController {
    App(uri = links.uri, onConsumedUri = links::consume)
}
