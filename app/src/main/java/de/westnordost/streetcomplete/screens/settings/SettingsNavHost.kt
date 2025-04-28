package de.westnordost.streetcomplete.screens.settings

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.screens.settings.debug.ShowQuestFormsScreen
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionScreen
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsScreen
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.androidx.compose.koinViewModel

@Composable fun SettingsNavHost(
    onClickBack: () -> Unit,
    onClickShowQuestTypeForDebug: (QuestType) -> Unit,
    startDestination: String? = null,
) {
    val navController = rememberNavController()
    val dir = LocalLayoutDirection.current.dir

    fun goBack() {
        if (!navController.popBackStack()) onClickBack()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination ?: SettingsDestination.Settings,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }) }
    ) {
        composable(SettingsDestination.Settings) {
            SettingsScreen(
                viewModel = koinViewModel(),
                onClickShowQuestForms = { navController.navigate(SettingsDestination.ShowQuestForms) },
                onClickPresetSelection = { navController.navigate(SettingsDestination.EditTypePresets) },
                onClickQuestSelection = { navController.navigate(SettingsDestination.QuestSelection) },
                onClickOverlaySelection = { navController.navigate(SettingsDestination.OverlaySelection) },
                onClickBack = ::goBack,
                onClickQuestSettings = { navController.navigate(SettingsDestination.QuestSettings) },
                onClickUiSettings = { navController.navigate(SettingsDestination.UiSettings) },
                onClickDisplaySettings = { navController.navigate(SettingsDestination.DisplaySettings) },
                onClickNoteSettings = { navController.navigate(SettingsDestination.NoteSettings) },
                onClickDataSettings = { navController.navigate(SettingsDestination.DataManagementSettings) },
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
        composable(SettingsDestination.ShowQuestForms) {
            ShowQuestFormsScreen(
                viewModel = koinViewModel(),
                onClickQuestType = onClickShowQuestTypeForDebug,
                onClickBack = ::goBack,
            )
        }
        composable(SettingsDestination.QuestSettings) {
            QuestSettingsScreen(
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.UiSettings) {
            UiSettingsScreen(
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.DisplaySettings) {
            DisplaySettingsScreen(
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.NoteSettings) {
            NoteSettingsScreen(
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.DataManagementSettings) {
            DataManagementScreen(
                onClickBack = ::goBack
            )
        }
    }
}

object SettingsDestination {
    const val Settings = "settings"
    const val EditTypePresets = "edit_type_presets"
    const val QuestSelection = "quest_selection"
    const val OverlaySelection = "overlay_selection"
    const val ShowQuestForms = "show_quest_forms"
    const val QuestSettings = "scee_quest_settings"
    const val UiSettings = "scee_ui_settings"
    const val DisplaySettings = "scee_display_settings"
    const val NoteSettings = "scee_note_settings"
    const val DataManagementSettings = "scee_data_settings"
}
