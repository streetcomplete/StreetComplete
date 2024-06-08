package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.ConnectionException
import de.westnordost.streetcomplete.data.wrapApiClientExceptions
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

/**
 * Talks with OSM user API
 */
class UserApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
    private val userApiParser: UserApiParser,
) {
    /**
     * @return the user info of the current user
     *
     * @throws AuthorizationException if we are not authorized to read user details (scope "read_prefs")
     * @throws ConnectionException on connection or server error
     */
    suspend fun getMine(): UserInfo = wrapApiClientExceptions {
        val response = httpClient.get(baseUrl + "user/details") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
            expectSuccess = true
        }
        val body = response.body<String>()
        return userApiParser.parseUsers(body).first()
    }

    /**
     * @param userId id of the user to get the user info for
     * @return the user info of the given user. Null if the user does not exist.
     *
     * @throws ConnectionException on connection or server error
     */
    suspend fun get(userId: Long): UserInfo? = wrapApiClientExceptions {
        try {
            val response = httpClient.get(baseUrl + "user/$userId") { expectSuccess = true }
            val body = response.body<String>()
            return userApiParser.parseUsers(body).first()
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Gone, HttpStatusCode.NotFound -> return null
                else -> throw e
            }
        }
    }
}
