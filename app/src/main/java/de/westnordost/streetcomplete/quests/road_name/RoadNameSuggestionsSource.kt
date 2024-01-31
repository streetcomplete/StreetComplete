package de.westnordost.streetcomplete.quests.road_name

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy

class RoadNameSuggestionsSource(
    private val mapDataSource: MapDataWithEditsSource
) {

    fun getNames(points: List<LatLon>, maxDistance: Double): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        /* add 100m radius for bbox query because roads will only be included in the result that have
           at least one node in the bounding box around the tap position. This is a problem for long
           straight roads (#3797). This doesn't completely solve this issue but mitigates it */
        val bbox = points.enclosingBoundingBox().enlargedBy(maxDistance + 100)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        val roadsWithNames = mapData.ways.filter { it.isRoadWithName() }

        val result = mutableMapOf<List<LocalizedName>, Double>()
        for (road in roadsWithNames) {
            val geometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry ?: continue

            val polyline = geometry.polylines.firstOrNull() ?: continue
            if (polyline.isEmpty()) continue

            val minDistanceToRoad = points.distanceTo(polyline)
            if (minDistanceToRoad > maxDistance) continue

            val names = parseLocalizedNames(road.tags) ?: continue

            // eliminate duplicates (same road, different segments, different distances)
            val prev = result[names]
            if (prev != null && prev < minDistanceToRoad) continue

            result[names] = minDistanceToRoad
        }
        // return only the road names, sorted by distance ascending
        return result.entries.sortedBy { it.value }.map { it.key }
    }

    private fun Way.isRoadWithName(): Boolean {
        return tags.containsKey("name") && tags["highway"] in ALL_ROADS_AND_PATHS
    }

    companion object {
        private val ALL_ROADS_AND_PATHS = ALL_ROADS + ALL_PATHS
    }
}
