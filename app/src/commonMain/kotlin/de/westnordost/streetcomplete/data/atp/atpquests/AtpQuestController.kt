package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.atp.AtpEntry
import de.westnordost.streetcomplete.data.atp.ReportType
import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.distance
import de.westnordost.streetcomplete.util.math.enlargedBy

/** Used to get visible atp quests */
class AtpQuestController(
    private val mapDataSource: MapDataWithEditsSource,
    private val atpDataSource: AtpDataWithEditsSource, // TODO what exactly should be feed here?
    private val noteSource: NotesWithEditsSource, // Do I need it to suppress quests? Probably no, TODO
    private val questTypeRegistry: QuestTypeRegistry,
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

    fun isThereOsmAtpMatch(osm: Map<String, String>, atp: Map<String, String>, osmIdentifier: ElementKey, atpPosition: LatLon): Boolean {
        fun isItWithinRange(osmIdentifier: ElementKey, atpPosition: LatLon): Boolean {
            // TODO what about large shops where element may be within it but distance to center is still great?
            // TODO is there some already implemented or reasonable to implement function?
            // should it be ignored?
            val distance = mapDataSource.getGeometry(osmIdentifier.type, osmIdentifier.id)?.distance(atpPosition)
            return distance != null && distance < ApplicationConstants.ATP_QUEST_FILTER_PADDING
        }

        val atpNames = listOfNotNull(atp["name"]?.lowercase(), atp["brand"]?.lowercase())
        if (atpNames.contains(osm["name"]?.lowercase()) || atpNames.contains(osm["brand"]?.lowercase())) {
            return isItWithinRange(osmIdentifier, atpPosition)
        }
        // yes, with following any shop=convenience will block any shop=convenience
        // within range
        // this is extreme and making filter smarter may be an improvement
        listOf("shop", "amenity", "leisure", "office", "tourism", "craft", "healthcare", "attraction").forEach { mainKey ->
            if(atp[mainKey] != null && atp[mainKey] == osm[mainKey]) {
                return isItWithinRange(osmIdentifier, atpPosition)
            }
        }
        return false
    }

    private val atpUpdatesListener = object : AtpDataWithEditsSource.Listener {
        override fun onUpdatedAtpElement(added: Collection<AtpEntry>, deleted: Collection<Long>) {
            // handle deletion somehow? TODO
            // probably do the same as class OsmQuestController did? TODO (in private val notesSourceListener = object : NotesWithEditsSource.Listener )
            // actually test passing deleted ids TODO
            // todo actually have ids to be passed
            // todo check whether ATP ids are actually unique
            // https://github.com/alltheplaces/alltheplaces/blob/master/DATA_FORMAT.md
            // TODO: I guess my API may ensure uniqueness? But it is not integer, it is text it seems
            // use some hash function to convert string into longs?

            val filtered = added.filter { atpEntry ->
                // TODO is speed of this reasonable? I suspect that something more efficient is needed, profile
                val paddedBounds = BoundingBox(atpEntry.position, atpEntry.position) //..enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
                mapDataSource.getMapDataWithGeometry(paddedBounds).none { osm ->
                    isThereOsmAtpMatch(osm.tags, atpEntry.tagsInATP, ElementKey(osm.type, osm.id), atpEntry.position) // true
                }
            }
            val quests = createQuestsForAtpEntries(filtered)
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
            val deletedQuestIds = mutableListOf<Long>()
            updated.forEach { osm ->
                // TODO STUCK how can I get access to existing quests here?
                // do I really need to do atpDataSource.getAll()
                // and then filter droppable quests
                // and then pass ids to obsoleteQuestIds so they will be deleted
                // this seems silly
                // but it seems how note and osm element quests do things
                // so maybe there is no better way?

                val geometry = mapDataSource.getGeometry(osm.type, osm.id)
                if (geometry == null) {
                    // TODO: in which cases it may happen? If it happens then we cannot do anything about it anyway
                    // should we crash? log? If log, then with something better
                    Log.e(TAG, "why, why mapDataSource.getGeometry got me null?")
                } else {
                    val paddedBounds = geometry.bounds.enlargedBy(
                        ApplicationConstants.QUEST_FILTER_PADDING
                    )
                    val candidates = atpDataSource.getAll(paddedBounds)
                    // TODO: profile it whether it is too slow
                    candidates.forEach { atpCandidate ->
                        if(isThereOsmAtpMatch(osm.tags, atpCandidate.tagsInATP, ElementKey(osm.type, osm.id), atpCandidate.position)) {
                            deletedQuestIds.add(atpCandidate.id)
                            // TODO: what if this ATP entries were ineligible for quest already? and there was no quest?
                            // TODO: would listing them here again would harm anything?
                        }
                    }
                }
            }

            // in theory changing name or retagging shop may cause new quests to appear - lets not support this
            // as most cases will ve false positives anyway and this would be expensive to check
            // instead pass emptyList<CreateElementQuest>()
            onUpdatingQuestList(emptyList<CreateElementQuest>(), deletedQuestIds)
        }

        override fun onReplacedForBBox(
            bbox: BoundingBox,
            mapDataWithGeometry: MapDataWithGeometry,
        ) {
            val paddedBounds = bbox.enlargedBy(ApplicationConstants.ATP_QUEST_FILTER_PADDING)
            val obsoleteQuestIds = mutableListOf<Long>()
            val candidates = atpDataSource.getAll(paddedBounds)
            mapDataWithGeometry.forEach { osm ->
                candidates.forEach { atpCandidate ->
                    if(isThereOsmAtpMatch(osm.tags, atpCandidate.tagsInATP, ElementKey(osm.type, osm.id), atpCandidate.position)) {
                        obsoleteQuestIds.add(atpCandidate.id)
                        // TODO: what if this ATP entries were ineligible for quest already? and there was no quest?
                        // TODO: would listing them here again would harm anything?
                    }
                }
            }
            // TODO maybe quests outside downloaded area should not appear until OSM data is also downloaded to hide unwanted copies?
            onUpdatingQuestList(emptyList<CreateElementQuest>(), obsoleteQuestIds)
        }

        override fun onCleared() {
            //TODO("Not yet implemented")
        }
    }

    init {
        atpDataSource.addListener(atpUpdatesListener)
        noteSource.addListener(noteUpdatesListener)
        mapDataSource.addListener(mapDataSourceListener)
    }

    override fun get(questId: Long): CreateElementQuest? =
        atpDataSource.get(questId)?.let { createQuestForAtpEntry(it) }

    override fun getAllInBBox(bbox: BoundingBox): List<CreateElementQuest> {
        val candidates = atpDataSource.getAll(bbox)
        val paddedBounds = bbox.enlargedBy(ApplicationConstants.ATP_QUEST_FILTER_PADDING)
        val filteredOutCandidates = mutableListOf<AtpEntry>()
        mapDataSource.getMapDataWithGeometry(paddedBounds).forEach { osm ->
            candidates.forEach { atpCandidate ->
                if(!filteredOutCandidates.contains(atpCandidate)) {
                    if(isThereOsmAtpMatch(osm.tags, atpCandidate.tagsInATP, ElementKey(osm.type, osm.id), atpCandidate.position)) {
                        filteredOutCandidates.add(atpCandidate)
                    }
                }
            }
        }
        val filteredCandidates = candidates - filteredOutCandidates
        return createQuestsForAtpEntries(filteredCandidates)
    }

    private fun createQuestsForAtpEntries(entries: Collection<AtpEntry>): List<CreateElementQuest> =
        entries.mapNotNull { createQuestForAtpEntry(it) }

    private fun createQuestForAtpEntry(entry: AtpEntry): CreateElementQuest? {
        return if (entry.reportType == ReportType.MISSING_POI_IN_OPENSTREETMAP) {
            // TODO STUCK allQuestTypes[0] is a hilarious hack of worst variety TODO (in other places I just assume single
            // TODO STUCK maybe CreatePoiBasedOnAtp() and OsmCreateElementQuestType() should be merged?
            // TODO STUCK but simple
            //  type = CreatePoiBasedOnAtp()
            // TODO does not work
            // TODO specifically, import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtp
            // TODO fails as supposedly .atp. does not exist
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

    companion object {
        private const val TAG = "AtpQuestController"
    }
}
