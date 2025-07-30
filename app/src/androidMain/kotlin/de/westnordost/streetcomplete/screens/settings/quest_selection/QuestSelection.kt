package de.westnordost.streetcomplete.screens.settings.quest_selection

import androidx.compose.runtime.Immutable
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.quests.note_discussion.OsmNoteQuestType

@Immutable
data class QuestSelection(
    val questType: QuestType,
    val selected: Boolean,
    val enabledInCurrentCountry: Boolean,
) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
