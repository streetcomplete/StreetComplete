package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.Listeners

/** Used to get visible osm note quests */
class AtpQuestController(
    // should it exist as separate controller? or mayb OsmQuestController should get access to ATP data? TODO
    private val mapDataSource: MapDataWithEditsSource, // TODO use it to skip cases where shop was created in meantime
    private val atpDataSource: AtpDataWithEditsSource, // TODO what exactly should be feed here?
    private val noteSource: NotesWithEditsSource, // Do I need it to suppress quests? Probably yes, TODO
    private val questTypeRegistry: QuestTypeRegistry,
    //TODO? is it needed? private val prefs: Preferences,
) : AtpQuestSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<AtpQuestSource.Listener>()

    private val allQuestTypes get() = questTypeRegistry.filterIsInstance<OsmCreateElementQuestType<*>>()

    private val noteUpdatesListener = object : NotesWithEditsSource.Listener {
        override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            // probably do the same as class OsmQuestController did? TODO
        }

        override fun onCleared() {
            // probably do the same as class OsmQuestController did? TODO
            listeners.forEach { it.onInvalidated() }
        }
    }

    private val atpUpdatesListener = object : AtpDataWithEditsSource.Listener {
        override fun onUpdated(added: Collection<AtpEntry>, deleted: Collection<Long>) {
            // handle deletion somehow? TODO
            // probably do the same as class OsmQuestController did? TODO (in private val notesSourceListener = object : NotesWithEditsSource.Listener )
            val quests = createQuestsForAtpEntries(added)
            onUpdated(quests, deleted)
        }

        override fun onInvalidated() {
            // probably do the same as class OsmQuestController did? TODO
            listeners.forEach { it.onInvalidated() }
        }
    }

    private val mapDataSourceListener = object : MapDataWithEditsSource.Listener {
        // matches appear/disappear
        override fun onUpdated(
            updated: MapDataWithGeometry,
            deleted: Collection<ElementKey>,
        ) {
            //TODO("mapDataSourceListener - Not yet implemented - I guess that here POI creation/edit should be watched as it may cause")
        }

        override fun onReplacedForBBox(
            bbox: BoundingBox,
            mapDataWithGeometry: MapDataWithGeometry,
        ) {
            //TODO("Not yet implemented")
        }

        override fun onCleared() {
            //TODO("Not yet implemented")
        }
    }

    init {
        atpDataSource.addListener(atpUpdatesListener) // TODO should I monitor AtpQuestController or AtpDataWithEditsSource
        noteSource.addListener(noteUpdatesListener)
        mapDataSource.addListener(mapDataSourceListener)
        // settingsListener = prefs.onAllShowNotesChanged { onInvalidated() }
    }

    override fun get(questId: Long): CreateElementQuest? =
        atpDataSource.get(questId)?.let { createQuestForAtpEntry(it) }

    override fun getAllInBBox(bbox: BoundingBox): List<CreateElementQuest> =
        createQuestsForAtpEntries(atpDataSource.getAll(bbox))

    private fun createQuestsForAtpEntries(entries: Collection<AtpEntry>): List<CreateElementQuest> =
        entries.mapNotNull { createQuestForAtpEntry(it) }

    private fun createQuestForAtpEntry(entry: AtpEntry): CreateElementQuest? {
        // TODO: check should it be created first
        // allQuestTypes[0] is a hilarious hack of worst variety TODO
        return CreateElementQuest(entry.id, allQuestTypes[0], entry.position)
    }

    /* ---------------------------------------- Listener ---------------------------------------- */

    override fun addListener(listener: AtpQuestSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AtpQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        quests: Collection<CreateElementQuest>,
        deletedQuestIds: Collection<Long>
    ) {
        if (quests.isEmpty() && deletedQuestIds.isEmpty()) return
        listeners.forEach { it.onUpdated(quests, deletedQuestIds) }
    }

    private fun onInvalidated() {
        listeners.forEach { it.onInvalidated() }
    }
}
