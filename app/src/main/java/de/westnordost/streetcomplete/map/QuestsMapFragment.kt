package de.westnordost.streetcomplete.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.lifecycle.lifecycleScope
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderList
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.map.components.ElementGeometryMapComponent
import de.westnordost.streetcomplete.map.components.PinsMapComponent
import de.westnordost.streetcomplete.map.components.PointMarkersMapComponent
import de.westnordost.streetcomplete.util.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment() {

    @Inject internal lateinit var spriteSheet: TangramPinsSpriteSheet
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var questTypeOrderList: QuestTypeOrderList
    @Inject internal lateinit var visibleQuestsSource: VisibleQuestsSource

    private var pointMarkersMapComponent: PointMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var geometryMapComponent: ElementGeometryMapComponent? = null
    private var questPinLayerManager: QuestPinLayerManager? = null

    interface Listener {
        fun onClickedQuest(questKey: QuestKey)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        Injector.applicationComponent.inject(this)
    }

    override suspend fun onMapReady() {
        val ctrl = controller ?: return
        ctrl.setPickRadius(1f)
        pointMarkersMapComponent = PointMarkersMapComponent(ctrl)
        pinsMapComponent = PinsMapComponent(requireContext(), ctrl)
        geometryMapComponent = ElementGeometryMapComponent(ctrl)

        questPinLayerManager = QuestPinLayerManager(ctrl, pinsMapComponent!!, questTypeRegistry, questTypeOrderList, resources, visibleQuestsSource)
        lifecycle.addObserver(questPinLayerManager!!)

        super.onMapReady()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinLayerManager?.onNewScreenPosition()
    }

    /* ------------------------------------- Map setup ------------------------------------------ */

    override suspend fun onBeforeLoadScene() {
        super.onBeforeLoadScene()
        val questSceneUpdates = withContext(Dispatchers.IO) { spriteSheet.sceneUpdates }
        sceneMapComponent?.putSceneUpdates(questSceneUpdates)
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
        lifecycleScope.launch {
            val props = controller?.pickLabel(x, y)?.properties

            val questKey = props?.let { questPinLayerManager?.getQuestKey(it) }
            if (questKey != null) {
                listener?.onClickedQuest(questKey)
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
        geometryMapComponent?.beginFocusGeometry(quest.geometry, offset)
        geometryMapComponent?.showGeometry(quest.geometry)
        pinsMapComponent?.showSelectedPins(quest.type.icon, quest.markerLocations)
    }

    /** Clear focus on current quest but do not return to normal view yet */
    fun clearFocusQuest() {
        pinsMapComponent?.clearSelectedPins()
        geometryMapComponent?.clearGeometry()
        clearMarkersForCurrentQuest()
    }

    fun endFocusQuest() {
        clearFocusQuest()
        geometryMapComponent?.endFocusGeometry()
        centerCurrentPositionIfFollowing()
    }

    /* --------------------------------------  Quest Pins --------------------------------------- */

    var isShowingQuestPins: Boolean
        get() = questPinLayerManager?.isVisible == true
        set(value) { questPinLayerManager?.isVisible = value }

    /* -------------------------  Markers for current quest (split way) ------------------------- */

    fun putMarkerForCurrentQuest(pos: LatLon, @DrawableRes drawableResId: Int) {
        pointMarkersMapComponent?.put(pos, drawableResId)
    }

    fun deleteMarkerForCurrentQuest(pos: LatLon) {
        pointMarkersMapComponent?.delete(pos)
    }

    private fun clearMarkersForCurrentQuest() {
        pointMarkersMapComponent?.clear()
    }

    /* --------------------------------- Position tracking -------------------------------------- */

    override fun shouldCenterCurrentPosition(): Boolean =
        // don't center position while displaying a quest
        super.shouldCenterCurrentPosition() && geometryMapComponent?.isZoomedToContainGeometry != true

    companion object {
        // see streetcomplete.yaml for the definitions of the below layers
        private const val CLICK_AREA_SIZE_IN_DP = 48
    }
}
