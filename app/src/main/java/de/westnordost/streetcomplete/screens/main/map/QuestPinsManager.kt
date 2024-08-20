package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
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
import de.westnordost.streetcomplete.overlays.places.PlacesOverlay
import de.westnordost.streetcomplete.quests.show_poi.ShowBusiness
import de.westnordost.streetcomplete.screens.main.map.components.Pin
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.util.getNameLabel
import de.westnordost.streetcomplete.util.isDay
import de.westnordost.streetcomplete.util.math.contains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap

/** Manages the layer of quest pins in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the quests
 *  for the bbox surrounding the area from database and holds it in memory. */
class QuestPinsManager(
    private val map: MapLibreMap,
    private val pinsMapComponent: PinsMapComponent,
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestsSource: VisibleQuestsSource,
    private val prefs: ObservableSettings,
    private val mapDataSource: MapDataWithEditsSource,
    private val selectedOverlaySource: SelectedOverlaySource,
) : DefaultLifecycleObserver {

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrders: MutableMap<QuestType, Int> = hashMapOf()
    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // quests in current view: key -> [pin, ...]
    private val questsInView: MutableMap<QuestKey, List<Pin>> = hashMapOf()
    var reversedOrder = false
        private set
    private val questsInViewMutex = Mutex()

    private val visibleQuestsSourceMutex = Mutex()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO) // todo: remove?

    private var updateJob: Job? = null
    private val m = Mutex() // todo: remove?

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
        viewLifecycleScope.launch { questsInViewMutex.withLock { questsInView.clear() } }
        lastDisplayedRect = null
        onNewScreenPosition()
    }

    private fun clear() {
        lastDisplayedRect = null
        viewLifecycleScope.launch {
            questsInViewMutex.withLock { questsInView.clear() }
            withContext(Dispatchers.Main) { pinsMapComponent.clear() }
        }
    }

    fun getQuestKey(properties: Map<String, String>): QuestKey? =
        properties.toQuestKey()

    fun onNewScreenPosition() {
        if (!isStarted || !isVisible) return
        viewLifecycleScope.launch { updateCurrentScreenArea() }
    }

    private suspend fun updateCurrentScreenArea() {
        // require zoom >= 14, which is the lowest zoom level where quests are shown
        val zoom = map.cameraPosition.zoom
        if (zoom < 14) return
        val displayedArea = withContext(Dispatchers.Main) { map.screenAreaToBoundingBox() }
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 32) return
        val isNewRect = lastDisplayedRect?.contains(tilesRect) != true
        if (!isNewRect) return

        lastDisplayedRect = tilesRect
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
            withContext(Dispatchers.IO) { visibleQuestsSource.getAllVisible(bbox) }
        }
        val pins = questsInViewMutex.withLock {
            /* Usually, we would call questsInView.clear() here. However,
               quests have only a single position, but may have multiple pins (see
               Quest::markerLocations), e.g. at the start and end of a long road. A pin of a quest
               whose center is outside the current view may hence be within the current view. Quest
               pins like these should not disappear when panning the map.
               Therefore, remove all quests that are not in view anymore that  ...
             */
            questsInView.entries.removeAll { (_, pins) ->
                // only have one pin (pin position = quest position)
                pins.size == 1 ||
                // or has no pins in the current view
                pins.none { it.position in bbox }
            }
            quests.forEach { questsInView[it.key] = createQuestPins(it) }
            questsInView.values.flatten()
        }
        pinsMapComponent.set(pins)
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
        pinsMapComponent.set(pins)
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
        val moveToFront = if (Prefs.DayNightBehavior.valueOf(prefs.getString(Prefs.DAY_NIGHT_BEHAVIOR, "IGNORE")) == Prefs.DayNightBehavior.PRIORITY)
            if (map.cameraPosition.target?.toLatLon()?.let { isDay(it) } != false)
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
        val color = quest.type.dotColor
        val label = if (color != null && quest is OsmQuest) getLabel(quest) else null
        val geometry = if (quest.geometry !is ElementPointGeometry && prefs.getBoolean(Prefs.QUEST_GEOMETRIES, false) && color == null)
                quest.geometry
            else null

        val props = if (label == null) quest.key.toProperties() else (quest.key.toProperties() + ("label" to label))
        val order = synchronized(questTypeOrders) { questTypeOrders[quest.type] ?: 0 }

        val pins = quest.markerLocations.map { Pin(it, quest.type.icon, props, order, geometry, color) }
        // storing importance in the quest requires the VisibleQuestsSource.cache to be invalidated on order change!
        // or what we do: clear quest.pins if the order changed
        quest.pins = pins
        return pins
    }

    private fun getLabel(quest: OsmQuest): String? {
        if (quest.type is ShowBusiness && selectedOverlaySource.selectedOverlay is PlacesOverlay)
            return null // avoid duplicate business labels if shops overlay is active
        val labelSources = quest.type.dotLabelSources.ifEmpty { return null }
        val tags = mapDataSource.get(quest.elementType, quest.elementId)?.tags ?: return null
        return labelSources.firstNotNullOfOrNull {
            if (it == "label") getNameLabel(tags) else tags[it]
        }
    }

    private fun reinitializeQuestTypeOrders() {
        visibleQuestsSource.clearCachedQuestPins() // pin.importance contains quest order, so we need to reset it
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
            ElementType.valueOf(getValue(MARKER_ELEMENT_TYPE)),
            getValue(MARKER_ELEMENT_ID).toLong(),
            getValue(MARKER_QUEST_TYPE)
        )
    QUEST_GROUP_OTHER ->
        ExternalSourceQuestKey(getValue(MARKER_OTHER_ID), getValue(MARKER_OTHER_SOURCE))
    else -> null
}
