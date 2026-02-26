package de.westnordost.streetcomplete.osm.maxspeed

import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit.*
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class Speed(
    val value: Int,
    val unit: SpeedMeasurementUnit,
) {
    fun toKilometersPerHour(): Double = value * when (unit) {
        KILOMETERS_PER_HOUR -> 1.0
        MILES_PER_HOUR -> 1.60934
    }

    fun toOsmString(): String = when (unit) {
        KILOMETERS_PER_HOUR -> value.toString()
        MILES_PER_HOUR -> "$value mph"
    }
}
