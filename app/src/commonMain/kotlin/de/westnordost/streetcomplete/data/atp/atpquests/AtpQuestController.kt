package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.ReportType
import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestDao
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.Listeners

/** Used to get visible osm note quests */
class AtpQuestController(
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

    fun isThereOsmAtpMatch(osm: Map<String, String>, atp: Map<String, String>): Boolean {
        val atpNames = listOfNotNull(atp["name"]?.lowercase(), atp["brand"]?.lowercase())
        if (atpNames.contains(osm["name"]?.lowercase()) || atpNames.contains(osm["brand"]?.lowercase())) {
            return true
        }
        //yes, with following any shop=convenience will block any shop=convenience
        //within range
        // this is extreme and tweaking is beneficial
        listOf("shop", "amenity", "leisure").forEach { mainKey ->
            if(atp[mainKey] != null && atp[mainKey] == osm[mainKey]) {
                return true
            }
        }
        return false
    }

    private val atpUpdatesListener = object : AtpDataWithEditsSource.Listener {
        override fun onUpdatedAtpElement(added: Collection<AtpEntry>, deleted: Collection<Long>) {
            // handle deletion somehow? TODO
            // probably do the same as class OsmQuestController did? TODO (in private val notesSourceListener = object : NotesWithEditsSource.Listener )
            // actually test passing deleted ids TODO
            // todo actually hae ids to be passed
            // todo check whether ATP ids are actually unique
            // https://github.com/alltheplaces/alltheplaces/blob/master/DATA_FORMAT.md
            // TODO: I guess my API may ensure uniqueness? But it is not integer, it is text it seems

            added.filter { atpEntry ->
                // TODO is speed of this reasonable? I suspect that something more efficient is needed, profile
                val paddedBounds = BoundingBox(atpEntry.position, atpEntry.position) //..enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
                mapDataSource.getMapDataWithGeometry(paddedBounds).none { osm ->
                    isThereOsmAtpMatch(osm.tags, atpEntry.tagsInATP)
                }
            }
            val quests = createQuestsForAtpEntries(added)
            onUpdatingQuestList(quests, deleted)
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
            // TODO: if POI was added then it may obsolete some ATP quests
            // I guess that being overly careful is not a great idea, hide too many ATP quests it is fine
            //TODO("mapDataSourceListener - Not yet implemented - I guess that here POI creation/edit should be watched as it may cause")

            // which kind of synchronization is needed here TODO, see OsmQuestController
            updated.forEach { osm ->
                // TODO STUCK how can I get access to existing quests here?
                // do I really need to do atpDataSource.getAll()
                // and then pass ids to obsoleteQuestIds so they will be deleted
                // this seems silly
                // but it seems how note and osm element quests do things

                // TODO: for each element find CreateElementQuest quests in range
                // TODO: check whether it new element tag list matches quest element tag list
                // TODO: profile it whether it is too slow
            }
            val obsoleteQuestIds = listOf<Long>() // TODO obviously calculate rather than pass empty

            // in theory changing name or retagging shop may cause new quests to appear - lets not support this
            // as most cases will ve false positives anyway and this would be expensive to check
            // instead pass emptyList<CreateElementQuest>()
            onUpdatingQuestList(emptyList<CreateElementQuest>(), obsoleteQuestIds)
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
    }

    override fun get(questId: Long): CreateElementQuest? =
        atpDataSource.get(questId)?.let { createQuestForAtpEntry(it) }

    override fun getAllInBBox(bbox: BoundingBox): List<CreateElementQuest> =
        createQuestsForAtpEntries(atpDataSource.getAll(bbox))

    private fun createQuestsForAtpEntries(entries: Collection<AtpEntry>): List<CreateElementQuest> =
        entries.mapNotNull { createQuestForAtpEntry(it) }

    private fun createQuestForAtpEntry(entry: AtpEntry): CreateElementQuest? {
        return if (entry.reportType == ReportType.MISSING_POI_IN_OPENSTREETMAP) {
            // TODO STUCK allQuestTypes[0] is a hilarious hack of worst variety TODO (in other places I just assume single
            // TODO STUCK maybe CreatePoiBasedOnAtp() and OsmCreateElementQuestType() should be merged?
            // TODO STUCK but simple type = CreatePoiBasedOnAtp()
            // TODO STUCK maybe because it is stuck in Android part of source code?
            CreateElementQuest(entry.id, entry,allQuestTypes[0], entry.position)
        } else {
            null
        }
    }

    /* ---------------------------------------- Listener ---------------------------------------- */

    override fun addListener(listener: AtpQuestSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: AtpQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdatingQuestList(
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
