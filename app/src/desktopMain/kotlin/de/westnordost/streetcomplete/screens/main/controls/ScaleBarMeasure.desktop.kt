package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.FeetAndMiles
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.Metric
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.YardsAndMiles
import java.util.Locale

@Composable
internal actual fun systemDefaultPrimaryMeasure(): ScaleBarMeasure? {
    val locale = Locale.getDefault()
    return when (locale.getUnicodeLocaleType("ms")) {
        "metric" -> Metric
        "ussystem" -> FeetAndMiles
        "uksystem" -> YardsAndMiles
        else -> fallbackDefaultPrimaryMeasure(locale.country.uppercase())
    }
}
