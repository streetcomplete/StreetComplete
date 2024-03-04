package de.westnordost.streetcomplete.quests.oneway_suspects.data

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.util.ktx.format
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Dao for using this API: https://github.com/streetcomplete/oneway-data-api  */
class TrafficFlowSegmentsApi(
    private val httpClient: HttpClient,
    private val apiUrl: String
) {

    suspend fun get(bbox: BoundingBox): Map<Long, List<TrafficFlowSegment>> {
        val leftBottomRightTopString = listOf(
            bbox.min.longitude,
            bbox.min.latitude,
            bbox.max.longitude,
            bbox.max.latitude
        ).joinToString(",") { it.format(7) }

        val response = httpClient.get("$apiUrl?bbox=$leftBottomRightTopString") {
            expectSuccess = true
        }
        return parse(response.body())
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        @Serializable
        data class TrafficFlowSegmentList(val segments: List<TrafficFlowSegment>)
        fun parse(jsonString: String): Map<Long, List<TrafficFlowSegment>> =
            json.decodeFromString<TrafficFlowSegmentList>(jsonString).segments.groupBy { it.wayId }
    }
}
