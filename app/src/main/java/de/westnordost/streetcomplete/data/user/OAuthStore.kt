package de.westnordost.streetcomplete.data.user

import android.content.SharedPreferences
import androidx.core.content.edit
import de.westnordost.streetcomplete.Prefs
import oauth.signpost.OAuthConsumer
import javax.inject.Inject
import javax.inject.Provider

/** Manages saving and loading OAuthConsumer persistently  */
class OAuthStore @Inject constructor(
    private val prefs: SharedPreferences,
    private val oAuthConsumerProvider: Provider<OAuthConsumer>
) {
    var oAuthConsumer: OAuthConsumer?
        get() {
            val result = oAuthConsumerProvider.get()
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
