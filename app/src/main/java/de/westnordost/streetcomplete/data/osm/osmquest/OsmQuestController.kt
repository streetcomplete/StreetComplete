package de.westnordost.streetcomplete.data.osm.osmquest


import android.util.Log
import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.countryboundaries.intersects
import de.westnordost.countryboundaries.isInAny
import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.OsmElementSource
import de.westnordost.streetcomplete.data.osmnotes.NoteSource
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.ktx.format
import de.westnordost.streetcomplete.util.contains
import de.westnordost.streetcomplete.util.enlargedBy
import de.westnordost.streetcomplete.util.measuredLength
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.FutureTask
import javax.inject.Inject
import javax.inject.Singleton

// TODO hiddenDB shoudl prevent quest creation? Or maybe blacklistedElementDb etc should just prevent them from being idsplayed?

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects and notifying
 *  listeners about changes */
@Singleton class OsmQuestController @Inject internal constructor(
    private val db: OsmQuestDao,
    private val hiddenDB: OsmQuestsHiddenDao,
    private val osmElementSource: OsmElementSource,
    private val notesSource: NoteSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>
): OsmQuestSource {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    private val listeners: MutableList<OsmQuestSource.Listener> = CopyOnWriteArrayList()

    private val questTypes get() = questTypeRegistry.all.filterIsInstance<OsmElementQuestType<*>>()

    private val osmElementSourceListener = object : OsmElementSource.Listener {

        /** For the given elements, replace the current quests with the given ones. Called when
         *  OSM elements are updated, so the quests that reference that element need to be updated
         *  as well. */
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            val addedQuests = mutableListOf<OsmQuest>()
            val obsoleteQuestIds = mutableListOf<Long>()

            for (element in updated) {
                val quests = createQuestsForElement(element, updated)

                val previousQuestsByType = mutableMapOf<OsmElementQuestType<*>, OsmQuest>()
                db.getAllForElement(element.type, element.id)
                    .associateByTo(previousQuestsByType) { it.osmElementQuestType }

                for (quest in quests) {
                    if (previousQuestsByType.containsKey(quest.type)) {
                        previousQuestsByType.remove(quest.type)
                    } else {
                        addedQuests.add(quest)
                    }
                }
                // quests that were created previously for an element but now not anymore shall be deleted
                obsoleteQuestIds.addAll(previousQuestsByType.values.mapNotNull { it.id })
            }
            for (key in deleted) {
                // quests that refer to elements that have been deleted shall be deleted
                obsoleteQuestIds.addAll(db.getAllForElement(key.elementType, key.elementId).mapNotNull { it.id })
            }

            updateQuests(addedQuests, obsoleteQuestIds)
        }

        /** Replace all quests of the given types in the given bounding box with the given quests.
         *  Called on download of a quest type for a bounding box. */
        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            val quests = createQuestsForBBox(bbox, mapDataWithGeometry)

            val previousQuestsByKey = mutableMapOf<OsmQuestKey, OsmQuest>()
            db.getAllInBBox(bbox).associateByTo(previousQuestsByKey) { it.key }

            val addedQuests = mutableListOf<OsmQuest>()
            for (quest in quests) {
                val questKey = quest.key
                if (previousQuestsByKey.containsKey(questKey)) {
                    previousQuestsByKey.remove(quest.key)
                } else {
                    addedQuests.add(quest)
                }
            }
            val obsoleteQuestIds = previousQuestsByKey.values.mapNotNull { it.id }

            updateQuests(addedQuests, obsoleteQuestIds)
        }
    }

    init {
        osmElementSource.addListener(osmElementSourceListener)
    }

    private fun createQuestsForBBox(
        bbox: BoundingBox,
        mapDataWithGeometry: MapDataWithGeometry
    ): Collection<OsmQuest> {
        val time = System.currentTimeMillis()

        val quests = ConcurrentLinkedQueue<OsmQuest>()
        val truncatedBlacklistedPositions = notesSource
            .getAllPositions(bbox)
            .map { it.truncateTo5Decimals() }
            .toSet()
        val countryBoundaries = countryBoundariesFuture.get()

        runBlocking {
            for (questType in questTypes) {
                launch(Dispatchers.Default) {
                    val questTypeName = questType.javaClass.simpleName
                    if (!countryBoundaries.intersects(bbox, questType.enabledInCountries)) {
                        Log.d(TAG, "$questTypeName: Skipped because it is disabled for this country")
                    } else {
                        val questTime = System.currentTimeMillis()
                        for (element in questType.getApplicableElements(mapDataWithGeometry)) {
                            val geometry = mapDataWithGeometry.getGeometry(element.type, element.id)
                            if (!mayCreateQuest(questType, element, geometry, truncatedBlacklistedPositions, bbox)) continue
                            quests.add(OsmQuest(null, questType, element.type, element.id, geometry!!))
                        }

                        val questSeconds = System.currentTimeMillis() - questTime
                        Log.d(TAG, "$questTypeName: Found ${quests.size} quests in ${questSeconds}ms")
                    }
                }
            }
        }
        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Created ${quests.size} quests for bbox in ${secondsSpentAnalyzing}s")

        return quests
    }

    private fun createQuestsForElement(
        element: Element,
        mapDataWithGeometry: MapDataWithGeometry
    ): Collection<OsmQuest> {
        val time = System.currentTimeMillis()

        val geometry = mapDataWithGeometry.getGeometry(element.type, element.id) ?: return emptyList()
        val paddedBounds = geometry.getBounds().enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
        val lazyMapData by lazy { osmElementSource.getMapDataWithGeometry(paddedBounds) }

        val quests = ConcurrentLinkedQueue<OsmQuest>()
        val truncatedBlacklistedPositions = notesSource
            .getAllPositions(paddedBounds)
            .map { it.truncateTo5Decimals() }
            .toSet()

        runBlocking {
            for (questType in questTypes) {
                launch(Dispatchers.Default) {
                    val appliesToElement = questType.isApplicableTo(element)
                        ?: questType.getApplicableElements(lazyMapData).contains(element)

                    if (appliesToElement && mayCreateQuest(questType, element, geometry, truncatedBlacklistedPositions, null)) {
                        quests.add(OsmQuest(null, questType, element.type, element.id, geometry))
                    }
                }
            }
        }
        val secondsSpentAnalyzing = (System.currentTimeMillis() - time) / 1000
        Log.i(TAG,"Created ${quests.size} quests for ${element.type.name}#${element.id} in ${secondsSpentAnalyzing}s")

        return quests
    }

    private fun updateQuests(added: Collection<OsmQuest>, deletedIds: Collection<Long>) {
        val time = System.currentTimeMillis()

        val deletedCount = db.deleteAll(deletedIds)
        val addedCount = db.addAll(added)
        val reallyAddedQuests = added.filter { it.id != null }

        val secondsSpentPersisting = (System.currentTimeMillis() - time) / 1000.0
        Log.i(TAG,"Added $addedCount new and removed $deletedCount already resolved quests in ${secondsSpentPersisting.format(1)}s")

        // TODO hidden quests sind außen vor1
        onUpdated(added = reallyAddedQuests, deletedIds = deletedIds)
    }

    private fun mayCreateQuest(
        questType: OsmElementQuestType<*>,
        element: Element,
        geometry: ElementGeometry?,
        blacklistedPositions: Set<LatLon>,
        downloadedBoundingBox: BoundingBox?
    ): Boolean {
        // invalid geometry -> can't show this quest, so skip it
        val pos = geometry?.center ?: return false

        // outside downloaded area: skip
        if (downloadedBoundingBox != null && !downloadedBoundingBox.contains(pos)) return false

        // do not create quests whose marker is at/near a blacklisted position
        if (blacklistedPositions.contains(pos.truncateTo5Decimals()))  return false

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

    /** Mark the quest as hidden by user interaction */
    fun hide(quest: OsmQuest) {
        val questId = quest.id ?: return
        hiddenDB.add(quest.key)
        onUpdated(deletedIds = listOf(questId))
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val previouslyHiddenQuestKeys = hiddenDB.getAll()
        val result = hiddenDB.deleteAll()
        val unhiddenQuests = previouslyHiddenQuestKeys.mapNotNull { db.get(it) }
        onUpdated(added = unhiddenQuests)
        return result
    }

    override fun get(questId: Long): OsmQuest? {
        val q = db.get(questId) ?: return null
        if (hiddenDB.contains(q.key)) return null
        return q
    }

    override fun getAllInBBoxCount(bbox: BoundingBox): Int = db.getAllInBBoxCount(bbox)

    override fun getAllVisibleInBBox(
        bbox: BoundingBox,
        questTypes: Collection<String>?
    ): List<OsmQuest> {
        val quests = mutableMapOf<OsmQuestKey, OsmQuest>()
        db.getAllInBBox(bbox, questTypes).associateByTo(quests) { it.key }
        val hiddenQuestKeys = hiddenDB.getAll().toSet()
        return quests.filterKeys { it !in hiddenQuestKeys }.values.toList()
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
        listeners.forEach { it.onUpdated(added, deletedIds) }
    }

    companion object {
        private const val TAG = "OsmQuestController"
    }
}

private val OsmQuest.key get() =
    OsmQuestKey(elementType, elementId, osmElementQuestType.javaClass.simpleName)

// the resulting precision is about ~1 meter (see #1089)
private fun LatLon.truncateTo5Decimals() = OsmLatLon(latitude.truncateTo5Decimals(), longitude.truncateTo5Decimals())

private fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5

const val MAX_GEOMETRY_LENGTH_IN_METERS = 600
