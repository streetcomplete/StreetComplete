package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.length.Length

interface ArMeasureAppLauncher {
    fun measure(
        lengthUnit: LengthUnit,
        measureVertical: Boolean,
        onResult: (ArMeasureResult) -> Unit,
    )
}

sealed interface ArMeasureResult {
    data class Success(val length: Length) : ArMeasureResult
    data object Error : ArMeasureResult
    data object NotInstalled : ArMeasureResult
}

@Composable
expect fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher
