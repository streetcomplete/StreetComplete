package de.westnordost.streetcomplete.data.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName

class UserApiParser {
    private val xml = XML { defaultPolicy { ignoreUnknownChildren() } }

    fun parseUsers(osmXml: String): List<UserInfo> {
        val osm = xml.decodeFromString<ApiOsm>(osmXml)
        return osm.users.map { it.toUserInfo() }
    }
}

private fun ApiUser.toUserInfo() = UserInfo(
    id = id,
    displayName = displayName,
    profileImageUrl = img?.href,
    unreadMessagesCount = messages?.received?.unread
)

// https://wiki.openstreetmap.org/wiki/API_v0.6#Details_of_the_logged-in_user:_GET_/api/0.6/user/details
// It's a bit unwieldy and we are lazy. We just include what we actually use in the app and tell
// the parser to ignore "unknown" values.

@Serializable
@SerialName("osm")
private data class ApiOsm(val users: List<ApiUser>)

@Serializable
@XmlSerialName("user")
private data class ApiUser(
    @SerialName("display_name") val displayName: String,
    val id: Long,
    val img: ApiUserImg?,
    val messages: ApiUserMessages?,
)

@Serializable
@XmlSerialName("img")
private data class ApiUserImg(val href: String)

@Serializable
@XmlSerialName("messages")
private data class ApiUserMessages(val received: ApiUserReceivedMessages?)

@Serializable
@XmlSerialName("received")
private data class ApiUserReceivedMessages(val unread: Int)
