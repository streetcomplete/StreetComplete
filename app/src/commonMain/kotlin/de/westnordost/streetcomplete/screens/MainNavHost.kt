package de.westnordost.streetcomplete.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.about.AboutDestination
import de.westnordost.streetcomplete.screens.about.aboutGraph
import de.westnordost.streetcomplete.screens.main.MainScreen
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.map.MainMapTrackState
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.settings.SettingsDestination
import de.westnordost.streetcomplete.screens.settings.settingsGraph
import de.westnordost.streetcomplete.screens.tutorial.TutorialDestination
import de.westnordost.streetcomplete.screens.tutorial.tutorialScreens
import de.westnordost.streetcomplete.screens.user.UserDestination
import de.westnordost.streetcomplete.screens.user.userScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavHost(
    uri: String?,
    onConsumedUri: () -> Unit,
    openSettings: Boolean,
    onConsumedSettingsRequest: () -> Unit,
) {
    val navController = rememberNavController()
    // Navigation keeps the full tracks; restoring the app uses the bounded saved copy.
    val tracks = rememberSaveable(saver = MainMapTrackState.Saver) { MainMapTrackState() }
    // The main screen is always at the bottom of the back stack, so its view model lives as long
    // as the app and can receive requests while another destination is shown.
    val mainViewModel = koinViewModel<MainViewModel>()
    val dir = LocalLayoutDirection.current.dir

    NavHost(
        navController = navController,
        startDestination = MainDestination.Main,
        enterTransition = {
            if (targetState.isFullScreenDialog) fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
            else slideInHorizontally(initialOffsetX = { +it * dir })
        },
        exitTransition = {
            // the screen below stays put while a dialog appears on top of it
            if (targetState.isFullScreenDialog) ExitTransition.KeepUntilTransitionsFinished
            else slideOutHorizontally(targetOffsetX = { -it * dir })
        },
        popEnterTransition = {
            if (initialState.isFullScreenDialog) EnterTransition.None
            else slideInHorizontally(initialOffsetX = { -it * dir })
        },
        popExitTransition = {
            if (initialState.isFullScreenDialog) fadeOut() + slideOutVertically(targetOffsetY = { it / 10 })
            else slideOutHorizontally(targetOffsetX = { +it * dir })
        },
    ) {
        composable(MainDestination.Main) {
            MainScreen(
                tracks = tracks,
                onClickSettings = { navController.navigate(SettingsDestination.Settings) },
                onClickQuestSettings = { navController.navigate(SettingsDestination.QuestSelection) },
                onClickAbout = { navController.navigate(AboutDestination.About) },
                onClickProfile = { navController.navigate(UserDestination.user()) },
                onClickLogin = { navController.navigate(UserDestination.user(launchAuth = true)) },
                onClickEnterTeamMode = { navController.navigate(MainDestination.TeamModeWizard) },
                onShowIntroTutorial = { navController.navigate(TutorialDestination.intro(onboarding = true)) },
                onShowOverlaysTutorial = { navController.navigate(TutorialDestination.Overlays) },
                viewModel = mainViewModel,
            )
        }
        composable(MainDestination.TeamModeWizard) {
            val questIcons = remember { mainViewModel.allQuestTypes.map { it.icon } }
            TeamModeWizard(
                onDismissRequest = { navController.popBackStack() },
                onFinished = { teamSize, indexInTeam ->
                    mainViewModel.enableTeamMode(teamSize = teamSize, indexInTeam = indexInTeam)
                },
                allQuestIcons = questIcons,
            )
        }
        tutorialScreens(
            navController = navController,
            onIntroFinished = { mainViewModel.hasShownTutorial = true },
            onOverlaysFinished = { mainViewModel.hasShownOverlaysTutorial = true },
        )
        settingsGraph(navController)
        aboutGraph(navController)
        userScreen(onClickBack = { navController.popBackStack() })
    }

    LaunchedEffect(uri, openSettings) {
        uri?.let {
            navController.popBackStack(MainDestination.Main, inclusive = false)
            mainViewModel.setUri(it)
            onConsumedUri()
        }
        if (openSettings) {
            navController.navigate(SettingsDestination.Settings) {
                popUpTo(MainDestination.Main)
                launchSingleTop = true
            }
            onConsumedSettingsRequest()
        }
    }
}

object MainDestination {
    const val Main = "main"
    const val TeamModeWizard = "team_mode_wizard"
}

/** Screens that appear on top of the current one like a full-screen dialog */
private val NavBackStackEntry.isFullScreenDialog: Boolean get() = destination.route in setOf(
    MainDestination.TeamModeWizard,
    TutorialDestination.IntroRoute,
    TutorialDestination.Overlays,
)
