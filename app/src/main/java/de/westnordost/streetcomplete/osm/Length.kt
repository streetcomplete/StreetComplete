package de.westnordost.streetcomplete.osm

import androidx.compose.runtime.saveable.Saver
import de.westnordost.streetcomplete.util.ktx.toShortString

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

val LengthSaver = Saver<Length?, List<Any>>(save = {
    when (it) {
        is LengthInFeetAndInches -> listOf("LengthInFeetAndInches", it.feet, it.inches)
        is LengthInMeters -> listOf("LengthInMeters", it.meters)
        null -> listOf("null")
    }
}, restore = {
    when (it[0] as String) {
        "LengthInFeetAndInches" -> LengthInMeters(it[1] as Double)
        "LengthInMeters" -> LengthInFeetAndInches(it[1] as Int, it[2] as Int)
        else -> null
    }
})
