package de.westnordost.streetcomplete.screens.measure

import kotlinx.serialization.Serializable
import kotlin.math.floor
import kotlin.math.round

/** In which unit the measurement is displayed */
@Serializable
sealed class MeasureDisplayUnit {
    abstract fun format(distanceMeters: Float): String
}

/** Measurement displayed in meters rounded to the nearest [cmStep] */
@Serializable
data class MeasureDisplayUnitMeter(val cmStep: Int) : MeasureDisplayUnit() {
    init {
        require(cmStep > 0)
    }

    override fun format(distanceMeters: Float): String {
        val decimals = when {
            cmStep % 100 == 0 -> 0
            cmStep % 10 == 0 -> 1
            else -> 2
        }
        return "%.${decimals}f m".format(getRounded(distanceMeters))
    }

    /** Returns the given distance in meters rounded to the given precision */
    fun getRounded(distanceMeters: Float): Float =
        round(distanceMeters * 100 / cmStep) * cmStep / 100
}

/** Measurement displayed in feet+inch, inches rounded to nearest [inchStep]. Must be between 1-12 */
@Serializable
data class MeasureDisplayUnitFeetInch(val inchStep: Int) : MeasureDisplayUnit() {
    init {
        require(inchStep in 1..12)
    }

    override fun format(distanceMeters: Float): String {
        val (feet, inches) = getRounded(distanceMeters)
        return if (inches < 10) "$feet′ $inches″" else "$feet′$inches″"
    }

    /** Returns the given distance in meters as feet + inch */
    fun getRounded(distanceMeters: Float): Pair<Int, Int> {
        val distanceFeet = distanceMeters / 0.3048
        var feet = floor(distanceFeet).toInt()
        val inches = (distanceFeet - feet) * 12
        var inchesStepped = round(inches / inchStep).toInt() * inchStep
        if (inchesStepped == 12) {
            ++feet
            inchesStepped = 0
        }
        return Pair(feet, inchesStepped)
    }
}
