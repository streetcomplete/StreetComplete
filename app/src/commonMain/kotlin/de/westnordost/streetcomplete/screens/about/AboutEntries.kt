package de.westnordost.streetcomplete.screens.about

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.screens.LocalParentViewModelStoreOwner
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.about.logs.LogsFiltersScreen
import de.westnordost.streetcomplete.screens.about.logs.LogsScreen
import de.westnordost.streetcomplete.screens.goBack
import de.westnordost.streetcomplete.screens.parentViewModelStore
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.aboutEntries(backStack: NavBackStack<Route>) {
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
    // the logs filters screen edits the filters of the logs screen's view model
    entry<Route.Logs>(clazzContentKey = { it.toString() }) {
        LogsScreen(
            viewModel = koinViewModel(),
            onClickFilters = { backStack.add(Route.LogsFilters) },
            onClickBack = ::goBack
        )
    }
    entry<Route.LogsFilters>(metadata = parentViewModelStore(Route.Logs.toString())) {
        LogsFiltersScreen(
            viewModel = koinViewModel(viewModelStoreOwner = LocalParentViewModelStoreOwner.current),
            onClickBack = ::goBack
        )
    }
}
