package de.westnordost.streetcomplete.quests.oneway_suspects.data

import android.annotation.SuppressLint
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.format
import org.json.JSONObject
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
        fun parse(json: String): Map<Long, List<TrafficFlowSegment>> {
            val obj = JSONObject(json)
            if (!obj.has("segments")) return mapOf()

            val segments = obj.getJSONArray("segments")
            @SuppressLint("UseSparseArrays")
            val result = mutableMapOf<Long, MutableList<TrafficFlowSegment>>()

            for (i in 0 until segments.length()) {
                if (segments.isNull(i)) continue
                val segment = segments.getJSONObject(i)
                val wayId = segment.getLong("wayId")
                result.getOrPut(wayId) { mutableListOf() }.add(
                    TrafficFlowSegment(
                        parseLatLon(segment.getJSONObject("fromPosition")),
                        parseLatLon(segment.getJSONObject("toPosition"))
                    )
                )
            }
            return result
        }

        private fun parseLatLon(pos: JSONObject) = LatLon(pos.getDouble("lat"), pos.getDouble("lon"))
    }
}
