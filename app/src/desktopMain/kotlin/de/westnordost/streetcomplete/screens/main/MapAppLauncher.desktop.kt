package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberMapAppLauncher(): MapAppLauncher = object : MapAppLauncher {
    override fun isAvailable(): Boolean = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    override fun openAt(position: LatLon, zoom: Double) {
        Desktop.getDesktop().browse(URI("https://www.openstreetmap.org/#map=${zoom.toInt()}/${position.latitude}/${position.longitude}"))
    }
}
