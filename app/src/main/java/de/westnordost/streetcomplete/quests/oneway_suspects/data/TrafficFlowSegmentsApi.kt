package de.westnordost.streetcomplete.quests.oneway_suspects.data

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

/** Dao for using this API: https://github.com/ENT8R/oneway-data-api  */
class TrafficFlowSegmentsApi(private val apiUrl: String) {

    fun get(bbox: BoundingBox): Map<Long, List<TrafficFlowSegment>> {
        val leftBottomRightTopString = listOf(
            bbox.min.longitude,
            bbox.min.latitude,
            bbox.max.longitude,
            bbox.max.latitude
        ).joinToString(",") { it.format(7) }

        val url = URL("$apiUrl?bbox=$leftBottomRightTopString")
        val json = url.openConnection().getInputStream().bufferedReader().use { it.readText() }
        return parse(json)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        @Serializable
        data class TrafficFlowSegmentList(val segments: List<TrafficFlowSegment>)
        fun parse(jsonString: String): Map<Long, List<TrafficFlowSegment>> {
            return json.decodeFromString<TrafficFlowSegmentList>(jsonString).segments.groupBy { it.wayId }
        }
    }
}
