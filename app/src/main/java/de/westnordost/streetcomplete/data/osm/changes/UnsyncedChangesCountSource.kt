package de.westnordost.streetcomplete.data.osm.changes

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
    private val elementEditsSource: ElementEditsSource
) {
    private val listeners: MutableList<UnsyncedChangesCountListener> = CopyOnWriteArrayList()

    val count: Int get() = commentNoteCount + createNoteCount + osmElementChangesCount

    /** count of unsynced changes that count towards the statistics. That is, unsynced note stuff
     *  doesn't count and reverts of changes count negative */
    var solvedCount: Int = elementEditsSource.getEditsCountSolved()
    private set

    private var commentNoteCount: Int = commentNoteDao.getCount()
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
    private var osmElementChangesCount: Int = elementEditsSource.getUnsyncedEditsCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }

    private val commentNoteListener = object : CommentNoteDao.Listener {
        override fun onAddedCommentNote(note: CommentNote) { ++commentNoteCount }
        override fun onDeletedCommentNote(noteId: Long) { --commentNoteCount }
    }
    private val createNoteListener = object : CreateNoteDao.Listener {
        override fun onAddedCreateNote() { ++createNoteCount }
        override fun onDeletedCreateNote() { --createNoteCount }
    }
    private val osmElementChangesListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) {
            if (edit.isSynced) return
            ++osmElementChangesCount
            if (edit.action is IsRevert) --solvedCount else ++solvedCount
        }
        override fun onSyncedEdit(edit: ElementEdit) {
            --osmElementChangesCount
            if (edit.action is IsRevert) ++solvedCount else --solvedCount
        }
        override fun onDeletedEdit(edit: ElementEdit) {
            if (edit.isSynced) return
            --osmElementChangesCount
            if (edit.action is IsRevert) ++solvedCount else --solvedCount
        }
    }

    init {
        elementEditsSource.addListener(osmElementChangesListener)
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
