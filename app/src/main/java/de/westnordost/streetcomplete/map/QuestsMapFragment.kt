package de.westnordost.streetcomplete.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
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
import de.westnordost.streetcomplete.ktx.dpToPx
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.map.components.FocusGeometryMapComponent
import de.westnordost.streetcomplete.map.components.GeometryMarkersMapComponent
import de.westnordost.streetcomplete.map.components.PinsMapComponent
import de.westnordost.streetcomplete.map.components.SelectedPinsMapComponent
import de.westnordost.streetcomplete.quests.ShowsGeometryMarkers
import de.westnordost.streetcomplete.util.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** Manages a map that shows the quest pins, quest geometry */
class QuestsMapFragment : LocationAwareMapFragment(), ShowsGeometryMarkers {

    private val spriteSheet: TangramPinsSpriteSheet by inject()
    private val questTypeOrderSource: QuestTypeOrderSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val editHistorySource: EditHistorySource by inject()
    private val mapDataSource: MapDataWithEditsSource by inject()

    private var geometryMarkersMapComponent: GeometryMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var selectedPinsMapComponent: SelectedPinsMapComponent? = null
    private var geometryMapComponent: FocusGeometryMapComponent? = null
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

    override suspend fun onMapReady() {
        val ctrl = controller ?: return
        ctrl.setPickRadius(1f)
        geometryMarkersMapComponent = GeometryMarkersMapComponent(resources, ctrl)
        pinsMapComponent = PinsMapComponent(ctrl)
        selectedPinsMapComponent = SelectedPinsMapComponent(requireContext(), ctrl)
        geometryMapComponent = FocusGeometryMapComponent(ctrl)

        questPinsManager = QuestPinsManager(ctrl, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, resources, visibleQuestsSource)
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)
        questPinsManager!!.isActive = pinMode == PinMode.QUESTS

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource, resources)
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)
        editHistoryPinsManager!!.isActive = pinMode == PinMode.EDITS

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
            val pickMarkerResult = controller?.pickMarker(x, y)
            if (pickMarkerResult == null) {
                onClickedMap(x, y)
            }
        }
        return true
    }

    private fun onClickedMap(x: Float, y: Float) {
        val context = context ?: return

        val clickPos = controller?.screenPositionToLatLon(PointF(x, y)) ?: return

        val fingerRadius = context.dpToPx(CLICK_AREA_SIZE_IN_DP) / 2
        val fingerEdgeClickPos = controller?.screenPositionToLatLon(PointF(x + fingerRadius, y)) ?: return
        val fingerRadiusInMeters = clickPos.distanceTo(fingerEdgeClickPos)

        listener?.onClickedMapAt(clickPos, fingerRadiusInMeters)
    }

    /* --------------------------------- Focusing on edit --------------------------------------- */

    fun startFocusEdit(edit: Edit, offset: RectF) {
        geometryMapComponent?.beginFocusGeometry(ElementPointGeometry(edit.position), offset)
        geometryMapComponent?.showGeometry(edit.getGeometry())
        selectedPinsMapComponent?.set(edit.icon, listOf(edit.position))
    }

    fun endFocusEdit() {
        selectedPinsMapComponent?.clear()
        geometryMapComponent?.endFocusGeometry(returnToPreviousPosition = false)
        geometryMapComponent?.clearGeometry()
    }

    private fun Edit.getGeometry(): ElementGeometry = when (this) {
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
        selectedPinsMapComponent?.set(quest.type.icon, quest.markerLocations)
        // while quest is focused, we actually don't want to see all the other quest pins (since v38)
        pinsMapComponent?.isVisible = false
    }

    /** Clear focus on current quest but do not return to normal view yet */
    fun clearFocusQuest() {
        selectedPinsMapComponent?.clear()
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
    }

    fun endFocusQuest() {
        pinsMapComponent?.isVisible = true
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

    /* -------------------------------  Markers for current quest ------------------------------- */

    override fun putMarkerForCurrentQuest(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    ) {
        geometryMarkersMapComponent?.put(geometry, drawableResId, title)
    }

    override fun deleteMarkerForCurrentQuest(geometry: ElementGeometry) {
        geometryMarkersMapComponent?.delete(geometry)
    }

    override fun clearMarkersForCurrentQuest() {
        geometryMarkersMapComponent?.clear()
    }

    /* --------------------- Switching between quests and edit history pins --------------------- */

    private fun updatePinMode() {
        /* both managers use the same resource (PinsMapComponent), so the newly visible manager
           may only be activated after the old has been deactivated
         */
        geometryMarkersMapComponent?.clear()
        selectedPinsMapComponent?.clear()
        geometryMapComponent?.endFocusGeometry(returnToPreviousPosition = false)
        geometryMapComponent?.clearGeometry()

        when (pinMode) {
            PinMode.QUESTS -> {
                editHistoryPinsManager?.isActive = false
                questPinsManager?.isActive = true
            }
            PinMode.EDITS -> {
                questPinsManager?.isActive = false
                editHistoryPinsManager?.isActive = true
            }
            else -> {
                questPinsManager?.isActive = false
                editHistoryPinsManager?.isActive = false
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
