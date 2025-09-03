package de.westnordost.streetcomplete.osm.mtb_scale

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.mtb_scale.MtbScale.*
import de.westnordost.streetcomplete.osm.updateWithCheckDate

data class MtbScale(
    val value: Int,
    val modifier: Mod? = null
) {
    init {
        require(value in 0..6) { "Mtb scale value must be between 0 and 6." }
    }

    enum class Mod(val value: Char?) {
        PLUS('+'),
        MINUS('-'),
        NONE(null)
    }
}

fun parseMtbScale(tags: Map<String, String>): MtbScale? {
    val scale = tags["mtb:scale"] ?: return null
    if (scale.length > 2) return null

    val value = scale.getOrNull(0)?.digitToIntOrNull() ?: return null
    if (value < 0 || value > 6) return null

    val modifierValue = scale.getOrNull(1)
    val modifier = Mod.entries.find { it.value == modifierValue } ?: return null

    return MtbScale(value, modifier)
}

fun MtbScale.applyTo(tags: Tags) {
    // don't overwrite values such as "3+" with "3" if the new value doesn't have the modifier
    // specified unless the scale value is also different. (I.e. "4" can replace "3+")
    val previous = parseMtbScale(tags)
    val modifier = modifier ?: if (previous?.value == value) previous.modifier else null

    val newValue = value.toString() + modifier?.value?.toString().orEmpty()
    tags.updateWithCheckDate("mtb:scale", newValue)
}
