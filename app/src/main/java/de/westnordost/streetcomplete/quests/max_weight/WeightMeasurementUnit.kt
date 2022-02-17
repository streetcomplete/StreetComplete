package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.POUND
import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.SHORT_TON
import de.westnordost.streetcomplete.quests.max_weight.WeightMeasurementUnit.TON
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class WeightMeasurementUnit {
    @SerialName("ton") TON,
    @SerialName("short ton") SHORT_TON,
    @SerialName("pound") POUND
}

fun WeightMeasurementUnit.toDisplayString() = when (this) {
    TON       -> "TONS"
    SHORT_TON -> "TONS"
    POUND     -> "POUNDS"
}
