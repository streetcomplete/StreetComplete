package de.westnordost.streetcomplete.util

import android.icu.util.LocaleData
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
internal actual fun systemDefaultUnitSystem(): DistanceFormatter.UnitSystem? {
    if (android.os.Build.VERSION.SDK_INT < 28) return null
    val locales = LocalConfiguration.current.locales
    if (locales.isEmpty) return null
    return when (LocaleData.getMeasurementSystem(ULocale.forLocale(locales[0]))) {
        LocaleData.MeasurementSystem.SI -> DistanceFormatter.UnitSystem.METRIC
        LocaleData.MeasurementSystem.US -> DistanceFormatter.UnitSystem.IMPERIAL_FEET
        LocaleData.MeasurementSystem.UK -> DistanceFormatter.UnitSystem.IMPERIAL_YARDS
        else -> null
    }
}
