package de.westnordost.streetcomplete.screens.measure

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.getSystemService

class ArSupportChecker(private val context: Context) {
    operator fun invoke(): Boolean = hasArMeasureSupport(context)
}

private fun hasArMeasureSupport(context: Context): Boolean =
    // extra requirements for Sceneform: min Android SDK and OpenGL ES 3.1
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    && context.getSystemService<ActivityManager>()!!.deviceConfigurationInfo.glEsVersion.toDouble() >= 3.1
    /* Google Play is required to lead the user through installing the app */
    && (
        context.packageManager.getLaunchIntentForPackage("de.westnordost.streetmeasure") != null
        || context.packageManager.resolveActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=de.westnordost.streetmeasure")), 0) != null
    )
