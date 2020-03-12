package de.westnordost.streetcomplete.data.osmnotes

import android.annotation.SuppressLint
import android.util.Log
import de.westnordost.osmapi.user.UserDao
import de.westnordost.streetcomplete.ktx.saveToFile
import java.io.File
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class OsmAvatarsDownloader @Inject constructor(
    private val userDao: UserDao,
    @Named("AvatarsCacheDirectory") private val cacheDir: File
) {

    fun download(userIds: Collection<Long>) {
        val userAvatars = downloadUserAvatarUrls(userIds)

        downloadUserAvatars(userAvatars)
    }

    private fun downloadUserAvatars(userAvatars: Map<Long, String>) {
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            Log.w(TAG, "Unable to create directories for avatars")
            return
        }

        for ((userId, avatarUrl) in userAvatars) {
            try {
                val avatarFile = File(cacheDir, "$userId")
                URL(avatarUrl).saveToFile(avatarFile)
                Log.i(TAG, "Saved file: ${avatarFile.path}")
            } catch (e: IOException) {
                Log.w(TAG, "Unable to download avatar for user id $userId")
            }
        }
    }

    private fun downloadUserAvatarUrls(userIds: Collection<Long>): Map<Long, String> {
        @SuppressLint("UseSparseArrays")
        val userAvatars = HashMap<Long, String>()
        for (userId in userIds) {
            userDao.get(userId)?.profileImageUrl?.let {
                userAvatars[userId] = it
            }
        }
        return userAvatars
    }

    companion object {
        private const val TAG = "OsmAvatarsDownload"
    }
}
