package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.screens.settings.genericQuestTitle
import de.westnordost.streetcomplete.ui.common.CenteredLargeTitleHint
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.theme.titleMedium
import de.westnordost.streetcomplete.util.ktx.containsAll
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable fun QuestSelectionScreen(
    viewModel: QuestSelectionViewModel,
    onClickBack: () -> Unit,
) {
    val quests by viewModel.quests.collectAsState()

    val filter = remember { mutableStateOf("") } // TODO

    val displayCountry = remember {
        viewModel.currentCountry?.let { Locale("", it).displayCountry } ?: "Atlantis"
    }

    // TODO Compose: reordering items not implemented. Seems to be not possible out of the box in
    //  Compose, third-party libraries exist (like sh.calvin.reorderable:reorderable), but I didn't
    //  find how to call viewModel.orderQuest only on drop, i.e end of dragging.
    //  (quests should be reordered visibly while dragging, but only on drop, function is called and
    //  quests in viewModel is updated)

    Column(Modifier.fillMaxSize()) {
        val presetName = viewModel.selectedQuestPresetName
            ?: stringResource(R.string.quest_presets_default_name)

        TopAppBar(
            title = {
                Column {
                    Text(stringResource(R.string.pref_title_quests2))
                    Text(
                        text = stringResource(R.string.pref_subtitle_quests_preset_name, presetName),
                        style = MaterialTheme.typography.body1,
                    )
                }
            },
            navigationIcon = { IconButton(onClick = { onClickBack() }) { BackIcon() } },
            actions = {
                // TODO
            },

        )
        if (quests.isEmpty()) {
            CenteredLargeTitleHint(stringResource(R.string.no_search_results))
        } else {
            BoxWithConstraints {
                val iconSize = (maxHeight / 12).coerceIn(32.dp, 64.dp)
                LazyColumn {
                    stickyHeader {
                        QuestSelectionHeader()
                    }
                    itemsIndexed(
                        items = quests,
                        key = { _, it -> it.questType.name }
                    ) { index, item ->
                        if (index != 0) Divider()
                        val isEnabled = viewModel.isQuestEnabledInCurrentCountry(item.questType)
                        QuestSelectionItem(
                            questType = item.questType,
                            isSelected = item.selected,
                            onToggleSelection = { viewModel.selectQuest(item.questType, !it) },
                            isInteractionEnabled = item.isInteractionEnabled,
                            displayCountry = displayCountry,
                            isEnabledInCurrentCountry = isEnabled,
                            iconSize = iconSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.QuestSelectionHeader() {
    Column(Modifier.background(MaterialTheme.colors.surface)) {
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .padding(24.dp, 8.dp, 8.dp, 8.dp)
        ) {
            Text(
                text = stringResource(R.string.quest_type),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.quest_enabled),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Divider()
    }
}

@Composable
@ReadOnlyComposable
private fun filterQuests(quests: List<QuestSelection>, filter: String?): List<QuestSelection> {
    val words = filter.orEmpty().trim().lowercase()
    return if (words.isEmpty()) {
        quests
    } else {
        quests.filter { questTypeMatchesSearchWords(it.questType, words.split(' ')) }
    }
}

@Composable
@ReadOnlyComposable
private fun questTypeMatchesSearchWords(questType: QuestType, words: List<String>) =
    genericQuestTitle(questType).lowercase().containsAll(words)
    // TODO Compose: no idea how to access English resources in order to also search English words.
    //               Not really worth investigating until after switching to compose multiplatform
