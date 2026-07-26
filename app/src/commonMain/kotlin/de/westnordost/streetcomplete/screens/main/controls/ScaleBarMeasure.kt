package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.FeetAndMiles
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.Metric
import de.westnordost.streetcomplete.screens.main.controls.ScaleBarMeasure.YardsAndMiles
import de.westnordost.streetcomplete.util.DistanceFormatter
import kotlin.math.pow

/** A measure to show in the scale bar */
interface ScaleBarMeasure {
    /** one unit of this measure in meters */
    val unitInMeters: Double

    /** List of stops, sorted ascending, at which the scale bar should show */
    val stops: List<Double>

    @Composable public fun getText(stop: Double): String

    /** A measure of meters and kilometers */
    data object Metric : ScaleBarMeasure {
        override val unitInMeters: Double = 1.0

        override val stops: List<Double> = buildStops(mantissas = listOf(1, 2, 5), exponents = -1..7)

        @Composable
        override fun getText(stop: Double): String =
            DistanceFormatter.format(stop * unitInMeters, DistanceFormatter.UnitSystem.METRIC)
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
            DistanceFormatter.format(stop * unitInMeters, DistanceFormatter.UnitSystem.IMPERIAL_FEET)
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
            DistanceFormatter.format(stop * unitInMeters, DistanceFormatter.UnitSystem.IMPERIAL_YARDS)
    }

    companion object {
        /** Returns the [ScaleBarMeasure] corresponding to the given [DistanceFormatter.UnitSystem]. */
        fun from(unitSystem: DistanceFormatter.UnitSystem): ScaleBarMeasure =
            when (unitSystem) {
                DistanceFormatter.UnitSystem.IMPERIAL_FEET -> FeetAndMiles
                DistanceFormatter.UnitSystem.IMPERIAL_YARDS -> YardsAndMiles
                DistanceFormatter.UnitSystem.METRIC -> Metric
            }
    }
}

/** build a list of stops by multiplying mantissas by 10^exponents, like scientific notation */
private fun buildStops(mantissas: List<Int>, exponents: IntRange) = buildList {
    for (e in exponents) for (m in mantissas) add(m * 10.0.pow(e))
}

/** countries using non-metric units will see both systems by default */
internal fun defaultSecondaryMeasure(primary: ScaleBarMeasure, region: String? = Locale.current.region): ScaleBarMeasure? =
    when (primary) {
        FeetAndMiles -> Metric
        YardsAndMiles -> Metric
        Metric ->
            when (DistanceFormatter.UnitSystem.fromRegion(region)) {
                DistanceFormatter.UnitSystem.IMPERIAL_FEET -> FeetAndMiles
                DistanceFormatter.UnitSystem.IMPERIAL_YARDS -> YardsAndMiles
                DistanceFormatter.UnitSystem.METRIC -> null
            }
        else -> null // should never happen because the primary is always one of the above
    }

/**
 * Default scale bar measures to use based on [DistanceFormatter.defaultUnitSystem].
 */
@Composable
internal fun defaultScaleBarMeasures(): ScaleBarMeasures {
    val primarySystem = DistanceFormatter.defaultUnitSystem()
    val primary = ScaleBarMeasure.from(primarySystem)
    return ScaleBarMeasures(primary = primary, secondary = defaultSecondaryMeasure(primary))
}
