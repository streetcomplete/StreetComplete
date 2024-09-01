package de.westnordost.streetcomplete.screens.user.login

import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.UnsyncedChangesCountSource
import de.westnordost.streetcomplete.data.user.OAUTH2_AUTHORIZATION_URL
import de.westnordost.streetcomplete.data.user.OAUTH2_CLIENT_ID
import de.westnordost.streetcomplete.data.user.OAUTH2_REDIRECT_URI
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUESTED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_REQUIRED_SCOPES
import de.westnordost.streetcomplete.data.user.OAUTH2_TOKEN_URL
import de.westnordost.streetcomplete.data.user.UserLoginController
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.user.oauth.OAuthApiClient
import de.westnordost.streetcomplete.data.user.oauth.OAuthAuthorizationParams
import de.westnordost.streetcomplete.data.user.oauth.OAuthException
import de.westnordost.streetcomplete.util.ktx.launch
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

abstract class LoginViewModel : ViewModel() {
    abstract val unsyncedChangesCount: StateFlow<Int>

    abstract val loginState: StateFlow<LoginState>

    abstract val authorizationRequestUrl: String

    /** Starts the OAuth2 based login flow. */
    abstract fun startLogin()

    /** Call when the web view / browser received an error when loading the (authorization) page */
    abstract fun failAuthorization(url: String, errorCode: Int, description: String?)

    /** Returns whether the url is a redirect url destined for this OAuth authorization flow */
    abstract fun isAuthorizationResponseUrl(url: String): Boolean

    /** Continues OAuth authorization flow with given redirect url */
    abstract fun finishAuthorization(authorizationResponseUrl: String)

    /** Resets the login state to LoggedOut. Only works if current state is LoginError */
    abstract fun resetLogin()
}

sealed interface LoginState
data object LoggedOut : LoginState
data object RequestingAuthorization : LoginState
data object RetrievingAccessToken : LoginState
enum class LoginError : LoginState {
    RequiredPermissionsNotGranted,
    CommunicationError
}
data object LoggedIn : LoginState

class LoginViewModelImpl(
    private val unsyncedChangesCountSource: UnsyncedChangesCountSource,
    private val userLoginController: UserLoginController,
    private val oAuthApiClient: OAuthApiClient
) : LoginViewModel() {
    override val loginState = MutableStateFlow<LoginState>(LoggedOut)
    override val unsyncedChangesCount = MutableStateFlow(0)

    override val authorizationRequestUrl: String get() = oAuth.authorizationRequestUrl

    private val oAuth = OAuthAuthorizationParams(
        OAUTH2_AUTHORIZATION_URL,
        OAUTH2_TOKEN_URL,
        OAUTH2_CLIENT_ID,
        OAUTH2_REQUESTED_SCOPES,
        OAUTH2_REDIRECT_URI
    )

    private val loginStatusListener = object : UserLoginSource.Listener {
        override fun onLoggedIn() { loginState.value = LoggedIn }
        override fun onLoggedOut() { loginState.value = LoggedOut }
    }

    init {
        launch(Dispatchers.IO) {
            unsyncedChangesCount.update { unsyncedChangesCountSource.getCount() }
        }
        userLoginController.addListener(loginStatusListener)
    }

    override fun onCleared() {
        userLoginController.removeListener(loginStatusListener)
    }

    override fun startLogin() {
        loginState.compareAndSet(LoggedOut, RequestingAuthorization)
    }

    override fun failAuthorization(url: String, errorCode: Int, description: String?) {
        Log.e(TAG, "Error for URL " + url + if (description != null) ": $description" else "")
        loginState.compareAndSet(RequestingAuthorization, LoginError.CommunicationError)
    }

    override fun isAuthorizationResponseUrl(url: String): Boolean =
        oAuth.itsForMe(url)

    override fun finishAuthorization(authorizationResponseUrl: String) {
        launch {
            val accessToken = retrieveAccessToken(authorizationResponseUrl)
            if (accessToken != null) {
                login(accessToken)
            }
        }
    }

    private suspend fun retrieveAccessToken(authorizationResponseUrl: String): String? {
        try {
            loginState.value = RetrievingAccessToken
            val accessTokenResponse = oAuthApiClient.getAccessToken(oAuth, authorizationResponseUrl)
            if (accessTokenResponse.grantedScopes?.containsAll(OAUTH2_REQUIRED_SCOPES) == false) {
                loginState.value = LoginError.RequiredPermissionsNotGranted
                return null
            }
            return accessTokenResponse.accessToken
        } catch (e: Exception) {
            if (e is OAuthException && e.error == "access_denied") {
                loginState.value = LoginError.RequiredPermissionsNotGranted
            } else {
                Log.e(TAG, "Error during authorization", e)
                loginState.value = LoginError.CommunicationError
            }
            return null
        }
    }

    private suspend fun login(accessToken: String) {
        userLoginController.logIn(accessToken)
    }

    override fun resetLogin() {
        if (loginState.value is LoginError) loginState.value = LoggedOut
    }

    companion object {
        private const val TAG = "Login"
    }
}
