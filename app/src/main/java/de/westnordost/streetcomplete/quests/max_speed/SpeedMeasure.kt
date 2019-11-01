package de.westnordost.streetcomplete.quests.max_speed

import kotlin.math.roundToInt

sealed class SpeedMeasure {
    abstract fun toKmh(): Int
    abstract override fun toString(): String
    abstract fun toValue(): Int
}

data class Kmh(val kmh: Int) : SpeedMeasure() {
    override fun toKmh() = kmh
    override fun toString() = kmh.toString()
    override fun toValue() = kmh
}

data class Mph(val mph: Int) : SpeedMeasure() {
    override fun toKmh() = (mph * 1.60934).roundToInt()
    override fun toString() = "$mph mph"
    override fun toValue() = mph
}
