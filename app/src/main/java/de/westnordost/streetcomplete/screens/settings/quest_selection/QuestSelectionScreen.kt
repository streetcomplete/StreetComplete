package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import java.util.Locale

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
@Composable
fun QuestSelectionScreen(
    viewModel: QuestSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val quests by viewModel.quests.collectAsState()
    val selectedQuestPresetName by viewModel.selectedQuestPresetName.collectAsState()

    val searchText by viewModel.searchText.collectAsStateWithLifecycle()

    val displayCountry = remember {
        viewModel.currentCountry?.let { Locale("", it).displayCountry } ?: "Atlantis"
    }

    Column(Modifier.fillMaxSize()) {
        QuestSelectionTopAppBar(
            currentPresetName = selectedQuestPresetName
                ?: stringResource(R.string.quest_presets_default_name),
            onClickBack = onClickBack,
            onUnselectAll = { viewModel.unselectAllQuests() },
            onReset = { viewModel.resetQuestSelectionsAndOrder() },
            search = searchText,
            onSearchChange = viewModel::updateSearchText,
        )

        val filteredQuests by viewModel.filteredQuests.collectAsStateWithLifecycle()

        if (filteredQuests.isEmpty()) {
            CenteredLargeTitleHint(stringResource(R.string.no_search_results))
        } else {
            val insets = WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            ).asPaddingValues()
            QuestSelectionList(
                items = filteredQuests,
                displayCountry = displayCountry,
                onSelectQuest = { questType, selected ->
                    viewModel.selectQuest(questType, selected)
                },
                onReorderQuest = { questType, toAfter ->
                    viewModel.orderQuest(questType, toAfter)
                },
                modifier = Modifier.consumeWindowInsets(insets),
                contentPadding = insets,
            )
        }
    }
}
