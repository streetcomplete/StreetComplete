package de.westnordost.streetcomplete.screens.main.map

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.RectF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.DayNightCycle
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.ExternalSourceQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.Pin
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.isDay
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

/** Manages the layer of quest pins in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the quests
 *  for the bbox surrounding the area from database and holds it in memory. */
class QuestPinsManager(
    private val ctrl: KtMapController,
    private val pinsMapComponent: PinsMapComponent,
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val resources: Resources,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: SharedPreferences,
    private val mapDataSource: MapDataWithEditsSource,
) : DefaultLifecycleObserver {

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrders: MutableMap<QuestType, Int> = mutableMapOf()
    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // quests in current view: key -> [pin, ...]
    private val questsInView: MutableMap<QuestKey, List<Pin>> = mutableMapOf()
    var reversedOrder = false
        private set

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var updateJob: Job? = null
    private val m = Mutex()

    /** Switch visibility of quest pins layer */
    var isVisible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) show() else hide()
        }

    private var isStarted: Boolean = false

    private val visibleQuestsListener = object : VisibleQuestsSource.Listener {
        override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
            val oldUpdateJob = updateJob
            updateJob = viewLifecycleScope.launch {
                oldUpdateJob?.join() // don't cancel, as updateQuestPins only updates existing data
                updateQuestPins(added, removed)
            }
        }

        override fun onVisibleQuestsInvalidated() {
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

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isStarted = true
        show()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isStarted = false
        hide()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun show() {
        if (!isStarted || !isVisible) return
        initializeQuestTypeOrders()
        onNewScreenPosition()
        visibleQuestsSource.addListener(visibleQuestsListener)
        questTypeOrderSource.addListener(questTypeOrderListener)
    }

    private fun hide() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        clear()
        visibleQuestsSource.removeListener(visibleQuestsListener)
        questTypeOrderSource.removeListener(questTypeOrderListener)
    }

    private fun invalidate() {
        clear()
        onNewScreenPosition()
    }

    private fun clear() {
        synchronized(questsInView) { questsInView.clear() }
        lastDisplayedRect = null
        viewLifecycleScope.launch { pinsMapComponent.clear() }
    }

    fun getQuestKey(properties: Map<String, String>): QuestKey? =
        properties.toQuestKey()

    fun onNewScreenPosition() {
        if (!isStarted || !isVisible) return
        val zoom = ctrl.cameraPosition.zoom
        // require zoom >= 14, which is the lowest zoom level where quests are shown
        if (zoom < 14) return
        val displayedArea = ctrl.screenAreaToBoundingBox(RectF()) ?: return
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 32) return
        if (lastDisplayedRect?.contains(tilesRect) != true) {
            lastDisplayedRect = tilesRect
            onNewTilesRect(tilesRect)
        }
    }

    private fun onNewTilesRect(tilesRect: TilesRect) {
        val bbox = tilesRect.asBoundingBox(TILES_ZOOM)
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            while (m.isLocked) { delay(50) }
            if (!coroutineContext.isActive) return@launch
            val quests = m.withLock { visibleQuestsSource.getAllVisible(bbox) }
            setQuestPins(quests)
        }
    }

    private suspend fun setQuestPins(newQuests: List<Quest>) {
        val bbox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM)
        val pins = synchronized(questsInView) {
            // remove only quests without visible pins, because
            //  now newQuests are only quests we might not have had in questsInView
            //  we don't want to remove quests for long ways only because the center is not visible
            questsInView.values.removeAll { pins -> pins.none { bbox?.contains(it.position) != false } }
            newQuests.forEach { questsInView[it.key] = createQuestPins(it) }
            questsInView.values.flatten()
        }
        synchronized(pinsMapComponent) {
            if (coroutineContext.isActive) {
                pinsMapComponent.set(pins)
                ctrl.requestRender()
            }
        }
    }

    private suspend fun updateQuestPins(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM)
        val addedInView = added.filter { displayedBBox?.contains(it.position) != false }
        var deletedAny = false
        val pins = synchronized(questsInView) {
            if (prefs.getBoolean(Prefs.DYNAMIC_QUEST_CREATION, false) && removed.isEmpty() && added.size < 20) {
                // issue with dynamic quest creation: quests near tile edge are not in database,
                // so there are no previous quests to remove -> remove all previous pins here, or they will continue showing
                // works because added contains all quests for that element, and if an answered quest was in db it will be in removed

                val elementKeys = added.mapNotNull { if (it is OsmQuest) ElementKey(it.elementType, it.elementId) else null }.toHashSet()
                questsInView.keys.removeAll { questKey ->
                    questKey is OsmQuestKey
                        // remove all pins for this element (better would be a detection whether we are withing 20 m of tile edge...)
                        && elementKeys.any { questKey.elementId == it.id && questKey.elementType == it.type }
                }
                deletedAny = true
            }

            addedInView.forEach { questsInView[it.key] = createQuestPins(it) }
            removed.forEach { if (questsInView.remove(it) != null) deletedAny = true }
            questsInView.values.flatten()
        }
        if (deletedAny || addedInView.isNotEmpty()) {
            synchronized(pinsMapComponent) {
                if (coroutineContext.isActive) {
                    pinsMapComponent.set(pins)
                    ctrl.requestRender()
                }
            }
        }
    }

    fun reverseQuestOrder() {
        reversedOrder = !reversedOrder
        reinitializeQuestTypeOrders()
    }

    private fun initializeQuestTypeOrders() {
        // this needs to be reinitialized when the quest order changes
        val sortedQuestTypes = questTypeRegistry.toMutableList()
        questTypeOrderSource.sort(sortedQuestTypes)
        // move specific quest types to front if set by preference
        val moveToFront = if (Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")!!) == Prefs.DayNightBehavior.PRIORITY)
            if (isDay(ctrl.cameraPosition.position))
                sortedQuestTypes.filter { it.dayNightCycle == DayNightCycle.ONLY_DAY }
            else
                sortedQuestTypes.filter { it.dayNightCycle == DayNightCycle.ONLY_NIGHT }
        else
            emptyList()
        moveToFront.reversed().forEach { // reversed to keep order within moveToFront
            sortedQuestTypes.remove(it)
            sortedQuestTypes.add(0, it)
        }
        if (reversedOrder) sortedQuestTypes.reverse() // invert only after doing the sorting changes
        synchronized(questTypeOrders) {
            questTypeOrders.clear()
            sortedQuestTypes.forEachIndexed { index, questType ->
                questTypeOrders[questType] = index
            }
        }
    }

    private fun createQuestPins(quest: Quest): List<Pin> {
        val iconName = resources.getResourceEntryName(quest.type.icon)
        val color = quest.type.dotColor
        val importance = getQuestImportance(quest)
        val label = if (color != "no" && quest is OsmQuest) getLabel(quest) else null
        val props = if (label == null) quest.key.toProperties() else (quest.key.toProperties() + ("label" to label))

        val geometry = if (prefs.getBoolean(Prefs.QUEST_GEOMETRIES, false) && quest.geometry !is ElementPointGeometry && color == "no")
            quest.geometry
        else null
        return quest.markerLocations.map { Pin(it, iconName, props, importance, geometry, color) }
    }

    private fun getLabel(quest: OsmQuest): String? {
        val labelSources = quest.type.dotLabelSources.ifEmpty { return null }
        val tags = mapDataSource.get(quest.elementType, quest.elementId)?.tags ?: return null
        return labelSources.firstNotNullOfOrNull {
            if (it == "label") getNameLabel(tags) else tags[it]
        }
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
        private const val TILES_ZOOM = 16
    }
}

