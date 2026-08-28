package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.maplibre.compose.location.AndroidLocationProvider
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.createDefaultLocationProvider

@Composable
public actual fun rememberDefaultLocationProvider(): LocationProvider {
    val context = LocalContext.current
    AndroidLocationProvider(context)
    return remember(context) { createDefaultLocationProvider(context) }
}
