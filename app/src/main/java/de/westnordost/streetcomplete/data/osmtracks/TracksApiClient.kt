package de.westnordost.streetcomplete.data.osmtracks

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import de.westnordost.streetcomplete.util.ktx.truncate
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Instant

/**
 * Talks with OSM traces API to uploads GPS trackpoints
 */
class TracksApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
    private val tracksSerializer: TracksSerializer
) {
    /**
     * Upload a list of trackpoints as a GPX
     *
     * @param trackpoints recorded trackpoints
     * @param noteText optional description text
     *
     * @throws AuthorizationException if not logged in or not not authorized to upload traces
     *                                (scope "write_gpx")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return id of the uploaded track
     */
    suspend fun create(trackpoints: List<Trackpoint>, noteText: String? = null): Long = wrapApiClientExceptions {
        val name = Instant.fromEpochMilliseconds(trackpoints.first().time).toString() + ".gpx"
        val description = noteText ?: "Uploaded via ${ApplicationConstants.USER_AGENT}"
        val tags = listOf(ApplicationConstants.NAME.lowercase()).joinToString()
        val xml = tracksSerializer.serialize(trackpoints)

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
            expectSuccess = true
        }
        return response.body<String>().toLong()
    }
}
