package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestGroup

interface VisibleQuestListener {
    fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup)
    /** Called when the given quests are removed I.e. solved, hidden by the user or when they become obsolete.  */
    fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup)
}
