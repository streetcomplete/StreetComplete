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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML

/**
 * Talks with OSM user API
 */
class UserApi(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val userLoginSource: UserLoginSource,
) {
    private val xml = XML { defaultPolicy { ignoreUnknownChildren() } }

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
            val osm = xml.decodeFromString<Osm>(body)
            return osm.toUserInfo()
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
            val osm = xml.decodeFromString<Osm>(body)
            return osm.toUserInfo()
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

data class UserInfo(
    val id: Long,
    val displayName: String,
    val profileImageUrl: String?,
    val unreadMessagesCount: Int? = null,
)

private fun Osm.toUserInfo() = UserInfo(
    id = user.id,
    displayName = user.displayName,
    profileImageUrl = user.img?.href,
    unreadMessagesCount = user.messages?.received?.unread
)

// https://wiki.openstreetmap.org/wiki/API_v0.6#Details_of_the_logged-in_user:_GET_/api/0.6/user/details
// It's a bit unwieldy and we are lazy. We just include what we actually use in the app and tell
// the parser to ignore "unknown" values.

@Serializable
@SerialName("osm")
private data class Osm(val user: OsmUser)

@Serializable
private data class OsmUser(
    @SerialName("display_name")
    val displayName: String,
    val id: Long,
    val img: OsmUserImg?,
    val messages: OsmUserMessages?,
)

@Serializable
private data class OsmUserImg(val href: String)

@Serializable
private data class OsmUserMessages(val received: OsmUserReceivedMessages?)

@Serializable
private data class OsmUserReceivedMessages(val unread: Int)
