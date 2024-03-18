package de.westnordost.streetcomplete.screens.user.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

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
