package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.maplibre.compose.location.IosLocationProvider
import org.maplibre.compose.location.LocationProvider

// Identical to the upstream maplibre-compose implementation.
@Composable actual fun rememberDefaultLocationProvider(): LocationProvider {
    val provider = remember { IosLocationProvider() }
    DisposableEffect(provider) { onDispose { provider.close() } }
    return provider
}
