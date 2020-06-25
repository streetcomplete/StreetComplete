package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.quests.max_speed.SpeedMeasurementUnit.*

enum class SpeedMeasurementUnit(private val displayString: String) {
    KILOMETERS_PER_HOUR("km/h"),
    MILES_PER_HOUR("mph");

    override fun toString() = displayString
}

fun String.toSpeedMeasurementUnit() = when(this) {
    "kilometers per hour" -> KILOMETERS_PER_HOUR
    "miles per hour" -> MILES_PER_HOUR
    else -> throw UnsupportedOperationException("not implemented")
}
