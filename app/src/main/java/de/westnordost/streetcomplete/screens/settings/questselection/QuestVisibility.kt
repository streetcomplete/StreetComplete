package de.westnordost.streetcomplete.screens.settings.questselection

import de.westnordost.streetcomplete.ApplicationConstants.EE_QUEST_OFFSET
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.prefs.Preferences

data class QuestVisibility(val questType: QuestType, var visible: Boolean, val prefs: Preferences) {
    fun isInteractionEnabled(questTypeRegistry: QuestTypeRegistry) = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        || (questType !is OsmNoteQuestType && questTypeRegistry.getOrdinalOf(questType)!! < EE_QUEST_OFFSET)
}
