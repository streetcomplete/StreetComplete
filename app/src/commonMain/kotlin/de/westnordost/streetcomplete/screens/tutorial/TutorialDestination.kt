package de.westnordost.streetcomplete.screens.tutorial

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.westnordost.streetcomplete.screens.main.MainViewModel

fun NavGraphBuilder.tutorialScreens(navController: NavHostController, viewModel: MainViewModel) {
    fun goBack() { navController.popBackStack() }

    composable(TutorialDestination.Intro) {
        IntroTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = { viewModel.hasShownTutorial = true },
            // on first start, the tutorial must be finished
            dismissOnBackPress = viewModel.hasShownTutorial,
        )
    }
    composable(TutorialDestination.Overlays) {
        OverlaysTutorialScreen(
            onDismissRequest = ::goBack,
            onFinished = { viewModel.hasShownOverlaysTutorial = true },
        )
    }
}

object TutorialDestination {
    const val Intro = "intro_tutorial"
    const val Overlays = "overlays_tutorial"
}
