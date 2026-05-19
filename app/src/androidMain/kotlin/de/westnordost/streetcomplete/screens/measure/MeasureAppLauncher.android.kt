package de.westnordost.streetcomplete.screens.measure

import android.content.ActivityNotFoundException
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.util.ktx.openUri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ArMeasureAppLauncher(
    private val activityProvider: () -> ComponentActivity,
) {
    actual suspend fun measure(
        lengthUnit: LengthUnit,
        measureVertical: Boolean
    ): ArMeasureResult = suspendCancellableCoroutine { continuation ->
        val activity = activityProvider()
        val registry = activity.activityResultRegistry

        var launcher: ActivityResultLauncher<MeasureContract.Params>? = null
        launcher = registry.register("app_measure", MeasureContract()) { length ->
            launcher?.unregister()
            if (length != null) {
                continuation.resume(ArMeasureResult.Success(length))
            } else {
                continuation.resume(ArMeasureResult.Error)
            }
        }

        try {
            launcher.launch(MeasureContract.Params(lengthUnit, measureVertical))
        } catch (e: ActivityNotFoundException) {
            launcher.unregister()
            activity.openUri("market://details?id=${ApplicationConstants.STREETMEASURE}")
            continuation.resume(ArMeasureResult.NotInstalled)
        }
    }
}
