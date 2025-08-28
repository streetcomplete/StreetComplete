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
    fun toMetricTons(): Double = when (unit) {
        METRIC_TON -> value
        SHORT_TON -> value * 0.90718474
        POUND -> value * 0.45359237 / 1000.0
    }

    fun toOsmString(): String = when (unit) {
        METRIC_TON -> value.toShortString()
        SHORT_TON -> value.toShortString() + " st"
        POUND -> "$value lbs"
    }
}
