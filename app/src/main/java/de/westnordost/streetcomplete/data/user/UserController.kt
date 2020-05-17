package de.westnordost.streetcomplete.data.user

import android.util.Log
import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.data.UserApi
import de.westnordost.streetcomplete.data.osmnotes.OsmAvatarsDownloader
import de.westnordost.streetcomplete.data.user.achievements.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import oauth.signpost.OAuthConsumer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class UserController @Inject constructor(
    private val userApi: UserApi,
    private val oAuthStore: OAuthStore,
    private val userStore: UserStore,
    private val userAchievementsDao: UserAchievementsDao,
    private val userLinksDao: UserLinksDao,
    private val avatarsDownloader: OsmAvatarsDownloader,
    private val statisticsUpdater: StatisticsUpdater,
    private val statisticsDao: QuestStatisticsDao,
    private val countryStatisticsDao: CountryStatisticsDao,
    private val osmConnection: OsmConnection
): CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val loginStatusListeners: MutableList<UserLoginStatusListener> = CopyOnWriteArrayList()
    private val userAvatarListeners: MutableList<UserAvatarListener> = CopyOnWriteArrayList()

    val isLoggedIn: Boolean get() = oAuthStore.isAuthorized

    fun logIn(consumer: OAuthConsumer) {
        oAuthStore.oAuthConsumer = consumer
        osmConnection.oAuth = consumer
        updateUser()
        loginStatusListeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        userStore.clear()
        oAuthStore.oAuthConsumer = null
        osmConnection.oAuth = null
        statisticsDao.clear()
        countryStatisticsDao.clear()
        userAchievementsDao.clear()
        userLinksDao.clear()
        userStore.clear()
        loginStatusListeners.forEach { it.onLoggedOut() }
    }

    fun updateUser() = launch(Dispatchers.IO) {
        try {
            val userDetails = userApi.getMine()

            userStore.setDetails(userDetails)
            val profileImageUrl = userDetails.profileImageUrl
            if (profileImageUrl != null) {
                updateAvatar(userDetails.id, profileImageUrl)
            }
            updateStatistics(userDetails.id)
        }
        catch (e: Exception) {
            Log.w(TAG, "Unable to download user details", e)
        }
    }

    private fun updateAvatar(userId: Long, imageUrl: String) = launch(Dispatchers.IO) {
        avatarsDownloader.download(userId, imageUrl)
        userAvatarListeners.forEach { it.onUserAvatarUpdated() }
    }

    private fun updateStatistics(userId: Long) = launch(Dispatchers.IO) {
        statisticsUpdater.updateFromBackend(userId)
    }

    fun addLoginStatusListener(listener: UserLoginStatusListener) {
        loginStatusListeners.add(listener)
    }
    fun removeLoginStatusListener(listener: UserLoginStatusListener) {
        loginStatusListeners.remove(listener)
    }
    fun addUserAvatarListener(listener: UserAvatarListener) {
        userAvatarListeners.add(listener)
    }
    fun removeUserAvatarListener(listener: UserAvatarListener) {
        userAvatarListeners.remove(listener)
    }

    companion object {
        const val TAG = "UserController"
    }
}

interface UserLoginStatusListener {
    fun onLoggedIn()
    fun onLoggedOut()
}

interface UserAvatarListener {
    fun onUserAvatarUpdated()
}