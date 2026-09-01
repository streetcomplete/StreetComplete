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
import de.westnordost.streetcomplete.screens.main.MainScreen
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.settings.SettingsDestination
import de.westnordost.streetcomplete.screens.settings.SettingsNavHost
import de.westnordost.streetcomplete.screens.user.UserNavHost
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/** The shared top-level application flow used by every platform entry point. */
@Composable
fun StreetCompleteApp(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel(),
    editHistoryViewModel: EditHistoryViewModel = koinViewModel(),
    mainBottomSheetViewModel: MainBottomSheetViewModel = koinViewModel(),
    autoSyncer: AutoSyncer = koinInject(),
    onMainShown: () -> Unit = {},
) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, autoSyncer) {
        lifecycleOwner.lifecycle.addObserver(autoSyncer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(autoSyncer) }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.Main,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(AppDestination.Main) {
            LaunchedEffect(Unit) { onMainShown() }
            MainScreen(
                viewModel = mainViewModel,
                editHistoryViewModel = editHistoryViewModel,
                mainBottomSheetViewModel = mainBottomSheetViewModel,
                onClickSettings = { navController.navigate(AppDestination.Settings) },
                onClickQuestSettings = {
                    navController.navigate(AppDestination.QuestSettings)
                },
                onClickAbout = { navController.navigate(AppDestination.About) },
                onClickProfile = { navController.navigate(AppDestination.Profile) },
                onClickLogin = { navController.navigate(AppDestination.Login) },
            )
        }
        composable(AppDestination.Settings) {
            SettingsNavHost(onClickBack = navController::returnToMain)
        }
        composable(AppDestination.QuestSettings) {
            SettingsNavHost(
                onClickBack = navController::returnToMain,
                startDestination = SettingsDestination.QuestSelection,
            )
        }
        composable(AppDestination.About) {
            AboutNavHost(onClickBack = navController::returnToMain)
        }
        composable(AppDestination.Profile) {
            UserNavHost(
                launchAuth = false,
                onClickBack = navController::returnToMain,
            )
        }
        composable(AppDestination.Login) {
            UserNavHost(
                launchAuth = true,
                onClickBack = navController::returnToMain,
            )
        }
    }
}

private fun NavHostController.returnToMain() {
    if (!popBackStack()) navigate(AppDestination.Main) { launchSingleTop = true }
}

private object AppDestination {
    const val Main = "main"
    const val Settings = "settings"
    const val QuestSettings = "quest_settings"
    const val About = "about"
    const val Profile = "profile"
    const val Login = "login"
}
