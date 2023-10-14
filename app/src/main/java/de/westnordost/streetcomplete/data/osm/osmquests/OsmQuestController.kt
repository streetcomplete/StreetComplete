package de.westnordost.streetcomplete.data.osm.osmquests

import android.util.Log
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
import de.westnordost.streetcomplete.quests.oneway_suspects.AddSuspectedOneway
import de.westnordost.streetcomplete.quests.opening_hours.AddOpeningHours
import de.westnordost.streetcomplete.quests.place_name.AddPlaceName
import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.ktx.format
import de.westnordost.streetcomplete.util.ktx.intersects
import de.westnordost.streetcomplete.util.ktx.isInAny
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.truncateTo5Decimals
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
import java.util.concurrent.FutureTask

/** Controller for managing OsmQuests. Takes care of persisting OsmQuest objects and notifying
 *  listeners about changes */
class OsmQuestController internal constructor(
    private val db: OsmQuestDao,
    private val hiddenDB: OsmQuestsHiddenDao,
    private val mapDataSource: MapDataWithEditsSource,
    private val notesSource: NotesWithEditsSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val countryBoundariesFuture: FutureTask<CountryBoundaries>
) : OsmQuestSource, HideOsmQuestController {

    /* Must be a singleton because there is a listener that should respond to a change in the
     *  database table */

    interface HideOsmQuestListener {
        fun onHid(edit: OsmQuestHidden)
        fun onUnhid(edit: OsmQuestHidden)
        fun onUnhidAll()
    }
    private val hideListeners = Listeners<HideOsmQuestListener>()

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
            synchronized(this) {
                val previousQuests = db.getAllForElements(updated.map { it.key })
                // quests that refer to elements that have been deleted shall be deleted
                val deleteQuestKeys = db.getAllForElements(deleted).map { it.key }

                val seconds = (nowAsEpochMilliseconds() - time) / 1000.0
                Log.i(TAG, "Created ${quests.size} quests for ${updated.size} updated elements in ${seconds.format(1)}s")

                obsoleteQuestKeys = getObsoleteQuestKeys(quests, previousQuests, deleteQuestKeys)
                updateQuests(quests, obsoleteQuestKeys)
            }

            onUpdated(added = quests, deletedKeys = obsoleteQuestKeys)
        }

        /** Replace all quests of the given types in the given bounding box with the given quests.
         *  Called on download of a quest type for a bounding box. */
        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            val quests = createQuestsForBBox(bbox, mapDataWithGeometry, allQuestTypes)
            val obsoleteQuestKeys: List<OsmQuestKey>
            synchronized(this) {
                val previousQuests = db.getAllInBBox(bbox)
                obsoleteQuestKeys = getObsoleteQuestKeys(quests, previousQuests, emptyList())
                updateQuests(quests, obsoleteQuestKeys)
            }

            onUpdated(added = quests, deletedKeys = obsoleteQuestKeys)
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

        val countryBoundaries = countryBoundariesFuture.get()

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
        if (!countryBoundariesFuture.get().isInAny(pos, countries)) return false

        return true
    }

    fun delete(key: OsmQuestKey) {
        db.delete(key)
        onUpdated(deletedKeys = listOf(key))
    }

    override fun getVisible(key: OsmQuestKey): OsmQuest? {
        val entry = db.get(key) ?: return null
        if (hiddenDB.contains(entry.key)) return null
        val geometry = mapDataSource.getGeometry(entry.elementType, entry.elementId) ?: return null
        if (isBlacklistedPosition(geometry.center)) return null
        return createOsmQuest(entry, geometry)
    }

    override fun getAllVisibleInBBox(bbox: BoundingBox, questTypes: Collection<String>?): List<OsmQuest> {
        val hiddenQuestKeys = getHiddenQuests()
        val hiddenPositions = getBlacklistedPositions(bbox)
        val entries = db.getAllInBBox(bbox, questTypes).filter { entry ->
            entry.key !in hiddenQuestKeys && entry.position.truncateTo5Decimals() !in hiddenPositions
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

    /* ----------------------------------- Hiding / Unhiding  ----------------------------------- */

    private fun getBlacklistedPositions(bbox: BoundingBox): Set<LatLon> =
        notesSource
            .getAllPositions(bbox.enlargedBy(1.2))
            .map { it.truncateTo5Decimals() }
            .toSet()

    private fun isBlacklistedPosition(pos: LatLon): Boolean =
        pos.truncateTo5Decimals() in getBlacklistedPositions(BoundingBox(pos, pos))

    private fun getHiddenQuests(): Set<OsmQuestKey> =
        hiddenDB.getAllIds().toSet()

    /** Mark the quest as hidden by user interaction */
    override fun hide(key: OsmQuestKey) {
        synchronized(this) { hiddenDB.add(key) }

        val hidden = getHidden(key)
        if (hidden != null) onHid(hidden)
        onUpdated(deletedKeys = listOf(key))
    }

    fun unhide(key: OsmQuestKey): Boolean {
        val hidden = getHidden(key)
        synchronized(this) {
            if (!hiddenDB.delete(key)) return false
        }
        if (hidden != null) onUnhid(hidden)
        val quest = getVisible(key)
        if (quest != null) onUpdated(added = listOf(quest))
        return true
    }

    /** Un-hides all previously hidden quests by user interaction */
    fun unhideAll(): Int {
        val unhidCount = synchronized(this) { hiddenDB.deleteAll() }
        onUnhidAll()
        onInvalidated()
        return unhidCount
    }

    fun getHidden(key: OsmQuestKey): OsmQuestHidden? {
        val timestamp = hiddenDB.getTimestamp(key) ?: return null
        val pos = mapDataSource.getGeometry(key.elementType, key.elementId)?.center
        return createOsmQuestHidden(key, pos, timestamp)
    }

    fun getAllHiddenNewerThan(timestamp: Long): List<OsmQuestHidden> {
        val questKeysWithTimestamp = hiddenDB.getNewerThan(timestamp)

        val elementKeys = questKeysWithTimestamp.mapTo(HashSet()) {
            ElementKey(it.osmQuestKey.elementType, it.osmQuestKey.elementId)
        }

        val geometriesByKey = mapDataSource.getGeometries(elementKeys).associateBy { it.key }

        return questKeysWithTimestamp.mapNotNull { (key, timestamp) ->
            val pos = geometriesByKey[ElementKey(key.elementType, key.elementId)]?.geometry?.center
            createOsmQuestHidden(key, pos, timestamp)
        }
    }

    private fun createOsmQuestHidden(key: OsmQuestKey, position: LatLon?, timestamp: Long): OsmQuestHidden? {
        if (position == null) return null
        val questType = questTypeRegistry.getByName(key.questTypeName) as? OsmElementQuestType<*> ?: return null
        return OsmQuestHidden(key.elementType, key.elementId, questType, position, timestamp)
    }

    /* ---------------------------------------- Listeners --------------------------------------- */

    override fun addListener(listener: OsmQuestSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: OsmQuestSource.Listener) {
        listeners.remove(listener)
    }

    private fun onUpdated(
        added: Collection<OsmQuest> = emptyList(),
        deletedKeys: Collection<OsmQuestKey> = emptyList()
    ) {
        if (added.isEmpty() && deletedKeys.isEmpty()) return

        val visibleAdded = if (added.isNotEmpty()) {
            val hiddenIds = getHiddenQuests()
            val bbox = added.map { it.position }.enclosingBoundingBox()
            val hiddenPositions = getBlacklistedPositions(bbox)
            added.filter { it.key !in hiddenIds && it.position.truncateTo5Decimals() !in hiddenPositions }
        } else {
            added
        }

        listeners.forEach { it.onUpdated(visibleAdded, deletedKeys) }
    }
    private fun onInvalidated() {
        listeners.forEach { it.onInvalidated() }
    }

    /* ------------------------------------- Hide Listeners ------------------------------------- */

    fun addHideQuestsListener(listener: HideOsmQuestListener) {
        hideListeners.add(listener)
    }
    fun removeHideQuestsListener(listener: HideOsmQuestListener) {
        hideListeners.remove(listener)
    }

    private fun onHid(edit: OsmQuestHidden) {
        hideListeners.forEach { it.onHid(edit) }
    }
    private fun onUnhid(edit: OsmQuestHidden) {
        hideListeners.forEach { it.onUnhid(edit) }
    }
    private fun onUnhidAll() {
        hideListeners.forEach { it.onUnhidAll() }
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
    is AddSuspectedOneway -> 0 // Download, IO TODO
    is CheckExistence -> 1 // FeatureDictionary, extensive filter
    is AddHousenumber -> 1 // complex filter
    is AddCycleway -> 2 // complex filter
    is AddPlaceName -> 2 // FeatureDictionary, extensive filter
    else -> 10
}
