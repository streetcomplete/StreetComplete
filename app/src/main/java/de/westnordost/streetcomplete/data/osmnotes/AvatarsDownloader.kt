package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.ktx.format
import de.westnordost.streetcomplete.ktx.saveToFile
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

/** Downloads and stores the OSM avatars of users */
class AvatarsDownloader @Inject constructor(
    private val userApi: UserApi,
    @Named("AvatarsCacheDirectory") private val cacheDir: File
) {

    fun download(userIds: Collection<Long>) {
        if (!ensureCacheDirExists()) {
            Log.w(TAG, "Unable to create directories for avatars")
            return
        }

        val time = System.currentTimeMillis()
        for (userId in userIds) {
            val avatarUrl = userApi.get(userId)?.profileImageUrl
            if (avatarUrl != null) {
                download(userId, avatarUrl)
            }
        }
        val seconds = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Downloaded ${userIds.size} avatar images in ${seconds.format(1)}s")
    }

    /** download avatar for the given user and a known avatar url */
    fun download(userId: Long, avatarUrl: String) {
        if (!ensureCacheDirExists()) return
        try {
            val avatarFile = File(cacheDir, "$userId")
            URL(avatarUrl).saveToFile(avatarFile)
            Log.d(TAG, "Downloaded file: ${avatarFile.path}")
        } catch (e: IOException) {
            Log.w(TAG, "Unable to download avatar for user id $userId")
        }
    }

    private fun ensureCacheDirExists(): Boolean {
        return cacheDir.exists() || cacheDir.mkdirs()
    }

    companion object {
        private const val TAG = "OsmAvatarsDownload"
    }
}
