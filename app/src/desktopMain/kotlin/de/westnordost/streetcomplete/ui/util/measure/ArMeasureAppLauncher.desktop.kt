package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.LengthUnit

@Composable
actual fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher = object : ArMeasureAppLauncher {
    override fun measure(lengthUnit: LengthUnit, measureVertical: Boolean, onResult: (ArMeasureResult) -> Unit) {
        onResult(ArMeasureResult.NotInstalled)
    }
}
