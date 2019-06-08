package de.westnordost.streetcomplete.settings.questselection

import de.westnordost.streetcomplete.data.QuestType
import de.westnordost.streetcomplete.data.osmnotes.OsmNoteQuestType

data class QuestVisibility(val questType: QuestType<*>, var visible:Boolean) {
    val isInteractionEnabled get() = questType !is OsmNoteQuestType
}
