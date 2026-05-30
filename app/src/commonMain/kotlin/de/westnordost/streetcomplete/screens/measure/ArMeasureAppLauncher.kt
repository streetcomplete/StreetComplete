package de.westnordost.streetcomplete.screens.measure

import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.length.Length

expect class ArMeasureAppLauncher {
    suspend fun measure(lengthUnit: LengthUnit, measureVertical: Boolean): ArMeasureResult
}

sealed interface ArMeasureResult {
    data class Success(val length: Length) : ArMeasureResult
    data object Error : ArMeasureResult
    data object NotInstalled : ArMeasureResult
}
