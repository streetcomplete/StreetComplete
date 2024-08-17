package de.westnordost.streetcomplete.screens.user.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.request.RequestInterceptor
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.screens.user.login.LoginError.CommunicationError
import de.westnordost.streetcomplete.screens.user.login.LoginError.RequiredPermissionsNotGranted
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.ktx.toast
import java.util.Locale

/** Leads user through the OAuth 2 auth flow to login */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val state by viewModel.loginState.collectAsState()
    val unsyncedChangesCount by viewModel.unsyncedChangesCount.collectAsState()

    // handle error state: just show message once and return to login state
    val context = LocalContext.current
    LaunchedEffect(state) {
        val errorState = state as? LoginError
        if (errorState != null) {
            context.toast(when (errorState) {
                RequiredPermissionsNotGranted -> R.string.oauth_failed_permissions
                CommunicationError -> R.string.oauth_communication_error
            }, Toast.LENGTH_LONG)
            viewModel.resetLogin()
        }
    }

    if (state is LoggedOut) {
        LoginButtonWithText(
            unsyncedChangesCount = unsyncedChangesCount,
            onClickLogin = { viewModel.startLogin() },
            modifier = Modifier.fillMaxSize()
        )
    } else if (state is RequestingAuthorization) {
        val webViewState = rememberWebViewState(
            url = viewModel.authorizationRequestUrl,
            additionalHttpHeaders = mapOf(
                "Accept-Language" to Locale.getDefault().toLanguageTag()
            )
        )

        val webViewNavigator = rememberWebViewNavigator(
            // handle authorization url response
            requestInterceptor = object : RequestInterceptor {
                override fun onInterceptUrlRequest(
                    request: WebRequest,
                    navigator: WebViewNavigator
                ): WebRequestInterceptResult {
                    if (viewModel.isAuthorizationResponseUrl(request.url)) {
                        viewModel.finishAuthorization(request.url)
                        return WebRequestInterceptResult.Reject
                    }
                    return WebRequestInterceptResult.Allow
                }
            }
        )

        // handle error response
        LaunchedEffect(webViewState.errorsForCurrentRequest) {
            val error = webViewState.errorsForCurrentRequest.firstOrNull()
            if (error != null) {
                viewModel.failAuthorization(
                    url = webViewState.lastLoadedUrl.toString(),
                    errorCode = error.code,
                    description = error.description
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            if (webViewState.loadingState is LoadingState.Loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                captureBackPresses = true,
                navigator = webViewNavigator,
                onCreated = {
                    val settings = webViewState.webSettings
                    settings.isJavaScriptEnabled = true
                    settings.customUserAgentString = ApplicationConstants.USER_AGENT
                    settings.supportZoom = false
                } as () -> Unit,
            )
        }
    } else if (state is RetrievingAccessToken || state is LoggedIn) {
        Box(Modifier.fillMaxSize()) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LoginButtonWithText(
    unsyncedChangesCount: Int,
    onClickLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        if (unsyncedChangesCount > 0) {
            Text(
                text = stringResource(
                    R.string.unsynced_quests_not_logged_in_description,
                    unsyncedChangesCount
                ),
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Button(onClick = onClickLogin) {
            Text(stringResource(R.string.user_login))
        }
    }
}
