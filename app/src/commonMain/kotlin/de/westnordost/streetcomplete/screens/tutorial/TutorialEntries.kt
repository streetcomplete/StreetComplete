package de.westnordost.streetcomplete.screens.tutorial

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.fullScreenDialogTransitions
import de.westnordost.streetcomplete.screens.goBack
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
}
