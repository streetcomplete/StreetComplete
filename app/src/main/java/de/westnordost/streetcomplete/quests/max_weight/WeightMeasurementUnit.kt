package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.*

enum class WeightMeasurementUnit {
    TON,
    SHORT_TON,
    POUND
}

fun WeightMeasurementUnit.toDisplayString() = when(this) {
    TON       -> "TONS"
    SHORT_TON -> "TONS"
    POUND     -> "POUNDS"
}

fun String.toWeightMeasurementUnit() = when(this) {
    "ton"       -> TON
    "short ton" -> SHORT_TON
    "pound"     -> POUND
    else -> throw UnsupportedOperationException("not implemented")
}
