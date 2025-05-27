package de.westnordost.streetcomplete.screens.main.controls

import android.icu.util.LocaleData
import android.icu.util.ULocale
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.FeetAndMiles
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.Metric
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.YardsAndMiles
import kotlin.math.pow

/** A measure to show in the scale bar */
interface ScaleBarMeasure {
    /** one unit of this measure in meters */
    val unitInMeters: Double

    /** List of stops, sorted ascending, at which the scalebar should show */
    val stops: List<Double>

    @Composable public fun getText(stop: Double): String

    /** A measure of meters and kilometers */
    data object Metric : ScaleBarMeasure {
        override val unitInMeters: Double = 1.0

        override val stops: List<Double> = buildStops(mantissas = listOf(1, 2, 5), exponents = -1..7)

        @Composable
        override fun getText(stop: Double): String =
            if (stop >= 1000) {
                (stop / 1000).formatForDisplay(stringResource(R.string.kilometers_symbol))
            } else {
                stop.formatForDisplay(stringResource(R.string.meters_symbol))
            }
    }

    /** A measure of international feet and miles */
    data object FeetAndMiles : ScaleBarMeasure {

        private const val FEET_IN_MILE: Int = 5280

        override val unitInMeters: Double = 0.3048

        override val stops: List<Double> =
            listOf(
                buildStops(mantissas = listOf(1, 2, 5), exponents = -1..3).dropLast(1),
                buildStops(mantissas = listOf(1, 2, 5), exponents = 0..4).map { it * FEET_IN_MILE },
            )
                .flatten()

        @Composable
        override fun getText(stop: Double): String =
            if (stop >= FEET_IN_MILE) {
                (stop / FEET_IN_MILE).formatForDisplay(stringResource(R.string.miles_symbol))
            } else {
                stop.formatForDisplay(stringResource(R.string.feet_symbol))
            }
    }

    /** A measure of international yard and miles */
    data object YardsAndMiles : ScaleBarMeasure {

        private const val YARDS_IN_MILE: Int = 1760

        override val unitInMeters: Double = 0.9144

        override val stops: List<Double> =
            listOf(
                buildStops(mantissas = listOf(1, 2, 5), exponents = -1..3).dropLast(2),
                buildStops(mantissas = listOf(1, 2, 5), exponents = 0..4).map { it * YARDS_IN_MILE },
            )
                .flatten()

        @Composable
        override fun getText(stop: Double): String =
            if (stop >= YARDS_IN_MILE) {
                (stop / YARDS_IN_MILE).formatForDisplay(stringResource(R.string.miles_symbol))
            } else {
                stop.formatForDisplay(stringResource(R.string.yards_symbol))
            }
    }
}

/** format a number with a unit symbol, not showing the decimal point if it's an integer */
private fun Double.formatForDisplay(symbol: String) =
    if (this.toInt().toDouble() == this) "${this.toInt()} $symbol" else "$this $symbol"

/** build a list of stops by multiplying mantissas by 10^exponents, like scientific notation */
private fun buildStops(mantissas: List<Int>, exponents: IntRange) = buildList {
    for (e in exponents) for (m in mantissas) add(m * 10.0.pow(e))
}

/** use system locale APIs for the primary scale bar measure */
@Composable
internal fun systemDefaultPrimaryMeasure(): ScaleBarMeasure? {
    if (android.os.Build.VERSION.SDK_INT < 28) return null
    val locales = LocalContext.current.resources.configuration.locales
    if (locales.isEmpty) return null
    return when (LocaleData.getMeasurementSystem(ULocale.forLocale(locales[0]))) {
        LocaleData.MeasurementSystem.SI -> Metric
        LocaleData.MeasurementSystem.US -> FeetAndMiles
        LocaleData.MeasurementSystem.UK -> YardsAndMiles
        else -> null
    }
}

/** if the system APIs don't provide a primary measure, fall back to our hardcoded lists */
internal fun fallbackDefaultPrimaryMeasure(region: String?): ScaleBarMeasure =
    when (region) {
        in regionsUsingFeetAndMiles -> FeetAndMiles
        in regionsUsingYardsAndMiles -> YardsAndMiles
        else -> Metric
    }

/** countries using non-metric units will see both systems by default */
internal fun defaultSecondaryMeasure(primary: ScaleBarMeasure, region: String?): ScaleBarMeasure? =
    when (primary) {
        FeetAndMiles -> Metric
        YardsAndMiles -> Metric
        Metric ->
            when (region) {
                in regionsUsingFeetAndMiles -> FeetAndMiles
                in regionsUsingYardsAndMiles -> YardsAndMiles
                else -> null
            }
        else -> null // should never happen because the primary is always one of the above
    }

private val regionsUsingFeetAndMiles =
    setOf(
        // United states and its unincorporated territories
        "US",
        "AS",
        "GU",
        "MP",
        "PR",
        "VI",
        // former United states territories / Compact of Free Association
        "FM",
        "MH",
        "PW",
        // Liberia
        "LR",
    )

private val regionsUsingYardsAndMiles =
    setOf(
        // United kingdom with its overseas territories and crown dependencies
        "GB",
        "AI",
        "BM",
        "FK",
        "GG",
        "GI",
        "GS",
        "IM",
        "IO",
        "JE",
        "KY",
        "MS",
        "PN",
        "SH",
        "TC",
        "VG",
        // former British overseas territories / colonies
        "BS",
        "BZ",
        "GD",
        "KN",
        "VC",
        // Myanmar
        "MM",
    )

/**
 * default scale bar measures to use, depending on the user's locale (or system preferences, if
 * available)
 */
@Composable
internal fun defaultScaleBarMeasures(): ScaleBarMeasures {
    val region = Locale.current.region
    val primary = systemDefaultPrimaryMeasure() ?: fallbackDefaultPrimaryMeasure(region)
    return ScaleBarMeasures(primary = primary, secondary = defaultSecondaryMeasure(primary, region))
}
