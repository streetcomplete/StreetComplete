package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.util.ktx.toShortString
import kotlinx.serialization.Serializable

@Serializable
sealed interface Weight {
    fun toMetricTons(): Double
    override fun toString(): String
}

@Serializable
data class MetricTons(val tons: Double) : Weight {
    override fun toMetricTons() = tons
    override fun toString() = tons.toShortString()
}

@Serializable
data class ShortTons(val tons: Double) : Weight {
    override fun toMetricTons() = tons * 0.90718474
    override fun toString() = tons.toShortString() + " st"
}

@Serializable
data class ImperialPounds(val pounds: Int) : Weight {
    override fun toMetricTons() = pounds * 0.45359237 / 1000
    override fun toString() = "$pounds lbs"
}
