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

    private val cache: MutableMap<QuestKey, Long> by lazy {
        val allOsmHidden = osmDb.getAll()
        val allNotesHidden = notesDb.getAll()
        val result = HashMap<QuestKey, Long>(allOsmHidden.size + allNotesHidden.size)
        allOsmHidden.forEach { result[it.key] = it.timestamp }
        allNotesHidden.forEach { result[OsmNoteQuestKey(it.noteId)] = it.timestamp }
        result
    }

    /** Mark the quest as hidden by user interaction */
    override fun hide(key: QuestKey) {
        val timestamp: Long
        synchronized(this) {
            when (key) {
                is OsmQuestKey -> osmDb.add(key)
                is OsmNoteQuestKey -> notesDb.add(key.noteId)
            }
            timestamp = getTimestamp(key) ?: return
            cache[key] = timestamp
        }
        listeners.forEach { it.onHid(key, timestamp) }
    }

    /** Un-hide the given quest. Returns whether it was hid before */
    fun unhide(key: QuestKey): Boolean {
        val timestamp: Long
        synchronized(this) {
            timestamp = getTimestamp(key) ?: return false
            val result = when (key) {
                is OsmQuestKey -> osmDb.delete(key)
                is OsmNoteQuestKey -> notesDb.delete(key.noteId)
            }
            if (!result) return false
            cache.remove(key)
        }
        listeners.forEach { it.onUnhid(key, timestamp) }
        return true
    }

    private fun getTimestamp(key: QuestKey): Long? =
        when (key) {
            is OsmQuestKey -> osmDb.getTimestamp(key)
            is OsmNoteQuestKey -> notesDb.getTimestamp(key.noteId)
        }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val unhidCount: Int
        synchronized(this) {
            unhidCount = osmDb.deleteAll() + notesDb.deleteAll()
            cache.clear()
        }
        listeners.forEach { it.onUnhidAll() }
        return unhidCount
    }

    override fun get(key: QuestKey): Long? =
        synchronized(this) { cache[key] }

    override fun getAllNewerThan(timestamp: Long): List<Pair<QuestKey, Long>> {
        val pairs = synchronized(this) { cache.toList() }
        return pairs.filter { it.second > timestamp }.sortedByDescending { it.second }
    }

    override fun countAll(): Int =
        synchronized(this) { cache.size }

    override fun addListener(listener: QuestsHiddenSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: QuestsHiddenSource.Listener) {
        listeners.remove(listener)
    }
}
