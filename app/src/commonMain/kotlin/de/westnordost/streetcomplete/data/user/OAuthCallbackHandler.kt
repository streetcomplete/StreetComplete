package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.data.user.oauth.OAuthAuthorizationParams
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/**
 * When the user completes the OAuth flow and returns to the app via the callback URI,
 * this handler processes the authorization response.
 */
class OAuthCallbackHandler {
    private val _oAuthCallbackUri = MutableStateFlow<String?>(null)
    val oAuthCallbackUri: StateFlow<String?> = _oAuthCallbackUri

    // Store the OAuth params so the same codeVerifier can be used during token exchange
    private var storedOAuthParams: OAuthAuthorizationParams? = null

    fun storeOAuthParams(params: OAuthAuthorizationParams) {
        storedOAuthParams = params
    }

    fun getStoredOAuthParams(): OAuthAuthorizationParams? = storedOAuthParams

    // Process a potential OAuth callback URI
    fun handleUri(uriString: String): Boolean {
        return if (isOAuthCallback(uriString)) {
            _oAuthCallbackUri.update { uriString }
            true
        } else {
            false
        }
    }

    // checks if the uri is oauth callback
    private fun isOAuthCallback(uriString: String): Boolean {
        return uriString.startsWith("$OAUTH2_CALLBACK_SCHEME://$OAUTH2_CALLBACK_HOST")
    }

    fun consumeCallback(): String? {
        return _oAuthCallbackUri.getAndUpdate { null }
    }

    fun clearStoredParams() {
        storedOAuthParams = null
    }

    companion object {
        private const val TAG = "OAuthCallbackHandler"
    }
}

