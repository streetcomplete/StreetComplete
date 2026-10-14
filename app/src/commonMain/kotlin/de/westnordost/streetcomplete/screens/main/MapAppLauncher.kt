package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

interface MapAppLauncher {
    /** Open a map app at the given position */
    fun openAt(position: LatLon, zoom: Double)

    /** Return whether any map app is available */
    fun isAvailable(): Boolean
}

@Composable
expect fun rememberMapAppLauncher(): MapAppLauncher
