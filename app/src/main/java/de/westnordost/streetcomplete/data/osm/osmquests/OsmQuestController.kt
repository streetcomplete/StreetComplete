package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.osmnotes.Note
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.quests.address.AddHousenumber
import de.westnordost.streetcomplete.quests.cycleway.AddCycleway
import de.westnordost.streetcomplete.quests.existence.CheckExistence
import de.westnordost.streetcomplete.quests.max_height.AddMaxHeight
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.quests.shop_type.CheckShopExistence
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.intersects
import de.westnordost.streetcomplete.util.ktx.isInAny
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.truncateTo6Decimals
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects and notifying
 *  listeners about changes */
class OsmQuestController internal constructor(
    private val db: OsmQuestDao,
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val countryBoundaries: Lazy<CountryBoundaries>
) : OsmQuestSource {

    private val listeners = Listeners<OsmQuestSource.Listener>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val allQuestTypes get() = questTypeRegistry.filterIsInstance<OsmElementQuestType<*>>()
        .sortedBy { it.chonkerIndex }

    private val mapDataSourceListener = object : MapDataWithEditsSource.Listener {

        /** For the given elements, replace the current quests with the given ones. Called when
         *  OSM elements are updated, so the quests that reference that element need to be updated
         *  as well. */
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            val time = nowAsEpochMilliseconds()

            val deferredQuests = mutableListOf<Deferred<OsmQuest?>>()

            for (element in updated) {
                val geometry = updated.getGeometry(element.type, element.id) ?: continue
                deferredQuests.addAll(createQuestsForElementDeferred(element, geometry, allQuestTypes))
            }
            val quests = runBlocking { deferredQuests.awaitAll().filterNotNull() }

            for (quest in quests) {
                Log.d(TAG, "Created ${quest.type.name} for ${quest.elementType.name}#${quest.elementId}")
            }

            val obsoleteQuestKeys: List<OsmQuestKey>
            val visibleQuests: Collection<OsmQuest>
            synchronized(this) {
                val previousQuests = db.getAllForElements(updated.map { it.key })
                // quests that refer to elements that have been deleted shall be deleted
                val deleteQuestKeys = db.getAllForElements(deleted).map { it.key }

                val millis = nowAsEpochMilliseconds() - time
                Log.i(TAG, "Created ${quests.size} quests for ${updated.size} updated elements in ${millis}ms")

                obsoleteQuestKeys = getObsoleteQuestKeys(quests, previousQuests, deleteQuestKeys)
                updateQuests(quests, obsoleteQuestKeys)
                visibleQuests = quests.filterVisible()
            }

            onUpdated(added = visibleQuests, deleted = obsoleteQuestKeys)
        }

        /** Replace all quests of the given types in the given bounding box with the given quests.
         *  Called on download of a quest type for a bounding box. */
        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            val quests = createQuestsForBBox(bbox, mapDataWithGeometry, allQuestTypes)
            val obsoleteQuestKeys: List<OsmQuestKey>
            val visibleQuests: Collection<OsmQuest>
            synchronized(this) {
                val previousQuests = db.getAllInBBox(bbox)
                obsoleteQuestKeys = getObsoleteQuestKeys(quests, previousQuests, emptyList())
                updateQuests(quests, obsoleteQuestKeys)
                visibleQuests = quests.filterVisible()
            }

            onUpdated(added = visibleQuests, deleted = obsoleteQuestKeys)
        }

        override fun onCleared() {
            db.clear()
            listeners.forEach { it.onInvalidated() }
        }
    }

    private val notesSourceListener = object : NotesWithEditsSource.Listener {
        override fun onUpdated(added: Collection<Note>, updated: Collection<Note>, deleted: Collection<Long>) {
            onInvalidated()
        }

        override fun onCleared() {
            onInvalidated()
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
        val time = nowAsEpochMilliseconds()

        val countryBoundaries = countryBoundaries.value

        // Remove elements without tags, to be used for quests that are never applicable without
        // tags. These quests are usually OsmFilterQuestType, where questType.filter.mayEvaluateToTrueWithNoTags
        // guarantees we can skip elements without tags completely. Also those quests don't use geometry.
        // This shortcut reduces time for creating quests by ~15-30%.
        val onlyElementsWithTags = MutableMapDataWithGeometry(mapDataWithGeometry.filter { it.tags.isNotEmpty() }, emptyList())

        val deferredQuests: List<Deferred<List<OsmQuest>>> = questTypes.map { questType ->
            scope.async {
                val questsForType = ArrayList<OsmQuest>()
                val questTypeName = questType.name
                if (!countryBoundaries.intersects(bbox, questType.enabledInCountries)) {
                    Log.d(TAG, "$questTypeName: Skipped because it is disabled for this country")
                    emptyList()
                } else {
                    val questTime = nowAsEpochMilliseconds()
                    var questCount = 0
                    val mapDataToUse = if (questType is OsmFilterQuestType && !questType.filter.mayEvaluateToTrueWithNoTags) {
                        onlyElementsWithTags
                    } else {
                        mapDataWithGeometry
                    }
                    for (element in questType.getApplicableElements(mapDataToUse)) {
                        val geometry = mapDataWithGeometry.getGeometry(element.type, element.id)
                            ?: continue
                        if (!mayCreateQuest(questType, geometry, bbox)) continue
                        questsForType.add(OsmQuest(questType, element.type, element.id, geometry))
                        questCount++
                    }

                    val questSeconds = nowAsEpochMilliseconds() - questTime
                    Log.d(TAG, "$questTypeName: Found $questCount quests in ${questSeconds}ms")
                    questsForType
                }
            }
        }
        val quests = runBlocking { deferredQuests.awaitAll().flatten() }

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Created ${quests.size} quests for bbox in ${seconds.format(1)}s")

        return quests
    }

    private fun createQuestsForElementDeferred(
        element: Element,
        geometry: ElementGeometry,
        questTypes: Collection<OsmElementQuestType<*>>
    ): List<Deferred<OsmQuest?>> {
        val paddedBounds = geometry.getBounds().enlargedBy(ApplicationConstants.QUEST_FILTER_PADDING)
        val lazyMapData by lazy { mapDataSource.getMapDataWithGeometry(paddedBounds) }

        return questTypes.map { questType ->
            scope.async {
                var appliesToElement = questType.isApplicableTo(element)
                if (appliesToElement == null) {
                    Log.d(TAG, "${questType.name} requires surrounding map data to determine applicability to ${element.type.name}#${element.id}")
                    val mapData = withContext(Dispatchers.IO) { lazyMapData }
                    appliesToElement = questType.getApplicableElements(mapData)
                        .any { it.id == element.id && it.type == element.type }
                }
                if (!appliesToElement) return@async null

                if (mayCreateQuest(questType, geometry, null)) {
                    OsmQuest(questType, element.type, element.id, geometry)
                } else {
                    null
                }
            }
        }
    }

    private fun getObsoleteQuestKeys(
        questsNow: Collection<OsmQuest>,
        questsPreviously: Collection<OsmQuestDaoEntry>,
        deletedQuestKeys: Collection<OsmQuestKey>
    ): List<OsmQuestKey> {
        val previousQuestsByKey = mutableMapOf<OsmQuestKey, OsmQuestDaoEntry>()
        questsPreviously.associateByTo(previousQuestsByKey) { it.key }

        for (quest in questsNow) {
            previousQuestsByKey.remove(quest.key)
        }
        // quests that were created previously for an element but now not anymore shall be deleted
        return previousQuestsByKey.values.map { it.key } + deletedQuestKeys
    }

    private fun updateQuests(questsNow: Collection<OsmQuest>, obsoleteQuestKeys: Collection<OsmQuestKey>) {
        val time = nowAsEpochMilliseconds()

        db.deleteAll(obsoleteQuestKeys)
        db.putAll(questsNow)

        val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
        Log.i(TAG, "Persisted ${questsNow.size} new and removed ${obsoleteQuestKeys.size} already resolved quests in ${seconds.format(1)}s")
    }

    private fun mayCreateQuest(
        questType: OsmElementQuestType<*>,
        geometry: ElementGeometry,
        downloadedBoundingBox: BoundingBox?
    ): Boolean {
        val pos = geometry.center

        // outside downloaded area: skip
        if (downloadedBoundingBox != null && !downloadedBoundingBox.contains(pos)) return false

        // do not create quests in countries where the quest is not activated
        val countries = questType.enabledInCountries
        if (!countryBoundaries.value.isInAny(pos, countries)) return false

        return true
    }

    fun delete(key: OsmQuestKey) {
        db.delete(key)
        onUpdated(deleted = listOf(key))
    }

    override fun get(key: OsmQuestKey): OsmQuest? {
        val entry = db.get(key) ?: return null
        val geometry = mapDataSource.getGeometry(entry.elementType, entry.elementId) ?: return null
        if (isBlacklistedPosition(geometry.center)) return null
        return createOsmQuest(entry, geometry)
    }

    override fun getAllInBBox(bbox: BoundingBox, questTypes: Collection<String>?): List<OsmQuest> {
        val hiddenPositions = getBlacklistedPositions(bbox)
        val entries = db.getAllInBBox(bbox, questTypes).filter {
            it.position.truncateTo6Decimals() !in hiddenPositions
        }

        val elementKeys = HashSet<ElementKey>()
        entries.mapTo(elementKeys) { ElementKey(it.elementType, it.elementId) }

        val geometriesByKey = mapDataSource.getGeometries(elementKeys).associateBy { it.key }

        return entries.mapNotNull { entry ->
            val geometry = geometriesByKey[ElementKey(entry.elementType, entry.elementId)]?.geometry
            createOsmQuest(entry, geometry)
        }
    }

    private fun createOsmQuest(entry: OsmQuestDaoEntry, geometry: ElementGeometry?): OsmQuest? {
        if (geometry == null) return null
        val questType = questTypeRegistry.getByName(entry.questTypeName) as? OsmElementQuestType<*> ?: return null
        return OsmQuest(questType, entry.elementType, entry.elementId, geometry)
    }

    private fun getBlacklistedPositions(bbox: BoundingBox): Set<LatLon> =
        notesSource
            .getAllPositions(bbox.enlargedBy(0.2))
            .map { it.truncateTo6Decimals() }
            .toSet()

    private fun isBlacklistedPosition(pos: LatLon): Boolean =
        pos.truncateTo6Decimals() in getBlacklistedPositions(BoundingBox(pos, pos))

    /* ---------------------------------------- Listeners --------------------------------------- */

    override fun addListener(listener: OsmQuestSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        added: Collection<OsmQuest> = emptyList(),
        deleted: Collection<OsmQuestKey> = emptyList()
    ) {
        if (added.isEmpty() && deleted.isEmpty()) return

        listeners.forEach { it.onUpdated(added, deleted) }
    }

    private fun Collection<OsmQuest>.filterVisible(): Collection<OsmQuest> =
        if (isNotEmpty()) {
            val bbox = map { it.position }.enclosingBoundingBox()
            val hiddenPositions = getBlacklistedPositions(bbox)
            filter { it.position.truncateTo6Decimals() !in hiddenPositions }
        } else {
            this
        }

    private fun onInvalidated() {
        listeners.forEach { it.onInvalidated() }
    }

    companion object {
        private const val TAG = "OsmQuestController"
    }
}

/** an index by which a list of quest types can be sorted so that quests that are the slowest to
 *  evaluate are evaluated first. This is a performance improvement because the evaluation is done
 *  in parallel on as many threads as there are CPU cores. So if all threads are done except one,
 *  all have to wait for that one thread. So, better enqueue the expensive work at the beginning. */
private val OsmElementQuestType<*>.chonkerIndex: Int get() = when (this) {
    is AddOpeningHours -> 0 // OpeningHoursParser, extensive filter
    is CheckExistence -> 1 // FeatureDictionary, extensive filter
    is CheckShopExistence -> 1 // FeatureDictionary, extensive filter
    is AddHousenumber -> 1 // complex filter
    is AddMaxHeight -> 1 // complex filter
    is AddCycleway -> 2 // complex filter
    is AddPlaceName -> 2 // FeatureDictionary, extensive filter
    else -> 10
}
