package de.westnordost.streetcomplete.data.osmcal

import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import kotlinx.datetime.Instant
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource

class OsmCalParser() {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val idRegex = Regex("https:\\/\\/osmcal\\.org\\/event\\/([0-9]+)\\/?")

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(json: Source): List<CalendarEvent> =
        jsonParser.decodeFromSource<List<CalendarEventJson>>(json).mapNotNull { it.toCalendarEvent() }

    private fun CalendarEventJson.toCalendarEvent(): CalendarEvent? {
        // ignore cancelled events!
        if (cancelled == true) return null

        // fish the id out of the url string
        val match = idRegex.matchEntire(url) ?: throw SerializationException("Url $url didn't match expected scheme")
        val id = match.groupValues[1].toLong()

        // no location? ignore!
        if (location == null) return null

        return CalendarEvent(
            id = id,
            name = name,
            startDate = Instant.parse(date.start),
            endDate = date.end?.let { Instant.parse(it) },
            wholeDay = date.wholeDay,
            position = LatLon(location.coords[1], location.coords[0]),
            venue = location.venue,
            address = location.detailed,
            notified = false
        )
    }
}

@Serializable
private data class CalendarEventJson(
    val name: String,
    val url: String,
    val date: CalendarEventDateJson,
    val location: CalendarEventLocationJson? = null,
    val cancelled: Boolean? = false
)

@Serializable
private data class CalendarEventDateJson(
    val start: String,
    val end: String? = null,
    val human: String,
    @SerialName("human_short")
    val humanShort: String,
    @SerialName("whole_day")
    val wholeDay: Boolean,
)

@Serializable
private data class CalendarEventLocationJson(
    val short: String? = null,
    val detailed: String? = null,
    val coords: DoubleArray,
    val venue: String? = null,
)
