package de.westnordost.streetcomplete.screens.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.user.login.LoginScreen
import org.koin.androidx.compose.koinViewModel

/** There are two screens on the user screen: The login and the user screen. Which one is displayed
 *  depends on whether the user is logged in or not. */
@Composable
fun UserNavHost(
    launchAuth: Boolean,
    onClickBack: () -> Unit
) {
    val navController = rememberNavController()
    val viewModel = koinViewModel<UserViewModel>()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (!isLoggedIn) UserDestination.Login else UserDestination.User
    ) {
        composable(UserDestination.Login) {
            LoginScreen(
                viewModel = koinViewModel(),
                launchAuth = launchAuth,
                onClickBack = onClickBack
            )
        }
        composable(UserDestination.User) {
            UserScreen(
                onClickBack = onClickBack
            )
        }
    }
}

private object UserDestination {
    const val Login = "login"
    const val User = "user"
}
