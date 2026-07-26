package de.westnordost.streetcomplete.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

object DistanceFormatter {
    enum class UnitSystem(val unitInMeters: Double, val limit: Double) {
        METRIC(1.0, 1000.0),
        IMPERIAL_FEET(0.3048, 5280.0),
        IMPERIAL_YARDS(0.9144, 1760.0);

        companion object {
            fun fromRegion(region: String?): UnitSystem = when (region) {
                in regionsUsingFeetAndMiles -> IMPERIAL_FEET
                in regionsUsingYardsAndMiles -> IMPERIAL_YARDS
                else -> METRIC
            }
        }
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

    enum class DisplayUnit {
        METERS, KILOMETERS, FEET, YARDS, MILES
    }

    /**
     * Resolves the default UnitSystem for the current device (checking system measurement settings first, falling back to locale region).
     */
    @Composable
    fun defaultUnitSystem(region: String? = Locale.current.region): UnitSystem =
        systemDefaultUnitSystem() ?: UnitSystem.fromRegion(region)

    /**
     * Formats a raw distance in meters to a clean, localized string using the system default unit system.
     */
    @Composable
    fun format(distanceInMeters: Double): String =
        format(distanceInMeters, defaultUnitSystem())

    /**
     * Formats a raw distance in meters to a clean, localized string using Compose string resources.
     */
    @Composable
    fun format(distanceInMeters: Double, system: UnitSystem): String {
        val meters = stringResource(Res.string.meters_symbol)
        val kilometers = stringResource(Res.string.kilometers_symbol)
        val feet = stringResource(Res.string.feet_symbol)
        val yards = stringResource(Res.string.yards_symbol)
        val miles = stringResource(Res.string.miles_symbol)

        return format(distanceInMeters, system) { unit ->
            when (unit) {
                DisplayUnit.METERS -> meters
                DisplayUnit.KILOMETERS -> kilometers
                DisplayUnit.FEET -> feet
                DisplayUnit.YARDS -> yards
                DisplayUnit.MILES -> miles
            }
        }
    }

    /**
     * Non-composable core distance formatter that converts meters to the target unit system
     * and applies number rounding. Accepts a symbol provider for unit testing without Compose runtime.
     */
    fun format(
        distanceInMeters: Double,
        system: UnitSystem,
        getSymbol: (DisplayUnit) -> String
    ): String {
        val (value, unit) = formatToValueAndUnit(distanceInMeters, system)
        return formatForDisplay(value, getSymbol(unit))
    }

    /**
     * Converts distance in meters to a rounded value and target display unit.
     */
    fun formatToValueAndUnit(distanceInMeters: Double, system: UnitSystem): Pair<Double, DisplayUnit> {
        val distanceInUnit = distanceInMeters / system.unitInMeters
        return if (distanceInUnit >= system.limit) {
            val valueInLargerUnit = distanceInUnit / system.limit
            val roundedLargerUnit = (valueInLargerUnit * 10.0).roundToInt() / 10.0
            val unit = when (system) {
                UnitSystem.METRIC -> DisplayUnit.KILOMETERS
                else -> DisplayUnit.MILES
            }
            Pair(roundedLargerUnit, unit)
        } else {
            val roundedUnit = distanceInUnit.roundToInt().toDouble()
            val unit = when (system) {
                UnitSystem.METRIC -> DisplayUnit.METERS
                UnitSystem.IMPERIAL_FEET -> DisplayUnit.FEET
                UnitSystem.IMPERIAL_YARDS -> DisplayUnit.YARDS
            }
            Pair(roundedUnit, unit)
        }
    }

    private fun formatForDisplay(value: Double, symbol: String): String =
        if (value.toInt().toDouble() == value) "${value.toInt()} $symbol" else "$value $symbol"
}

