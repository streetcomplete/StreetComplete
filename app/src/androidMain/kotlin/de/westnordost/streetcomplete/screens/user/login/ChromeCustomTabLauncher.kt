package de.westnordost.streetcomplete.screens.user.login

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.R
import androidx.core.net.toUri

/**
 * Utility class to launch URLs in Chrome Custom Tabs for OAuth flows.
 */
object ChromeCustomTabLauncher {

    fun launchUrl(context: Context, url: String): Boolean {
        return try {
            val uri = url.toUri()
            val customTabsIntent = CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.primary))
                .setShowTitle(true)
                .build()

            customTabsIntent.launchUrl(context, uri)
            true
        } catch (e: Exception) {
            // Fallback to default browser if Custom Tabs fail
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(browserIntent)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    fun launchOAuthFlow(context: Context, authUrl: String, redirectScheme: String): Boolean {
        return launchUrl(context, authUrl)
    }
}

