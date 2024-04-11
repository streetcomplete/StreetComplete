package de.westnordost.streetcomplete.screens.settings.questselection

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

data class QuestSelection(val questType: QuestType, val selected: Boolean) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
