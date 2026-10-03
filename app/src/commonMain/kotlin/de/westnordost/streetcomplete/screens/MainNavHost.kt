package de.westnordost.streetcomplete.screens

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.navigation3.ui.NavDisplay
import de.westnordost.streetcomplete.screens.about.aboutEntries
import de.westnordost.streetcomplete.screens.main.MainScreen
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.map.MainMapTrackState
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeViewModel
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.settings.settingsEntries
import de.westnordost.streetcomplete.screens.tutorial.tutorialEntries
import de.westnordost.streetcomplete.screens.user.userEntry
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainNavHost(
    uri: String?,
    onConsumedUri: () -> Unit,
) {
    // The main screen is always at the bottom of the back stack, so its view model lives as long
    // as the app and can receive requests while another destination is shown.
    val mainViewModel = koinViewModel<MainViewModel>()
    val navViewModel = koinViewModel<MainNavViewModel>()
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(Route.serializer())) {
        if (mainViewModel.shouldShowIntroTutorial) {
            NavBackStack(Route.Main, Route.IntroTutorial(onboarding = true))
        } else {
            NavBackStack<Route>(Route.Main)
        }
    }
    // Navigation keeps the full tracks; restoring the app uses the bounded saved copy.
    val tracks = rememberSaveable(saver = MainMapTrackState.Saver) { MainMapTrackState() }
    val viewModelStoreProvider = rememberViewModelStoreProvider()
    val dir = LocalLayoutDirection.current.dir

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberParentViewModelStoreNavEntryDecorator(viewModelStoreProvider),
            rememberViewModelStoreNavEntryDecorator(viewModelStoreProvider),
            // Each screen needs its own opaque background. Otherwise, the map below can punch
            // through it while screens slide over it (seen on Android 7)
            remember { NavEntryDecorator<Route> { entry -> Surface { entry.Content() } } },
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { +it * dir }) togetherWith
            slideOutHorizontally(targetOffsetX = { -it * dir })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it * dir }) togetherWith
            slideOutHorizontally(targetOffsetX = { +it * dir })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it * dir }) togetherWith
            slideOutHorizontally(targetOffsetX = { +it * dir })
        },
        entryProvider = entryProvider {
            entry<Route.Main> {
                MainScreen(
                    tracks = tracks,
                    onClickSettings = { backStack.add(Route.Settings) },
                    onClickQuestSettings = { backStack.add(Route.QuestSelection) },
                    onClickAbout = { backStack.add(Route.About) },
                    onClickProfile = { backStack.add(Route.User()) },
                    onClickLogin = { backStack.add(Route.User(launchAuth = true)) },
                    onClickEnterTeamMode = { backStack.add(Route.TeamModeWizard) },
                    onShowOverlaysTutorial = { backStack.add(Route.OverlaysTutorial) },
                    navViewModel = navViewModel,
                    viewModel = mainViewModel,
                )
            }
            entry<Route.TeamModeWizard>(metadata = fullScreenDialogTransitions) {
                val viewModel = koinViewModel<TeamModeViewModel>()
                TeamModeWizard(
                    onDismissRequest = { backStack.goBack() },
                    onFinished = { teamSize, indexInTeam ->
                        viewModel.enableTeamMode(teamSize = teamSize, indexInTeam = indexInTeam)
                    },
                    allQuestIcons = viewModel.allQuestIcons,
                )
            }
            tutorialEntries(backStack)
            settingsEntries(backStack)
            aboutEntries(backStack)
            userEntry(onClickBack = { backStack.goBack() })
        },
    )

    LaunchedEffect(uri) {
        uri?.let {
            while (backStack.size > 1) backStack.goBack()
            navViewModel.setUri(it)
            onConsumedUri()
        }
    }
}

/** For screens that appear on top of the current one like a full-screen dialog: The screen below
 *  stays put while the dialog appears on top of it */
val fullScreenDialogTransitions = metadata {
    put(NavDisplay.TransitionKey) {
        fadeIn() + slideInVertically(initialOffsetY = { it / 10 }) togetherWith
        ExitTransition.KeepUntilTransitionsFinished
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith
        fadeOut() + slideOutVertically(targetOffsetY = { it / 10 })
    }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith
        fadeOut() + slideOutVertically(targetOffsetY = { it / 10 })
    }
}
