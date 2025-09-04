package de.westnordost.streetcomplete.screens.main.controls

import android.icu.util.LocaleData
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.FeetAndMiles
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.Metric
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.YardsAndMiles
import androidx.compose.ui.platform.LocalConfiguration

/** use system locale APIs for the primary scale bar measure */
@Composable
internal actual fun systemDefaultPrimaryMeasure(): ScaleBarMeasure? {
    if (android.os.Build.VERSION.SDK_INT < 28) return null
    val locales = LocalConfiguration.current.locales
    if (locales.isEmpty) return null
    return when (LocaleData.getMeasurementSystem(ULocale.forLocale(locales[0]))) {
        LocaleData.MeasurementSystem.SI -> Metric
        LocaleData.MeasurementSystem.US -> FeetAndMiles
        LocaleData.MeasurementSystem.UK -> YardsAndMiles
        else -> null
    }
}
