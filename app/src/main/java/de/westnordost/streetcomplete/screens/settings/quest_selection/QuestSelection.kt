package de.westnordost.streetcomplete.screens.settings.quest_selection

import de.westnordost.streetcomplete.ApplicationConstants.EE_QUEST_OFFSET
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestType
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry

data class QuestSelection(val questType: QuestType, var selected: Boolean, val enabledInCurrentCountry: Boolean, val prefs: Preferences) {
    fun isInteractionEnabled(questTypeRegistry: QuestTypeRegistry) = prefs.getBoolean(Prefs.EXPERT_MODE, false)
        // not sure how questTypeRegistry can be empty / not contain a quest initially coming from that repository
        // but it can happen, so just don't crash, see https://github.com/Helium314/SCEE/issues/639
        || (questType !is OsmNoteQuestType && (questTypeRegistry.getOrdinalOf(questType) ?: EE_QUEST_OFFSET) < EE_QUEST_OFFSET)
}
