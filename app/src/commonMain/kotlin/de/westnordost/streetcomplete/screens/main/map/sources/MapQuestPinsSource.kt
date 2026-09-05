package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.layers.PinSnapshot
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Renderer-independent owner of the quest pins surrounding the current viewport.
 *
 * The viewport is expressed in StreetComplete domain types so this source can be driven by
 * MapLibre Compose on Android, iOS, and desktop without retaining a native map object. A tile-sized
 * cache preserves the legacy behavior for quests whose multiple marker positions straddle the
 * viewport, while cancelling superseded database fetches during fast pans.
 */
class MapQuestPinsSource(
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestsSource: VisibleQuestsSource,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)

    private val _pins = MutableStateFlow(PinSnapshot.Empty)
    val pins: StateFlow<PinSnapshot> = _pins.asStateFlow()

    private val questTypeOrdersLock = ReentrantLock()
    private val questTypeOrders = mutableMapOf<QuestType, Int>()
    private val stateLock = ReentrantLock()

    private var lastDisplayedRect: TilesRect? = null
    private val questsInView = mutableMapOf<QuestKey, List<Pin>>()
    private val questsInViewMutex = Mutex()
    private val visibleQuestsSourceMutex = Mutex()
    private var updateJob: Job? = null
    private var viewportGeneration = 0L
    private var isActive = false
    private var isClosed = false

    private val visibleQuestsListener = object : VisibleQuestsSource.Listener {
        override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
            stateLock.withLock {
                if (isClosed || !isActive) return
                val precedingUpdate = updateJob
                val generation = viewportGeneration
                updateJob = scope.launch {
                    // A full viewport fetch must finish first: this callback is a delta against it.
                    precedingUpdate?.join()
                    updateQuestPins(added, removed, generation)
                }
            }
        }

        override fun onInvalidated() {
            reloadCurrentViewport()
        }
    }

    private val questTypeOrderListener = object : QuestTypeOrderSource.Listener {
        override fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType) {
            reinitializeQuestTypeOrders()
        }

        override fun onQuestTypeOrdersChanged() {
            reinitializeQuestTypeOrders()
        }
    }

    /** Updates the cached pins when the visible map region enters a new zoom-16 tile rectangle. */
    fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) {
        if (zoom < MIN_ZOOM || displayedArea == null) return

        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (tilesRect.size > MAX_TILES_IN_VIEW) return
        stateLock.withLock {
            if (isClosed || !isActive || lastDisplayedRect?.contains(tilesRect) == true) return
            lastDisplayedRect = tilesRect
            loadViewport(tilesRect)
        }
    }

    /** Starts or stops the database/listener pipeline to match the currently visible pin mode. */
    fun setActive(active: Boolean) {
        stateLock.withLock {
            if (isClosed || isActive == active) return
            isActive = active
            if (active) {
                initializeQuestTypeOrders()
                visibleQuestsSource.addListener(visibleQuestsListener)
                questTypeOrderSource.addListener(questTypeOrderListener)
            } else {
                val generation = ++viewportGeneration
                updateJob?.cancel()
                lastDisplayedRect = null
                publish(emptyList())
                visibleQuestsSource.removeListener(visibleQuestsListener)
                questTypeOrderSource.removeListener(questTypeOrderListener)
                scope.launch {
                    questsInViewMutex.withLock {
                        stateLock.withLock {
                            if (!isActive && generation == viewportGeneration) questsInView.clear()
                        }
                    }
                }
            }
        }
    }

    fun getQuestKey(properties: Map<String, String>): QuestKey? =
        properties.toQuestKeyOrNull()

    /** Releases listeners and pending work. This source must not be used afterwards. */
    fun close() {
        stateLock.withLock {
            if (isClosed) return
            isClosed = true
            updateJob?.cancel()
            if (isActive) {
                visibleQuestsSource.removeListener(visibleQuestsListener)
                questTypeOrderSource.removeListener(questTypeOrderListener)
            }
        }
        scope.cancel()
    }

    private fun loadViewport(tilesRect: TilesRect) {
        stateLock.withLock {
            if (isClosed || !isActive) return
            updateJob?.cancel()
            val generation = ++viewportGeneration
            updateJob = scope.launch {
                setQuestPins(tilesRect.asBoundingBox(TILES_ZOOM), generation)
            }
        }
    }

    private suspend fun setQuestPins(bbox: BoundingBox, generation: Long) {
        val quests = visibleQuestsSourceMutex.withLock {
            visibleQuestsSource.getAll(bbox)
        }
        currentCoroutineContext().ensureActive()

        questsInViewMutex.withLock {
            currentCoroutineContext().ensureActive()
            val canPublish = stateLock.withLock {
                !isClosed && isActive && generation == viewportGeneration
            }
            if (!canPublish) return@withLock

            // Multi-marker quests can have their center outside the viewport while a marker
            // remains inside. Retain exactly those entries, matching the legacy manager.
            questsInView.entries.removeAll { (_, pins) ->
                pins.size == 1 || pins.none { it.position in bbox }
            }
            quests.forEach { questsInView[it.key] = createQuestPins(it) }
            val previous = _pins.value
            // Pin conversion and complete GeoJSON serialization can be substantial in dense
            // cities. Keep it outside stateLock so camera callbacks never wait for that work.
            val prepared = previous.updated(questsInView.values.flatten())
            currentCoroutineContext().ensureActive()
            stateLock.withLock {
                if (
                    !isClosed && isActive && generation == viewportGeneration &&
                    _pins.value === previous
                ) {
                    _pins.value = prepared
                }
            }
        }
    }

    private suspend fun updateQuestPins(
        added: Collection<Quest>,
        removed: Collection<QuestKey>,
        generation: Long,
    ) {
        val coroutineContext = currentCoroutineContext()
        val displayedBBox = stateLock.withLock {
            if (isClosed || !isActive || generation != viewportGeneration) return
            lastDisplayedRect?.asBoundingBox(TILES_ZOOM)
        } ?: return
        questsInViewMutex.withLock {
            coroutineContext.ensureActive()
            var hasChanges = false
            removed.forEach { if (questsInView.remove(it) != null) hasChanges = true }
            added.forEach { quest ->
                if (displayedBBox.contains(quest.position)) {
                    questsInView[quest.key] = createQuestPins(quest)
                    hasChanges = true
                } else if (questsInView.remove(quest.key) != null) {
                    hasChanges = true
                }
            }
            if (!hasChanges) return
            val previous = _pins.value
            val prepared = previous.updated(questsInView.values.flatten())
            coroutineContext.ensureActive()
            stateLock.withLock {
                if (
                    !isClosed && isActive && generation == viewportGeneration &&
                    _pins.value === previous
                ) {
                    _pins.value = prepared
                }
            }
        }
    }

    private fun createQuestPins(quest: Quest): List<Pin> {
        val properties = quest.key.questKeyProperties()
        val order = questTypeOrdersLock.withLock { questTypeOrders[quest.type] ?: 0 }
        return quest.markerLocations.map { Pin(it, quest.type.icon, properties, order) }
    }

    private fun initializeQuestTypeOrders() {
        val sortedQuestTypes = questTypeRegistry.toMutableList()
        questTypeOrderSource.sort(sortedQuestTypes)
        questTypeOrdersLock.withLock {
            questTypeOrders.clear()
            sortedQuestTypes.forEachIndexed { index, questType ->
                questTypeOrders[questType] = index
            }
        }
    }

    private fun reinitializeQuestTypeOrders() {
        initializeQuestTypeOrders()
        reloadCurrentViewport()
    }

    private fun reloadCurrentViewport() {
        val displayedRect = stateLock.withLock {
            if (isClosed || !isActive) return
            lastDisplayedRect
        }
        displayedRect?.let(::loadViewport) ?: clear()
    }

    private fun clear() {
        stateLock.withLock {
            if (isClosed || !isActive) return
            ++viewportGeneration
            updateJob?.cancel()
            publish(emptyList())
            updateJob = scope.launch {
                questsInViewMutex.withLock { questsInView.clear() }
            }
        }
    }

    private fun publish(pins: List<Pin>) {
        _pins.update { it.updated(pins) }
    }

    private companion object {
        const val TILES_ZOOM = 16
        const val MIN_ZOOM = 14.0
        const val MAX_TILES_IN_VIEW = 32
    }
}

private const val MARKER_QUEST_GROUP = "quest_group"
private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"
private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"

internal fun QuestKey.questKeyProperties(): List<Pair<String, String>> = when (this) {
    is OsmNoteQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM_NOTE,
        MARKER_NOTE_ID to noteId.toString(),
    )
    is OsmQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questTypeName,
    )
}

internal fun Map<String, String>.toQuestKeyOrNull(): QuestKey? = when (get(MARKER_QUEST_GROUP)) {
    QUEST_GROUP_OSM_NOTE -> get(MARKER_NOTE_ID)?.toLongOrNull()?.let(::OsmNoteQuestKey)
    QUEST_GROUP_OSM -> {
        val elementType = get(MARKER_ELEMENT_TYPE)?.let {
            ElementType.entries.firstOrNull { type -> type.name == it }
        }
        val elementId = get(MARKER_ELEMENT_ID)?.toLongOrNull()
        val questType = get(MARKER_QUEST_TYPE)
        if (elementType != null && elementId != null && questType != null) {
            OsmQuestKey(elementType, elementId, questType)
        } else {
            null
        }
    }
    else -> null
}
