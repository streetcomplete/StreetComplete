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
import de.westnordost.streetcomplete.screens.main.map.toBoundingBox
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.maplibre.compose.camera.CameraState

// TODO the issue with this construct is that it also pushes new updates while the layer that
//      displays this is not actually visible

class MapQuestPinsSource(
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestsSource: VisibleQuestsSource
) {
    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    val pins: StateFlow<Collection<Pin>> get() = _pins
    private val _pins = MutableStateFlow<Collection<Pin>>(emptyList())

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrdersLock = ReentrantLock()
    private val questTypeOrders: MutableMap<QuestType, Int> = mutableMapOf()

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null

    // quests in current view: key -> [pin, ...]
    private val questsInView: MutableMap<QuestKey, List<Pin>> = mutableMapOf()
    private val questsInViewMutex = Mutex()

    private val visibleQuestsSourceMutex = Mutex()

    private var updateJob: Job? = null

    private val visibleQuestsListener = object : VisibleQuestsSource.Listener {
        override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
            val oldUpdateJob = updateJob
            updateJob = viewLifecycleScope.launch {
                oldUpdateJob?.join() // don't cancel, as updateQuestPins only updates existing data
                updateQuestPins(added, removed)
            }
        }

        override fun onInvalidated() {
            invalidate()
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

    init {
        initializeQuestTypeOrders()
        visibleQuestsSource.addListener(visibleQuestsListener)
        questTypeOrderSource.addListener(questTypeOrderListener)
    }

    fun onDestroy() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        visibleQuestsSource.removeListener(visibleQuestsListener)
        questTypeOrderSource.removeListener(questTypeOrderListener)
    }

    fun getQuestKey(properties: JsonObject): QuestKey? =
        properties.toQuestKey()

    fun onMapMoved(cameraState: CameraState) {
        // require zoom >= 14, which is the lowest zoom level where quests are shown
        val zoom = cameraState.position.zoom
        if (zoom < 14) return
        val displayedArea = cameraState.viewport
            ?.visibleBoundingBox
            ?.toBoundingBox()
            ?: return
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 32) return
        val isNewRect = lastDisplayedRect?.contains(tilesRect) != true
        if (!isNewRect) return
        setQuestPins(tilesRect)
        lastDisplayedRect = tilesRect
    }

    private fun setQuestPins(tilesRect: TilesRect) {
        /* Imagine you are panning the map fast, many different tiles come into and vanish from view
           again quickly. Suppose, that fetching the data from DB takes longer than panning through
           and out of a tile - we would end up with a long queue of DB fetches (and subsequent
           map updates) of which the data is discarded immediately after because it is out of view
           again.
           So, what we do here is to discard each such update except the last one. All jobs started
           in potentially quick succession have to wait at for the DB fetch to complete and will
           stop when they have been cancelled in the meantime. The same with if they have been
           cancelled just after the DB fetch etc. (The coroutine can be cancelled at every place
           where you see that arrow with that green squiggle in the IDE)
         */
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            val bbox = tilesRect.asBoundingBox(TILES_ZOOM)
            setQuestPins(bbox)
        }
    }

    private suspend fun setQuestPins(bbox: BoundingBox) {
        val quests = visibleQuestsSourceMutex.withLock {
            withContext(Dispatchers.IO) { visibleQuestsSource.getAll(bbox) }
        }
        val pins = questsInViewMutex.withLock {
            /* Usually, we would call questsInView.clear() here. However,
               quests have only a single position, but may have multiple pins (see
               Quest::markerLocations), e.g. at the start and end of a long road. A pin of a quest
               whose center is outside the current view may hence be within the current view. Quest
               pins like these should not disappear when panning the map.
               Therefore, only remove all quests that are not in view anymore that  ...
             */
            questsInView.entries.removeAll { (_, pins) ->
                // only have one pin (pin position = quest position)
                pins.size == 1 ||
                // or have no pins in the current view
                pins.none { it.position in bbox }
            }
            quests.forEach { questsInView[it.key] = createQuestPins(it) }
            questsInView.values.flatten()
        }
        _pins.value = pins
    }

    private suspend fun updateQuestPins(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val pins = questsInViewMutex.withLock {
            val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM) ?: return
            var hasChanges = false

            removed.forEach {
                if (questsInView.remove(it) != null) hasChanges = true
            }
            added.forEach {
                if (displayedBBox.contains(it.position)) {
                    questsInView[it.key] = createQuestPins(it)
                    hasChanges = true
                } else {
                    if (questsInView.remove(it.key) != null) hasChanges = true
                }
            }

            if (!hasChanges) return

            questsInView.values.flatten()
        }
        _pins.value = pins
    }

    private fun createQuestPins(quest: Quest): List<Pin> {
        val props = quest.key.toProperties()
        val order = questTypeOrdersLock.withLock { questTypeOrders[quest.type] ?: 0 }
        return quest.markerLocations.map { Pin(it, quest.type.icon, props, order) }
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
        invalidate()
    }

    private fun invalidate() {
        val rect = lastDisplayedRect
        if (rect != null) {
            setQuestPins(rect)
        } else {
            clear()
        }
    }

    private fun clear() {
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            questsInViewMutex.withLock { questsInView.clear() }
            _pins.value = emptyList()
        }
    }

    companion object {
        private const val TILES_ZOOM = 16
    }
}


private const val MARKER_QUEST_GROUP = "quest_group"

private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"

private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"

private fun QuestKey.toProperties(): JsonObject = JsonObject(when (this) {
    is OsmNoteQuestKey -> mapOf(
        MARKER_QUEST_GROUP to JsonPrimitive(QUEST_GROUP_OSM_NOTE),
        MARKER_NOTE_ID to JsonPrimitive(noteId)
    )
    is OsmQuestKey -> mapOf(
        MARKER_QUEST_GROUP to JsonPrimitive(QUEST_GROUP_OSM),
        MARKER_ELEMENT_TYPE to JsonPrimitive(elementType.name),
        MARKER_ELEMENT_ID to JsonPrimitive(elementId),
        MARKER_QUEST_TYPE to JsonPrimitive(questTypeName)
    )
})

private fun JsonObject.toQuestKey(): QuestKey? {
    val questGroup = get(MARKER_QUEST_GROUP)?.jsonPrimitive?.contentOrNull
    return when (questGroup) {
        QUEST_GROUP_OSM_NOTE ->
            OsmNoteQuestKey(getValue(MARKER_NOTE_ID).jsonPrimitive.long)
        QUEST_GROUP_OSM ->
            OsmQuestKey(
                ElementType.valueOf(getValue(MARKER_ELEMENT_TYPE).jsonPrimitive.content),
                getValue(MARKER_ELEMENT_ID).jsonPrimitive.long,
                getValue(MARKER_QUEST_TYPE).jsonPrimitive.content
            )
        else -> null
    }
}
