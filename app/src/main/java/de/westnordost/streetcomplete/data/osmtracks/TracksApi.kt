package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.CommunicationException
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.util.ktx.truncate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.datetime.Instant
import nl.adaptivity.xmlutil.serialization.XML

/**
 * Talks with OSM traces API to uploads GPS trackpoints
 */
class TracksApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
) {

    /**
     * Upload a list of trackpoints as a GPX
     *
     * @param trackpoints recorded trackpoints
     * @param noteText optional description text
     *
     * @throws AuthorizationException if we are not authorized to upload traces (scope "write_gpx")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return id of the uploaded track
     */
    suspend fun create(trackpoints: List<Trackpoint>, noteText: String? = null): Long {
        val name = Instant.fromEpochMilliseconds(trackpoints.first().time).toString() + ".gpx"
        val description = noteText ?: "Uploaded via ${ApplicationConstants.USER_AGENT}"
        val tags = listOf(ApplicationConstants.NAME.lowercase()).joinToString()
        val xml = XML.encodeToString(trackpoints.toGpx())

        val response = httpClient.post(baseUrl + "gpx/create") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
            setBody(MultiPartFormDataContent(formData {
                append("file", xml, Headers.build {
                    append(HttpHeaders.ContentType, "application/gpx+xml")
                    append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                })
                append("description", description.truncate(255))
                append("tags", tags)
                append("visibility", "identifiable")
            }))
        }
        val status = response.status

        when {
            status.isSuccess() -> {
                return response.body<String>().toLong()
            }
            status == HttpStatusCode.Forbidden || status == HttpStatusCode.Unauthorized -> {
                throw AuthorizationException(status.toString())
            }
            status.value in 500..599 -> {
                throw ConnectionException(status.toString())
            }
            else -> {
                throw CommunicationException(status.toString())
            }
        }
    }
}

private fun List<Trackpoint>.toGpx() = Gpx(
    version = 1.0f,
    creator = ApplicationConstants.USER_AGENT,
    tracks = listOf(GpsTrack(listOf(GpsTrackSegment(map { it.toGpsTrackPoint() }))))
)

private fun Trackpoint.toGpsTrackPoint() = GpsTrackPoint(
    lat = position.latitude,
    lon = position.longitude,
    time = Instant.fromEpochMilliseconds(time),
    ele = elevation,
    hdop = accuracy
)
