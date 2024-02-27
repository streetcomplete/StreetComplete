package de.westnordost.streetcomplete.quests.max_speed

import kotlin.math.roundToInt

sealed interface Speed {
    fun toKmh(): Int
    override fun toString(): String
    fun toValue(): Int
}

data class Kmh(val kmh: Int) : Speed {
    override fun toKmh() = kmh
    override fun toString() = kmh.toString()
    override fun toValue() = kmh
}

data class Mph(val mph: Int) : Speed {
    override fun toKmh() = (mph * 1.60934).roundToInt()
    override fun toString() = "$mph mph"
    override fun toValue() = mph
}
