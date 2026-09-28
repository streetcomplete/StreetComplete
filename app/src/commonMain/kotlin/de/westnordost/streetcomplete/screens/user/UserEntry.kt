package de.westnordost.streetcomplete.screens.user

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.user.login.LoginScreen
import de.westnordost.streetcomplete.screens.user.login.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Login and profile share one destination, so logging out cannot leave a profile in the back stack. */
fun EntryProviderScope<Route>.userEntry(onClickBack: () -> Unit) {
    entry<Route.User> { route ->
        val viewModel = koinViewModel<UserViewModel>()
        val loginViewModel = koinViewModel<LoginViewModel>()
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
        LaunchedEffect(Unit) {
            if (route.launchAuth && !isLoggedIn) loginViewModel.startLogin()
        }
        Crossfade(targetState = isLoggedIn, modifier = Modifier.fillMaxSize()) { loggedIn ->
            if (loggedIn) {
                UserScreen(onClickBack = onClickBack)
            } else {
                LoginScreen(viewModel = loginViewModel, onClickBack = onClickBack)
            }
        }
    }
}
