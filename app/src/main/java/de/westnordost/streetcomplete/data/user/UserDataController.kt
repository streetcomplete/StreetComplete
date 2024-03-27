package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.user.UserDetails
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.data.preferences.Preferences

/** Controller that handles user login, logout, auth and updated data */
class UserDataController(
    private val prefs: Preferences,
    private val userLoginStatusSource: UserLoginStatusSource
) : UserDataSource {

    private val userLoginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() {}
        override fun onLoggedOut() {
            clear()
        }
    }

    private val listeners = Listeners<UserDataSource.Listener>()

    override val userId: Long get() = prefs.userId
    override val userName: String? get() = prefs.userName

    override var unreadMessagesCount: Int
        get() = prefs.userUnreadMessages
        set(value) {
            prefs.userUnreadMessages = value
            listeners.forEach { it.onUpdated() }
        }

    init {
        userLoginStatusSource.addListener(userLoginStatusListener)
    }

    fun setDetails(userDetails: UserDetails) {
        prefs.userId = userDetails.id
        prefs.userName = userDetails.displayName
        prefs.userUnreadMessages = userDetails.unreadMessagesCount
        listeners.forEach { it.onUpdated() }
    }

    private fun clear() {
        prefs.clearUserData()
        listeners.forEach { it.onUpdated() }
    }

    override fun addListener(listener: UserDataSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserDataSource.Listener) {
        listeners.remove(listener)
    }
}
