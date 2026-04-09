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
import de.westnordost.streetcomplete.screens.settings.language_selection.LanguageSelectionScreen
import de.westnordost.streetcomplete.screens.settings.messages.MessageSelectionScreen
import de.westnordost.streetcomplete.screens.settings.overlay_selection.OverlaySelectionScreen
import de.westnordost.streetcomplete.screens.settings.presets.EditTypePresetsScreen
import de.westnordost.streetcomplete.screens.settings.quest_selection.QuestSelectionScreen
import de.westnordost.streetcomplete.ui.ktx.dir
import org.koin.androidx.compose.koinViewModel

@Composable fun SettingsNavHost(
    onClickBack: () -> Unit,
    onClickShowQuestTypeForDebug: (QuestType) -> Unit,
    startDestination: String? = null
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
                onClickQuestType = onClickShowQuestTypeForDebug,
                onClickBack = ::goBack,
            )
        }
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
