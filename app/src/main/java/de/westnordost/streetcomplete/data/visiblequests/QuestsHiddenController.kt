package de.westnordost.streetcomplete.data.visiblequests

import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestsHiddenDao
import de.westnordost.streetcomplete.data.osmnotes.notequests.NoteQuestsHiddenDao
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.util.Listeners

/** Controller for managing which quests have been hidden by user interaction. */
class QuestsHiddenController(
    private val osmDb: OsmQuestsHiddenDao,
    private val notesDb: NoteQuestsHiddenDao,
) : QuestsHiddenSource, HideQuestController {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<QuestsHiddenSource.Listener>()

    /** Mark the quest as hidden by user interaction */
    override fun hide(key: QuestKey) {
        when (key) {
            is OsmQuestKey -> osmDb.add(key)
            is OsmNoteQuestKey -> notesDb.add(key.noteId)
        }
        val timestamp = get(key) ?: return
        listeners.forEach { it.onHid(key, timestamp) }
    }

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: QuestKey): Boolean {
        val result = when (key) {
            is OsmQuestKey -> osmDb.delete(key)
            is OsmNoteQuestKey -> notesDb.delete(key.noteId)
        }
        if (!result) return false
        val timestamp = get(key) ?: return false
        listeners.forEach { it.onUnhid(key, timestamp) }
        return true
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val unhidCount = osmDb.deleteAll() + notesDb.deleteAll()
        listeners.forEach { it.onUnhidAll() }
        return unhidCount
    }

    override fun get(key: QuestKey): Long? =
        when (key) {
            is OsmQuestKey -> osmDb.getTimestamp(key)
            is OsmNoteQuestKey -> notesDb.getTimestamp(key.noteId)
        }

    override fun getAllNewerThan(timestamp: Long): List<Pair<QuestKey, Long>> =
        (
            osmDb.getNewerThan(timestamp).map { it.osmQuestKey to it.timestamp } +
            notesDb.getNewerThan(timestamp).map { OsmNoteQuestKey(it.noteId) to it.timestamp }
        ).sortedByDescending { it.second }

    override fun countAll(): Long =
        osmDb.countAll() + notesDb.countAll()

    override fun addListener(listener: QuestsHiddenSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestsHiddenSource.Listener) {
        listeners.remove(listener)
    }
}
