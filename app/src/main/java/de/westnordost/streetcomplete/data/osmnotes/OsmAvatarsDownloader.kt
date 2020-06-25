package de.westnordost.streetcomplete.data.osmnotes

import android.util.Log
import de.westnordost.streetcomplete.data.UserApi
import de.westnordost.streetcomplete.ktx.saveToFile
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

/** Downloads and stores the OSM avatars of users */
class OsmAvatarsDownloader @Inject constructor(
    private val userApi: UserApi,
    @Named("AvatarsCacheDirectory") private val cacheDir: File
) {

    fun download(userIds: Collection<Long>) {
        if (!ensureCacheDirExists()) {
            Log.w(TAG, "Unable to create directories for avatars")
            return
        }

        for (userId in userIds) {
            val avatarUrl = userApi.get(userId)?.profileImageUrl
            if (avatarUrl != null) {
                download(userId, avatarUrl)
            }
        }
    }

    /** download avatar for the given user and a known avatar url */
    fun download(userId: Long, avatarUrl: String) {
        if (!ensureCacheDirExists()) return
        try {
            val avatarFile = File(cacheDir, "$userId")
            URL(avatarUrl).saveToFile(avatarFile)
            Log.i(TAG, "Saved file: ${avatarFile.path}")
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
