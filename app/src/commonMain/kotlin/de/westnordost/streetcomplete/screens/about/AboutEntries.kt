package de.westnordost.streetcomplete.screens.about

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.data.logs.LogsFilters
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.about.logs.LogsFiltersScreen
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.screens.goBack
import org.koin.compose.viewmodel.koinViewModel

/** @param logsFilters filters shared by the logs screen and the logs filters screen */
fun EntryProviderScope<Route>.aboutEntries(
    backStack: NavBackStack<Route>,
    logsFilters: MutableState<LogsFilters>,
) {
    fun goBack() { backStack.goBack() }

    entry<Route.About> {
        AboutScreen(
            onClickChangelog = { backStack.add(Route.Changelog) },
            onClickCredits = { backStack.add(Route.Credits) },
            onClickPrivacyStatement = { backStack.add(Route.PrivacyStatement) },
            onClickLogs = { backStack.add(Route.Logs) },
            onClickIntroTutorial = { backStack.add(Route.IntroTutorial()) },
            onClickBack = ::goBack
        )
    }
    entry<Route.Changelog> {
        ChangelogScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.Credits> {
        CreditsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.PrivacyStatement> {
        PrivacyStatementScreen(
            onClickBack = ::goBack
        )
    }
    entry<Route.Logs> {
        LogsScreen(
            viewModel = koinViewModel(),
            filters = logsFilters.value,
            onClickFilters = { backStack.add(Route.LogsFilters) },
            onClickBack = ::goBack
        )
    }
    entry<Route.LogsFilters> {
        LogsFiltersScreen(
            filters = logsFilters.value,
            onFiltersChange = { logsFilters.value = it },
            onClickBack = ::goBack
        )
    }
}
