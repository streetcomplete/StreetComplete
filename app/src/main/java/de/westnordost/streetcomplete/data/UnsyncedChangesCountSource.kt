package de.westnordost.streetcomplete.data

import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEdit
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/** Access and listen to how many unsynced (=uploadable) changes there are */
@Singleton class UnsyncedChangesCountSource @Inject constructor(
    private val noteEditsSource: NoteEditsSource,
    private val elementEditsSource: ElementEditsSource
) {
    interface Listener {
        fun onIncreased()
        fun onDecreased()
    }

    private val listeners = CopyOnWriteArrayList<Listener>()

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        elementEditsSource.getUnsyncedCount() + noteEditsSource.getUnsyncedCount()
    }

    /** count of unsynced changes that count towards the statistics. That is, unsynced note stuff
     *  doesn't count and reverts of changes count negative */
    suspend fun getSolvedCount(): Int = withContext(Dispatchers.IO) {
        elementEditsSource.getPositiveUnsyncedCount()
    }

    private val noteEditsListener = object : NoteEditsSource.Listener {
        override fun onAddedEdit(edit: NoteEdit) { if (!edit.isSynced) onUpdate(+1) }
        override fun onSyncedEdit(edit: NoteEdit) { onUpdate(-1) }
        override fun onDeletedEdits(edits: List<NoteEdit>) { onUpdate(-edits.filter { !it.isSynced }.size) }
    }

    private val elementEditsListener = object : ElementEditsSource.Listener {
        override fun onAddedEdit(edit: ElementEdit) { if (!edit.isSynced) onUpdate(+1) }
        override fun onSyncedEdit(edit: ElementEdit) { onUpdate(-1) }
        override fun onDeletedEdits(edits: List<ElementEdit>) { onUpdate(-edits.filter { !it.isSynced }.size) }
    }

    init {
        elementEditsSource.addListener(elementEditsListener)
        noteEditsSource.addListener(noteEditsListener)
    }

    private fun onUpdate(diff: Int) {
        if (diff > 0) listeners.forEach { it.onIncreased() }
        else if (diff < 0) listeners.forEach { it.onDecreased() }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }
}
