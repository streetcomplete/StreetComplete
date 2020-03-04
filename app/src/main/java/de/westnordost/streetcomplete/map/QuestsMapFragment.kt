package de.westnordost.streetcomplete.map

import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.mapzen.tangram.MapData
import com.mapzen.tangram.SceneUpdate
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.map.QuestPinLayerManager.Companion.MARKER_QUEST_GROUP
import de.westnordost.streetcomplete.map.QuestPinLayerManager.Companion.MARKER_QUEST_ID
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.util.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment() {

    @Inject internal lateinit var spriteSheet: TangramQuestSpriteSheet
    @Inject internal lateinit var questPinLayerManager: QuestPinLayerManager

    // layers
    private var questsLayer: MapData? = null
    private var geometryLayer: MapData? = null

    // markers: LatLon -> Marker Id
    private val markerIds: MutableMap<LatLon, Long> = HashMap()

    // for restoring position
    private var cameraPositionBeforeShowingQuest: CameraPosition? = null

    interface Listener {
        fun onClickedQuest(questGroup: QuestGroup, questId: Long)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(questPinLayerManager)
        questPinLayerManager.mapFragment = this
    }

    override fun onMapReady() {
        controller?.setPickRadius(1f)
        geometryLayer = controller?.addDataLayer(GEOMETRY_LAYER)
        questsLayer = controller?.addDataLayer(QUESTS_LAYER)
        questPinLayerManager.questsLayer = questsLayer
        super.onMapReady()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinLayerManager.onNewScreenPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        geometryLayer = null
        questsLayer = null
    }

    /* ------------------------------------- Map setup ------------------------------------------ */

    override suspend fun getSceneUpdates(): List<SceneUpdate> {
        return super.getSceneUpdates() + withContext(Dispatchers.IO) { spriteSheet.sceneUpdates }
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
        val fingerRadiusInMeters = clickPos.distanceTo(fingerEdgeClickPos)

        listener?.onClickedMapAt(clickPos, fingerRadiusInMeters)
    }

    /* --------------------------------- Focusing on quest -------------------------------------- */

    fun startFocusQuest(geometry: ElementGeometry, offset: RectF) {
        zoomAndMoveToContain(geometry, offset)
        putQuestGeometry(geometry)
    }

    fun endFocusQuest() {
        removeQuestGeometry()
        clearMarkersForCurrentQuest()
        restoreCameraPosition()
        followPosition()
    }

    private fun zoomAndMoveToContain(g: ElementGeometry, offset: RectF) {
        val controller = controller ?: return
        val pos = controller.getEnclosingCameraPosition(g.getBounds(), offset) ?: return

        val currentPos = controller.cameraPosition
        cameraPositionBeforeShowingQuest = currentPos

        val zoomTime = max(300L, (abs(currentPos.zoom - pos.zoom) * 300).roundToLong())

        controller.updateCameraPosition(zoomTime, DecelerateInterpolator()) {
            position = pos.position
        }
        controller.updateCameraPosition(zoomTime, AccelerateDecelerateInterpolator()) {
            zoom = pos.zoom
        }
    }

    private fun restoreCameraPosition() {
        val controller = controller ?: return

        val pos = cameraPositionBeforeShowingQuest
        if (pos != null) {
            val currentPos = controller.cameraPosition
            val zoomTime = max(300L, (abs(currentPos.zoom - pos.zoom) * 300).roundToLong())

            controller.updateCameraPosition(zoomTime, AccelerateDecelerateInterpolator()) {
                position = pos.position
            }
            controller.updateCameraPosition(zoomTime, DecelerateInterpolator()) {
                zoom = pos.zoom
                tilt = pos.tilt
                rotation = pos.rotation
            }
        }
        cameraPositionBeforeShowingQuest = null
    }

    /* --------------------------------------  Quest Pins --------------------------------------- */

    var isShowingQuestPins: Boolean
        get() = questPinLayerManager.isVisible
        set(value) { questPinLayerManager.isVisible = value }

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

    /* --------------------------------- Position tracking -------------------------------------- */

    override fun shouldCenterCurrentPosition(): Boolean {
        // don't center position while displaying a quest
        return super.shouldCenterCurrentPosition() && cameraPositionBeforeShowingQuest == null
    }

    companion object {
        private const val GEOMETRY_LAYER = "streetcomplete_geometry"
        private const val QUESTS_LAYER = "streetcomplete_quests"
        private const val CLICK_AREA_SIZE_IN_DP = 48
    }
}
