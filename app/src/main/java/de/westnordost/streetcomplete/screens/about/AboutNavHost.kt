package de.westnordost.streetcomplete.screens.about

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable fun AboutNavHost(onClickBack: () -> Unit) {
    val navController = rememberNavController()
    val dir = LocalLayoutDirection.current.dir

    NavHost(
        navController = navController,
        startDestination = AboutDestination.About,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }) }
    ) {
        composable(AboutDestination.About) {
            AboutScreen(
                onClickChangelog = { navController.navigate(AboutDestination.Changelog) },
                onClickCredits = { navController.navigate(AboutDestination.Credits) },
                onClickPrivacyStatement = { navController.navigate(AboutDestination.PrivacyStatement) },
                onClickLogs = { navController.navigate(AboutDestination.Logs) },
                onClickBack = onClickBack
            )
        }
        composable(AboutDestination.Changelog) {
            ChangelogScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(AboutDestination.Credits) {
            CreditsScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(AboutDestination.PrivacyStatement) {
            PrivacyStatementScreen(
                vectorTileProvider = koinInject(),
                onClickBack = { navController.popBackStack() }
            )
        }
        composable(AboutDestination.Logs) {
            LogsScreen(
                viewModel = koinViewModel(),
                onClickBack = { navController.popBackStack() }
            )
        }
    }
}

object AboutDestination {
    const val About = "about"
    const val Credits = "credits"
    const val Changelog = "changelog"
    const val PrivacyStatement = "privacy_statement"
    const val Logs = "logs"
}
