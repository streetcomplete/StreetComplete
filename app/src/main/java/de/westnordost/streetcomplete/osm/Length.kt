package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.ktx.toShortString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LengthUnit(private val abbr: String) {
    @SerialName("meter") METER("m"),
    @SerialName("foot and inch") FOOT_AND_INCH("ft / in");

    override fun toString() = abbr
}

sealed interface Length {
    fun toMeters(): Double
    fun toOsmValue(): String
}

data class LengthInMeters(val meters: Double) : Length {
    override fun toMeters() = meters
    override fun toOsmValue() = meters.toShortString()
}

data class LengthInFeetAndInches(val feet: Int, val inches: Int) : Length {
    override fun toMeters() = (feet * 12 + inches) * 0.0254
    override fun toOsmValue() = "$feet'$inches\""
}
