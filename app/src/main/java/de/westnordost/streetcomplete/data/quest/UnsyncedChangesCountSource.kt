package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestController
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestController
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to how many unsynced (=uploadable) changes there are */
@Singleton class UnsyncedChangesCountSource @Inject constructor(
    private val osmQuestController: OsmQuestController,
    private val osmNoteQuestController: OsmNoteQuestController,
    private val createNoteDao: CreateNoteDao,
    private val splitWayDao: OsmQuestSplitWayDao,
    private val undoOsmQuestDao: UndoOsmQuestDao
) {
    private val listeners: MutableList<UnsyncedChangesCountListener> = CopyOnWriteArrayList()

    val count: Int get() =
            answeredOsmQuestCount +
            answeredOsmNoteQuestCount +
            splitWayCount +
            createNoteCount +
            undoOsmQuestCount

    val questCount: Int get() = answeredOsmQuestCount + splitWayCount - undoOsmQuestCount
    
    private var answeredOsmQuestCount: Int = osmQuestController.getAllAnsweredCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var answeredOsmNoteQuestCount: Int = osmNoteQuestController.getAllAnsweredCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var splitWayCount: Int = splitWayDao.getCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var createNoteCount: Int = createNoteDao.getCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var undoOsmQuestCount: Int = undoOsmQuestDao.getCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }

    private val splitWayListener = object : OsmQuestSplitWayDao.Listener {
        override fun onAddedSplitWay() { ++splitWayCount }
        override fun onDeletedSplitWay() { --splitWayCount }
    }
    private val undoOsmQuestListener = object : UndoOsmQuestDao.Listener {
        override fun onAddedUndoOsmQuest() { ++undoOsmQuestCount }
        override fun onDeletedUndoOsmQuest() { --undoOsmQuestCount }
    }
    private val createNoteListener = object : CreateNoteDao.Listener {
        override fun onAddedCreateNote() { ++createNoteCount }
        override fun onDeletedCreateNote() { --createNoteCount }
    }
    private val noteQuestStatusListener = object : OsmNoteQuestController.QuestStatusListener {
        override fun onAdded(quest: OsmNoteQuest) {
            if (quest.status.isAnswered) { ++answeredOsmNoteQuestCount }
        }

        override fun onChanged(quest: OsmNoteQuest, previousStatus: QuestStatus) {
            if(quest.status.isAnswered && !previousStatus.isAnswered) {
                ++answeredOsmNoteQuestCount
            } else if (!quest.status.isAnswered && previousStatus.isAnswered) {
                --answeredOsmNoteQuestCount
            }
        }

        override fun onRemoved(questId: Long, previousStatus: QuestStatus) {
            if (previousStatus.isAnswered) { --answeredOsmNoteQuestCount }
        }

        override fun onUpdated(added: Collection<OsmNoteQuest>, updated: Collection<OsmNoteQuest>, deleted: Collection<Long>) {
            answeredOsmNoteQuestCount = osmNoteQuestController.getAllAnsweredCount()
        }
    }
    private val questStatusListener = object : OsmQuestController.QuestStatusListener {
        override fun onChanged(quest: OsmQuest, previousStatus: QuestStatus) {
            if(quest.status.isAnswered && !previousStatus.isAnswered) {
                ++answeredOsmQuestCount
            } else if (!quest.status.isAnswered && previousStatus.isAnswered) {
                --answeredOsmQuestCount
            }
        }

        override fun onRemoved(questId: Long, previousStatus: QuestStatus) {
            if (previousStatus.isAnswered) { --answeredOsmQuestCount }
        }

        override fun onUpdated(added: Collection<OsmQuest>, updated: Collection<OsmQuest>, deleted: Collection<Long>) {
            answeredOsmQuestCount = osmQuestController.getAllAnsweredCount()
        }
    }

    init {
        splitWayDao.addListener(splitWayListener)
        undoOsmQuestDao.addListener(undoOsmQuestListener)
        createNoteDao.addListener(createNoteListener)
        osmNoteQuestController.addQuestStatusListener(noteQuestStatusListener)
        osmQuestController.addQuestStatusListener(questStatusListener)
    }

    private fun onUpdate(diff: Int) {
        if (diff > 0) listeners.forEach { it.onUnsyncedChangesCountIncreased() }
        else if (diff < 0) listeners.forEach { it.onUnsyncedChangesCountDecreased() }
    }

    fun addListener(listener: UnsyncedChangesCountListener) {
        listeners.add(listener)
    }
    fun removeListener(listener: UnsyncedChangesCountListener) {
        listeners.remove(listener)
    }
}

interface UnsyncedChangesCountListener {
    fun onUnsyncedChangesCountIncreased()
    fun onUnsyncedChangesCountDecreased()
}

private val QuestStatus.isAnswered get() = this == QuestStatus.ANSWERED