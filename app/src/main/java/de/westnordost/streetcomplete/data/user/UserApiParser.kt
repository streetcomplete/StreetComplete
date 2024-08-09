package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.util.ktx.attribute
import kotlinx.serialization.SerializationException
import nl.adaptivity.xmlutil.EventType.*
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

class UserApiParser {
    fun parseUsers(osmXml: String): List<UserInfo> =
        xmlStreaming.newReader(osmXml).parseUsers()
}

private fun XmlReader.parseUsers(): List<UserInfo> = try {
    val result = ArrayList<UserInfo>(1)
    var id: Long? = null
    var displayName: String? = null
    var img: String? = null
    var unread: Int? = null
    var isMessages = false
    forEach { when (it) {
        START_ELEMENT -> when (localName) {
            "user" -> {
                id = attribute("id").toLong()
                displayName = attribute("display_name")
                img = null
                unread = null
            }
            "img" -> img = attribute("href")
            "messages" -> isMessages = true
            "received" -> if (isMessages) unread = attribute("unread").toInt()
        }
        END_ELEMENT -> when (localName) {
            "user" -> result.add(UserInfo(id!!, displayName!!, img, unread))
            "messages" -> isMessages = false
        }
        else -> {}
    } }
    result
} catch (e: Exception) { throw SerializationException(e) }
