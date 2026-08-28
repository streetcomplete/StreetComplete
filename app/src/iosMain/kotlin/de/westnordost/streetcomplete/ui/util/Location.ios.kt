package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.maplibre.compose.location.IosLocationProvider
import org.maplibre.compose.location.LocationProvider

@Composable actual fun rememberDefaultLocationProvider(): LocationProvider = remember {
    IosLocationProvider()
}
