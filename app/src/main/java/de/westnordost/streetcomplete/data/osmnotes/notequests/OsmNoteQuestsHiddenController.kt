package de.westnordost.streetcomplete.data.osmnotes.notequests

import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.util.Listeners

/** Controller for managing which osm note quests have been hidden by user interaction */
class OsmNoteQuestsHiddenController(
    private val db: NoteQuestsHiddenDao,
    private val noteSource: NotesWithEditsSource,
) : OsmNoteQuestsHiddenSource {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<OsmNoteQuestsHiddenSource.Listener>()

    /** Mark the note quest as hidden by user interaction */
    fun hide(questId: Long) {
        db.add(questId)
        val hidden = get(questId)
        if (hidden != null) onHid(hidden)
    }

    /** Un-hides a specific hidden quest by user interaction */
    fun unhide(questId: Long): Boolean {
        val hidden = get(questId)
        if (!db.delete(questId)) return false
        if (hidden != null) onUnhid(hidden)
        return true
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val unhidCount = db.deleteAll()
        onUnhidAll()
        return unhidCount
    }

    override fun isHidden(questId: Long): Boolean {
        return db.contains(questId)
    }

    override fun get(questId: Long): OsmNoteQuestHidden? {
        val timestamp = db.getTimestamp(questId) ?: return null
        val note = noteSource.get(questId) ?: return null
        return OsmNoteQuestHidden(note, timestamp)
    }

    override fun getAllNewerThan(timestamp: Long): List<OsmNoteQuestHidden> {
        val noteIdsWithTimestamp = db.getNewerThan(timestamp)
        val notesById = noteSource.getAll(noteIdsWithTimestamp.map { it.noteId }).associateBy { it.id }

        return noteIdsWithTimestamp.mapNotNull { (noteId, timestamp) ->
            notesById[noteId]?.let { OsmNoteQuestHidden(it, timestamp) }
        }
    }

    override fun countAll(): Long = db.countAll()

    override fun addListener(listener: OsmNoteQuestsHiddenSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmNoteQuestsHiddenSource.Listener) {
        listeners.remove(listener)
    }

    private fun onHid(edit: OsmNoteQuestHidden) {
        listeners.forEach { it.onHid(edit) }
    }
    private fun onUnhid(edit: OsmNoteQuestHidden) {
        listeners.forEach { it.onUnhid(edit) }
    }
    private fun onUnhidAll() {
        listeners.forEach { it.onUnhidAll() }
    }
}
