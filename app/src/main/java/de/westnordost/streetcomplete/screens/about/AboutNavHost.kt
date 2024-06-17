package de.westnordost.streetcomplete.screens.about

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable fun AboutNavHost(onClickBack: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destination.About,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it } ) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it } ) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it } ) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it } ) }
    ) {
        composable(Destination.About) {
            AboutScreen(
                onClickChangelog = { navController.navigate(Destination.Changelog, ) },
                onClickCredits = { navController.navigate(Destination.Credits) },
                onClickPrivacyStatement = { navController.navigate(Destination.PrivacyStatement) },
                onClickLogs = { navController.navigate(Destination.Logs) },
                onClickBack = onClickBack
            )
        }
        composable(Destination.Changelog) {
            ChangelogScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(Destination.Credits) {
            CreditsScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(Destination.PrivacyStatement) {
            PrivacyStatementScreen(
                vectorTileProvider = koinInject(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(Destination.Logs) {
            LogsScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
    }
}

private object Destination {
    const val About = "about"
    const val Credits = "credits"
    const val Changelog = "changelog"
    const val PrivacyStatement = "privacy_statement"
    const val Logs = "logs"
}
