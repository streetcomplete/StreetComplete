package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osm.edits.IsRevertAction
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to how many unsynced (=uploadable) changes there are */
@Singleton class UnsyncedChangesCountSource @Inject constructor(
    private val noteEditsSource: NoteEditsSource,
    private val elementEditsSource: ElementEditsSource
) {
    private val listeners: MutableList<UnsyncedChangesCountListener> = CopyOnWriteArrayList()

    val count: Int get() = unsyncedNotesCount + unsyncedElementsCount

    /** count of unsynced changes that count towards the statistics. That is, unsynced note stuff
     *  doesn't count and reverts of changes count negative */
    var solvedCount: Int = elementEditsSource.getPositiveUnsyncedCount()
    private set


    private var unsyncedNotesCount: Int = noteEditsSource.getUnsyncedCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }
    private var unsyncedElementsCount: Int = elementEditsSource.getUnsyncedCount()
    set(value) {
        val diff = value - field
        field = value
        onUpdate(diff)
    }

    private val noteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) {
            if (edit.isSynced) return
            ++unsyncedNotesCount
        }

        override fun onSyncedEdit(edit: NoteEdit) {
            --unsyncedNotesCount
        }

        override fun onDeletedEdit(edit: NoteEdit) {
            if (edit.isSynced) return
            --unsyncedNotesCount
        }
    }

    private val elementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) {
            if (edit.isSynced) return
            ++unsyncedElementsCount
            if (edit.action is IsRevertAction) --solvedCount else ++solvedCount
        }
        override fun onSyncedEdit(edit: ElementEdit) {
            --unsyncedElementsCount
            if (edit.action is IsRevertAction) ++solvedCount else --solvedCount
        }
        override fun onDeletedEdit(edit: ElementEdit) {
            if (edit.isSynced) return
            --unsyncedElementsCount
            if (edit.action is IsRevertAction) ++solvedCount else --solvedCount
        }
    }

    init {
        elementEditsSource.addListener(elementEditsListener)
        noteEditsSource.addListener(noteEditsListener)
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
