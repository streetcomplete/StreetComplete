package de.westnordost.streetcomplete

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import org.koin.compose.viewmodel.koinViewModel
import platform.UIKit.UIViewController

/** Holds an incoming URL even when SwiftUI receives it before the composition is ready. */
class IosAppLinks {
    internal var uri by mutableStateOf<String?>(null)
        private set

    fun openUri(uri: String) { this.uri = uri }
    internal fun consume() { uri = null }
}

fun MainViewController(links: IosAppLinks): UIViewController = ComposeUIViewController {
    val viewModel = koinViewModel<AppViewModel>()
    LaunchedEffect(links.uri) {
        links.uri?.let(viewModel::openUri)
        links.consume()
    }
    App(viewModel)
}
