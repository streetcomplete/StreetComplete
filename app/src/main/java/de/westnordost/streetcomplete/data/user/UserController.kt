package de.westnordost.streetcomplete.data.user

import android.util.Log
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.user.UserApi
import de.westnordost.streetcomplete.data.osmnotes.AvatarsDownloader
import de.westnordost.streetcomplete.data.user.statistics.StatisticsController
import de.westnordost.streetcomplete.data.user.statistics.StatisticsDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import oauth.signpost.OAuthConsumer
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Controller that handles user login, logout, auth and updated data */
@Singleton class UserController @Inject constructor(
    private val userApi: UserApi,
    private val oAuthStore: OAuthStore,
    private val userStore: UserStore,
    private val avatarsDownloader: AvatarsDownloader,
    private val statisticsDownloader: StatisticsDownloader,
    private val statisticsController: StatisticsController,
    private val osmConnection: OsmConnection
): LoginStatusSource, UserAvatarUpdateSource {
    private val loginStatusListeners: MutableList<UserLoginStatusListener> = CopyOnWriteArrayList()
    private val userAvatarListeners: MutableList<UserAvatarListener> = CopyOnWriteArrayList()

    private val scope = CoroutineScope(SupervisorJob())

    override val isLoggedIn: Boolean get() = oAuthStore.isAuthorized

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
        statisticsController.clear()
        loginStatusListeners.forEach { it.onLoggedOut() }
    }

    fun updateUser() = scope.launch(Dispatchers.IO) {
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

    private fun updateAvatar(userId: Long, imageUrl: String) = scope.launch(Dispatchers.IO) {
        avatarsDownloader.download(userId, imageUrl)
        userAvatarListeners.forEach { it.onUserAvatarUpdated() }
    }

    private fun updateStatistics(userId: Long) = scope.launch(Dispatchers.IO) {
        try {
            val statistics = statisticsDownloader.download(userId)
            statisticsController.updateAll(statistics)
        }  catch (e: Exception) {
            Log.w(TAG, "Unable to download statistics", e)
        }
    }

    override fun addLoginStatusListener(listener: UserLoginStatusListener) {
        loginStatusListeners.add(listener)
    }
    override fun removeLoginStatusListener(listener: UserLoginStatusListener) {
        loginStatusListeners.remove(listener)
    }
    override fun addUserAvatarListener(listener: UserAvatarListener) {
        userAvatarListeners.add(listener)
    }
    override fun removeUserAvatarListener(listener: UserAvatarListener) {
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

interface LoginStatusSource {
    val isLoggedIn: Boolean

    fun addLoginStatusListener(listener: UserLoginStatusListener)
    fun removeLoginStatusListener(listener: UserLoginStatusListener)
}

interface UserAvatarUpdateSource {
    fun addUserAvatarListener(listener: UserAvatarListener)
    fun removeUserAvatarListener(listener: UserAvatarListener)
}
