package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.util.ktx.containsAll
import java.util.Locale

/** Shows a screen in which the user can enable and disable quests as well as re-order them */
@Composable
fun QuestSelectionScreen(
    viewModel: QuestSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val quests by viewModel.quests.collectAsState()

    var searchText by remember { mutableStateOf(TextFieldValue()) }

    val displayCountry = remember {
        viewModel.currentCountry?.let { Locale("", it).displayCountry } ?: "Atlantis"
    }

    Column(Modifier.fillMaxSize()) {
        QuestSelectionTopAppBar(
            currentPresetName = viewModel.selectedQuestPresetName ?: stringResource(R.string.quest_presets_default_name),
            onClickBack = onClickBack,
            onUnselectAll = { viewModel.unselectAllQuests() },
            onReset = { viewModel.resetQuestSelectionsAndOrder() },
            search = searchText,
            onSearchChange = { searchText = it }
        )

        // the filtering is not done in the view model because it involves accessing localized
        // resources, which we consider UI (framework) specific data and view models should be
        // free of that.
        // NOTE: This is very slow though, it involves getting the string resource, lowercasing it
        //       and comparing to the filter for each quest on each recomposition (e.g. if the
        //       user scrolls the list by a tiny amount). Unfortunately, getting a stringResource
        //       (the quest title) is a composable function and composable functions cannot be
        //       placed in a remember { } lambda, so no idea how to improve this
        val filteredQuests = filterQuests(quests, searchText.text)

        if (filteredQuests.isEmpty()) {
            CenteredLargeTitleHint(stringResource(R.string.no_search_results))
        } else {
            QuestSelectionList(
                items = filteredQuests,
                displayCountry = displayCountry,
                onSelectQuest = { questType, selected ->
                    viewModel.selectQuest(questType, selected)
                },
                onReorderQuest = { questType, toAfter ->
                    viewModel.orderQuest(questType, toAfter)
                }
            )
        }
    }
}

@Composable
@ReadOnlyComposable
private fun filterQuests(quests: List<QuestSelection>, filter: String): List<QuestSelection> {
    val words = filter.trim().lowercase()
    return if (words.isEmpty()) {
        quests
    } else {
        val wordList = words.split(' ')
        quests.filter { stringResource(it.questType.title).lowercase().containsAll(wordList) }
    }
}
