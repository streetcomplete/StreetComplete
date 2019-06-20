package de.westnordost.streetcomplete.quests.max_weight

//if the minimum required API would be 24, we could maybe use https://developer.android.com/reference/android/icu/util/Measure
abstract class Measure {
    abstract fun toTons(): Double
    abstract override fun toString(): String
}

data class MetricMeasure(val tons: Double) : Measure() {
    override fun toTons() = tons
    override fun toString() =
        if (tons % 1 == 0.0) {
            tons.toInt().toString()
        } else {
            tons.toString()
        }
}

data class UsShortTons(val tons: Double) : Measure() {
    override fun toTons() = tons * 0.90718474
    override fun toString() =
            if (tons % 1 == 0.0) {
                tons.toInt().toString() + " st"
            } else {
                "$tons st"
            }
}

data class ImperialMeasure(val pounds: Int) : Measure() {
    override fun toTons() = pounds * 0.45359237 / 1000
    override fun toString() = "$pounds lbs"
}
