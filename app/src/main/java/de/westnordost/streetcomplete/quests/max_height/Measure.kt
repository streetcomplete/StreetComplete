package de.westnordost.streetcomplete.quests.max_height

//if the minimum required API would be 24, we could maybe use https://developer.android.com/reference/android/icu/util/Measure
abstract class Measure {
    abstract fun toMeters(): Double
    abstract override fun toString(): String
}

data class MetricMeasure(val meters: Double) : Measure() {
    override fun toMeters() = meters
    override fun toString() =
        if (meters % 1 == 0.0) {
            meters.toInt().toString()
        } else {
            meters.toString()
        }
}

data class ImperialMeasure(val feet: Int, val inches: Int) : Measure() {
    override fun toMeters() = (feet * 12 + inches) * 0.0254
    override fun toString() = "$feet'$inches\""
}
