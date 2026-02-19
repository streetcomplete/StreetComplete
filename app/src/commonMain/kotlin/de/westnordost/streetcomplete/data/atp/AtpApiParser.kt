package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AtpApiParser {

    fun isParsedNull(element: JsonElement?): Boolean {
        return element is JsonPrimitive && element?.jsonPrimitive is JsonNull
    }
    fun parseAtpEntries(source: Source) : List<AtpEntry> {
        val returned = mutableListOf<AtpEntry>()
        val features = Json.parseToJsonElement(source.readString()).jsonArray
        features.forEach { feature ->
            //val properties = feature.jsonObject.entries
            val lon = feature.jsonObject["atp_center_lon"]?.toString()?.toDoubleOrNull()
            if (lon == null) {
                Log.e(
                    TAG,
                    "lon entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val lat = feature.jsonObject["atp_center_lat"]?.toString()?.toDoubleOrNull()
            if (lat == null) {
                Log.e(
                    TAG,
                    "lat entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val id = feature.jsonObject["atp_entry_id"]?.toString()?.toLongOrNull()
            if (id == null) {
                Log.e(
                    TAG,
                    "id entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val rawOsmObjectId = feature.jsonObject["osm_element_match_id"]
            val rawOsmObjectType = feature.jsonObject["osm_element_match_type"]
            val osmObjectType = if(isParsedNull(rawOsmObjectType)) {
                null
            } else {
                rawOsmObjectType?.jsonPrimitive?.content
            }
            val osmObjectId = if(isParsedNull(rawOsmObjectId)) {
                null
            } else {
                rawOsmObjectId?.jsonPrimitive?.content?.toLongOrNull()
            }
            val osmMatch = if (osmObjectType == null || osmObjectId == null) {
                null
            } else {
                when (osmObjectType) {
                    "node" -> {
                        ElementKey(ElementType.NODE, osmObjectId)
                    }

                    "way" -> {
                        ElementKey(ElementType.WAY, osmObjectId)
                    }

                    "relation" -> {
                        ElementKey(ElementType.RELATION, osmObjectId)
                    }

                    else -> {
                        Log.e(
                            TAG,
                            "osm_object_type has invalid value OSM_ATP API comparison response, this response is malformed"
                        )
                        return@forEach
                    }
                }
            }
            val unparsedAtpTags = feature.jsonObject["atp_tags"]?.jsonPrimitive?.content
            if (unparsedAtpTags == null) {
                Log.e(
                    TAG,
                    "tagsInATP entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            //val tagsInATP = unparsedAtpTags.mapValues { // TODO avoid double-parsing
            val tagsInATP = Json.parseToJsonElement(unparsedAtpTags).jsonObject.mapValues {
                it.value.jsonPrimitive.content
            }
            val rawOsmTags = feature.jsonObject["osm_match_tags"]?.jsonPrimitive?.content
            val parsedRawOsmTags = Json.parseToJsonElement(rawOsmTags!!) // TODO avoid double-parsing
            //val tagsInOSM = if (isParsedNull(feature.jsonObject["osm_match_tags"])) {  // TODO avoid double-parsing
            val tagsInOSM = if (isParsedNull(parsedRawOsmTags)) {
                null
            } else {
                //feature.jsonObject["osm_match_tags"]?.jsonObject?.mapValues { // TODO avoid double-parsing
                parsedRawOsmTags.jsonObject.mapValues {
                    it.value.jsonPrimitive.content
                }
            }
            val rawErrorValue = feature.jsonObject["report_type"]?.jsonPrimitive?.content
            val reportType = rawErrorValue.let { errorValue ->
                when (errorValue) {
                    "MISSING_POI_IN_OPENSTREETMAP" -> {
                        ReportType.MISSING_POI_IN_OPENSTREETMAP
                    }
                    "OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP" -> {
                        ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP
                    }
                    else -> {
                        Log.e(TAG, "report_type has invalid value ($errorValue) OSM_ATP API comparison response, this response is malformed")
                        return@forEach
                    }
                }
            }
            returned.add(AtpEntry(
                position = LatLon(lat, lon),
                id = id,
                osmMatch = osmMatch,
                tagsInATP = tagsInATP,
                tagsInOSM = tagsInOSM,
                reportType = reportType,
            ))
        }
        return returned
    }
    companion object {
        private const val TAG = "AtpApiParser"
    }
}
