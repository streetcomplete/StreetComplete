package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.Listeners

class UserLoginController(
    private val osmConnection: OsmConnection,
    private val prefs: Preferences,
) : UserLoginSource {

    private val listeners = Listeners<UserLoginSource.Listener>()

    override val isLoggedIn: Boolean get() = accessToken != null

    override val accessToken: String? get() =
        prefs.oAuth2AccessToken

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

    override fun addListener(listener: UserLoginSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginSource.Listener) {
        listeners.remove(listener)
    }
}
