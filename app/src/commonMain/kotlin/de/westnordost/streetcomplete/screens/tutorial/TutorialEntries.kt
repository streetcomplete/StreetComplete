package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.metadata
import androidx.navigation3.ui.NavDisplay
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.goBack
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeViewModel
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.tutorialEntries(backStack: NavBackStack<Route>) {
    fun goBack() { backStack.goBack() }

    entry<Route.IntroTutorial>(metadata = fullScreenDialogTransitions) { route ->
        val viewModel = koinViewModel<IntroTutorialViewModel>()
        IntroTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = { viewModel.markShown() },
            // Only automatic onboarding requires completion; help opened from About is dismissible.
            dismissOnBackPress = !route.onboarding,
        )
    }
    entry<Route.OverlaysTutorial>(metadata = fullScreenDialogTransitions) {
        val viewModel = koinViewModel<OverlaysTutorialViewModel>()
        OverlaysTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = { viewModel.markShown() },
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
}

/** For screens that appear on top of the current one like a full-screen dialog: The screen below
 *  stays put while the dialog appears on top of it */
private val fullScreenDialogTransitions = metadata {
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
