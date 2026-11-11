package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.logs.Log
import java.awt.Desktop
import java.io.IOException
import java.net.URI

@Composable
actual fun rememberMapAppLauncher(): MapAppLauncher = object : MapAppLauncher {
    override fun isAvailable(): Boolean =
        Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

    override fun openAt(position: LatLon, zoom: Double) {
        val latitude = position.latitude
        val longitude = position.longitude
        val url = URI("https://www.openstreetmap.org/#map=${zoom.toInt()}/$latitude/$longitude")
        try {
            Desktop.getDesktop().browse(url)
        } catch (e: IOException) {
            Log.w("MapAppLauncher", "Unable to open $url", e)
        }
    }
}
