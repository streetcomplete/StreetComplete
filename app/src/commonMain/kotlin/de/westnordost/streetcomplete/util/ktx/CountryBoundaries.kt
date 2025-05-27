package de.westnordost.streetcomplete.util.ktx

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept

/** Whether the given position is in any of the given countries */
fun CountryBoundaries.isInAny(pos: LatLon, countries: Countries): Boolean = when (countries) {
    is AllCountries -> true
    is AllCountriesExcept -> !isInAny(pos, countries.exceptions)
    is NoCountriesExcept -> isInAny(pos, countries.exceptions)
}

/** Whether the given bounding box at least intersects with the given countries */
fun CountryBoundaries.intersects(bbox: BoundingBox, countries: Countries): Boolean = when (countries) {
    is AllCountries -> true
    is AllCountriesExcept -> !getContainingIds(bbox).containsAny(countries.exceptions)
    is NoCountriesExcept -> getIntersectingIds(bbox).containsAny(countries.exceptions)
}

/**
 * Returns which countries are guaranteed to contain the given [bounds] fully.
 *
 * The given bounding box may wrap around the 180th longitude, e.g. minLongitude = 170 and
 * maxLongitude = -170.
 */
fun CountryBoundaries.getContainingIds(bounds: BoundingBox): Set<String> = getContainingIds(
    bounds.min.longitude, bounds.min.latitude, bounds.max.longitude, bounds.max.latitude
)

/**
 * Returns which countries may intersect with the given [bounds]. In other words, any
 * point within the given [bounds] can only be in any of the returned countries (or none).
 *
 * The given bounding box may wrap around the 180th longitude, i.e minLongitude = 170 and
 * maxLongitude = -170.
 */
fun CountryBoundaries.getIntersectingIds(bounds: BoundingBox): Set<String> = getIntersectingIds(
    bounds.min.longitude, bounds.min.latitude, bounds.max.longitude, bounds.max.latitude
)

/**
 * Returns whether the given [pos] is contained in any of the countries with the given [ids].
 */
fun CountryBoundaries.isInAny(pos: LatLon, ids: Collection<String>): Boolean = isInAny(
    pos.longitude, pos.latitude, ids
)

/**
 * Returns the ids of the countries the given [pos] is contained in, ordered by size ascending.
 */
fun CountryBoundaries.getIds(pos: LatLon): List<String> = getIds(pos.longitude, pos.latitude)
