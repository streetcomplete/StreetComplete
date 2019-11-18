package de.westnordost.countryboundaries

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon

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
