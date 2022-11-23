package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.*

data class LeftAndRightSidewalk(val left: Sidewalk?, val right: Sidewalk?)

fun LeftAndRightSidewalk?.hasSidewalk(): Boolean =
    this?.left == YES || this?.right == YES

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
