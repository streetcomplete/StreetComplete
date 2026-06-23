package de.westnordost.streetcomplete.util.countryboundaries

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.containsAny

/** A convenience wrapper around [de.westnordost.countryboundaries.CountryBoundaries]. It is an
 *  interface also for mockability in tests. */
interface CountryBoundaries {

    /** Whether the given [position] is in any of the given [countries] */
    fun isInAny(position: LatLon, countries: Countries): Boolean

    /** Returns the ids of the countries the given [position] is contained in, ordered by size
     *  ascending. */
    fun getIds(position: LatLon): List<String>

    /** Whether the given bounding box [bbox] at least intersects with the given [countries] */
    fun intersects(bbox: BoundingBox, countries: Countries): Boolean
}

class CountryBoundariesImpl(
    private val countryBoundaries: de.westnordost.countryboundaries.CountryBoundaries
): CountryBoundaries {

    override fun isInAny(position: LatLon, countries: Countries): Boolean =
        when (countries) {
            is AllCountries -> true
            is AllCountriesExcept -> !isInAny(position, countries.exceptions)
            is NoCountriesExcept -> isInAny(position, countries.exceptions)
        }

    private fun isInAny(position: LatLon, ids: Collection<String>): Boolean =
        countryBoundaries.isInAny(
            longitude = position.longitude,
            latitude = position.latitude,
            ids = ids
        )

    override fun getIds(position: LatLon): List<String> =
        countryBoundaries.getIds(
            longitude = position.longitude,
            latitude = position.latitude
        )

    override fun intersects(bbox: BoundingBox, countries: Countries): Boolean = when (countries) {
        is AllCountries -> true
        is AllCountriesExcept -> !getContainingIds(bbox).containsAny(countries.exceptions)
        is NoCountriesExcept -> getIntersectingIds(bbox).containsAny(countries.exceptions)
    }

    private fun getContainingIds(bounds: BoundingBox): Set<String> =
        countryBoundaries.getContainingIds(
            minLongitude = bounds.min.longitude,
            minLatitude = bounds.min.latitude,
            maxLongitude = bounds.max.longitude,
            maxLatitude = bounds.max.latitude
        )

    private fun getIntersectingIds(bounds: BoundingBox): Set<String> =
        countryBoundaries.getIntersectingIds(
            minLongitude = bounds.min.longitude,
            minLatitude = bounds.min.latitude,
            maxLongitude = bounds.max.longitude,
            maxLatitude = bounds.max.latitude
        )
}
