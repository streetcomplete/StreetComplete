package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import oauth.signpost.OAuthConsumer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Manages saving and loading OAuthConsumer persistently  */
class OAuthStore(
    private val prefs: SharedPreferences,
) : KoinComponent {
    var oAuthConsumer: OAuthConsumer?
        get() {
            val result = get<OAuthConsumer>()
            val accessToken = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN, null)
            val accessTokenSecret = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null)
            if (accessToken == null || accessTokenSecret == null) return null
            result.setTokenWithSecret(accessToken, accessTokenSecret)
            return result
        }
        set(value) {
            if (value != null && value.token != null && value.tokenSecret != null) {
                prefs.edit {
                    putString(Prefs.OAUTH_ACCESS_TOKEN, value.token)
                    putString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, value.tokenSecret)
                }
            } else {
                prefs.edit {
                    remove(Prefs.OAUTH_ACCESS_TOKEN)
                    remove(Prefs.OAUTH_ACCESS_TOKEN_SECRET)
                }
            }
        }

    val isAuthorized: Boolean
        get() = prefs.getString(Prefs.OAUTH_ACCESS_TOKEN_SECRET, null) != null
}
