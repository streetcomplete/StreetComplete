package de.westnordost.streetcomplete.settings.questselection

import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType

data class QuestVisibility(val questType: QuestType<*>, var visible: Boolean) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
