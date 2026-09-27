package de.westnordost.streetcomplete.screens.tutorial

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.savedstate.read

fun NavGraphBuilder.tutorialScreens(
    navController: NavHostController,
    onIntroFinished: () -> Unit,
    onOverlaysFinished: () -> Unit,
) {
    fun goBack() { navController.popBackStack() }

    composable(
        route = TutorialDestination.IntroRoute,
        arguments = listOf(navArgument(TutorialDestination.Onboarding) {
            type = NavType.BoolType
            defaultValue = false
        }),
    ) {
        val onboarding = it.arguments?.read { getBoolean(TutorialDestination.Onboarding) } == true
        IntroTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = onIntroFinished,
            // Only automatic onboarding requires completion; help opened from About is dismissible.
            dismissOnBackPress = !onboarding,
        )
    }
    composable(TutorialDestination.Overlays) {
        OverlaysTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = onOverlaysFinished,
        )
    }
}

object TutorialDestination {
    const val Intro = "intro_tutorial"
    const val Onboarding = "onboarding"
    const val IntroRoute = "$Intro?$Onboarding={$Onboarding}"
    const val Overlays = "overlays_tutorial"

    fun intro(onboarding: Boolean = false) = "$Intro?$Onboarding=$onboarding"
}
