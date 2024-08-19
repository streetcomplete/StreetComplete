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
import de.westnordost.streetcomplete.screens.settings.quest_presets.QuestPresetsScreen
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
                onClickPresetSelection = { navController.navigate(SettingsDestination.QuestPresets) },
                onClickQuestSelection = { navController.navigate(SettingsDestination.QuestSelection) },
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.QuestPresets) {
            QuestPresetsScreen(
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
    const val QuestPresets = "quest_presets"
    const val QuestSelection = "quest_selection"
    const val ShowQuestForms = "show_quest_forms"
}
