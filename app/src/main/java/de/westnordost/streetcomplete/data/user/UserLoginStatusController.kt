package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.data.preferences.Preferences

class UserLoginStatusController(
    private val osmConnection: OsmConnection,
    private val prefs: Preferences,
) : UserLoginStatusSource {

    private val listeners = Listeners<UserLoginStatusSource.Listener>()

    override val isLoggedIn: Boolean get() = prefs.oAuth2AccessToken != null

    fun logIn(accessToken: String) {
        prefs.oAuth2AccessToken = accessToken
        osmConnection.oAuthAccessToken = accessToken
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        prefs.oAuth2AccessToken = null
        prefs.removeOAuth1Data()
        osmConnection.oAuthAccessToken = null
        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginStatusSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginStatusSource.Listener) {
        listeners.remove(listener)
    }
}
