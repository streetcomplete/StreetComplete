package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.POUND
import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.SHORT_TON
import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.TON

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
