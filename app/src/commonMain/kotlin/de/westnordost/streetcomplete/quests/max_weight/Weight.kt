package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit.*
import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

@Serializable
data class Weight(
    val value: Double,
    val unit: WeightMeasurementUnit,
) {
    fun toMetricTons(): Double = value * when (unit) {
        METRIC_TON -> 1.0
        SHORT_TON -> 0.90718474
        POUND -> 0.45359237 / 1000.0
    }

    fun toOsmString(): String = value.toShortString() + when (unit) {
        METRIC_TON -> ""
        SHORT_TON -> " st"
        POUND -> " lbs"
    }
}
