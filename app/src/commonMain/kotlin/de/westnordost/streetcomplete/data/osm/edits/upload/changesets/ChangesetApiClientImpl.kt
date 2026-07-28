package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.user.UserAccessTokenSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class ChangesetApiClientImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userAccessTokenSource: UserAccessTokenSource,
    private val serializer: ChangesetApiSerializer,
) : ChangesetApiClient {

    override suspend fun open(tags: Map<String, String>): Long = wrapApiClientExceptions {
        val response = httpClient.put(baseUrl + "changeset/create") {
            userAccessTokenSource.accessToken?.let { bearerAuth(it) }
            setBody(serializer.serialize(tags))
            expectSuccess = true
        }
        return response.body<String>().toLong()
    }

    override suspend fun close(id: Long): Unit = wrapApiClientExceptions {
        try {
            httpClient.put(baseUrl + "changeset/$id/close") {
                userAccessTokenSource.accessToken?.let { bearerAuth(it) }
                expectSuccess = true
            }
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Conflict, HttpStatusCode.NotFound -> {
                    throw ConflictException(e.message, e)
                }
                else -> throw e
            }
        }
    }
}
