package de.westnordost.streetcomplete.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toast

fun sendEmail(activity: Activity, email: String, subject: String, text: String? = null) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, ApplicationConstants.USER_AGENT + " " + subject)
        if (text != null) {
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    try {
        activity.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        activity.toast(R.string.no_email_client)
    }
}
