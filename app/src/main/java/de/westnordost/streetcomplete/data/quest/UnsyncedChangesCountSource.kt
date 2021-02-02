package de.westnordost.streetcomplete.data.quest

import de.westnordost.streetcomplete.data.osm.delete_element.DeleteOsmElementDao
import de.westnordost.streetcomplete.data.osm.osmquest.changes.OsmElementTagChangesDao
import de.westnordost.streetcomplete.data.osm.splitway.SplitOsmWayDao
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNote
import de.westnordost.streetcomplete.data.osmnotes.commentnotes.CommentNoteDao
import de.westnordost.streetcomplete.data.osmnotes.createnotes.CreateNoteDao
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to how many unsynced (=uploadable) changes there are */
@Singleton class UnsyncedChangesCountSource @Inject constructor(
    private val commentNoteDao: CommentNoteDao,
    private val createNoteDao: CreateNoteDao,
    private val splitWayDao: SplitOsmWayDao,
    private val deleteElementDao: DeleteOsmElementDao,
    private val osmElementTagChangesDao: OsmElementTagChangesDao
) {
    private val listeners: MutableList<UnsyncedChangesCountListener> = CopyOnWriteArrayList()

    val count: Int get() =
            commentNoteCount +
            splitWayCount +
            createNoteCount +
            elementTagChangesCount +
            deleteOsmElementCount

    // TODO it depends whether undoOsmQuestCount is positive or negative!!
    val questCount: Int get() = + elementTagChangesCount + splitWayCount  + deleteOsmElementCount

    private var commentNoteCount: Int = commentNoteDao.getCount()
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
    private var elementTagChangesCount: Int = osmElementTagChangesDao.getCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var deleteOsmElementCount: Int = deleteElementDao.getCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }

    private val splitWayListener = object : SplitOsmWayDao.Listener {
        override fun onAddedSplitWay() { ++splitWayCount }
        override fun onDeletedSplitWay() { --splitWayCount }
    }
    private val undoOsmQuestListener = object : OsmElementTagChangesDao.Listener {
        override fun onAddedElementTagChanges() { ++elementTagChangesCount }
        override fun onDeletedElementTagChanges() { --elementTagChangesCount }
    }
    private val commentNoteListener = object : CommentNoteDao.Listener {
        override fun onAddedCommentNote(note: CommentNote) { ++commentNoteCount }
        override fun onDeletedCommentNote(noteId: Long) { --commentNoteCount }
    }
    private val createNoteListener = object : CreateNoteDao.Listener {
        override fun onAddedCreateNote() { ++createNoteCount }
        override fun onDeletedCreateNote() { --createNoteCount }
    }
    private val deleteElementListener = object : DeleteOsmElementDao.Listener {
        override fun onAddedDeleteOsmElement() { ++deleteOsmElementCount }
        override fun onDeletedDeleteOsmElement() { --deleteOsmElementCount }
    }

    init {
        splitWayDao.addListener(splitWayListener)
        deleteElementDao.addListener(deleteElementListener)
        osmElementTagChangesDao.addListener(undoOsmQuestListener)
        createNoteDao.addListener(createNoteListener)
        commentNoteDao.addListener(commentNoteListener)
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
