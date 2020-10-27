package de.westnordost.streetcomplete.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.mapzen.tangram.MapData
import com.mapzen.tangram.SceneUpdate
import com.mapzen.tangram.geometry.Point
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestGroup
import de.westnordost.streetcomplete.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.ktx.toDp
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.map.QuestPinLayerManager.Companion.MARKER_ELEMENT_ID
import de.westnordost.streetcomplete.map.QuestPinLayerManager.Companion.MARKER_QUEST_GROUP
import de.westnordost.streetcomplete.map.QuestPinLayerManager.Companion.MARKER_QUEST_ID
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.Marker
import de.westnordost.streetcomplete.map.tangram.toLngLat
import de.westnordost.streetcomplete.map.tangram.toTangramGeometry
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.util.centerPointOfPolygon
import de.westnordost.streetcomplete.util.distanceTo
import de.westnordost.streetcomplete.util.initialBearingTo
import de.westnordost.streetcomplete.util.translate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.math.*


/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment() {

    @Inject internal lateinit var spriteSheet: TangramQuestSpriteSheet
    @Inject internal lateinit var questPinLayerManager: QuestPinLayerManager

    // layers
    private var questsLayer: MapData? = null
    private var geometryLayer: MapData? = null
    private var additionalQuestHighlightingLayer: MapData? = null
    private var selectedQuestPinsLayer: MapData? = null

    private val questSelectionMarkers: MutableList<Marker> = mutableListOf()

    // markers: LatLon -> Marker Id
    private val markerIds: MutableMap<LatLon, Long> = HashMap()

    // for restoring position
    private var cameraPositionBeforeShowingQuest: CameraPosition? = null

    interface Listener {
        fun onClickedQuest(questGroup: QuestGroup, questId: Long)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)
        fun onClickedLocationMarker()
    }

    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(questPinLayerManager)
        questPinLayerManager.mapFragment = this
    }

    override fun onMapReady() {
        controller?.setPickRadius(1f)
        geometryLayer = controller?.addDataLayer(GEOMETRY_LAYER)
        additionalQuestHighlightingLayer = controller?.addDataLayer(ADDITIONAL_QUEST_HIGHLIGHTING_LAYER)
        questsLayer = controller?.addDataLayer(QUESTS_LAYER)
        selectedQuestPinsLayer = controller?.addDataLayer(SELECTED_QUESTS_LAYER)
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
        additionalQuestHighlightingLayer = null
        questsLayer = null
        selectedQuestPinsLayer = null
        questSelectionMarkers.clear()
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
            val pickedElementId = pickResult?.properties?.get(MARKER_ELEMENT_ID)?.toLong()
            val pickedQuestGroup = pickResult?.properties?.get(MARKER_QUEST_GROUP)?.let { QuestGroup.valueOf(it) }


            if (pickedQuestId != null && pickedQuestGroup != null) {
                listener?.onClickedQuest(pickedQuestGroup, pickedQuestId)
            } else if (pickedElementId != null && pickedQuestGroup != null) {
                val quests = questPinLayerManager.findQuestsBelongingToOsmElementId(pickedElementId)
                val adapter = object : ArrayAdapter<Quest>(
                    requireContext(),
                    android.R.layout.select_dialog_item,
                    android.R.id.text1,
                    quests) {

                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val v: View = super.getView(position, convertView, parent)
                        val tv: TextView = v.findViewById(android.R.id.text1)
                        tv.text = resources.getString(quests[position].type.title)

                        val dr = ContextCompat.getDrawable(context, quests[position].type.icon)
                        val bitmap = Bitmap.createBitmap(dr!!.intrinsicWidth, dr!!.intrinsicHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        dr.setBounds(0, 0, canvas.width, canvas.height);
                        dr.draw(canvas);

                        val size = tv.textSize * 1.5
                        val drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, size.toInt(), size.toInt(), true))

                        tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                        tv.compoundDrawablePadding = (12 * resources.displayMetrics.density).toInt()
                        return v
                    }
                }

                AlertDialog.Builder(requireContext())
                    .setTitle("Multiple quests available here. Choose one:")
                    .setAdapter(adapter) { _, item ->
                        listener?.onClickedQuest(pickedQuestGroup, quests[item].id!!)
                    }.show()
            } else {
                val pickMarkerResult = controller?.pickMarker(x,y)
                if (pickMarkerResult == null) {
                    onClickedMap(x, y)
                }
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

    fun startFocusQuest(quest: Quest, offset: RectF) {
        zoomAndMoveToContain(quest.geometry, offset)
        showQuestSelectionMarkers(quest.markerLocations)
        putSelectedQuestPins(quest)
        putQuestGeometry(quest.geometry)
    }

    /** Clear focus on current quest but do not return to normal view yet */
    fun clearFocusQuest() {
        removeQuestGeometry()
        clearMarkersForCurrentQuest()
        hideQuestSelectionMarkers()
        removeSelectedQuestPins()
    }

    fun endFocusQuest() {
        clearFocusQuest()
        restoreCameraPosition()
        centerCurrentPositionIfFollowing()
    }

    private fun zoomAndMoveToContain(g: ElementGeometry, offset: RectF) {
        val controller = controller ?: return
        val pos = controller.getEnclosingCameraPosition(g.getBounds(), offset) ?: return
        val currentPos = controller.cameraPosition
        val targetZoom = min(pos.zoom, 20f)

        // do not zoom in if the element is already nicely in the view
        if (screenAreaContains(g, RectF()) && targetZoom - currentPos.zoom < 2) return

        cameraPositionBeforeShowingQuest = currentPos

        val zoomTime = max(450L, (abs(currentPos.zoom - targetZoom) * 300).roundToLong())

        controller.updateCameraPosition(zoomTime, DecelerateInterpolator()) {
            position = pos.position
            zoom = targetZoom
        }
    }

    private fun screenAreaContains(g: ElementGeometry, offset: RectF): Boolean {
        val controller = controller ?: return false
        val p = PointF()
        return when (g) {
            is ElementPolylinesGeometry -> g.polylines
            is ElementPolygonsGeometry -> g.polygons
            else -> listOf(listOf(g.center))
        }.flatten().all {
            val isContained = controller.latLonToScreenPosition(it, p, false)
            isContained && p.x >= offset.left && p.x <= mapView.width - offset.right
                && p.y >= offset.top && p.y <= mapView.height - offset.bottom
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

    /* ---------------------------------  Selected quest pins ----------------------------------- */

    private fun createQuestSelectionMarker(): Marker? {
        val ctx = context ?: return null

        val frame = ctx.resources.getBitmapDrawable(R.drawable.quest_selection_ring)
        val w = frame.intrinsicWidth.toFloat().toDp(ctx)
        val h = frame.intrinsicHeight.toFloat().toDp(ctx)

        val marker = controller?.addMarker() ?: return null
        marker.setStylingFromString(
            "{ style: 'quest-selection', color: 'white', size: [${w}px, ${h}px], flat: false, collide: false, offset: ['1px', '-78px'] }"
        )
        marker.setDrawable(frame)
        return marker
    }

    private fun showQuestSelectionMarkers(positions: Collection<LatLon>) {
        while (positions.size > questSelectionMarkers.size) {
            val marker = createQuestSelectionMarker() ?: return
            questSelectionMarkers.add(marker)
        }
        positions.forEachIndexed { index, pos ->
            val marker = questSelectionMarkers[index]
            marker.setPoint(pos)
            marker.isVisible = true
        }
    }

    private fun hideQuestSelectionMarkers() {
        questSelectionMarkers.forEach { it.isVisible = false }
    }

    private fun putSelectedQuestPins(quest: Quest) {
        val questIconName = resources.getResourceEntryName(quest.type.icon)
        val positions = quest.markerLocations
        val points = positions.map { position ->
            val properties = mapOf(
                "type" to "point",
                "kind" to questIconName
            )
            Point(position.toLngLat(), properties)
        }
        selectedQuestPinsLayer?.setFeatures(points)
    }

    private fun removeSelectedQuestPins() {
        selectedQuestPinsLayer?.clear()
    }

    /* ------------------------------  Geometry for current quest ------------------------------- */

    private fun putQuestGeometry(geometry: ElementGeometry) {
        geometryLayer?.setFeatures(geometry.toTangramGeometry())
    }

    fun highlightSidewalkForQuest(quest: Quest, sidewalkSide: AbstractQuestAnswerFragment.Listener.SidewalkSide) {
        val questGeometry = quest.geometry
        if (questGeometry is ElementPolylinesGeometry) {
            val sidewalkPolyline =
                if (sidewalkSide == AbstractQuestAnswerFragment.Listener.SidewalkSide.LEFT)
                    questGeometry.polylines.first().translateToLeft(1.0)
                else
                    questGeometry.polylines.first().translateToRight(1.0)

            val newGeometry = ElementPolylinesGeometry(listOf(sidewalkPolyline), sidewalkPolyline.centerPointOfPolygon())
            additionalQuestHighlightingLayer?.setFeatures(newGeometry.toTangramGeometry())
            controller?.requestRender()
        }
    }

    private fun List<LatLon>.translateToLeft(distance: Double): List<LatLon> {
        return translate(distance, 270.0)
    }

    private fun List<LatLon>.translateToRight(distance: Double): List<LatLon> {
        return translate(distance, 90.0)
    }

    private fun List<LatLon>.translate(distance: Double, offsetAngle: Double): List<LatLon> {
        require(isNotEmpty()) { "list is empty" }
        val result = mutableListOf<LatLon>()

        val it = iterator()
        if (!it.hasNext()) {
            return result
        }

        var first = it.next()
        var previousPairAngle: Double? = null

        while (it.hasNext()) {
            val second = it.next()

            val currentPairAngle = (first.initialBearingTo(second).toFloat() + offsetAngle) % 360.0
            if (previousPairAngle == null) {
                result.add(first.translate(distance, currentPairAngle))
                if (!it.hasNext()) {
                    result.add(second.translate(distance, currentPairAngle))
                }
            } else if (!it.hasNext()) {
                result.add(first.translate(distance, calcAvgAngle(previousPairAngle, currentPairAngle)))
                result.add(second.translate(distance, currentPairAngle))
            } else {
                result.add(first.translate(distance, calcAvgAngle(previousPairAngle, currentPairAngle)))
            }
            previousPairAngle = currentPairAngle

            first = second
        }
        return result
    }

    private fun calcAvgAngle(a1: Double, a2: Double): Double {
        val a1Rad = Math.toRadians(a1)
        val a2Rad = Math.toRadians(a2)
        val x = cos(a1Rad) + cos(a2Rad)
        val y = sin(a1Rad) + sin(a2Rad)
        return Math.toDegrees(atan2(y, x))
    }

    private fun removeQuestGeometry() {
        geometryLayer?.clear()
        additionalQuestHighlightingLayer?.clear()
    }

    /* -------------------------  Markers for current quest (split way) ------------------------- */

    fun putMarkerForCurrentQuest(pos: LatLon) {
        deleteMarkerForCurrentQuest(pos)
        val marker = controller?.addMarker() ?: return
        marker.setDrawable(R.drawable.crosshair_marker)
        marker.setStylingFromString("{ style: 'points', color: 'red', size: 32px, order: 1, collide: false }")
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
        // see streetcomplete.yaml for the definitions of the below layers
        private const val GEOMETRY_LAYER = "streetcomplete_geometry"
        private const val ADDITIONAL_QUEST_HIGHLIGHTING_LAYER = "additional_quest_highlighting_layer"
        private const val QUESTS_LAYER = "streetcomplete_quests"
        private const val SELECTED_QUESTS_LAYER = "streetcomplete_selected_quests"
        private const val CLICK_AREA_SIZE_IN_DP = 48
    }
}
