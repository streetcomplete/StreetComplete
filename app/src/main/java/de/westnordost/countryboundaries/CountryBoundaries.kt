package de.westnordost.countryboundaries

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.data.osm.AllCountries
import de.westnordost.streetcomplete.data.osm.AllCountriesExcept
import de.westnordost.streetcomplete.data.osm.Countries
import de.westnordost.streetcomplete.data.osm.NoCountriesExcept
import de.westnordost.streetcomplete.ktx.containsAny

/** Whether the given position is in any of the given countries */
fun CountryBoundaries.isInAny(pos: LatLon, countries: Countries) = when(countries) {
    is AllCountries -> true
    is AllCountriesExcept -> !isInAny(pos, countries.exceptions)
    is NoCountriesExcept -> isInAny(pos, countries.exceptions)
}

/** Whether the given bounding box at least intersects with the given countries */
fun CountryBoundaries.intersects(bbox: BoundingBox, countries: Countries) = when(countries) {
    is AllCountries -> true
    is AllCountriesExcept -> !getContainingIds(bbox).containsAny(countries.exceptions)
    is NoCountriesExcept -> getIntersectingIds(bbox).containsAny(countries.exceptions)
}

fun CountryBoundaries.getContainingIds(bounds: BoundingBox): Set<String> = getContainingIds(
    bounds.minLongitude, bounds.minLatitude, bounds.maxLongitude, bounds.maxLatitude
)

fun CountryBoundaries.getIntersectingIds(bounds: BoundingBox): Set<String> = getIntersectingIds(
    bounds.minLongitude, bounds.minLatitude, bounds.maxLongitude, bounds.maxLatitude
)

fun CountryBoundaries.isInAny(pos: LatLon, ids: Collection<String>) = isInAny(
    pos.longitude, pos.latitude, ids
)

fun CountryBoundaries.isIn(pos: LatLon, id: String) = isIn(pos.longitude, pos.latitude, id)

fun CountryBoundaries.getIds(pos: LatLon) = getIds(pos.longitude, pos.latitude)

