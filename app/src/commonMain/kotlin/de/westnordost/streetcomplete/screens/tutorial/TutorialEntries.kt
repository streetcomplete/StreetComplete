package de.westnordost.streetcomplete.screens.tutorial

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.fullScreenDialogTransitions
import de.westnordost.streetcomplete.screens.goBack

fun EntryProviderScope<Route>.tutorialEntries(
    backStack: NavBackStack<Route>,
    onIntroFinished: () -> Unit,
    onOverlaysFinished: () -> Unit,
) {
    fun goBack() { backStack.goBack() }

    entry<Route.IntroTutorial>(metadata = fullScreenDialogTransitions) { route ->
        IntroTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = onIntroFinished,
            // Only automatic onboarding requires completion; help opened from About is dismissible.
            dismissOnBackPress = !route.onboarding,
        )
    }
    entry<Route.OverlaysTutorial>(metadata = fullScreenDialogTransitions) {
        OverlaysTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = onOverlaysFinished,
        )
    }
}
