package de.westnordost.streetcomplete.data.upload

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

/** Asks remote server if this version of the app is banned */
class VersionIsBannedChecker @Inject constructor(private val url: String, private val userAgent: String) {

    fun get(): BannedInfo {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(url).openConnection() as HttpURLConnection)
            connection.inputStream.bufferedReader().use { reader ->
                for (line in reader.lineSequence()) {
                    val text = line.split("\t".toRegex())
                    if (text[0] == userAgent) {
                        return IsBanned(if (text.size > 1) text[1] else null)
                    }
                }
            }
        } catch (e: IOException) {
            // if there is an io exception, never mind then...! (The unreachability of the above
            // internet address should not lead to this app being unusable!)
        } finally {
            connection?.disconnect()
        }
        return IsNotBanned
    }
}

class VersionBannedException(val banReason: String?)
    : RuntimeException("This version is banned from making any changes!")

sealed class BannedInfo

data class IsBanned(val reason: String?): BannedInfo()
object IsNotBanned : BannedInfo()
