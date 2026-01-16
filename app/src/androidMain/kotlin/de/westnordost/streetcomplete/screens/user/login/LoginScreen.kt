package de.westnordost.streetcomplete.screens.user.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.unsynced_quests_not_logged_in_description
import de.westnordost.streetcomplete.resources.user_login
import de.westnordost.streetcomplete.screens.user.login.LoginError.CommunicationError
import de.westnordost.streetcomplete.screens.user.login.LoginError.RequiredPermissionsNotGranted
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.theme.titleLarge
import de.westnordost.streetcomplete.util.ktx.toast
import org.jetbrains.compose.resources.stringResource

// Import OAUTH2_CALLBACK_SCHEME
import de.westnordost.streetcomplete.data.user.OAUTH2_CALLBACK_SCHEME

/** Leads user through the OAuth 2 auth flow to login using Chrome Custom Tabs */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    launchAuth: Boolean,
    onClickBack: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()
    val unsyncedChangesCount by viewModel.unsyncedChangesCount.collectAsState()

    LaunchedEffect(launchAuth) {
        if (launchAuth) viewModel.startLogin()
    }

    // handle error state: just show message once and return to login state
    val context = LocalContext.current
    LaunchedEffect(state) {
        val errorState = state as? LoginError
        if (errorState != null) {
            val errorMessage = when (errorState) {
                RequiredPermissionsNotGranted -> R.string.oauth_failed_permissions
                CommunicationError -> R.string.oauth_communication_error
            }
            context.toast(errorMessage, Toast.LENGTH_LONG)
            viewModel.resetLogin()
        }
    }

    // Launch Custom Tab for OAuth when requesting authorization
    LaunchedEffect(state) {
        if (state is RequestingAuthorization) {
            val authUrl = viewModel.authorizationRequestUrl
            // Launch OAuth flow in Chrome Custom Tab
            ChromeCustomTabLauncher.launchOAuthFlow(context, authUrl, OAUTH2_CALLBACK_SCHEME)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(Res.string.user_login)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )

        if (state is LoggedOut) {
            LoginButtonWithText(
                unsyncedChangesCount = unsyncedChangesCount,
                onClickLogin = { viewModel.startLogin() },
                modifier = Modifier.fillMaxSize()
            )
        } else if (state is RequestingAuthorization) {
            // Show the loading state while Custom Tab is handling authorization
            Box(Modifier.fillMaxSize()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        } else if (state is RetrievingAccessToken || state is LoggedIn) {
            Box(Modifier.fillMaxSize()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
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
                    Res.string.unsynced_quests_not_logged_in_description,
                    unsyncedChangesCount
                ),
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Button(onClick = onClickLogin) {
            Text(stringResource(Res.string.user_login))
        }
    }
}