private const val MARKER_QUEST_GROUP = "quest_group"

private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"
private const val MARKER_OTHER_ID = "other_id"
private const val MARKER_OTHER_SOURCE = "other_source"

private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"
private const val QUEST_GROUP_OTHER = "other"

private fun QuestKey.toProperties(): List<Pair<String, String>> = when (this) {
    is OsmNoteQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM_NOTE,
        MARKER_NOTE_ID to noteId.toString()
    )
    is OsmQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questTypeName
    )
    is ExternalSourceQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OTHER,
        MARKER_OTHER_ID to id,
        MARKER_OTHER_SOURCE to source,
    )
}

private fun Map<String, String>.toQuestKey(): QuestKey? = when (get(MARKER_QUEST_GROUP)) {
    QUEST_GROUP_OSM_NOTE ->
        OsmNoteQuestKey(getValue(MARKER_NOTE_ID).toLong())
    QUEST_GROUP_OSM ->
        OsmQuestKey(
            getValue(MARKER_ELEMENT_TYPE).let { ElementType.valueOf(it) },
            getValue(MARKER_ELEMENT_ID).toLong(),
            getValue(MARKER_QUEST_TYPE)
        )
    QUEST_GROUP_OTHER ->
        ExternalSourceQuestKey(getValue(MARKER_OTHER_ID), getValue(MARKER_OTHER_SOURCE))
    else -> null
}
