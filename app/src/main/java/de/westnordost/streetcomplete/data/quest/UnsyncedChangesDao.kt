package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.osmquest.OsmQuestDao
import de.westnordost.streetcomplete.data.osm.osmquest.undo.UndoOsmQuestDao
import de.westnordost.streetcomplete.data.osm.splitway.OsmQuestSplitWayDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestDao
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

/** Access and listen to how many unsynced (=uploadable) changes there are */
class UnsyncedChangesDao @Inject constructor(
    private val questDao: OsmQuestDao,
    private val noteQuestDao: OsmNoteQuestDao,
    private val createNoteDao: CreateNoteDao,
    private val splitWayDao: OsmQuestSplitWayDao,
    private val undoOsmQuestDao: UndoOsmQuestDao
) {
    interface Listener {
        fun onUnsyncedChangesCountIncreased()
        fun onUnsyncedChangesCountDecreased()
    }
    private val listeners: MutableList<Listener> = CopyOnWriteArrayList()


    val count: Int get() =
        questDao.answeredCount +
        noteQuestDao.answeredCount +
        splitWayCount +
        createNoteCount +
        undoOsmQuestCount

    private var splitWayCount: Int = splitWayDao.getCount()
    private var createNoteCount: Int = createNoteDao.getCount()
    private var undoOsmQuestCount: Int = undoOsmQuestDao.getCount()

    init {
        splitWayDao.addListener(object : OsmQuestSplitWayDao.Listener {
            override fun onAddedSplitWay() {
                ++splitWayCount
                onUnsyncedChangesCountIncreased()
            }

            override fun onDeletedSplitWay() {
                --splitWayCount
                onUnsyncedChangesCountDecreased()
            }
        })
        undoOsmQuestDao.addListener(object : UndoOsmQuestDao.Listener {
            override fun onAddedUndoOsmQuest() {
                ++undoOsmQuestCount
                onUnsyncedChangesCountIncreased()
            }

            override fun onDeletedUndoOsmQuest() {
                --undoOsmQuestCount
                onUnsyncedChangesCountDecreased()
            }

        })
        createNoteDao.addListener(object : CreateNoteDao.Listener {
            override fun onAddedCreateNote() {
                ++createNoteCount
                onUnsyncedChangesCountIncreased()
            }

            override fun onDeletedCreateNote() {
                --createNoteCount
                onUnsyncedChangesCountDecreased()
            }
        })
        noteQuestDao.addAnsweredQuestCountListener(object : OsmNoteQuestDao.AnsweredQuestCountListener {
            override fun onAnsweredNoteQuestCountIncreased() {
                onUnsyncedChangesCountIncreased()
            }

            override fun onAnsweredNoteQuestCountDecreased() {
                onUnsyncedChangesCountDecreased()
            }
        })
        questDao.addAnsweredQuestCountListener(object : OsmQuestDao.AnsweredQuestCountListener {
            override fun onAnsweredQuestCountIncreased() {
                onUnsyncedChangesCountIncreased()
            }

            override fun onAnsweredQuestCountDecreased() {
                onUnsyncedChangesCountDecreased()
            }
        })
    }

    private fun onUnsyncedChangesCountIncreased() {
        listeners.forEach { it.onUnsyncedChangesCountIncreased() }
    }

    private fun onUnsyncedChangesCountDecreased() {
        listeners.forEach { it.onUnsyncedChangesCountDecreased() }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}