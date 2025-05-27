package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.*

data class LeftAndRightSidewalk(val left: Sidewalk?, val right: Sidewalk?)

fun LeftAndRightSidewalk.any(block: (sidewalk: Sidewalk?) -> Boolean): Boolean =
    block(left) || block(right)

fun LeftAndRightSidewalk.validOrNullValues(): LeftAndRightSidewalk {
    if (left != INVALID && right != INVALID) return this
    return LeftAndRightSidewalk(left?.takeIf { it != INVALID }, right?.takeIf { it != INVALID })
}

enum class Sidewalk {
    YES,
    NO,
    SEPARATE,
    INVALID
}
