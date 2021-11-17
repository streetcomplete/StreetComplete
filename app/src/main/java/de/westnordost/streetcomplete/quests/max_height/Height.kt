package de.westnordost.streetcomplete.quests.max_height

import de.westnordost.streetcomplete.ktx.toShortString

sealed class Height {
    abstract fun toMeters(): Double
    abstract override fun toString(): String
}

data class Meters(val meters: Double) : Height() {
    override fun toMeters() = meters
    override fun toString() = meters.toShortString()
}

data class ImperialFeetAndInches(val feet: Int, val inches: Int) : Height() {
    override fun toMeters() = (feet * 12 + inches) * 0.0254
    override fun toString() = "$feet'$inches\""
}
