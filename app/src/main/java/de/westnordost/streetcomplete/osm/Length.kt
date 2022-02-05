package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.ktx.toShortString

enum class LengthUnit(private val abbr: String) {
    METER("m"),
    FOOT_AND_INCH("ft / in");

    override fun toString() = abbr
}

sealed interface Length {
    fun toMeters(): Double
    fun toOsmValue(): String
}
data class LengthInMeters(val meters: Double) : Length{
    override fun toMeters() = meters
    override fun toOsmValue() = meters.toShortString()
}
data class LengthInFeetAndInches(val feet: Int, val inches: Int) : Length {
    override fun toMeters() = (feet * 12 + inches) * 0.0254
    override fun toOsmValue() = "$feet'$inches\""
}

fun String.toLengthUnit() = when(this) {
    "meter" -> LengthUnit.METER
    "foot and inch" -> LengthUnit.FOOT_AND_INCH
    else -> throw UnsupportedOperationException("not implemented")
}
