package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.runtime.Immutable
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

@Immutable
data class QuestSelection(
    val questType: QuestType,
    val selected: Boolean,
    val enabledInCurrentCountry: Boolean,
) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
