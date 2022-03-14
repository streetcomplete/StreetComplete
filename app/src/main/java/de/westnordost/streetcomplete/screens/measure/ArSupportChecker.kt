package de.westnordost.streetcomplete.screens.measure

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.google.ar.core.ArCoreApk

class ArSupportChecker(private val context: Context) {
    operator fun invoke(): Boolean =
        // extra requirements for Sceneform: min Android SDK and OpenGL ES 3.1
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
        && context.getSystemService<ActivityManager>()!!.deviceConfigurationInfo.glEsVersion.toDouble() >= 3.1
        // otherwise, ask ArCore
        && ArCoreApk.getInstance().checkAvailability(context).isSupported
}
