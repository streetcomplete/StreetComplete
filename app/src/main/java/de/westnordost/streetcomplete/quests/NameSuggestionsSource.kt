package de.westnordost.streetcomplete.quests

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy

class NameSuggestionsSource(
    private val mapDataSource: MapDataWithEditsSource
) {
    /**
     *  Return a list of [LocalizedName]s of elements with name(s), sorted by distance ascending to
     *  any of the given [points] that have at most a distance of [maxDistance] to those. The
     *  elements can be filtered with the given [elementFilter] expression, to e.g. only find
     *  roads with names.
     */
    fun getNames(points: List<LatLon>, maxDistance: Double, elementFilter: String): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        /* add 100m radius for bbox query because roads will only be included in the result that
           have at least one node in the bounding box around the tap position. This is a problem for
           long straight roads (#3797). This doesn't completely solve this issue but mitigates it */
        val bbox = points.enclosingBoundingBox().enlargedBy(maxDistance + 100)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        val filteredElements = mapData.filter(elementFilter)
        // map of localized names -> min distance
        val result = mutableMapOf<List<LocalizedName>, Double>()

        for (elem in filteredElements) {

            var minDistance = 0.0

            if (elem is Way) {
                val geometry = mapData.getWayGeometry(elem.id) as? ElementPolylinesGeometry ?: continue

                val polyline = geometry.polylines.firstOrNull() ?: continue
                if (polyline.isEmpty()) continue

                minDistance = points.distanceTo(polyline)
            }

            if (elem is Node) {
                minDistance = points.minOf { elem.position.distanceTo(it) }
            }

            if (elem is Relation) continue

            if (minDistance > maxDistance) continue
            val names = parseLocalizedNames(elem.tags) ?: continue

            // eliminate duplicates
            val prev = result[names]
            if (prev != null && prev < minDistance) continue

            result[names] = minDistance
        }

        // return only the road names, sorted by distance ascending
        return result.entries.sortedBy { it.value }.map { it.key }
    }
}
