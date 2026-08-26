package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import org.maplibre.compose.location.LocationProvider

// TODO maplibre-compose: can be deleted once map is based on maplibre-compose
@Composable
public expect fun rememberDefaultLocationProvider(): LocationProvider
