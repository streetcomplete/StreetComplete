package de.westnordost.streetcomplete.ui.util.measure

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import de.westnordost.streetcomplete.data.meta.LengthUnit

@Composable
actual fun rememberArMeasureAppLauncher(): ArMeasureAppLauncher = remember {
    object : ArMeasureAppLauncher {
        override fun measure(
            lengthUnit: LengthUnit,
            measureVertical: Boolean,
            onResult: (ArMeasureResult) -> Unit,
        ) {
            // TODO(multiplatform): Launch StreetMeasure when it offers an iOS result protocol.
            // ArSupportChecker keeps this unreachable in the UI in the meantime.
            onResult(ArMeasureResult.Error)
        }
    }
}
