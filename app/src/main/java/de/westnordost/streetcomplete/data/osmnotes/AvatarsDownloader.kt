package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File

/** Downloads and stores the OSM avatars of users */
class AvatarsDownloader(
    private val httpClient: HttpClient,
    private val userApi: UserApi,
    private val cacheDir: File
) {

    suspend fun download(userIds: Collection<Long>) {
        if (!ensureCacheDirExists()) {
            Log.w(TAG, "Unable to create directories for avatars")
            return
        }

        val time = nowAsEpochMilliseconds()
        for (userId in userIds) {
            val avatarUrl = getProfileImageUrl(userId)
            if (avatarUrl != null) {
                download(userId, avatarUrl)
            }
        }
        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Downloaded ${userIds.size} avatar images in ${seconds.format(1)}s")
    }

    private fun getProfileImageUrl(userId: Long): String? =
        try {
            userApi.get(userId)?.profileImageUrl
        } catch (e: Exception) {
            Log.w(TAG, "Unable to query info for user id $userId")
            null
        }

    /** download avatar for the given user and a known avatar url */
    suspend fun download(userId: Long, avatarUrl: String) {
        if (!ensureCacheDirExists()) return
        val avatarFile = File(cacheDir, "$userId")
        try {
            val response = httpClient.get(avatarUrl) {
                expectSuccess = true
            }
            response.bodyAsChannel().copyAndClose(avatarFile.writeChannel())
            Log.d(TAG, "Downloaded file: ${avatarFile.path}")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download avatar for user id $userId")
        }
    }

    private fun ensureCacheDirExists(): Boolean =
        cacheDir.exists() || cacheDir.mkdirs()

    companion object {
        private const val TAG = "OsmAvatarsDownload"
    }
}
