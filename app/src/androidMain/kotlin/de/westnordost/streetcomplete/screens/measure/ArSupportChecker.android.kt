package de.westnordost.streetcomplete.screens.measure

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.util.ktx.isPackageInstalled

actual class ArSupportChecker(private val context: Context) {
    actual operator fun invoke(): Boolean =
        // extra requirement for Sceneform: OpenGL ES 3.1
        context.getSystemService<ActivityManager>()!!.deviceConfigurationInfo.glEsVersion.toDouble() >= 3.1
        // Google Play is required to lead the user through installing the app
        && (
            // app is already installed
            context.packageManager.isPackageInstalled(ApplicationConstants.STREETMEASURE)
            // or at least google play is installed
            || context.packageManager.isPackageInstalled("com.android.vending")
        )
}
