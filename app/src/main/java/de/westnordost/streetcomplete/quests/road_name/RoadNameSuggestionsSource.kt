package de.westnordost.streetcomplete.quests.road_name

import javax.inject.Inject

import de.westnordost.streetcomplete.data.meta.ALL_PATHS
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.distanceTo
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import de.westnordost.streetcomplete.util.enlargedBy

class RoadNameSuggestionsSource @Inject constructor(
    private val mapDataSource: MapDataWithEditsSource
) {

    /** returns something like [{"": "17th Street", "de": "17. Straße", "en": "17th Street", "international": "17 🛣 ️" }, ...] */
    fun getNames(points: List<LatLon>, maxDistance: Double): List<MutableMap<String, String>> {
        if (points.isEmpty()) return emptyList()

        val bbox = points.enclosingBoundingBox().enlargedBy(maxDistance)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        val roadsWithNames = mapData.ways.filter { it.isRoadWithName() }

        val result = mutableMapOf<MutableMap<String, String>, Double>()
        for (road in roadsWithNames) {
            val geometry = mapData.getWayGeometry(road.id) as? ElementPolylinesGeometry ?: continue

            val polyline = geometry.polylines.firstOrNull() ?: continue
            if (polyline.isEmpty()) continue

            val minDistanceToRoad = points.distanceTo(polyline)
            if (minDistanceToRoad > maxDistance) continue

            val namesByLocale = road.tags.toRoadNameByLanguage()?.toMutableMap() ?: continue

            // eliminate duplicates (same road, different segments, different distances)
            val prev = result[namesByLocale]
            if (prev != null && prev < minDistanceToRoad) continue

            result[namesByLocale] = minDistanceToRoad
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

/** OSM tags to map of language code -> name.
 *
 *  For example:
 *
 *  "name:de": "Hauptstraße"
 *  "name": "Hauptstraße"
 *  "int_name": "main road"
 *  "name:de-Cyrl": "Хауптстра"
 *
 *  becomes
 *
 *  "de": "Hauptstraße"
 *  "": "Hauptstraße"
 *  "international": "main road"
 *  "de-Cyrl": "Хауптстра"
 *
 *  Tags that are not two- or three-letter ISO 639 language codes appended with an optional 4-letter
 *  ISO 15924 code, such as name:left, name:etymology, name:source etc., are ignored
 *  */
internal fun Map<String,String>.toRoadNameByLanguage(): Map<String, String>? {
    val result = mutableMapOf<String,String>()
    val namePattern = Regex("name(?::([a-z]{2,3}(?:-[a-zA-Z]{4})?))?")
    for ((key, value) in this) {
        val m = namePattern.matchEntire(key)
        if (m != null) {
            val languageTag = m.groupValues[1]
            result[languageTag] = value
        } else if(key == "int_name") {
            result["international"] = value
        }
    }
    return if (result.isEmpty()) null else result
}
