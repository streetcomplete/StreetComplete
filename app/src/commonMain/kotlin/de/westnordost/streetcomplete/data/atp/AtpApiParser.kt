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
        val jsonElement = Json.parseToJsonElement(source.readString())
        val features = jsonElement.jsonObject["features"]?.jsonArray
        if(features == null) {
            Log.e(TAG, "features entry missing in OSM_ATP API comparison response, this response is malformed")
            return emptyList()
        }
        features.forEach { feature ->
            val properties = feature.jsonObject["properties"]?.jsonObject
            if (properties == null) {
                Log.e(
                    TAG,
                    "properties entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val lon = properties["atp_center"]?.jsonObject["lon"]?.toString()?.toDoubleOrNull()
            if (lon == null) {
                Log.e(
                    TAG,
                    "lon entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val lat = properties["atp_center"]?.jsonObject["lat"]?.toString()?.toDoubleOrNull()
            if (lat == null) {
                Log.e(
                    TAG,
                    "lat entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val id = properties["entry_id"]?.toString()?.toLongOrNull()
            if (id == null) {
                Log.e(
                    TAG,
                    "id entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val rawOsmObjectId = properties["osm_object_id"]
            val rawOsmObjectType = properties["osm_object_type"]
            val osmObjectType = if(isParsedNull(rawOsmObjectType)) {
                null
            } else {
                rawOsmObjectType?.jsonPrimitive?.content?.toString()
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
            val unparsedAtpTags = properties["atp_tags"]?.jsonObject
            if (unparsedAtpTags == null) {
                Log.e(
                    TAG,
                    "tagsInATP entry missing in OSM_ATP API comparison response, this response is malformed"
                )
                return@forEach
            }
            val tagsInATP = unparsedAtpTags.mapValues {
                it.value.jsonPrimitive.content
            }
            val tagsInOSM = if (isParsedNull(properties["osm_match_tags"])) {
                null
            } else {
                properties["osm_match_tags"]?.jsonObject?.mapValues {
                    it.value.jsonPrimitive.content
                }
            }
            val rawErrorValue = properties["error_type"]?.jsonPrimitive?.content
            val reportType = rawErrorValue.let { errorValue ->
                when (errorValue) {
                    "MISSING_POI_IN_OPENSTREETMAP" -> {
                        ReportType.MISSING_POI_IN_OPENSTREETMAP
                    }
                    "OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP" -> {
                        ReportType.OPENING_HOURS_REPORTED_AS_OUTDATED_IN_OPENSTREETMAP
                    }
                    else -> {
                        Log.e(TAG, "error_type has invalid value ($errorValue) OSM_ATP API comparison response, this response is malformed")
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
