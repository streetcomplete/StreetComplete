package de.westnordost.streetcomplete.screens.user

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read
import de.westnordost.streetcomplete.screens.user.login.LoginScreen
import de.westnordost.streetcomplete.screens.user.login.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/** Login and profile share one destination, so logging out cannot leave a profile in the back stack. */
fun NavGraphBuilder.userScreen(onClickBack: () -> Unit) {
    composable(
        route = UserDestination.User + "?${UserDestination.LaunchAuth}={${UserDestination.LaunchAuth}}",
        arguments = listOf(navArgument(UserDestination.LaunchAuth) { type = NavType.BoolType; defaultValue = false }),
    ) {
        val viewModel = koinViewModel<UserViewModel>()
        val loginViewModel = koinViewModel<LoginViewModel>()
        val isLoggedIn by viewModel.isLoggedIn.collectAsState()
        val launchAuth = it.arguments?.read { getBoolean(UserDestination.LaunchAuth) } == true
        LaunchedEffect(Unit) {
            if (launchAuth && !isLoggedIn) loginViewModel.startLogin()
        }
        if (isLoggedIn) {
            UserScreen(onClickBack = onClickBack)
        } else {
            LoginScreen(viewModel = loginViewModel, onClickBack = onClickBack)
        }
    }
}

object UserDestination {
    const val User = "user"
    const val LaunchAuth = "launchAuth"

    fun user(launchAuth: Boolean = false) = "$User?$LaunchAuth=$launchAuth"
}
