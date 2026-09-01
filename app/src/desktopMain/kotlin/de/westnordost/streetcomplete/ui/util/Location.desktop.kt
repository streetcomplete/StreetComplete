package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.createDefaultLocationProvider

@Composable
public actual fun rememberDefaultLocationProvider(): LocationProvider {
    val provider = remember { createDefaultLocationProvider() }
    DisposableEffect(provider) {
        onDispose { provider.close() }
    }
    return provider
}
