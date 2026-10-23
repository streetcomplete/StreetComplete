package de.westnordost.streetcomplete

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.flow.MutableStateFlow
import platform.UIKit.UIViewController

fun MainViewController(pendingUri: MutableStateFlow<String?>): UIViewController = ComposeUIViewController {
    val uri by pendingUri.collectAsState()
    App(
        uri = uri,
        onConsumedUri = { pendingUri.value = null },
    )
}
