package de.westnordost.streetcomplete.data.user

import android.util.Log
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.data.osmnotes.AvatarsDownloader
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.data.user.statistics.StatisticsDownloader
import de.westnordost.streetcomplete.util.Listeners
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserUpdater(
    private val userApi: UserApi,
    private val avatarsDownloader: AvatarsDownloader,
    private val statisticsDownloader: StatisticsDownloader,
    private val userController: UserDataController,
    private val statisticsController: StatisticsController
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    interface Listener {
        fun onUserAvatarUpdated()
    }
    private val userAvatarListeners = Listeners<Listener>()

    fun update() = coroutineScope.launch(Dispatchers.IO) {
        try {
            val userDetails = userApi.getMine()

            userController.setDetails(userDetails)
            val profileImageUrl = userDetails.profileImageUrl
            if (profileImageUrl != null) {
                updateAvatar(userDetails.id, profileImageUrl)
            }
            updateStatistics(userDetails.id)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download user details", e)
        }
    }

    private fun updateAvatar(userId: Long, imageUrl: String) = coroutineScope.launch(Dispatchers.IO) {
        avatarsDownloader.download(userId, imageUrl)
        userAvatarListeners.forEach { it.onUserAvatarUpdated() }
    }

    private fun updateStatistics(userId: Long) = coroutineScope.launch(Dispatchers.IO) {
        try {
            val statistics = statisticsDownloader.download(userId)
            statisticsController.updateAll(statistics)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download statistics", e)
        }
    }

    fun addUserAvatarListener(listener: Listener) {
        userAvatarListeners.add(listener)
    }
    fun removeUserAvatarListener(listener: Listener) {
        userAvatarListeners.remove(listener)
    }

    companion object {
        private const val TAG = "UserUpdater"
    }
}
