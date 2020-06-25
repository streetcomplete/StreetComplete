package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.*

enum class WeightMeasurementUnit(private val displayString: String) {
    TON("t"),
    SHORT_TON("t"),
    POUND("lbs");

    override fun toString() = displayString
}

fun String.toWeightMeasurementUnit() = when(this) {
    "ton" -> TON
    "short ton" -> SHORT_TON
    "pound" -> POUND
    else -> throw UnsupportedOperationException("not implemented")
}
