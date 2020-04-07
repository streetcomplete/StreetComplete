package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestController
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to undoable (closed, hidden, answered) changes there are.
 *
 *  Currently, only OsmQuests are undoable. If more things become undoable or conditionally
 *  undoable, the structure of this needs to be re-thought. Probably undoable things should
 *  implement an interface, and the source for this would return those instead of quests. */
@Singleton class UndoableOsmQuestsSource @Inject constructor(
    private val osmQuestController: OsmQuestController
){
    private val listeners: MutableList<UndoableOsmQuestsCountListener> = CopyOnWriteArrayList()

    var count: Int = osmQuestController.getAllUndoableCount()
        set(value) {
            val diff = value - field
            field = value
            onUpdate(diff)
        }

    private val questStatusListener = object : OsmQuestController.QuestStatusListener {
        override fun onChanged(quest: OsmQuest, previousStatus: QuestStatus) {
            if(quest.status.isUndoable && !previousStatus.isUndoable) {
                ++count
            } else if (!quest.status.isUndoable && previousStatus.isUndoable) {
                --count
            }
        }

        override fun onRemoved(questId: Long, previousStatus: QuestStatus) {
            if (previousStatus.isUndoable) {
                --count
            }
        }

        override fun onUpdated(added: Collection<OsmQuest>, updated: Collection<OsmQuest>, deleted: Collection<Long>) {
            count = osmQuestController.getAllUndoableCount()
        }
    }

    init {
        osmQuestController.addQuestStatusListener(questStatusListener)
    }

    /** Get the last undoable quest (includes answered, hidden and uploaded) */
    fun getLastUndoable(): OsmQuest? = osmQuestController.getLastUndoable()

    fun addListener(listener: UndoableOsmQuestsCountListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UndoableOsmQuestsCountListener) {
        listeners.remove(listener)
    }

    private fun onUpdate(diff: Int) {
        if (diff > 0) listeners.forEach { it.onUndoableOsmQuestsCountIncreased() }
        else if (diff < 0) listeners.forEach { it.onUndoableOsmQuestsCountDecreased() }
    }
}

interface UndoableOsmQuestsCountListener {
    fun onUndoableOsmQuestsCountIncreased()
    fun onUndoableOsmQuestsCountDecreased()
}

private val QuestStatus.isUndoable: Boolean get() =
    when(this) {
        QuestStatus.ANSWERED, QuestStatus.HIDDEN, QuestStatus.CLOSED -> true
        else -> false
    }