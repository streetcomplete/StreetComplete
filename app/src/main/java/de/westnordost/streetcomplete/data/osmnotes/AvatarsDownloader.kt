package de.westnordost.streetcomplete.data.osmnotes

import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import okio.FileSystem
import okio.IOException
import okio.Path

interface AvatarStore {
    fun cachedProfileImagePath(userId: Long): String?
}

/** Downloads and stores the OSM avatars of users */
class AvatarsDownloader(
    private val httpClient: HttpClient,
    private val userApi: UserApi,
    private val filesystem: FileSystem,
    private val cacheDir: Path
) : AvatarStore {

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
        val avatarPath = avatarPath(userId)
        try {
            val response = httpClient.get(avatarUrl) {
                expectSuccess = true
            }
            filesystem.write(avatarPath) {
                this.write(response.readBytes())
            }
            Log.d(TAG, "Downloaded file: $avatarPath")
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download avatar for user id $userId")
        }
    }

    override fun cachedProfileImagePath(userId: Long): String? {
        val path = avatarPath(userId)
        return if (filesystem.exists(path)) {
            path.toString()
        } else {
            null
        }
    }

    private fun avatarPath(userId: Long) = cacheDir.resolve("$userId")

    private fun ensureCacheDirExists(): Boolean = try {
        filesystem.createDirectory(cacheDir)
        true
    } catch (e: IOException) {
        false
    }

    companion object {
        private const val TAG = "OsmAvatarsDownload"
    }
}
