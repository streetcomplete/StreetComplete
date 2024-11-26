package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.osmnotes.AvatarsDownloader
import de.westnordost.streetcomplete.data.user.statistics.StatisticsApiClient
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserUpdater(
    private val userApi: UserApiClient,
    private val avatarsDownloader: AvatarsDownloader,
    private val statisticsApiClient: StatisticsApiClient,
    private val userDataController: UserDataController,
    private val statisticsController: StatisticsController,
    private val userLoginSource: UserLoginSource
) {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val userLoginListener = object : UserLoginSource.Listener {
        override fun onLoggedIn() {
            update()
        }
        override fun onLoggedOut() {
            clear()
        }
    }

    interface Listener {
        fun onUserAvatarUpdated()
    }
    private val userAvatarListeners = Listeners<Listener>()

    init {
        userLoginSource.addListener(userLoginListener)
    }

    fun update() = coroutineScope.launch(Dispatchers.IO) {
        if (!userLoginSource.isLoggedIn) return@launch
        try {
            val userDetails = userApi.getMine()

            userDataController.setDetails(userDetails)
            val profileImageUrl = userDetails.profileImageUrl
            if (profileImageUrl != null) {
                updateAvatar(userDetails.id, profileImageUrl)
            }
            updateStatistics(userDetails.id)
        } catch (e: Exception) {
            Log.w(TAG, "Unable to download user details", e)
        }
    }

    fun clear() {
        userDataController.clear()
        statisticsController.clear()
    }

    private fun updateAvatar(userId: Long, imageUrl: String) = coroutineScope.launch(Dispatchers.IO) {
        avatarsDownloader.download(userId, imageUrl)
        userAvatarListeners.forEach { it.onUserAvatarUpdated() }
    }

    private fun updateStatistics(userId: Long) = coroutineScope.launch(Dispatchers.IO) {
        try {
            val statistics = statisticsApiClient.get(userId)
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
