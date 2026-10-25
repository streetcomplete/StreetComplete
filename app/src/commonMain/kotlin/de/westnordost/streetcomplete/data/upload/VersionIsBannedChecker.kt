package de.westnordost.streetcomplete.data.upload

import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get

/** Asks remote server if this version of the app is banned */
class VersionIsBannedChecker(
    private val httpClient: HttpClient,
    private val url: String,
    private val userAgent: String
) {

    suspend fun get(): BannedInfo {
        try {
            val response = httpClient.get(url) { expectSuccess = true }
            val bannedVersions = response.body<String>()
            for (bannedVersion in bannedVersions.lines()) {
                val destructuredVersion = bannedVersion.split("\t")
                if (destructuredVersion[0] == userAgent) {
                    val reason = if (destructuredVersion.size > 1) destructuredVersion[1] else null
                    return BannedInfo.IsBanned(reason)
                }
            }
        } catch (e: Exception) {
            // if there is an io exception, never mind then...! (The unreachability of the above
            // internet address should not lead to this app being unusable!)
            Log.w(TAG, "Unable to check banned versions", e)
        }
        return BannedInfo.IsNotBanned
    }

    companion object {
        private const val TAG = "VersionIsBannedChecker"
    }
}

class VersionBannedException(val banReason: String?) :
    RuntimeException("This version is banned from making any changes!")

sealed interface BannedInfo {
    data class IsBanned(val reason: String?) : BannedInfo
    data object IsNotBanned : BannedInfo
}
