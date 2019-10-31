package de.westnordost.streetcomplete.quests.max_weight

import de.westnordost.streetcomplete.ktx.toShortString

sealed class WeightMeasure {
    abstract fun toMetricTons(): Double
    abstract override fun toString(): String
}

data class MetricTons(val tons: Double) : WeightMeasure() {
    override fun toMetricTons() = tons
    override fun toString() = tons.toShortString()
}

data class ShortTons(val tons: Double) : WeightMeasure() {
    override fun toMetricTons() = tons * 0.90718474
    override fun toString() = tons.toShortString() + " st"
}

data class ImperialPounds(val pounds: Int) : WeightMeasure() {
    override fun toMetricTons() = pounds * 0.45359237 / 1000
    override fun toString() = "$pounds lbs"
}
