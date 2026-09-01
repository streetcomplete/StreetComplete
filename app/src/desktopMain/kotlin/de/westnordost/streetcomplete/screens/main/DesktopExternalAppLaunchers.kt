package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object DesktopMapAppLauncher : MapAppLauncher {
    override fun openAt(position: LatLon, zoom: Double) {
        browse(
            URI(
                "https://www.openstreetmap.org/" +
                    "?mlat=${position.latitude}&mlon=${position.longitude}" +
                    "#map=${zoom.toInt()}/${position.latitude}/${position.longitude}",
            ),
        )
    }

    override fun isAvailable(): Boolean = canBrowse()
}

object DesktopEmailAppLauncher : EmailAppLauncher {
    override fun compose(email: String, subject: String?, body: String?) {
        val query = buildList {
            subject?.let { add("subject=${it.urlEncoded()}") }
            body?.let { add("body=${it.urlEncoded()}") }
        }.joinToString("&")
        val suffix = query.takeIf(String::isNotEmpty)?.let { "?$it" }.orEmpty()
        val uri = URI("mailto:${email.urlEncoded()}$suffix")
        val desktop = Desktop.getDesktop()
        check(desktop.isSupported(Desktop.Action.MAIL)) {
            "Opening the default email application is not supported"
        }
        desktop.mail(uri)
    }

    override fun isAvailable(): Boolean =
        Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)
}

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher = DesktopEmailAppLauncher

private fun canBrowse(): Boolean =
    Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

private fun browse(uri: URI) {
    check(canBrowse()) { "Opening the default browser is not supported" }
    Desktop.getDesktop().browse(uri)
}

private fun String.urlEncoded(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)
