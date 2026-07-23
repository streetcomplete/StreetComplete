package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.*
import kotlinx.serialization.Serializable

@Serializable
enum class Sidewalk {
    YES,
    NO,
    SEPARATE,
    INVALID
}

fun Sides<Sidewalk>.validOrNullValues(): Sides<Sidewalk> {
    if (left != INVALID && right != INVALID) return this
    return Sides(left?.takeIf { it != INVALID }, right?.takeIf { it != INVALID })
}
