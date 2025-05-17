package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.elementfilter.ElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.util.math.distance
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy

class NameSuggestionsSource(private val mapDataSource: MapDataWithEditsSource) {
    /**
     *  Return a list of [LocalizedName]s of elements with name(s), sorted by distance ascending to
     *  any of the given [points] that have at most a distance of [maxDistance] in m to those. The
     *  elements can be filtered with the given [filter] expression, to e.g. only find
     *  roads with names.
     */
    fun getNames(
        points: List<LatLon>,
        maxDistance: Double,
        filter: ElementFilterExpression
    ): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        /* add 100m radius for bbox query because roads will only be included in the result that
           have at least one node in the bounding box around the tap position. This is a problem for
           long straight roads (#3797). This doesn't completely solve this issue but mitigates it */
        val bbox = points.enclosingBoundingBox().enlargedBy(maxDistance + 100)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        val filteredElements = mapData.filter(filter)
        // map of localized names -> min distance
        val result = mutableMapOf<List<LocalizedName>, Double>()

        for (element in filteredElements) {
            val geometry = mapData.getGeometry(element.type, element.id) ?: continue

            val minDistance = points.minOf { geometry.distance(it) }
            if (minDistance > maxDistance) continue

            val names = parseLocalizedNames(element.tags) ?: continue

            // eliminate duplicates (e.g. same road, different segments, different distances)
            val prev = result[names]
            if (prev != null && prev < minDistance) continue

            result[names] = minDistance
        }

        // return only the road names, sorted by distance ascending
        return result.entries.sortedBy { it.value }.map { it.key }
    }
}
