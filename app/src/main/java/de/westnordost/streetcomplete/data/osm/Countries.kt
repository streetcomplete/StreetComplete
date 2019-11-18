package de.westnordost.streetcomplete.data.osm

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.getContainingIds
import de.westnordost.countryboundaries.getIntersectingIds
import de.westnordost.countryboundaries.isInAny
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.ktx.containsAny

sealed class Countries

object AllCountries : Countries()
data class AllCountriesExcept(val exceptions: List<String>) : Countries() {
    constructor(vararg exceptions: String) : this(exceptions.toList())
}
data class NoCountriesExcept(val exceptions: List<String>) : Countries() {
    constructor(vararg exceptions: String) : this(exceptions.toList())
}

/** Whether these countries contain the given position.
 *  Needs a CountryBoundaries to calculate that. */
fun Countries.containsPosition(pos: LatLon, boundaries: CountryBoundaries) = when(this) {
    is AllCountries -> true
    is AllCountriesExcept -> !boundaries.isInAny(pos, exceptions)
    is NoCountriesExcept -> boundaries.isInAny(pos, exceptions)
}

/** Whether these countries at least intersect with the given bounding box.
 *  Needs a CountryBoundaries to calculate that. */
fun Countries.intersectsBBox(bbox: BoundingBox, boundaries: CountryBoundaries) = when(this) {
    is AllCountries -> true
    is AllCountriesExcept -> !boundaries.getContainingIds(bbox).containsAny(exceptions)
    is NoCountriesExcept -> boundaries.getIntersectingIds(bbox).containsAny(exceptions)
}
