package de.westnordost.streetcomplete.map

import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import androidx.collection.LongSparseArray
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.mapzen.tangram.MapData
import com.mapzen.tangram.geometry.Point
import de.westnordost.streetcomplete.data.*
import de.westnordost.streetcomplete.data.visiblequests.OrderedVisibleQuestTypesProvider
import de.westnordost.streetcomplete.ktx.values
import de.westnordost.streetcomplete.map.tangram.toLngLat
import de.westnordost.streetcomplete.quests.bikeway.AddCycleway
import de.westnordost.streetcomplete.util.SlippyMapMath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/** Manages the layer of quest pins in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the quests
 *  for the bbox surrounding the area from database and holds it in memory. */
class QuestPinLayerManager @Inject constructor(
    private val questTypesProvider: OrderedVisibleQuestTypesProvider,
    private val resources: Resources,
    private val questController: QuestController
): LifecycleObserver, VisibleQuestListener, CoroutineScope by CoroutineScope(Dispatchers.Default) {

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrders: MutableMap<QuestType<*>, Int> = mutableMapOf()
    // all the (zoom 14) tiles that have been retrieved from DB into memory already
    private val retrievedTiles: MutableSet<android.graphics.Point> = mutableSetOf()
    // last displayed rect of (zoom 14) tiles
    private var lastDisplayedRect: Rect? = null

    // quest group -> ( quest Id -> [point, ...] )
    private val quests: EnumMap<QuestGroup, LongSparseArray<List<Point>>> = EnumMap(QuestGroup::class.java)

    lateinit var mapFragment: MapFragment

    var questsLayer: MapData? = null
        set(value) {
            if (field === value) return
            field = value
            updateLayer()
        }

    /** Switch visibility of quest pins layer */
    var isVisible: Boolean = true
        set(value) {
            if (field == value) return
            field = value
            updateLayer()
        }

    init {
        questController.addListener(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START) fun onStart() {
        /* When reentering the fragment, the database may have changed (quest download in
        * background or change in settings), so the quests must be pulled from DB again */
        initializeQuestTypeOrders()
        clear()
        onNewScreenPosition()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP) fun onStop() {
        clear()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY) fun onDestroy() {
        questsLayer = null
        questController.removeListener(this)
        coroutineContext.cancel()
    }

    fun onNewScreenPosition() {
        val zoom = mapFragment.cameraPosition?.zoom ?: return
        if (zoom < TILES_ZOOM) return
        val displayedArea = mapFragment.getDisplayedArea() ?: return
        val tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM)
        if (lastDisplayedRect != tilesRect) {
            lastDisplayedRect = tilesRect
            launch { updateQuestsInRect(tilesRect) }
        }
    }

    override fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup) {
        for (quest in quests) {
            add(quest, group)
        }
        updateLayer()
    }

    override fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup) {
        for (questId in questIds) {
            remove(questId, group)
        }
        updateLayer()
    }

    private suspend fun updateQuestsInRect(tilesRect: Rect) {
        // area too big -> skip
        if (tilesRect.width() * tilesRect.height() > 4) {
            return
        }
        val tiles = SlippyMapMath.asTileList(tilesRect)
        synchronized(retrievedTiles) { tiles.removeAll(retrievedTiles) }
        val minRect = SlippyMapMath.minRect(tiles) ?: return
        val bbox = SlippyMapMath.asBoundingBox(minRect, TILES_ZOOM)
        questController.retrieve(bbox)
        synchronized(retrievedTiles) { retrievedTiles.addAll(tiles) }
    }

    private fun add(quest: Quest, group: QuestGroup) {
        // hack away cycleway quests for old Android SDK versions (#713)
        if (quest.type is AddCycleway && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return
        }
        val questIconName = resources.getResourceEntryName(quest.type.icon)
        val positions = quest.markerLocations
        val points = positions.map { position ->
            val properties = mapOf(
                "type" to "point",
                "kind" to questIconName,
                "order" to getQuestDrawOrder(quest).toString(),
                MARKER_QUEST_GROUP to group.name,
                MARKER_QUEST_ID to quest.id!!.toString()
            )
            Point(position.toLngLat(), properties)
        }
        synchronized(quests) {
            if (quests[group] == null) quests[group] = LongSparseArray(256)
            quests[group]?.put(quest.id!!, points)
        }
    }

    private fun remove(questId: Long, group: QuestGroup) {
        quests[group]?.remove(questId)
    }

    private fun clear() {
        synchronized(quests) {
            for (value in quests.values) {
                value.clear()
            }
        }
        synchronized(retrievedTiles) {
            retrievedTiles.clear()
        }
        questsLayer?.clear()
        lastDisplayedRect = null
    }

    private fun updateLayer() {
        if (isVisible) {
            questsLayer?.setFeatures(getPoints())
        } else {
            questsLayer?.clear()
        }
    }

    private fun getPoints(): List<Point> {
        synchronized(quests) {
            return quests.values.flatMap { questsById ->
                questsById.values.flatten()
            }
        }
    }

    private fun initializeQuestTypeOrders() {
        // this needs to be reinitialized when the quest order changes
        var order = 0
        for (questType in questTypesProvider.get()) {
            questTypeOrders[questType] = order++
        }
    }

    private fun getQuestDrawOrder(quest: Quest): Int {
        /* priority is decided by
           - primarily by quest type to allow quest prioritization
           - for quests of the same type - influenced by quest id,
             this is done to reduce chance that as user zoom in a quest disappears,
             especially in case where disappearing quest is one that user selected to solve
             main priority part - values fit into Integer, but with as large steps as possible */
        val questTypeOrder = questTypeOrders[quest.type] ?: 0
        val freeValuesForEachQuest = Int.MAX_VALUE / questTypeOrders.size
        /* quest ID is used to add values unique to each quest to make ordering consistent
           freeValuesForEachQuest is an int, so % freeValuesForEachQuest will fit into int */
        val hopefullyUniqueValueForQuest = (quest.id!! % freeValuesForEachQuest).toInt()
        return questTypeOrder * freeValuesForEachQuest + hopefullyUniqueValueForQuest
    }

    companion object {
        const val MARKER_QUEST_ID = "quest_id"
        const val MARKER_QUEST_GROUP = "quest_group"
        private const val TILES_ZOOM = 14
    }
}
