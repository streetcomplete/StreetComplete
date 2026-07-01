package de.westnordost.streetcomplete.ui.util.measure

import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.util.ktx.openUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidArMeasureAppLauncher(
    private val activityProvider: () -> ComponentActivity,
) : ArMeasureAppLauncher {
    override suspend fun measure(
        lengthUnit: LengthUnit,
        measureVertical: Boolean
    ): ArMeasureResult = suspendCancellableCoroutine { continuation ->
        val activity = activityProvider()
        val registry = activity.activityResultRegistry

        var launcher: ActivityResultLauncher<ArMeasureContract.Params>? = null
        launcher = registry.register("app_measure", ArMeasureContract()) { length ->
            launcher?.unregister()
            if (length != null) {
                continuation.resume(ArMeasureResult.Success(length))
            } else {
                continuation.resume(ArMeasureResult.Error)
            }
        }

        try {
            launcher.launch(ArMeasureContract.Params(lengthUnit, measureVertical))
        } catch (e: ActivityNotFoundException) {
            launcher.unregister()
            activity.openUri("market://details?id=${ApplicationConstants.STREETMEASURE}")
            continuation.resume(ArMeasureResult.NotInstalled)
        }
    }
}
