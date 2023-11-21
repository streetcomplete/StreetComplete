package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.osmapi.OsmConnection
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.Listeners

class UserLoginStatusController(
    private val osmConnection: OsmConnection,
    private val prefs: SharedPreferences,
) : UserLoginStatusSource {

    private val listeners = Listeners<UserLoginStatusSource.Listener>()

    override val isLoggedIn: Boolean get() =
        prefs.getString(Prefs.OAUTH2_ACCESS_TOKEN, null) != null

    fun logIn(accessToken: String) {
        prefs.edit {
            putString(Prefs.OAUTH2_ACCESS_TOKEN, accessToken)
        }
        osmConnection.oauthAccessToken = accessToken
        listeners.forEach { it.onLoggedIn() }
    }

    fun logOut() {
        prefs.edit {
            putString(Prefs.OAUTH2_ACCESS_TOKEN, null)
            // make sure OAuth 1.0a data is cleared
            remove(Prefs.OSM_LOGGED_IN_AFTER_OAUTH_FUCKUP)
            remove(Prefs.OAUTH1_ACCESS_TOKEN)
            remove(Prefs.OAUTH1_ACCESS_TOKEN_SECRET)
        }
        osmConnection.oauthAccessToken = null
        // TODO revoke token?

        listeners.forEach { it.onLoggedOut() }
    }

    override fun addListener(listener: UserLoginStatusSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: UserLoginStatusSource.Listener) {
        listeners.remove(listener)
    }
}
