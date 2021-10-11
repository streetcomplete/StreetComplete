package de.westnordost.streetcomplete.map

import android.content.res.Resources
import android.graphics.RectF
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.map.components.Pin
import de.westnordost.streetcomplete.map.components.PinsMapComponent
import de.westnordost.streetcomplete.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.*
import kotlinx.coroutines.*

/** Manages the layer of quest pins in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the quests
 *  for the bbox surrounding the area from database and holds it in memory. */
class QuestPinsManager(
    private val ctrl: KtMapController,
    private val pinsMapComponent: PinsMapComponent,
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val resources: Resources,
    private val visibleQuestsSource: VisibleQuestsSource
): LifecycleObserver {

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrders: MutableMap<QuestType<*>, Int> = mutableMapOf()
    // all the (zoom 14) tiles that have been retrieved from DB into memory already
    private val retrievedTiles: MutableSet<TilePos> = mutableSetOf()
    // last displayed rect of (zoom 14) tiles
    private var lastDisplayedRect: TilesRect? = null

    // quest key -> [point, ...]
    private val quests: MutableMap<QuestKey, List<Pin>> = mutableMapOf()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    /** Switch visibility of quest pins layer */
    var isVisible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) show() else hide()
        }

    private val visibleQuestsListener = object : VisibleQuestsSource.Listener {
        override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
            var updates = 0
            added.forEach { if (add(it)) updates++ }
            removed.forEach { if (remove(it)) updates++ }
            if (updates > 0) updatePins()
        }

        override fun onVisibleQuestsInvalidated() {
            invalidate()
        }
    }

    private val questTypeOrderListener = object : QuestTypeOrderSource.Listener {
        override fun onQuestTypeOrderAdded(item: QuestType<*>, toAfter: QuestType<*>) {
            reinitializeQuestTypeOrders()
        }

        override fun onQuestTypeOrdersChanged() {
            reinitializeQuestTypeOrders()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY) fun onDestroy() {
        hide()
        viewLifecycleScope.cancel()
    }

    private fun show() {
        initializeQuestTypeOrders()
        onNewScreenPosition()
        visibleQuestsSource.addListener(visibleQuestsListener)
        questTypeOrderSource.addListener(questTypeOrderListener)
    }

    private fun hide() {
        clear()
        viewLifecycleScope.coroutineContext.cancelChildren()
        visibleQuestsSource.removeListener(visibleQuestsListener)
        questTypeOrderSource.removeListener(questTypeOrderListener)
    }

    private fun invalidate() {
        clear()
        onNewScreenPosition()
    }

    fun getQuestKey(properties: Map<String, String>): QuestKey? =
        properties.toQuestKey()

    fun onNewScreenPosition() {
        if (!isVisible) return
        val zoom = ctrl.cameraPosition.zoom
        if (zoom < TILES_ZOOM) return
        val displayedArea = ctrl.screenAreaToBoundingBox(RectF()) ?: return
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (lastDisplayedRect != tilesRect) {
            lastDisplayedRect = tilesRect
            updateQuestsInRect(tilesRect)
        }
    }

    private fun updateQuestsInRect(tilesRect: TilesRect) {
        // area too big -> skip (performance)
        if (tilesRect.size > 4) {
            return
        }
        var tiles: List<TilePos>
        synchronized(retrievedTiles) {
            tiles = tilesRect.asTilePosSequence().filter { !retrievedTiles.contains(it) }.toList()
        }
        val minRect = tiles.minTileRect() ?: return
        val bbox = minRect.asBoundingBox(TILES_ZOOM)
        viewLifecycleScope.launch {
            val quests = withContext(Dispatchers.IO) { visibleQuestsSource.getAllVisible(bbox) }
            var addedAny = false
            for (quest in quests) {
                if (add(quest)) addedAny = true
            }
            if (addedAny) updatePins()
        }
        synchronized(retrievedTiles) { retrievedTiles.addAll(tiles) }
    }

    private fun add(quest: Quest): Boolean {
        val questKey = quest.key
        val positions = quest.markerLocations
        val previousPositions = synchronized(quest) { quests[questKey]?.map { it.position } }
        if (positions == previousPositions) return false

        val pins = createQuestPins(quest)
        synchronized(quests) {
            quests[questKey] = pins
        }
        return true
    }

    private fun remove(questKey: QuestKey): Boolean {
        synchronized(quests) {
            if (!quests.containsKey(questKey)) return false

            quests.remove(questKey)
            return true
        }
    }

    private fun clear() {
        synchronized(quests) { quests.clear() }
        synchronized(retrievedTiles) { retrievedTiles.clear() }
        pinsMapComponent.clear()
        lastDisplayedRect = null
    }

    private fun updatePins() {
        if (isVisible) {
            val pins = synchronized(quests) { quests.values.flatten() }
            pinsMapComponent.showPins(pins)
        }
    }

    private fun initializeQuestTypeOrders() {
        // this needs to be reinitialized when the quest order changes
        val sortedQuestTypes = questTypeRegistry.toMutableList()
        questTypeOrderSource.sort(sortedQuestTypes)
        synchronized(questTypeOrders) {
            questTypeOrders.clear()
            sortedQuestTypes.forEachIndexed { index, questType ->
                questTypeOrders[questType] = index
            }
        }
    }

    private fun createQuestPins(quest: Quest): List<Pin> {
        val iconName = resources.getResourceEntryName(quest.type.icon)
        val props = quest.key.toProperties()
        val importance = getQuestImportance(quest)
        return quest.markerLocations.map { Pin(it, iconName, props, importance) }
    }

    /** returns values from 0 to 100000, the higher the number, the more important */
    private fun getQuestImportance(quest: Quest): Int = synchronized(questTypeOrders) {
        val questTypeOrder = questTypeOrders[quest.type] ?: 0
        val freeValuesForEachQuest = 100000 / questTypeOrders.size
        /* position is used to add values unique to each quest to make ordering consistent
           freeValuesForEachQuest is an int, so % freeValuesForEachQuest will fit into int */
        val hopefullyUniqueValueForQuest = quest.position.hashCode() % freeValuesForEachQuest
        return 100000 - questTypeOrder * freeValuesForEachQuest + hopefullyUniqueValueForQuest
    }

    private fun reinitializeQuestTypeOrders() {
        initializeQuestTypeOrders()
        invalidate()
    }

    companion object {
        private const val TILES_ZOOM = 14
    }
}

private const val MARKER_QUEST_GROUP = "quest_group"

private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"

private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"

private fun QuestKey.toProperties(): Map<String, String> = when(this) {
    is OsmNoteQuestKey -> mapOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM_NOTE,
        MARKER_NOTE_ID to noteId.toString()
    )
    is OsmQuestKey -> mapOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questTypeName
    )
}

private fun Map<String, String>.toQuestKey(): QuestKey? = when(get(MARKER_QUEST_GROUP)) {
    QUEST_GROUP_OSM_NOTE ->
        OsmNoteQuestKey(getValue(MARKER_NOTE_ID).toLong())
    QUEST_GROUP_OSM ->
        OsmQuestKey(
            getValue(MARKER_ELEMENT_TYPE).let { ElementType.valueOf(it) },
            getValue(MARKER_ELEMENT_ID).toLong(),
            getValue(MARKER_QUEST_TYPE)
        )
    else -> null
}
