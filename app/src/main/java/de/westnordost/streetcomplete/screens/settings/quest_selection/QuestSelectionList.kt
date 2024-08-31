package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.seating.AddSeating
import de.westnordost.streetcomplete.quests.tactile_paving.AddTactilePavingBusStop
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.theme.titleMedium

/** List of quest types to individually enable or disable or reorder them */
@Composable
fun QuestSelectionList(
    items: List<QuestSelection>,
    displayCountry: String,
    onSelectQuest: (questType: QuestType, selected: Boolean) -> Unit,
) {
    var showEnableQuestDialog by remember { mutableStateOf<QuestType?>(null) }

    Column {
        QuestSelectionHeader()
        // TODO Compose: scrollbars would be nice here (not supported yet by compose)
        //      When they are available: Check other places too, don't want to add a todo in every
        //      single place that could have a scrollbar
        LazyColumn {
            itemsIndexed(items, key = { _, it -> it.questType.name }) { index, item ->
                Column(Modifier.background(MaterialTheme.colors.surface)) {
                    if (index > 0) Divider()
                    QuestSelectionRow(
                        item = item,
                        onToggleSelection = { isSelected ->
                            // when enabling quest that is disabled by default, require confirmation
                            if (isSelected && item.questType.defaultDisabledMessage != 0) {
                                showEnableQuestDialog = item.questType
                            } else {
                                onSelectQuest(item.questType, isSelected)
                            }
                        },
                        displayCountry = displayCountry,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }

    showEnableQuestDialog?.let { questType ->
        ConfirmationDialog(
            onDismissRequest = { showEnableQuestDialog = null },
            onConfirmed = { onSelectQuest(questType, true) },
            title = { Text(stringResource(R.string.enable_quest_confirmation_title)) },
            text = { Text(stringResource(questType.defaultDisabledMessage)) }
        )
    }
}

@Composable
private fun QuestSelectionHeader() {
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
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

@Preview
@Composable
private fun PreviewQuestSelectionList() {
    QuestSelectionList(
        items = listOf(
            QuestSelection(OsmNoteQuestType, true, true),
            QuestSelection(AddSeating(), false, true),
            QuestSelection(AddTactilePavingBusStop(), true, false),
        ),
        displayCountry = "Atlantis",
        onSelectQuest = { _, _ -> }
    )
}
