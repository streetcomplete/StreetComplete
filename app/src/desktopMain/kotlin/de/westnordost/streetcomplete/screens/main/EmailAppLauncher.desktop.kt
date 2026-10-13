package de.westnordost.streetcomplete.screens.main

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.util.logs.Log
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URLEncoder

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher = object : EmailAppLauncher {
    override fun isAvailable(): Boolean =
        Desktop.isDesktopSupported()
        && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)

    override fun compose(email: String, subject: String?, body: String?) {
        val params = listOfNotNull(
            subject?.let { "subject=${encode(it)}" },
            body?.let { "body=${encode(it)}" },
        )
        val query = if (params.isEmpty()) "" else params.joinToString("&", prefix = "?")
        try {
            Desktop.getDesktop().mail(URI("mailto:$email$query"))
        } catch (e: IOException) {
            Log.w("EmailAppLauncher", "Unable to open mail app", e)
        }
    }
}

// URLEncoder is for application/x-www-form-urlencoded, mailto URIs need %20 instead of +
private fun encode(s: String): String =
    URLEncoder.encode(s, Charsets.UTF_8).replace("+", "%20")
