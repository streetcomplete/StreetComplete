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
    private val questTypeRegistry: QuestTypeRegistry,
) : AtpQuestSource {
    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners = Listeners<AtpQuestSource.Listener>()

    private val allQuestTypes get() = questTypeRegistry.filterIsInstance<OsmCreateElementQuestType<*>>()

    fun isThereOsmAtpMatch(osm: Map<String, String>, atp: Map<String, String>, osmIdentifier: ElementKey, atpPosition: LatLon): Boolean {
        fun isItWithinRange(osmIdentifier: ElementKey, atpPosition: LatLon): Boolean {
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
        override fun onUpdatedAtpElements(added: Collection<AtpEntry>, deleted: Collection<Long>) {
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
            // probably do the same as class OsmQuestController did? TODO LATER - maybe not needed at all? If used, add test, if not used at the end - purge
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

                if (!osm.tags.isEmpty()) { // TODO maybe both incoming ATP entries and OSM entries should be filtered? Maybe require it to be either place or thing to avoid maintaining even more filters? To check only places, not every tagged node?
                    val geometry = mapDataSource.getGeometry(osm.type, osm.id)
                    if (geometry != null) {
                        val paddedBounds = geometry.bounds.enlargedBy(
                            ApplicationConstants.QUEST_FILTER_PADDING
                        )
                        val candidates = atpDataSource.getAll(paddedBounds)
                        // TODO: profile it whether it is too slow
                        candidates.forEach { atpCandidate ->
                            if(isThereOsmAtpMatch(osm.tags, atpCandidate.tagsInATP, ElementKey(osm.type, osm.id), atpCandidate.position)) {
                                deletedQuestIds.add(atpCandidate.id)
                                // ATP entries already ineligible for quest will be also listed
                                // this is fine
                            }
                        }
                    }
                }
            }

            // in theory changing name or retagging shop may cause new quests to appear - lets not support this
            // as most cases will be false positives anyway and this would be expensive to check
            // instead pass emptyList<CreateElementQuest>()
            onUpdatingQuestList(emptyList<CreateElementUsingAtpQuest>(), deletedQuestIds)
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
                        // ATP entries already ineligible for quest will be also listed
                        // this is fine
                    }
                }
            }
            // TODO maybe quests outside downloaded area should not appear until OSM data is also downloaded to hide unwanted copies?
            onUpdatingQuestList(emptyList<CreateElementUsingAtpQuest>(), obsoleteQuestIds)
        }

        override fun onCleared() {
            listeners.forEach { it.onInvalidated() }
        }
    }

    init {
        atpDataSource.addListener(atpUpdatesListener)
        mapDataSource.addListener(mapDataSourceListener)
    }

    override fun get(questId: Long): CreateElementUsingAtpQuest? =
        atpDataSource.get(questId)?.let { createQuestForAtpEntry(it) }

    override fun getAllInBBox(bbox: BoundingBox): List<CreateElementUsingAtpQuest> {
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

    private fun createQuestsForAtpEntries(entries: Collection<AtpEntry>): List<CreateElementUsingAtpQuest> =
        entries.mapNotNull { createQuestForAtpEntry(it) }

    private fun createQuestForAtpEntry(entry: AtpEntry): CreateElementUsingAtpQuest? {
        return if (entry.reportType == ReportType.MISSING_POI_IN_OPENSTREETMAP) {
            // TODO STUCK allQuestTypes[0] is a hilarious hack of worst variety TODO (in other places I just assume single
            // TODO STUCK maybe CreatePoiBasedOnAtp() and OsmCreateElementQuestType() should be merged?
            // TODO STUCK but simple
            // type = CreatePoiBasedOnAtp()
            // does not work
            // specifically, import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtp
            // fails as supposedly .atp. does not exist
            // TODO STUCK maybe because it is stuck in Android part of source code?
            CreateElementUsingAtpQuest(entry.id, entry,allQuestTypes[0], entry.position)
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
        quests: Collection<CreateElementUsingAtpQuest>,
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
