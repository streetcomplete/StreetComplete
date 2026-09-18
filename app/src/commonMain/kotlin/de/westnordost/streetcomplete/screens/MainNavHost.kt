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
import de.westnordost.streetcomplete.screens.user.userScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavHost(appViewModel: AppViewModel) {
    val navController = rememberNavController()
    val dir = LocalLayoutDirection.current.dir
    val uri by appViewModel.pendingUri.collectAsState()
    val openSettings by appViewModel.openSettings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }) },
    ) {
        composable("main") {
            val viewModel = koinViewModel<MainViewModel>()
            LaunchedEffect(uri) {
                uri?.let {
                    viewModel.setUri(it)
                    appViewModel.consumeUri()
                }
            }
            MainScreen(
                onClickSettings = { navController.navigate(SettingsDestination.Settings) },
                onClickQuestSettings = { navController.navigate(SettingsDestination.QuestSelection) },
                onClickAbout = { navController.navigate(AboutDestination.About) },
                onClickProfile = { navController.navigate("user") },
                onClickLogin = { navController.navigate("user?launchAuth=true") },
                viewModel = viewModel,
            )
        }
        settingsGraph(navController)
        aboutGraph(navController)
        userScreen(onClickBack = { navController.popBackStack() })
    }

    LaunchedEffect(uri, openSettings) {
        if (uri != null) navController.popBackStack("main", inclusive = false)
        if (openSettings) {
            navController.navigate(SettingsDestination.Settings) {
                popUpTo("main")
                launchSingleTop = true
            }
            appViewModel.consumeSettingsRequest()
        }
    }
}
