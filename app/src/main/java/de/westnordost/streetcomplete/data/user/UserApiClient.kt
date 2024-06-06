package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.AuthorizationException
import de.westnordost.streetcomplete.data.CommunicationException
import de.westnordost.streetcomplete.data.ConnectionException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess

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
     * @throws AuthorizationException if we are not authorized to read user details (scope "read_prefs")
     */
    suspend fun getMine(): UserInfo {
        val response = httpClient.get(baseUrl + "user/details") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
        }
        val status = response.status

        if (status.isSuccess()) {
            val body = response.body<String>()
            return userApiParser.parseUsers(body).first()
        }

        when {
            status == HttpStatusCode.Forbidden || status == HttpStatusCode.Unauthorized -> {
                throw AuthorizationException(status.toString())
            }
            status == HttpStatusCode.RequestTimeout || status.value in 500..599 -> {
                throw ConnectionException(status.toString())
            }
            else -> {
                throw CommunicationException(status.toString())
            }
        }
    }

    /**
     * @param userId id of the user to get the user info for
     * @return the user info of the given user. Null if the user does not exist.
     */
    suspend fun get(userId: Long): UserInfo? {
        val response = httpClient.get("user/$userId") {
            userLoginSource.accessToken?.let { bearerAuth(it) }
        }
        val status = response.status

        if (status.isSuccess()) {
            val body = response.body<String>()
            return userApiParser.parseUsers(body).first()
        }

        if (status == HttpStatusCode.Gone || status == HttpStatusCode.NotFound) {
            return null
        }

        when {
            status == HttpStatusCode.Forbidden || status == HttpStatusCode.Unauthorized -> {
                throw AuthorizationException(status.toString())
            }
            status == HttpStatusCode.RequestTimeout || status.value in 500..599 -> {
                throw ConnectionException(status.toString())
            }
            else -> {
                throw CommunicationException(status.toString())
            }
        }
    }
}

// TODO TEST

