package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toOsmApiString
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import de.westnordost.streetcomplete.util.ktx.format
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode

/**
 * Creates, comments, closes, reopens and search for notes.
 */
class NotesApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
    private val notesApiParser: NotesApiParser
) {
    /**
     * Create a new note at the given location
     *
     * @param pos position of the note.
     * @param text text for the new note. Must not be empty.
     *
     * @throws AuthorizationException if this application is not authorized to write notes
     *                                (scope "write_notes")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the new note
     */
    suspend fun create(pos: LatLon, text: String): Note = wrapApiClientExceptions {
        val response = httpClient.post(baseUrl + "notes") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
            parameter("lat", pos.latitude.format(7))
            parameter("lon", pos.longitude.format(7))
            parameter("text", text)
            expectSuccess = true
        }
        return notesApiParser.parseNotes(response.body<String>()).single()
    }

    /**
     * @param id id of the note
     * @param text comment to be added to the note. Must not be empty
     *
     * @throws ConflictException if the note has already been closed or doesn't exist (anymore).
     * @throws AuthorizationException if this application is not authorized to write notes
     *                                (scope "write_notes")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the updated commented note
     */
    suspend fun comment(id: Long, text: String): Note = wrapApiClientExceptions {
        try {
            val response = httpClient.post(baseUrl + "notes/$id/comment") {
                userLoginSource.accessToken?.let { bearerAuth(it) }
                parameter("text", text)
                expectSuccess = true
            }
            return notesApiParser.parseNotes(response.body<String>()).single()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                // hidden by moderator, does not exist (yet), has already been closed
                HttpStatusCode.Gone, HttpStatusCode.NotFound, HttpStatusCode.Conflict -> {
                    throw ConflictException(e.message, e)
                }
                else -> throw e
            }
        }
    }

    /**
     * @param id id of the note
     *
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the note with the given id. null if the note with that id does not exist (anymore).
     */
    suspend fun get(id: Long): Note? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + "notes/$id") { expectSuccess = true }
            val body = response.body<String>()
            return notesApiParser.parseNotes(body).singleOrNull()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                // hidden by moderator, does not exist (yet)
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }

    /**
     * Retrieve all open notes in the given area
     *
     * @param bounds the area within the notes should be queried. This is usually limited at 25
     *               square degrees. Check the server capabilities.
     * @param limit number of entries returned at maximum. Any value between 1 and 10000
     *
     * @throws QueryTooBigException if the bounds area or the limit is too large
     * @throws IllegalArgumentException if the bounds cross the 180th meridian
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the incoming notes
     */
    suspend fun getAllOpen(bounds: BoundingBox, limit: Int? = null): List<Note> = wrapApiClientExceptions {
        if (bounds.crosses180thMeridian) {
            throw IllegalArgumentException("Bounding box crosses 180th meridian")
        }

        try {
            val response = httpClient.get(baseUrl + "notes") {
                userLoginSource.accessToken?.let { bearerAuth(it) }
                parameter("bbox", bounds.toOsmApiString())
                parameter("limit", limit)
                parameter("closed", 0)
                expectSuccess = true
            }
            val body = response.body<String>()
            return notesApiParser.parseNotes(body)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.BadRequest) {
                throw QueryTooBigException(e.message, e)
            } else {
                throw e
            }
        }
    }
}
