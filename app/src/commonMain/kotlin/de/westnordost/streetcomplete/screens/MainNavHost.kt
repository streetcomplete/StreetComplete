package de.westnordost.streetcomplete.screens

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.AppViewModel
import de.westnordost.streetcomplete.screens.about.AboutDestination
import de.westnordost.streetcomplete.screens.about.aboutGraph
import de.westnordost.streetcomplete.screens.main.MainScreen
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsDestination
import de.westnordost.streetcomplete.screens.settings.settingsGraph
import de.westnordost.streetcomplete.screens.user.UserDestination
import de.westnordost.streetcomplete.screens.user.userScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavHost(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    // The main screen is always at the bottom of the back stack, so its view model lives as long
    // as the app and can receive requests while another destination is shown.
    val mainViewModel = koinViewModel<MainViewModel>()
    val dir = LocalLayoutDirection.current.dir
    val uri by appViewModel.pendingUri.collectAsState()
    val openSettings by appViewModel.openSettings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = MainDestination.Main,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }) },
    ) {
        composable(MainDestination.Main) {
            MainScreen(
                onClickSettings = { navController.navigate(SettingsDestination.Settings) },
                onClickQuestSettings = { navController.navigate(SettingsDestination.QuestSelection) },
                onClickAbout = { navController.navigate(AboutDestination.About) },
                onClickProfile = { navController.navigate(UserDestination.user()) },
                onClickLogin = { navController.navigate(UserDestination.user(launchAuth = true)) },
                viewModel = mainViewModel,
            )
        }
        settingsGraph(navController)
        aboutGraph(navController)
        userScreen(onClickBack = { navController.popBackStack() })
    }

    LaunchedEffect(uri, openSettings) {
        uri?.let {
            navController.popBackStack(MainDestination.Main, inclusive = false)
            mainViewModel.setUri(it)
            appViewModel.consumeUri()
        }
        if (openSettings) {
            navController.navigate(SettingsDestination.Settings) {
                popUpTo(MainDestination.Main)
                launchSingleTop = true
            }
            appViewModel.consumeSettingsRequest()
        }
    }
}

object MainDestination {
    const val Main = "main"
}
