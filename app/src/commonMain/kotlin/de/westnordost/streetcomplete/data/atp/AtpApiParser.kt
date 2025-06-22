package de.westnordost.streetcomplete.data.atp

import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.io.Source
import kotlinx.io.readString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AtpApiParser {
    fun parseAtpEntries(source: Source) : List<AtpEntry> {
        val returned = mutableListOf<AtpEntry>()
        val jsonElement = Json.parseToJsonElement(source.readString())
        val features = jsonElement.jsonObject["features"]?.jsonArray
        if(features == null) {
            Log.e(TAG, "features entry missing in OSM_ATP API comparison response, this response is malformed")
            return emptyList()
        }
        features.forEach { feature ->
            val geometry = feature.jsonObject["geometry"]?.jsonObject
            if(geometry == null) {
                Log.e(TAG, "geometry entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val lon = geometry["coordinates"]?.jsonArray[0]?.toString()?.toDoubleOrNull()
            if(lon == null) {
                Log.e(TAG, "lon entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val lat = geometry["coordinates"]?.jsonArray[1]?.toString()?.toDoubleOrNull()
            if(lat == null) {
                Log.e(TAG, "lat entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val properties = feature.jsonObject["properties"]?.jsonObject
            if(properties == null) {
                Log.e(TAG, "properties entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val id = properties["entry_id"]?.toString()?.toLongOrNull()
            if(id == null) {
                Log.e(TAG, "id entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val osmObjectType = properties["osm_object_type"]?.toString()
            val osmObjectId = properties["osm_object_id"]?.toString()?.toLongOrNull()
            val osmMatch = if(osmObjectType == null || osmObjectId == null) {
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
                        Log.e(TAG, "osm_object_type has invalid value OSM_ATP API comparison response, this response is malformed")
                        return@forEach
                    }
                }
            }
            val unparsedAtpTags = properties["atp_tags"]?.jsonObject
            if(unparsedAtpTags == null) {
                Log.e(TAG, "tagsInATP entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val tagsInATP = unparsedAtpTags.mapValues { it.value.toString() }
            if(tagsInATP == null) {
                Log.e(TAG, "tagsInATP entry missing in OSM_ATP API comparison response, this response is malformed")
                return@forEach
            }
            val tagsInOSM = properties["atp_tags"]?.jsonObject?.mapValues { it.value.toString() }
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
