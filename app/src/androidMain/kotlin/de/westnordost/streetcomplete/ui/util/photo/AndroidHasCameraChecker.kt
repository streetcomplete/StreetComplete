package de.westnordost.streetcomplete.ui.util.photo

import android.content.Context
import android.content.pm.PackageManager

class AndroidHasCameraChecker(private val context: Context) : HasCameraChecker {
    override fun invoke(): Boolean =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
}
