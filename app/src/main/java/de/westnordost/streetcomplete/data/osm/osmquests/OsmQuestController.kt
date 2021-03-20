package de.westnordost.streetcomplete.data.osm.osmquests

import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.countryboundaries.isInAny
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.Note
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.format
import de.westnordost.streetcomplete.util.contains
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import de.westnordost.streetcomplete.util.enlargedBy
import de.westnordost.streetcomplete.util.measuredLength
import kotlinx.coroutines.*
import java.lang.System.currentTimeMillis
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects and notifying
 *  listeners about changes */
@Singleton class OsmQuestController @Inject internal constructor(
    private val db: OsmQuestDao,
    private val hiddenDB: OsmQuestsHiddenDao,
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>
): OsmQuestSource {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners: MutableList<OsmQuestSource.Listener> = CopyOnWriteArrayList()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val allQuestTypes get() = questTypeRegistry.all.filterIsInstance<OsmElementQuestType<*>>()

    private val mapDataSourceListener = object : MapDataWithEditsSource.Listener {

        /** For the given elements, replace the current quests with the given ones. Called when
         *  OSM elements are updated, so the quests that reference that element need to be updated
         *  as well. */
        @Synchronized override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            val time = currentTimeMillis()

            val deferredQuests = mutableListOf<Deferred<OsmQuest?>>()
            val previousQuests = mutableListOf<OsmQuestDaoEntry>()
            val hiddenQuests = getHiddenQuests()
            var count = 0

            for (element in updated) {
                previousQuests.addAll(db.getAllForElement(element.type, element.id))
                val geometry = updated.getGeometry(element.type, element.id) ?: continue
                deferredQuests.addAll(createQuestsForElementDeferred(element, geometry, allQuestTypes, hiddenQuests))
                count++
            }

            val deleteQuestIds = mutableListOf<Long>()
            for (key in deleted) {
                // quests that refer to elements that have been deleted shall be deleted
                deleteQuestIds.addAll(db.getAllForElement(key.type, key.id).mapNotNull { it.id })
            }

            val quests = runBlocking { deferredQuests.awaitAll().filterNotNull() }

            for (quest in quests) {
                Log.d(TAG, "Created ${quest::class.simpleName!!} for ${quest.elementType.name}#${quest.elementId}")
            }
            val seconds = (currentTimeMillis() - time) / 1000.0
            Log.i(TAG,"Created ${quests.size} quests for $count updated elements in ${seconds.format(1)}s")

            updateQuests(quests, previousQuests, deleteQuestIds)
        }

        /** Replace all quests of the given types in the given bounding box with the given quests.
         *  Called on download of a quest type for a bounding box. */
        @Synchronized override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            val quests = createQuestsForBBox(bbox, mapDataWithGeometry, allQuestTypes)
            val previousQuests = db.getAllInBBox(bbox)
            updateQuests(quests, previousQuests, emptyList())
        }
    }

    private val notesSourceListener = object : NotesWithEditsSource.Listener {
        @Synchronized override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            /* NOTE deleted notes are not taken into account because we just get the ID here which
               is useless for us, we'd need the note position. So if the quests should be re-created
               without a new download after closing a note, the listener would need to pass
               the actual deleted note. Currently it is not possible to close notes anyway, so who
               cares
             */

            val addedNotePositions = added.map { it.position }
            val questIdsAtNotes = mutableSetOf<Long>()
            for (pos in addedNotePositions) {
                questIdsAtNotes.addAll(db.getAllIdsInBBox(pos.enclosingBoundingBox(1.0)))
            }

            db.deleteAll(questIdsAtNotes)
            onUpdated(deletedIds = questIdsAtNotes)
        }
    }

    init {
        mapDataSource.addListener(mapDataSourceListener)
        notesSource.addListener(notesSourceListener)
    }

    private fun createQuestsForBBox(
        bbox: BoundingBox,
        mapDataWithGeometry: MapDataWithGeometry,
        questTypes: Collection<OsmElementQuestType<*>>,
    ): Collection<OsmQuest> {
        val time = currentTimeMillis()

        val blacklistedPositions = getBlacklistedPositions(bbox)
        val countryBoundaries = countryBoundariesFuture.get()
        val hiddenQuests = getHiddenQuests()

        val deferredQuests: List<Deferred<List<OsmQuest>>> = questTypes.map { questType ->
            scope.async {
                val questsForType = ArrayList<OsmQuest>()
                val questTypeName = questType::class.simpleName!!
                if (!countryBoundaries.intersects(bbox, questType.enabledInCountries)) {
                    Log.d(TAG, "$questTypeName: Skipped because it is disabled for this country")
                    emptyList()
                } else {
                    val questTime = currentTimeMillis()
                    var questCount = 0
                    for (element in questType.getApplicableElements(mapDataWithGeometry)) {
                        val geometry = mapDataWithGeometry.getGeometry(element.type, element.id)
                            ?: continue
                        if (!mayCreateQuest(questType, element, geometry, blacklistedPositions, hiddenQuests, bbox)) continue
                        questsForType.add(OsmQuest(null, questType, element.type, element.id, geometry))
                        questCount++
                    }

                    val questSeconds = currentTimeMillis() - questTime
                    Log.d(TAG, "$questTypeName: Found $questCount quests in ${questSeconds}ms")
                    questsForType
                }
            }
        }
        val quests = runBlocking { deferredQuests.awaitAll().flatten() }

        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG,"Created ${quests.size} quests for bbox in ${seconds.format(1)}s")

        return quests
    }

    private fun createQuestsForElementDeferred(
        element: Element,
        geometry: ElementGeometry,
        questTypes: Collection<OsmElementQuestType<*>>,
        hiddenQuests: Set<OsmQuestKey>
    ): List<Deferred<OsmQuest?>> {
        val paddedBounds = geometry.getBounds().enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
        val lazyMapData by lazy { mapDataSource.getMapDataWithGeometry(paddedBounds) }
        val blacklistedPositions = getBlacklistedPositions(paddedBounds)

        return questTypes.map { questType ->
            scope.async {
                val appliesToElement = questType.isApplicableTo(element)
                    ?: questType.getApplicableElements(lazyMapData).any { it.id == element.id && it.type == element.type }

                if (appliesToElement && mayCreateQuest(questType, element, geometry, blacklistedPositions, hiddenQuests, null)) {
                    OsmQuest(null, questType, element.type, element.id, geometry)
                } else {
                    null
                }
            }
        }
    }

    private fun createQuestsForQuestKeys(keys: Collection<OsmQuestKey>): List<OsmQuest> {
        val questTypesByElement = mutableMapOf<Element, MutableList<OsmElementQuestType<*>>>()
        for (key in keys) {
            val element = mapDataSource.get(key.elementType, key.elementId) ?: continue
            val questType = questTypeRegistry.getByName(key.questTypeName) as? OsmElementQuestType<*> ?: continue
            questTypesByElement.getOrPut(element) { mutableListOf() }.add(questType)
        }

        val hiddenQuests = getHiddenQuests()
        val deferredQuests = mutableListOf<Deferred<OsmQuest?>>()
        for ((element, questTypes) in questTypesByElement) {
            val geometry = mapDataSource.getGeometry(element.type, element.id) ?: continue
            deferredQuests.addAll(createQuestsForElementDeferred(element, geometry, questTypes, hiddenQuests))
        }

        val time = currentTimeMillis()
        val quests = runBlocking { deferredQuests.awaitAll().filterNotNull() }
        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG, "Created ${quests.size} quests for ${keys.size} updated quests in ${seconds.format(1)}s")

        return quests
    }

    private fun updateQuests(
        questsNow: Collection<OsmQuest>,
        questsPreviously: Collection<OsmQuestDaoEntry>,
        deletedQuestIds: Collection<Long>
    ) {

        val time = currentTimeMillis()

        val previousQuestsByKey = mutableMapOf<OsmQuestKey, OsmQuestDaoEntry>()
        questsPreviously.associateByTo(previousQuestsByKey) { it.key }

        val addedQuests = mutableListOf<OsmQuest>()
        for (quest in questsNow) {
            val questKey = quest.key
            if (previousQuestsByKey.containsKey(questKey)) {
                previousQuestsByKey.remove(questKey)
            } else {
                addedQuests.add(quest)
            }
        }
        // quests that were created previously for an element but now not anymore shall be deleted
        val obsoleteQuestIds = previousQuestsByKey.values.mapNotNull { it.id } + deletedQuestIds

        val deletedCount = db.deleteAll(obsoleteQuestIds)
        val addedCount = db.addAll(addedQuests)

        val seconds = (currentTimeMillis() - time) / 1000.0
        Log.i(TAG,"Persisted $addedCount new and removed $deletedCount already resolved quests in ${seconds.format(1)}s")

        val reallyAddedQuests = addedQuests.filter { it.id != null }
        onUpdated(added = reallyAddedQuests, deletedIds = obsoleteQuestIds)
    }

    private fun mayCreateQuest(
        questType: OsmElementQuestType<*>,
        element: Element,
        geometry: ElementGeometry,
        blacklistedPositions: Set<LatLon>,
        blacklistedQuests: Set<OsmQuestKey>,
        downloadedBoundingBox: BoundingBox?
    ): Boolean {
        val pos = geometry.center

        // outside downloaded area: skip
        if (downloadedBoundingBox != null && !downloadedBoundingBox.contains(pos)) return false

        val questKey = OsmQuestKey(element.type, element.id, questType::class.simpleName!!)
        if (blacklistedQuests.contains(questKey)) return false

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals())) return false

        // do not create quests in countries where the quest is not activated
        val countries = questType.enabledInCountries
        if (!countryBoundariesFuture.get().isInAny(pos, countries))  return false

        // do not create quests that refer to geometry that is too long for a surveyor to be expected to survey
        if (geometry is ElementPolylinesGeometry) {
            val totalLength = geometry.polylines.sumByDouble { it.measuredLength() }
            if (totalLength > MAX_GEOMETRY_LENGTH_IN_METERS) {
                return false
            }
        }
        return true
    }

    private fun getBlacklistedPositions(bbox: BoundingBox): Set<LatLon> =
        notesSource
            .getAllPositions(bbox)
            .map { it.truncateTo5Decimals() }
            .toSet()

    private fun getHiddenQuests(): Set<OsmQuestKey> =
        hiddenDB.getAll().toSet()

    /** Mark the quest as hidden by user interaction */
    @Synchronized fun hide(quest: OsmQuest) {
        val questId = quest.id ?: return
        hiddenDB.add(quest.key)
        db.delete(questId)
        onUpdated(deletedIds = listOf(questId))
    }

    /** Un-hides all previously hidden quests by user interaction */
    @Synchronized fun unhideAll(): Int {
        val previouslyHiddenQuestKeys = hiddenDB.getAll()
        /* must delete the hidden quests BEFORE recreating the quests, otherwise they would count
           as hidden again! */
        val result = hiddenDB.deleteAll()

        val createdQuests = createQuestsForQuestKeys(previouslyHiddenQuestKeys)

        db.addAll(createdQuests)

        // some quests may already be in the database
        val reallyAddedQuests = createdQuests.filter { it.id != null }
        onUpdated(added = reallyAddedQuests)

        return result
    }

    override fun get(questId: Long): OsmQuest? {
        val entry = db.get(questId) ?: return null
        val geometry = mapDataSource.getGeometry(entry.elementType, entry.elementId)
        return createOsmQuest(entry, geometry)
    }

    override fun getAllInBBoxCount(bbox: BoundingBox): Int =
        db.getAllInBBoxCount(bbox)

    override fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>?): List<OsmQuest> {
        val entries = db.getAllInBBox(bbox, questTypes)

        val elementKeys = HashSet<ElementKey>()
        entries.mapTo(elementKeys) { ElementKey(it.elementType, it.elementId) }

        val geometriesByKey = mapDataSource.getGeometries(elementKeys)
            .associateBy { ElementKey(it.elementType, it.elementId) }

        return entries.mapNotNull { entry ->
            val geometryEntry = geometriesByKey[ElementKey(entry.elementType, entry.elementId)]
            createOsmQuest(entry, geometryEntry?.geometry)
        }
    }

    private fun createOsmQuest(entry: OsmQuestDaoEntry, geometry: ElementGeometry?): OsmQuest? {
        if (geometry == null) return null
        val questType = questTypeRegistry.getByName(entry.questTypeName) as? OsmElementQuestType<*> ?: return null
        return OsmQuest(entry.id, questType, entry.elementType, entry.elementId, geometry)
    }

    override fun addListener(listener: OsmQuestSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: OsmQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        added: Collection<OsmQuest> = emptyList(),
        deletedIds: Collection<Long> = emptyList()
    ) {
        if (added.isEmpty() && deletedIds.isEmpty()) return

        listeners.forEach { it.onUpdated(added, deletedIds) }
    }

    companion object {
        private const val TAG = "OsmQuestController"
    }
}

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

const val MAX_GEOMETRY_LENGTH_IN_METERS = 600
