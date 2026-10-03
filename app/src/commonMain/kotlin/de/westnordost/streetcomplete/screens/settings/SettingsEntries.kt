package de.westnordost.streetcomplete.screens.settings

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import de.westnordost.streetcomplete.screens.Route
import de.westnordost.streetcomplete.screens.goBack
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsScreen
import de.westnordost.streetcomplete.screens.settings.locale_selection.LocaleSelectionScreen
import de.westnordost.streetcomplete.screens.settings.messages.MessageSelectionScreen
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionScreen
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsScreen
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionScreen
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<Route>.settingsEntries(backStack: NavBackStack<Route>) {
    fun goBack() { backStack.goBack() }

    entry<Route.Settings> {
        SettingsScreen(
            viewModel = koinViewModel(),
            onClickShowQuestForms = { backStack.add(Route.ShowQuestForms) },
            onClickPresetSelection = { backStack.add(Route.EditTypePresets) },
            onClickQuestSelection = { backStack.add(Route.QuestSelection) },
            onClickOverlaySelection = { backStack.add(Route.OverlaySelection) },
            onClickLocaleSelection = { backStack.add(Route.LocaleSelection) },
            onClickMessagesSelection = { backStack.add(Route.MessagesSelection) },
            onClickBack = ::goBack
        )
    }
    entry<Route.EditTypePresets> {
        EditTypePresetsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.QuestSelection> {
        QuestSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.OverlaySelection> {
        OverlaySelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.LocaleSelection> {
        LocaleSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.MessagesSelection> {
        MessageSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    entry<Route.ShowQuestForms> {
        ShowQuestFormsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack,
        )
    }
}
