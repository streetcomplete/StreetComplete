package de.westnordost.streetcomplete.quests.max_speed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SpeedMeasurementUnit(private val displayString: String) {
    @SerialName("kilometers per hour") KILOMETERS_PER_HOUR("km/h"),
    @SerialName("miles per hour") MILES_PER_HOUR("mph");

    override fun toString() = displayString
}
