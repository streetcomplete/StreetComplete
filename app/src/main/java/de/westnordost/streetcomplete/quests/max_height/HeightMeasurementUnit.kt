package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.quests.max_height.HeightMeasurementUnit.*

enum class HeightMeasurementUnit(private val displayString: String) {
    METER("m"),
    FOOT_AND_INCH("ft");

    override fun toString() = displayString
}

fun String.toHeightMeasurementUnit() = when(this) {
    "meter" -> METER
    "foot and inch" -> FOOT_AND_INCH
    else -> throw UnsupportedOperationException("not implemented")
}
