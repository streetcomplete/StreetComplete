package de.westnordost.streetcomplete.map

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import androidx.core.graphics.toRectF
import com.mapzen.tangram.*
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.map.QuestPinCollection.Companion.MARKER_QUEST_GROUP
import de.westnordost.streetcomplete.map.QuestPinCollection.Companion.MARKER_QUEST_ID
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.util.SlippyMapMath
import de.westnordost.streetcomplete.util.SphericalEarthMath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment() {

    // layers
    private var questsLayer: MapData? = null
    private var geometryLayer: MapData? = null

    // markers: LatLon -> Marker Id
    private val markerIds: MutableMap<LatLon, Long> = HashMap()

    private var cameraPositionBeforeShowingQuest: CameraPosition? = null

    private val retrievedTiles: MutableSet<Point> = mutableSetOf()

    private var lastDisplayedRect: Rect? = null

    private var listener: Listener? = null

    var questOffset: Rect = Rect(0, 0, 0, 0)

    @Inject internal lateinit var spriteSheet: TangramQuestSpriteSheet
    @Inject internal lateinit var questPinCollection: QuestPinCollection

    interface Listener {
        fun onClickedQuest(questGroup: QuestGroup?, questId: Long?)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)
        /** Called once the given bbox comes into view first (listener should get quests there)  */
        fun onFirstInView(bbox: BoundingBox?)
    }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? Listener ?: activity as? Listener
    }

    override fun onStart() {
        super.onStart()

        val displayedArea = controller?.screenAreaToBoundingBox(RectF())
        if (displayedArea != null) {
            val tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM)
            lastDisplayedRect = tilesRect
            updateQuestsInRect(tilesRect)
        }
    }

    override fun onMapControllerReady() {
        super.onMapControllerReady()
        controller?.setPickRadius(1f)
    }

    override fun onSceneReady() {
        retrievedTiles.clear()
        geometryLayer = controller?.addDataLayer(GEOMETRY_LAYER)
        questsLayer = controller?.addDataLayer(QUESTS_LAYER)
        super.onSceneReady()
    }

    override fun onStop() {
        super.onStop()
        /* When reentering the fragment, the database may have changed (quest download in
        *  background or change in settings), so the quests must be pulled from DB again */
        clearQuestPins()
    }

    override fun onDestroy() {
        super.onDestroy()
        geometryLayer = null
        questsLayer = null
    }

    /* ------------------------------------- Map setup ------------------------------------------ */

    override suspend fun getSceneUpdates(): List<SceneUpdate> {
        return super.getSceneUpdates() + withContext(Dispatchers.IO) { spriteSheet.get() }
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
        launch {
            val pickResult = controller?.pickLabel(x, y)

            val pickedQuestId = pickResult?.properties?.get(MARKER_QUEST_ID)?.toLong()
            val pickedQuestGroup = pickResult?.properties?.get(MARKER_QUEST_GROUP)?.let { QuestGroup.valueOf(it) }

            if (pickedQuestId != null && pickedQuestGroup != null) {
                listener?.onClickedQuest(pickedQuestGroup, pickedQuestId)
            } else {
                onClickedMap(x, y)
            }
        }
        return true
    }

    private fun onClickedMap(x: Float, y: Float) {
        val context = context ?: return

        val clickPos = controller?.screenPositionToLatLon(PointF(x, y)) ?: return

        val fingerRadius = CLICK_AREA_SIZE_IN_DP.toFloat().toPx(context) / 2
        val fingerEdgeClickPos = controller?.screenPositionToLatLon(PointF(x + fingerRadius, y)) ?: return
        val fingerRadiusInMeters = SphericalEarthMath.distance(clickPos, fingerEdgeClickPos)

        listener?.onClickedMapAt(clickPos, fingerRadiusInMeters)
    }

    /* --------------------------------- Focusing on quest -------------------------------------- */

    fun startFocusQuest(geometry: ElementGeometry) {
        zoomAndMoveToContain(geometry)
        putQuestGeometry(geometry)
    }

    fun endFocusQuest() {
        removeQuestGeometry()
        clearMarkersForCurrentQuest()
        restoreCameraPosition()
        followPosition()
    }

    /* ------------------------------  Geometry for current quest ------------------------------- */

    private fun putQuestGeometry(geometry: ElementGeometry) {
        geometryLayer?.setFeatures(geometry.toTangramGeometry())
    }

    private fun removeQuestGeometry() {
        geometryLayer?.clear()
    }

    /* -------------------------  Markers for current quest (split way) ------------------------- */

    fun putMarkerForCurrentQuest(pos: LatLon) {
        deleteMarkerForCurrentQuest(pos)
        val marker = controller?.addMarker() ?: return
        marker.setDrawable(R.drawable.crosshair_marker)
        marker.setStylingFromString("{ style: 'points', color: 'white', size: 48px, order: 2000, collide: false }")
        marker.setPoint(pos)
        markerIds[pos] = marker.markerId
    }

    fun deleteMarkerForCurrentQuest(pos: LatLon) {
        val markerId = markerIds[pos] ?: return
        controller?.removeMarker(markerId)
        markerIds.remove(pos)
    }

    fun clearMarkersForCurrentQuest() {
        for (markerId in markerIds.values) {
            controller?.removeMarker(markerId)
        }
        markerIds.clear()
    }

    /* ----------------------- Zoom to quest and restore camera position ------------------------ */

    private fun zoomAndMoveToContain(g: ElementGeometry) {
        val controller = controller ?: return

        cameraPositionBeforeShowingQuest = controller.cameraPosition

        var targetZoom = controller.getMaxZoomThatContainsGeometry(g, questOffset.toRectF())
        if (targetZoom == null || targetZoom > MAX_QUEST_ZOOM) {
            targetZoom = MAX_QUEST_ZOOM
        } else {
            // zoom out a bit
            targetZoom -= 0.4f
        }

        val currentZoom = controller.cameraPosition.zoom

        controller.updateCameraPosition { it.zoom = targetZoom }
        val pos = controller.getLatLonThatCentersGeometry(g, questOffset.toRectF())
        controller.updateCameraPosition { it.zoom = currentZoom }

        controller.updateCameraPosition(500) {
            if (pos != null) it.position = pos
            it.zoom = targetZoom
        }
    }

    private fun restoreCameraPosition() {
        val cameraPos = cameraPositionBeforeShowingQuest
        if (cameraPos != null) {
            controller?.updateCameraPosition {
                it.position = cameraPos.position
                it.rotation = cameraPos.rotation
                it.tilt = cameraPos.tilt
                it.zoom = cameraPos.zoom
            }
        }
        cameraPositionBeforeShowingQuest = null
    }

    /* --------------------------------  Quest Pin Layer on map --------------------------------- */

    override fun onMapIsChanging() {
        super.onMapIsChanging()
        if (controller?.cameraPosition?.zoom ?: 0f < TILES_ZOOM) return
        val displayedArea = controller?.screenAreaToBoundingBox(RectF()) ?: return
        val tilesRect = SlippyMapMath.enclosingTiles(displayedArea, TILES_ZOOM)
        if (lastDisplayedRect != tilesRect) {
            lastDisplayedRect = tilesRect
            updateQuestsInRect(tilesRect)
        }
    }

    private fun updateQuestsInRect(tilesRect: Rect) {
        // area too big -> skip
        Log.d("MAPFRAGMENT", "tiles:" + (tilesRect.width() * tilesRect.height()))
        if (tilesRect.width() * tilesRect.height() > 4) {
            return
        }
        val tiles = SlippyMapMath.asTileList(tilesRect)
        tiles.removeAll(retrievedTiles)
        val minRect = SlippyMapMath.minRect(tiles) ?: return
        val bbox = SlippyMapMath.asBoundingBox(minRect, TILES_ZOOM)
        listener?.onFirstInView(bbox)

        retrievedTiles.addAll(tiles)
    }

    var isShowingQuestPins: Boolean = true
    set(value) {
        if (field == value) return
        field = value

        if (!value) {
            questsLayer?.clear()
        }
        else {
            questsLayer?.setFeatures(questPinCollection.getPoints())
        }
    }

    fun addQuestPins(quests: Iterable<Quest>, group: QuestGroup) {
        for (quest in quests) {
            questPinCollection.add(quest, group)
        }
        if (isShowingQuestPins) questsLayer?.setFeatures(questPinCollection.getPoints())
    }

    fun removeQuestPins(questIds: Collection<Long>, group: QuestGroup) {
        for (questId in questIds) {
            questPinCollection.remove(questId, group)
        }
        if (isShowingQuestPins) questsLayer?.setFeatures(questPinCollection.getPoints())
    }

    private fun clearQuestPins() {
        questsLayer?.clear()
        questPinCollection.clear()
        lastDisplayedRect = null
    }

    /* --------------------------------- Position tracking -------------------------------------- */

    override fun shouldCenterCurrentPosition(): Boolean {
        // don't center position while displaying a quest
        return super.shouldCenterCurrentPosition() && cameraPositionBeforeShowingQuest == null
    }

    companion object {
        private const val GEOMETRY_LAYER = "streetcomplete_geometry"
        private const val QUESTS_LAYER = "streetcomplete_quests"
        private const val TILES_ZOOM = 14
        private const val MAX_QUEST_ZOOM = 19f
        private const val CLICK_AREA_SIZE_IN_DP = 48
    }
}
