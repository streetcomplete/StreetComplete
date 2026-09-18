package de.westnordost.streetcomplete.util.ktx

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.net.toUri

fun Context.openUri(uri: String): Boolean =
    try {
        startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
