package de.westnordost.streetcomplete.data.user

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
    private val osmConnection: OsmConnection
): CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val loginStatusListeners: MutableList<UserLoginStatusListener> = CopyOnWriteArrayList()
    private val userAvatarListeners: MutableList<UserAvatarListener> = CopyOnWriteArrayList()

    val isLoggedIn: Boolean get() = oAuthStore.isAuthorized

    fun logIn(consumer: OAuthConsumer) {
        oAuthStore.oAuthConsumer = consumer
        osmConnection.oAuth = consumer
        launch(Dispatchers.IO) {
            updateUser()
        }
        loginStatusListeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        userStore.clear()
        oAuthStore.oAuthConsumer = null
        osmConnection.oAuth = null
        statisticsDao.clear()
        userAchievementsDao.clear()
        userLinksDao.clear()
        loginStatusListeners.forEach { it.onLoggedOut() }
    }

    fun updateUser() {
        launch(Dispatchers.IO) {
            val userDetails = userApi.getMine()
            userStore.setDetails(userDetails)
            val profileImageUrl = userDetails.profileImageUrl
            if (profileImageUrl != null) {
                launch {
                    avatarsDownloader.download(userDetails.id, profileImageUrl)
                    userAvatarListeners.forEach { it.onUserAvatarUpdated() }
                }
            }
            launch {
                statisticsUpdater.updateFromBackend(userDetails.id)
            }
        }
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
}

interface UserLoginStatusListener {
    fun onLoggedIn()
    fun onLoggedOut()
}

interface UserAvatarListener {
    fun onUserAvatarUpdated()
}