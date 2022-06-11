package de.westnordost.streetcomplete.util.ktx

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon

fun CountryBoundaries.getIntersectingIds(bounds: BoundingBox): Set<String> = getIntersectingIds(
    bounds.min.longitude, bounds.min.latitude, bounds.max.longitude, bounds.max.latitude
)

fun CountryBoundaries.getIds(pos: LatLon): MutableList<String> = getIds(pos.longitude, pos.latitude)
