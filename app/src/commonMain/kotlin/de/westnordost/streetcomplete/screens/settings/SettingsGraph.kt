package de.westnordost.streetcomplete.screens.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsScreen
import de.westnordost.streetcomplete.screens.settings.language_selection.LanguageSelectionScreen
import de.westnordost.streetcomplete.screens.settings.messages.MessageSelectionScreen
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionScreen
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsScreen
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionScreen
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    fun goBack() { navController.popBackStack() }

    composable(SettingsDestination.Settings) {
        SettingsScreen(
            viewModel = koinViewModel(),
            onClickShowQuestForms = { navController.navigate(SettingsDestination.ShowQuestForms) },
            onClickPresetSelection = { navController.navigate(SettingsDestination.EditTypePresets) },
            onClickQuestSelection = { navController.navigate(SettingsDestination.QuestSelection) },
            onClickOverlaySelection = { navController.navigate(SettingsDestination.OverlaySelection) },
            onClickLanguageSelection = { navController.navigate(SettingsDestination.LanguageSelection) },
            onClickMessagesSelection = { navController.navigate(SettingsDestination.MessagesSelection) },
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.EditTypePresets) {
        EditTypePresetsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.QuestSelection) {
        QuestSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.OverlaySelection) {
        OverlaySelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.LanguageSelection) {
        LanguageSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.MessagesSelection) {
        MessageSelectionScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack
        )
    }
    composable(SettingsDestination.ShowQuestForms) {
        ShowQuestFormsScreen(
            viewModel = koinViewModel(),
            onClickBack = ::goBack,
        )
    }
}

object SettingsDestination {
    const val Settings = "settings"
    const val EditTypePresets = "edit_type_presets"
    const val QuestSelection = "quest_selection"
    const val OverlaySelection = "overlay_selection"
    const val LanguageSelection = "language_selection"
    const val MessagesSelection = "messages_selection"
    const val ShowQuestForms = "show_quest_forms"
}
