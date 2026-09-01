package de.westnordost.streetcomplete

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.data.quest.AutoSyncer
import de.westnordost.streetcomplete.screens.about.AboutNavHost
import de.westnordost.streetcomplete.screens.main.MainBottomSheetViewModel
import de.westnordost.streetcomplete.screens.main.MapAppLauncher
import de.westnordost.streetcomplete.screens.main.MainScreen
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.rememberMapAppLauncher
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsDestination
import de.westnordost.streetcomplete.screens.settings.SettingsNavHost
import de.westnordost.streetcomplete.screens.user.UserNavHost
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.location.LocationProvider

/** The shared top-level application flow used by every platform entry point. */
@Composable
fun StreetCompleteApp(
    modifier: Modifier = Modifier,
    startDestination: AppDestination = AppDestination.Main,
    navigationRequest: AppDestination? = null,
    onNavigationRequestHandled: () -> Unit = {},
    mainViewModel: MainViewModel = koinViewModel(),
    editHistoryViewModel: EditHistoryViewModel = koinViewModel(),
    mainBottomSheetViewModel: MainBottomSheetViewModel = koinViewModel(),
    autoSyncer: AutoSyncer = koinInject(),
    locationProvider: LocationProvider = koinInject(),
    mapAppLauncher: MapAppLauncher = rememberMapAppLauncher(),
    onMainShown: () -> Unit = {},
) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, autoSyncer) {
        lifecycleOwner.lifecycle.addObserver(autoSyncer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(autoSyncer) }
    }

    LaunchedEffect(navigationRequest) {
        val destination = navigationRequest ?: return@LaunchedEffect
        navController.navigate(destination.route) {
            popUpTo(AppDestination.Main.route)
            launchSingleTop = true
        }
        onNavigationRequestHandled()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(AppDestination.Main.route) {
            LaunchedEffect(Unit) { onMainShown() }
            MainScreen(
                viewModel = mainViewModel,
                editHistoryViewModel = editHistoryViewModel,
                mainBottomSheetViewModel = mainBottomSheetViewModel,
                locationProvider = locationProvider,
                mapAppLauncher = mapAppLauncher,
                onClickSettings = { navController.navigate(AppDestination.Settings.route) },
                onClickQuestSettings = {
                    navController.navigate(AppDestination.QuestSettings.route)
                },
                onClickAbout = { navController.navigate(AppDestination.About.route) },
                onClickProfile = { navController.navigate(AppDestination.Profile.route) },
                onClickLogin = { navController.navigate(AppDestination.Login.route) },
            )
        }
        composable(AppDestination.Settings.route) {
            SettingsNavHost(onClickBack = navController::returnToMain)
        }
        composable(AppDestination.QuestSettings.route) {
            SettingsNavHost(
                onClickBack = navController::returnToMain,
                startDestination = SettingsDestination.QuestSelection,
            )
        }
        composable(AppDestination.About.route) {
            AboutNavHost(
                onClickBack = navController::returnToMain,
                locationProvider = locationProvider,
            )
        }
        composable(AppDestination.Profile.route) {
            UserNavHost(
                launchAuth = false,
                onClickBack = navController::returnToMain,
            )
        }
        composable(AppDestination.Login.route) {
            UserNavHost(
                launchAuth = true,
                onClickBack = navController::returnToMain,
            )
        }
    }
}

private fun NavHostController.returnToMain() {
    if (!popBackStack()) navigate(AppDestination.Main.route) { launchSingleTop = true }
}

enum class AppDestination(val route: String) {
    Main("main"),
    Settings("settings"),
    QuestSettings("quest_settings"),
    About("about"),
    Profile("profile"),
    Login("login"),
}
