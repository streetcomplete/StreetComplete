package de.westnordost.streetcomplete

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.screens.main.MapAppLauncher

/** iOS entry into the same application graph as Android and desktop. */
@Composable
fun IosApp(mapAppLauncher: MapAppLauncher, incomingUriHandler: IncomingUriHandler) {
    StreetCompleteApp(
        mapAppLauncher = mapAppLauncher,
        incomingUriHandler = incomingUriHandler,
    )
}
