package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.LengthUnit

/** StreetMeasure has no desktop application or documented desktop URI contract. */
@Composable
actual fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher = remember {
    object : ArMeasureAppLauncher {
        override fun measure(
            lengthUnit: LengthUnit,
            measureVertical: Boolean,
            onResult: (ArMeasureResult) -> Unit,
        ) {
            // TODO(multiplatform): Launch a desktop AR tool if StreetMeasure defines a protocol.
            onResult(ArMeasureResult.NotInstalled)
        }
    }
}
