package de.westnordost.streetcomplete.screens.about

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.screens.about.logs.LogsFiltersScreen
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.androidx.compose.koinViewModel

@Composable
fun AboutNavHost(onClickBack: () -> Unit) {
    val navController = rememberNavController()
    val dir = LocalLayoutDirection.current.dir

    fun goBack() {
        if (!navController.popBackStack()) onClickBack()
    }

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
