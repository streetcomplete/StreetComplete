package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.QueryTooBigException
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.toOsmApiString
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import de.westnordost.streetcomplete.util.ktx.format
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.asSource
import kotlinx.io.buffered

class NotesApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userAccessTokenSource: UserAccessTokenSource,
    private val notesApiParser: NotesApiParser
) : NotesApiClient {

    override suspend fun create(pos: LatLon, text: String): Note = wrapApiClientExceptions {
        val response = httpClient.post(baseUrl + "notes") {
            userAccessTokenSource.accessToken?.let { bearerAuth(it) }
            parameter("lat", pos.latitude.format(7))
            parameter("lon", pos.longitude.format(7))
            parameter("text", text)
            expectSuccess = true
        }
        val source = response.bodyAsChannel().asSource().buffered()
        return notesApiParser.parseNotes(source).single()
    }

    override suspend fun comment(id: Long, text: String): Note = wrapApiClientExceptions {
        try {
            val response = httpClient.post(baseUrl + "notes/$id/comment") {
                userAccessTokenSource.accessToken?.let { bearerAuth(it) }
                parameter("text", text)
                expectSuccess = true
            }
            val source = response.bodyAsChannel().asSource().buffered()
            return notesApiParser.parseNotes(source).single()
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

    override suspend fun get(id: Long): Note? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + "notes/$id") { expectSuccess = true }
            val source = response.bodyAsChannel().asSource().buffered()
            return notesApiParser.parseNotes(source).singleOrNull()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                // hidden by moderator, does not exist (yet)
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }

    override suspend fun getAllOpen(
        bounds: BoundingBox,
        limit: Int?
    ): List<Note> = wrapApiClientExceptions {
        if (bounds.crosses180thMeridian) {
            throw IllegalArgumentException("Bounding box crosses 180th meridian")
        }

        try {
            val response = httpClient.get(baseUrl + "notes") {
                userAccessTokenSource.accessToken?.let { bearerAuth(it) }
                parameter("bbox", bounds.toOsmApiString())
                parameter("limit", limit)
                parameter("closed", 0)
                expectSuccess = true
            }
            val source = response.bodyAsChannel().asSource().buffered()
            return notesApiParser.parseNotes(source)
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.BadRequest) {
                throw QueryTooBigException(e.message, e)
            } else {
                throw e
            }
        }
    }
}
