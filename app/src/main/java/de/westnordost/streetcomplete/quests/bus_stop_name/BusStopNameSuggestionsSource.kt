package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.osm.LocalizedName
import de.westnordost.streetcomplete.osm.parseLocalizedNames
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy

class BusStopNameSuggestionsSource(
    private val mapDataSource: MapDataWithEditsSource
) {

    fun getNames(points: List<LatLon>, maxDistance: Double): List<List<LocalizedName>> {
        if (points.isEmpty()) return emptyList()

        /* add 100m radius for bbox query because roads will only be included in the result that have
           at least one node in the bounding box around the tap position. This is a problem for long
           straight roads (#3797). This doesn't completely solve this issue but mitigates it */
        val bbox = points.enclosingBoundingBox().enlargedBy(maxDistance + 100)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        val busStopWaysWithNames  = mapData.ways.filter { busStopFilter(it.tags) }
        val busStopNodesWithNames = mapData.nodes.filter { busStopFilter(it.tags) }

        val result = mutableMapOf<List<LocalizedName>, Double>()

        for (busStop in busStopWaysWithNames) {
            val geometry = mapData.getWayGeometry(busStop.id) as? ElementPolylinesGeometry ?: continue

            val polyline = geometry.polylines.firstOrNull() ?: continue
            if (polyline.isEmpty()) continue

            val minDistanceToRoad = points.distanceTo(polyline)
            if (minDistanceToRoad > maxDistance) continue

            val names = parseLocalizedNames(busStop.tags) ?: continue

            // eliminate duplicates (same road, different segments, different distances)
            val prev = result[names]
            if (prev != null && prev < minDistanceToRoad) continue

            result[names] = minDistanceToRoad
        }

        for (busStop in busStopNodesWithNames) {
            val geometry = mapData.getNodeGeometry(busStop.id) ?: continue

            val minDistanceToRoad = points.distanceTo(listOf(geometry.center))
            if (minDistanceToRoad > maxDistance) continue

            val names = parseLocalizedNames(busStop.tags) ?: continue
            // eliminate duplicates (same road, different segments, different distances)
            val prev = result[names]
            if (prev != null && prev < minDistanceToRoad) continue

            result[names] = minDistanceToRoad
        }

        // return only the road names, sorted by distance ascending
        return result.entries.sortedBy { it.value }.map { it.key }
    }

    private fun busStopFilter(tags: Map<String, String>) =
        tags.containsKey("name")
        && (
            (tags["highway"] == "bus_stop" && tags["public_transport"] != "stop_position")
            || (tags["public_transport"] == "platform" && tags["bus"] == "yes")
            || tags["railway"] == "halt"
            || tags["railway"] == "station"
            || tags["railway"] == "tram_stop"
        )
}
