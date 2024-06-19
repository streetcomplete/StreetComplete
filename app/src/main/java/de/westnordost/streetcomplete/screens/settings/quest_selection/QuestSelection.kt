package de.westnordost.streetcomplete.screens.settings.quest_selection

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

data class QuestSelection(
    val questType: QuestType,
    val selected: Boolean,
    val enabledInCurrentCountry: Boolean
) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
