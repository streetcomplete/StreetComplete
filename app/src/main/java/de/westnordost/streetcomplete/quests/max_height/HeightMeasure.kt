package de.westnordost.streetcomplete.quests.max_height

//if the minimum required API would be 24, we could maybe use https://developer.android.com/reference/android/icu/util/Measure
sealed class HeightMeasure {
    abstract fun toMeters(): Double
    abstract override fun toString(): String
}

data class MetricHeightMeasure(val meters: Double) : HeightMeasure() {
    override fun toMeters() = meters
    override fun toString() =
        if (meters % 1 == 0.0) {
            meters.toInt().toString()
        } else {
            meters.toString()
        }
}

data class ImperialHeightMeasure(val feet: Int, val inches: Int) : HeightMeasure() {
    override fun toMeters() = (feet * 12 + inches) * 0.0254
    override fun toString() = "$feet'$inches\""
}
