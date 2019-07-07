package de.westnordost.streetcomplete.quests.max_weight

sealed class WeightMeasure {
    abstract fun toTons(): Double
    abstract override fun toString(): String
}

data class MetricWeightMeasure(val tons: Double) : WeightMeasure() {
    override fun toTons() = tons
    override fun toString() =
        if (tons % 1 == 0.0) {
            tons.toInt().toString()
        } else {
            tons.toString()
        }
}

data class UsShortTons(val tons: Double) : WeightMeasure() {
    override fun toTons() = tons * 0.90718474
    override fun toString() =
            if (tons % 1 == 0.0) {
                tons.toInt().toString() + " st"
            } else {
                "$tons st"
            }
}

data class ImperialWeightMeasure(val pounds: Int) : WeightMeasure() {
    override fun toTons() = pounds * 0.45359237 / 1000
    override fun toString() = "$pounds lbs"
}
