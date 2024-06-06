package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.common.errors.OsmConnectionException
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.traces.GpsTraceDetails
import de.westnordost.osmapi.traces.GpsTracesApi
import de.westnordost.osmapi.traces.GpsTrackpoint
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime

class TracksApiImpl(osm: OsmConnection) : TracksApi {
    private val api: GpsTracesApi = GpsTracesApi(osm)

    override fun create(trackpoints: List<Trackpoint>, noteText: String?): Long = wrapExceptions {
        // Filename is just the start of the track
        // https://stackoverflow.com/a/49862573/7718197
        val name = Instant.fromEpochMilliseconds(trackpoints[0].time).toLocalDateTime(TimeZone.UTC).toTrackFilename()
        val visibility = GpsTraceDetails.Visibility.IDENTIFIABLE
        val description = noteText ?: "Uploaded via ${ApplicationConstants.USER_AGENT}"
        val tags = listOf(ApplicationConstants.NAME.lowercase())

        // Generate history of trackpoints
        val history = trackpoints.mapIndexed { idx, it ->
            GpsTrackpoint(
                OsmLatLon(it.position.latitude, it.position.longitude),
                Instant.fromEpochMilliseconds(it.time).toJavaInstant(),
                idx == 0,
                it.accuracy,
                it.elevation
            )
        }

        // Finally query the API and return!
        api.create(name, visibility, description, tags, history)
    }
}

private inline fun <T> wrapExceptions(block: () -> T): T =
    try {
        block()
    } catch (e: OsmAuthorizationException) {
        throw AuthorizationException(e.message, e)
    } catch (e: OsmConnectionException) {
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiReadResponseException) {
        // probably a temporary connection error
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiException) {
        // request timeout is a temporary connection error
        throw if (e.errorCode == 408) ConnectionException(e.message, e) else e
    }

private fun LocalDateTime.toTrackFilename(): String {
    fun Int.f(len: Int): String = toString().padStart(len, '0')
    return ("${year.f(4)}_${monthNumber.f(2)}_${dayOfMonth.f(2)}"
        + "T${hour.f(2)}_${minute.f(2)}_${second.f(2)}.${nanosecond.f(6)}Z.gpx")
}
