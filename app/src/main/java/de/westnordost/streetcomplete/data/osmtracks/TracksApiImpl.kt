package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.common.errors.OsmApiException
import de.westnordost.osmapi.common.errors.OsmApiReadResponseException
import de.westnordost.osmapi.common.errors.OsmAuthorizationException
import de.westnordost.osmapi.common.errors.OsmConflictException
import de.westnordost.osmapi.common.errors.OsmConnectionException
import de.westnordost.osmapi.common.errors.OsmQueryTooBigException
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.traces.GpsTraceDetails
import de.westnordost.osmapi.traces.GpsTracesApi
import de.westnordost.osmapi.traces.GpsTrackpoint
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.ConnectionException
import de.westnordost.streetcomplete.data.download.QueryTooBigException
import de.westnordost.streetcomplete.data.upload.ConflictException
import de.westnordost.streetcomplete.data.user.AuthorizationException
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TracksApiImpl(osm: OsmConnection) : TracksApi {
    private val api: GpsTracesApi = GpsTracesApi(osm)

    override fun create(tracks: List<Trackpoint>): Track = wrapExceptions {

        // Filename is just the start of the track
        // https://stackoverflow.com/a/49862573/7718197
        val name = DateTimeFormatter
            .ofPattern("yyyy_MM_dd'T'HH_mm_ss.SSSSSS'Z'")
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochSecond(tracks[0].time)) + ".gpx"
        val visibility = GpsTraceDetails.Visibility.IDENTIFIABLE
        val description = ApplicationConstants.USER_AGENT
        val tags = listOf(ApplicationConstants.NAME.lowercase())

        // Generate history of trackpoints
        val trackpoints = tracks.mapIndexed { idx, it -> 
            GpsTrackpoint(
                OsmLatLon(it.position.latitude, it.position.longitude),
                Instant.ofEpochMilli(it.time),
                idx == 0,
                it.horizontalDilutionOfPrecision,
                it.elevation
            )
        }

        // Finally query the API and return!
        val traceId = api.create(name, visibility, description, tags, trackpoints)
        val details = api.get(traceId)
        Track(details.id, details.userName)
    }


}

private inline fun <T> wrapExceptions(block: () -> T): T =
    try {
        block()
    } catch (e: OsmAuthorizationException) {
        throw AuthorizationException(e.message, e)
    } catch (e: OsmConflictException) {
        throw ConflictException(e.message, e)
    } catch (e: OsmQueryTooBigException) {
        throw QueryTooBigException(e.message, e)
    } catch (e: OsmConnectionException) {
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiReadResponseException) {
        // probably a temporary connection error
        throw ConnectionException(e.message, e)
    } catch (e: OsmApiException) {
        // request timeout is a temporary connection error
        throw if (e.errorCode == 408) ConnectionException(e.message, e) else e
    }

