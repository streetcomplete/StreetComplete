package de.westnordost.streetcomplete.screens.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.goBack
import de.westnordost.streetcomplete.screens.user.login.LoggedIn
import de.westnordost.streetcomplete.screens.user.login.LoginScreen
import de.westnordost.streetcomplete.screens.user.login.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Login and profile share one destination, so logging out cannot leave a profile in the back stack. */
fun EntryProviderScope<Route>.userEntry(backStack: NavBackStack<Route>) {
    fun goBack() { backStack.goBack() }

    entry<Route.User> { route ->
        val loginViewModel = koinViewModel<LoginViewModel>()
        val loginState by loginViewModel.loginState.collectAsState()
        val isLoggedIn by remember { derivedStateOf { loginState == LoggedIn } }
        LaunchedEffect(Unit) {
            if (route.launchAuth && !isLoggedIn) loginViewModel.startLogin()
        }
        // let's not even Crossfade here: Data shown in the profile screen gets invalidated
        // immediately. I.e. if we fade, we'll see empty data (no name, 0 stars, etc) during the
        // fade, which doesn't look too good.
        if (isLoggedIn) {
            UserScreen(onClickBack = ::goBack)
        } else {
            LoginScreen(viewModel = loginViewModel, onClickBack = ::goBack)
        }
    }
}
