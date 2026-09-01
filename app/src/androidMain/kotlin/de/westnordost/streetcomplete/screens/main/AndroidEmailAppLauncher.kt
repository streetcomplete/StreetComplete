package de.westnordost.streetcomplete.screens.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

class AndroidEmailAppLauncher(private val context: Context) : EmailAppLauncher {
    override fun compose(email: String, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body != null) putExtra(Intent.EXTRA_TEXT, body)
        }
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    override fun isAvailable(): Boolean =
        Intent(Intent.ACTION_SENDTO)
            .apply { data = "mailto:".toUri() }
            .resolveActivity(context.packageManager) != null
}

@Composable
actual fun rememberEmailAppLauncher(): EmailAppLauncher {
    val context = LocalContext.current
    return AndroidEmailAppLauncher(context)
}
