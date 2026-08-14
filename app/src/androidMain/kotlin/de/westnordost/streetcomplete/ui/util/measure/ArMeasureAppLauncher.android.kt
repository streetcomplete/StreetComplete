package de.westnordost.streetcomplete.ui.util.measure

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.length.Length
import de.westnordost.streetcomplete.util.ktx.openUri

@Composable
actual fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher {
    val context = LocalContext.current
    var currentCallback by remember { mutableStateOf<((ArMeasureResult) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ArMeasureContract(),
    ) { length: Length? ->
        if (length != null) {
            currentCallback?.invoke(ArMeasureResult.Success(length))
        } else {
            currentCallback?.invoke(ArMeasureResult.Error)
        }
        currentCallback = null
    }

    return object : ArMeasureAppLauncher {
        override fun measure(
            lengthUnit: LengthUnit,
            measureVertical: Boolean,
            onResult: (ArMeasureResult) -> Unit,
        ) {
            currentCallback = onResult
            try {
                launcher.launch(ArMeasureContract.Params(lengthUnit, measureVertical))
            } catch (e: ActivityNotFoundException) {
                context.openUri("market://details?id=${ApplicationConstants.STREETMEASURE}")
                onResult(ArMeasureResult.NotInstalled)
            }
        }
    }
}
