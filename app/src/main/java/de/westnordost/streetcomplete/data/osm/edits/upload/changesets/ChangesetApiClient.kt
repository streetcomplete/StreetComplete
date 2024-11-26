package de.westnordost.streetcomplete.data.osm.edits.upload.changesets

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConflictException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

class ChangesetApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
    private val serializer: ChangesetApiSerializer,
) {
    /**
     * Open a new changeset with the given tags
     *
     * @param tags tags of this changeset. Usually it is comment and source.
     *
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ConnectionException if a temporary network connection problem occurs
     *
     * @return the id of the changeset
     */
    suspend fun open(tags: Map<String, String>): Long = wrapApiClientExceptions {
        val response = httpClient.put(baseUrl + "changeset/create") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
            setBody(serializer.serialize(tags))
            expectSuccess = true
        }
        return response.body<String>().toLong()
    }

    /**
     * Closes the given changeset.
     *
     * @param id id of the changeset to close
     *
     * @throws ConflictException if the changeset has already been closed or does not exist
     * @throws AuthorizationException if the application does not have permission to edit the map
     *                                (OAuth scope "write_api")
     * @throws ConnectionException if a temporary network connection problem occurs
     */
    suspend fun close(id: Long): Unit = wrapApiClientExceptions {
        try {
            httpClient.put(baseUrl + "changeset/$id/close") {
                userLoginSource.accessToken?.let { bearerAuth(it) }
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
