package de.westnordost.streetcomplete.quests.max_height

//if the minimum required API would be 24, we could maybe use https://developer.android.com/reference/android/icu/util/Measure
class Measure {
    private val meters: Double

    private val feet: Int
    private val inches: Int

    var unit: Unit? = null
        private set

    val inMeters: Double
        get() = if (unit == Unit.METRIC) {
            meters
        } else {
            (feet * 12 + inches) * 0.0254
        }

    enum class Unit {
        METRIC,
        IMPERIAL
    }

    internal constructor(meters: Double) {
        this.meters = meters
        this.unit = Unit.METRIC
    }

    constructor(feet: Int, inches: Int) {
        this.feet = feet
        this.inches = inches
        this.unit = Unit.IMPERIAL
    }

    override fun toString(): String {
        return if (unit == Unit.METRIC) {
            if (meters % 1 == 0.0) {
                meters.toInt().toString()
            } else {
                meters.toString()
            }
        } else {
            //this adds an apostrophe and a double-quote to be in a format like e.g. 6'7"
            feet.toString() + "'" + inches.toString() + "\""
        }
    }
}
