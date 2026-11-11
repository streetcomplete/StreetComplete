package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.http.encodeURLParameter
import java.awt.Desktop
import java.io.IOException
import java.net.URI

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher = object : EmailAppLauncher {
    override fun isAvailable(): Boolean =
        Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)

    override fun compose(email: String, subject: String?, body: String?) {
        val params = listOfNotNull(
            subject?.let { "subject=${it.encodeURLParameter()}" },
            body?.let { "body=${it.encodeURLParameter()}" },
        )
        val query = if (params.isEmpty()) "" else params.joinToString("&", prefix = "?")
        try {
            Desktop.getDesktop().mail(URI("mailto:$email$query"))
        } catch (e: IOException) {
            Log.w("EmailAppLauncher", "Unable to open mail app", e)
        }
    }
}
