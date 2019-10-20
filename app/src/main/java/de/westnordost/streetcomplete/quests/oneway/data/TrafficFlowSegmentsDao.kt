package de.westnordost.streetcomplete.quests.oneway.data

import android.annotation.SuppressLint

import java.net.URL

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.util.StreamUtils
import org.json.JSONObject

/** Dao for using this API: https://github.com/ENT8R/oneway-data-api  */
class TrafficFlowSegmentsDao(private val apiUrl: String) {

    fun get(bbox: BoundingBox): Map<Long, List<TrafficFlowSegment>> {
        val url = URL("$apiUrl?bbox=${bbox.asLeftBottomRightTopString}")
        val json = StreamUtils.readToString(url.openConnection().getInputStream())
        return parse(json)
    }

    companion object {
        fun parse(json: String): Map<Long, List<TrafficFlowSegment>> {
            val obj = JSONObject(json)
            val segments = obj.getJSONArray("segments")

            @SuppressLint("UseSparseArrays")
            val result = mutableMapOf<Long, MutableList<TrafficFlowSegment>>()
            if (segments == null) return result

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

        private fun parseLatLon(pos: JSONObject) = OsmLatLon(pos.getDouble("lat"), pos.getDouble("lon"))
    }
}
