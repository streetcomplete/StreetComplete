package de.westnordost.streetcomplete.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.ktx.toPx
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.map.components.ElementGeometryMapComponent
import de.westnordost.streetcomplete.map.components.PinsMapComponent
import de.westnordost.streetcomplete.map.components.PointMarkersMapComponent
import de.westnordost.streetcomplete.util.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment() {

    @Inject internal lateinit var spriteSheet: TangramPinsSpriteSheet
    @Inject internal lateinit var questTypeOrderSource: QuestTypeOrderSource
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var visibleQuestsSource: VisibleQuestsSource
    @Inject internal lateinit var editHistorySource: EditHistorySource
    @Inject internal lateinit var mapDataSource: MapDataWithEditsSource

    private var pointMarkersMapComponent: PointMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var geometryMapComponent: ElementGeometryMapComponent? = null
    private var questPinsManager: QuestPinsManager? = null
    private var editHistoryPinsManager: EditHistoryPinsManager? = null

    interface Listener {
        fun onClickedQuest(questKey: QuestKey)
        fun onClickedEdit(editKey: EditKey)
        fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    enum class PinMode { NONE, QUESTS, EDITS }
    var pinMode: PinMode = PinMode.QUESTS
        set(value) {
            if (field == value) return
            field = value
            updatePinMode()
        }

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

        questPinsManager = QuestPinsManager(ctrl, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, resources, visibleQuestsSource)
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)
        questPinsManager!!.isVisible = pinMode == PinMode.QUESTS

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource, resources)
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)
        editHistoryPinsManager!!.isVisible = pinMode == PinMode.EDITS

        super.onMapReady()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
    }

    /* ------------------------------------- Map setup ------------------------------------------ */

    override suspend fun onBeforeLoadScene() {
        super.onBeforeLoadScene()
        val questSceneUpdates = withContext(Dispatchers.IO) { spriteSheet.sceneUpdates }
        sceneMapComponent?.putSceneUpdates(questSceneUpdates)
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
        viewLifecycleScope.launch {
            val props = controller?.pickLabel(x, y)?.properties

            val questKey = props?.let { questPinsManager?.getQuestKey(it) }
            if (questKey != null) {
                listener?.onClickedQuest(questKey)
                return@launch
            }
            val editKey = props?.let { editHistoryPinsManager?.getEditKey(it) }
            if (editKey != null) {
                listener?.onClickedEdit(editKey)
                return@launch
            }
            val pickMarkerResult = controller?.pickMarker(x,y)
            if (pickMarkerResult == null) {
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

    /* --------------------------------- Focusing on edit --------------------------------------- */

    fun startFocusEdit(edit: Edit, offset: RectF) {
        pinsMapComponent?.showSelectedPins(edit.icon, listOf(edit.position))
        geometryMapComponent?.beginFocusGeometry(ElementPointGeometry(edit.position), offset)
        geometryMapComponent?.showGeometry(edit.getGeometry())
    }

    fun endFocusEdit() {
        pinsMapComponent?.clearSelectedPins()
        geometryMapComponent?.endFocusGeometry(returnToPreviousPosition = false)
        geometryMapComponent?.clearGeometry()
    }

    private fun Edit.getGeometry(): ElementGeometry = when(this) {
        is ElementEdit -> originalGeometry
        is OsmQuestHidden -> mapDataSource.getGeometry(elementType, elementId)
        else -> null
    } ?: ElementPointGeometry(position)

    /* -------------------------------- Highlighting element ------------------------------------ */

    fun highlightElement(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
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
        pointMarkersMapComponent?.clear()
    }

    fun endFocusQuest() {
        clearFocusQuest()
        viewLifecycleScope.launch {
            /* small delay to wait for other animations when ending focus on quest to be done first
               Most specifically, the map is being updated after a quest is solved, if the zoom
               out animation already starts while the map is being updated, there can be a little
               lag/jump which is not visually pleasing.
             */
            delay(150)
            geometryMapComponent?.endFocusGeometry()
        }
        centerCurrentPositionIfFollowing()
    }

    /* -------------------------  Markers for current quest (split way) ------------------------- */

    fun putMarkerForCurrentQuest(pos: LatLon, @DrawableRes drawableResId: Int) {
        pointMarkersMapComponent?.put(pos, drawableResId)
    }

    fun deleteMarkerForCurrentQuest(pos: LatLon) {
        pointMarkersMapComponent?.delete(pos)
    }

    /* --------------------- Switching between quests and edit history pins --------------------- */

    private fun updatePinMode() {
        /* both managers use the same resource (PinsMapComponent), so the newly visible manager
           may only be activated after the old has been deactivated
         */
        pointMarkersMapComponent?.clear()
        pinsMapComponent?.clearSelectedPins()
        geometryMapComponent?.endFocusGeometry(returnToPreviousPosition = false)
        geometryMapComponent?.clearGeometry()

        when (pinMode) {
            PinMode.QUESTS -> {
                editHistoryPinsManager?.isVisible = false
                questPinsManager?.isVisible = true
            }
            PinMode.EDITS -> {
                questPinsManager?.isVisible = false
                editHistoryPinsManager?.isVisible = true
            }
            else -> {
                questPinsManager?.isVisible = false
                editHistoryPinsManager?.isVisible = false
            }
        }
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
