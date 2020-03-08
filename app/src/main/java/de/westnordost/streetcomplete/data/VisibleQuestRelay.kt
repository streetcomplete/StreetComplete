package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestGroup

/** Threadsafe relay for VisibleQuestListener
 * (setting the listener and calling the listener methods can safely be done from different threads)  */
class VisibleQuestRelay : VisibleQuestListener {
    var listener: VisibleQuestListener? = null

    override fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup) {
        listener?.onQuestsRemoved(questIds, group)
    }

    override fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup) {
        listener?.onQuestsCreated(quests, group)
    }

    fun onQuestRemoved(questId: Long, group: QuestGroup) {
        onQuestsRemoved(listOf(questId), group)
    }
}
