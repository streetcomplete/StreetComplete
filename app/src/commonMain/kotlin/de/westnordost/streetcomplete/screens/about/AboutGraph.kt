package de.westnordost.streetcomplete.screens.about

import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import de.westnordost.streetcomplete.screens.about.logs.LogsFiltersScreen
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.screens.tutorial.TutorialDestination
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.aboutGraph(navController: NavHostController) {
    fun goBack() { navController.popBackStack() }

    composable(AboutDestination.About) {
        AboutScreen(
            onClickChangelog = { navController.navigate(AboutDestination.Changelog) },
            onClickCredits = { navController.navigate(AboutDestination.Credits) },
            onClickPrivacyStatement = { navController.navigate(AboutDestination.PrivacyStatement) },
            onClickLogs = { navController.navigate(AboutDestination.Logs) },
            onClickIntroTutorial = { navController.navigate(TutorialDestination.Intro) },
            onClickBack = ::goBack
        )
    }
    composable(AboutDestination.Changelog) {
        ChangelogScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(AboutDestination.Credits) {
        CreditsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(AboutDestination.PrivacyStatement) {
        PrivacyStatementScreen(
            onClickBack = ::goBack
        )
    }
    navigation(startDestination = AboutDestination.LogsList, route = AboutDestination.Logs) {
        composable(AboutDestination.LogsList) {
            val parentEntry = remember(it) { navController.getBackStackEntry(AboutDestination.Logs) }
            LogsScreen(
                viewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                onClickFilters = { navController.navigate(AboutDestination.LogsFilters) },
                onClickBack = ::goBack
            )
        }
        composable(AboutDestination.LogsFilters) {
            val parentEntry = remember(it) { navController.getBackStackEntry(AboutDestination.Logs) }
            LogsFiltersScreen(
                viewModel = koinViewModel(viewModelStoreOwner = parentEntry),
                onClickBack = ::goBack
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
    const val LogsList = "logs_list"
    const val LogsFilters = "logs_filters"
}
