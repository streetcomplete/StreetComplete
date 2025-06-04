package de.westnordost.streetcomplete.data.atp.atpquests.edits

import de.westnordost.streetcomplete.data.atp.AtpController
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditAction.CREATE
import de.westnordost.streetcomplete.data.osmnotes.edits.NoteEditsSource
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.util.Listeners

class AtpDataWithEditsSource(
    private val atpController: AtpController,
    private val atpDataSource: AtpEditsSource, // holds unsynced ones (TODO: not implemented yet, do I even need this? what this would do?)
    private val userDataSource: UserDataSource
) {
    // TODO see MapDataWithEditsSource and NotesWithEditsSource
    // AtpDataWithEditsSource may make sense due to holding not-yet-uploaded
    // cases where ATP was surveyed to be a nonsense

    fun get(entryId: Long): AtpEntry? {
        var entry = atpController.get(entryId)
        return entry
        // TODO: try to take into account unsynced edits, otheriwse there is no point in this class
    }

    private val atpControllerListener = object : AtpController.Listener {
        override fun onUpdated(
            added: Collection<AtpEntry>,
            updated: Collection<AtpEntry>,
            deleted: Collection<Long>
        ) {
            // TODO merge with applied edits? is it even needed like it is for notes?
            //val noteCommentEdits = noteEditsSource.getAllUnsynced().filter { it.action != CREATE }
            callOnUpdated(
                //editsAppliedToNotes(added, noteCommentEdits),
                //editsAppliedToNotes(updated, noteCommentEdits),
                //deleted
                added,
                updated,
                deleted
            )
        }

        override fun onCleared() {
            //TODO is it even needed //callOnCleared()
        }
    }

    init {
        atpController.addListener(atpControllerListener)
    }
    interface Listener {
        fun onUpdated(added: Collection<AtpEntry>, deleted: Collection<Long>)
        fun onInvalidated()
    }
    private val listeners = Listeners<AtpDataWithEditsSource.Listener>()

    fun addListener(listener: AtpDataWithEditsSource.Listener) {
        listeners.add(listener)
    }
    fun removeListener(listener: AtpDataWithEditsSource.Listener) {
        listeners.remove(listener)
    }
    private fun callOnUpdated(added: Collection<AtpEntry> = emptyList(), updated: Collection<AtpEntry> = emptyList(), deleted: Collection<Long> = emptyList()) {
        listeners.forEach { it.onUpdated(added, deleted) }
    }

    fun getAll(bbox: BoundingBox): Collection<AtpEntry> {
        // TODO: what about taking edits into account, that is whole point of this class
        return atpController.getAll(bbox)
    }
}
