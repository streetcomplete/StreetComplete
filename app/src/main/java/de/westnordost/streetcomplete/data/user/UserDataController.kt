package de.westnordost.streetcomplete.data.user

import com.russhwolf.settings.ObservableSettings
import de.westnordost.osmapi.user.UserDetails
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.Listeners

/** Controller that handles user login, logout, auth and updated data */
class UserDataController(
    private val prefs: ObservableSettings,
    private val userLoginStatusSource: UserLoginStatusSource
) : UserDataSource {

    private val userLoginStatusListener = object : UserLoginStatusSource.Listener {
        override fun onLoggedIn() {}
        override fun onLoggedOut() {
            clear()
        }
    }

    private val listeners = Listeners<UserDataSource.Listener>()

    override val userId: Long get() = prefs.getLong(Prefs.OSM_USER_ID, -1)
    override val userName: String? get() = prefs.getStringOrNull(Prefs.OSM_USER_NAME)

    override var unreadMessagesCount: Int
        get() = prefs.getInt(Prefs.OSM_UNREAD_MESSAGES, 0)
        set(value) {
            prefs.putInt(Prefs.OSM_UNREAD_MESSAGES, value)
            listeners.forEach { it.onUpdated() }
        }

    init {
        userLoginStatusSource.addListener(userLoginStatusListener)
    }

    fun setDetails(userDetails: UserDetails) {
        prefs.putLong(Prefs.OSM_USER_ID, userDetails.id)
        prefs.putString(Prefs.OSM_USER_NAME, userDetails.displayName)
        prefs.putInt(Prefs.OSM_UNREAD_MESSAGES, userDetails.unreadMessagesCount)
        listeners.forEach { it.onUpdated() }
    }

    private fun clear() {
        prefs.remove(Prefs.OSM_USER_ID)
        prefs.remove(Prefs.OSM_USER_NAME)
        prefs.remove(Prefs.OSM_UNREAD_MESSAGES)
        listeners.forEach { it.onUpdated() }
    }

    override fun addListener(listener: UserDataSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserDataSource.Listener) {
        listeners.remove(listener)
    }
}
