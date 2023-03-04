package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.plugins.annotation.FillManager
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.GeometryMarkersMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.SelectedPinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.math.distanceTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** This is the map shown in the main view. It manages a map that shows the quest pins, quest
 *  geometry, overlays... */
class MainMapFragment : LocationAwareMapFragment(), ShowsGeometryMarkers {

    private val questPinsSpriteSheet: TangramPinsSpriteSheet by inject()
    private val iconsSpriteSheet: TangramIconsSpriteSheet by inject()
    private val questTypeOrderSource: QuestTypeOrderSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val editHistorySource: EditHistorySource by inject()
    private val mapDataSource: MapDataWithEditsSource by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()

    private var geometryMarkersMapComponent: GeometryMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var selectedPinsMapComponent: SelectedPinsMapComponent? = null
    private var geometryMapComponent: FocusGeometryMapComponent? = null
    private var questPinsManager: QuestPinsManager? = null
    private var editHistoryPinsManager: EditHistoryPinsManager? = null
    private var styleableOverlayMapComponent: StyleableOverlayMapComponent? = null
    private var styleableOverlayManager: StyleableOverlayManager? = null

    interface Listener {
        fun onClickedQuest(questKey: QuestKey)
        fun onClickedEdit(editKey: EditKey)
        fun onClickedElement(elementKey: ElementKey)
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

    private var overlaySceneUpdates: List<Pair<String, String>>? = null

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            val new = selectedOverlaySource.selectedOverlay?.sceneUpdates
            val old = overlaySceneUpdates
            if (old == new) return

            old?.let { sceneMapComponent?.removeSceneUpdates(it) }
            new?.let { sceneMapComponent?.addSceneUpdates(it) }

            if (old != null || new != null) {
                viewLifecycleScope.launch { sceneMapComponent?.loadScene() }
            }
            overlaySceneUpdates = new
        }
    }

    /* ------------------------------------- Map setup ------------------------------------------ */

    override suspend fun onBeforeLoadScene() {
        super.onBeforeLoadScene()
        val sceneUpdates = withContext(Dispatchers.IO) {
            questPinsSpriteSheet.sceneUpdates + iconsSpriteSheet.sceneUpdates
        }
        sceneMapComponent?.addSceneUpdates(sceneUpdates)

        overlaySceneUpdates = selectedOverlaySource.selectedOverlay?.sceneUpdates
        overlaySceneUpdates?.let { sceneMapComponent?.addSceneUpdates(it) }
    }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override suspend fun onMapReady(mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        val ctrl = controller ?: return
        ctrl.setPickRadius(1f)
        geometryMarkersMapComponent = GeometryMarkersMapComponent(resources, ctrl)

        // todo: here we should use pins, not round icons
        //  just create them as LayerDrawable?
        questTypeRegistry.forEach { style.addImage(resources.getResourceEntryName(it.icon), resources.getBitmapDrawable(it.icon)) }

        val symbolManager = initializeSymbolManager(mapView, mapboxMap, style)
        pinsMapComponent = PinsMapComponent(ctrl, symbolManager)
        selectedPinsMapComponent = SelectedPinsMapComponent(requireContext(), ctrl)
        geometryMapComponent = FocusGeometryMapComponent(ctrl, mapboxMap)

        questPinsManager = QuestPinsManager(ctrl, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, resources, visibleQuestsSource)
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)
        questPinsManager!!.isVisible = pinMode == PinMode.QUESTS

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource, resources)
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)
        editHistoryPinsManager!!.isVisible = pinMode == PinMode.EDITS

        styleableOverlayMapComponent = StyleableOverlayMapComponent(resources, ctrl)
        styleableOverlayManager = StyleableOverlayManager(ctrl, styleableOverlayMapComponent!!, mapDataSource, selectedOverlaySource)
        viewLifecycleOwner.lifecycle.addObserver(styleableOverlayManager!!)

        selectedOverlaySource.addListener(overlayListener)

        super.onMapReady(mapView, mapboxMap, style)

        mapboxMap.addOnMapClickListener { pos ->
            listener?.onClickedMapAt(LatLon(pos.latitude, pos.longitude), 1.0)
            false
        }
        mapboxMap.addOnMapLongClickListener { pos ->
            // how to convert position to (whatever is expected here)?
            // these float values are actually used
//            onLongPress()
            true
        }

        MainMapFragment.mapView = mapView
        MainMapFragment.mapboxMap = mapboxMap
        MainMapFragment.symbolManager = symbolManager
        MainMapFragment.style = style
        lineManager = LineManager(mapView, mapboxMap, style)
        fillManager = FillManager(mapView, mapboxMap, style)
        circleManager = CircleManager(mapView, mapboxMap, style)
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
        styleableOverlayManager?.onNewScreenPosition()
    }

    override fun onDestroy() {
        super.onDestroy()
        selectedOverlaySource.removeListener(overlayListener)
        mapboxMap = null
        mapView = null
        symbolManager = null
        lineManager = null
        fillManager = null
        circleManager = null
        style = null
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */

    // todo: overlay stuff
    private fun initializeSymbolManager(mapView: MapView, mapboxMap: MapboxMap, style: Style): SymbolManager {
        val sm = SymbolManager(mapView, mapboxMap, style)
        sm.addClickListener { symbol ->
            viewLifecycleScope.launch {
                when (pinMode) {
                    PinMode.QUESTS -> {
                        val questKey = symbol.data?.toQuestKey()
                        if (questKey != null) {
                            listener?.onClickedQuest(questKey)
                            return@launch
                        }
                    }
                    PinMode.EDITS -> {
                        val editKey = symbol.data?.toEditKey()
                        if (editKey != null) {
                            listener?.onClickedEdit(editKey)
                            return@launch
                        }
                    }
                    PinMode.NONE -> {}
                }
            }
            return@addClickListener true
        }
        return sm
    }

    override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
        viewLifecycleScope.launch {

            if (pinsMapComponent?.isVisible == true) {
                when (pinMode) {
                    PinMode.QUESTS -> {
                        val props = controller?.pickLabel(x, y)?.properties
                        val questKey = props?.let { questPinsManager?.getQuestKey(it) }
                        if (questKey != null) {
                            listener?.onClickedQuest(questKey)
                            return@launch
                        }
                    }
                    PinMode.EDITS -> {
                        val props = controller?.pickLabel(x, y)?.properties
                        val editKey = props?.let { editHistoryPinsManager?.getEditKey(it) }
                        if (editKey != null) {
                            listener?.onClickedEdit(editKey)
                            return@launch
                        }
                    }
                    PinMode.NONE -> {}
                }
            }

            if (styleableOverlayMapComponent?.isVisible == true) {
                if (selectedOverlaySource.selectedOverlay != null) {
                    val props = controller?.pickLabel(x, y)?.properties
                        ?: controller?.pickFeature(x, y)?.properties
                    val elementKey = props?.let { styleableOverlayMapComponent?.getElementKey(it) }
                    if (elementKey != null) {
                        listener?.onClickedElement(elementKey)
                        return@launch
                    }
                }
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

    /* --------------------------- Focusing on edit/quest/element ------------------------------- */

    /** Focus the view on the given geometry */
    fun startFocus(geometry: ElementGeometry, offset: RectF) {
        geometryMapComponent?.beginFocusGeometry(geometry, offset)
    }

    /** End the focussing but do not return to position before focussing */
    fun clearFocus() {
        geometryMapComponent?.clearFocusGeometry()
        centerCurrentPositionIfFollowing()
    }

    /** return to the position before focussing */
    fun endFocus() {
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

    fun highlightPins(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        selectedPinsMapComponent?.set(iconResId, pinPositions)
    }

    fun hideNonHighlightedPins() {
        pinsMapComponent?.isVisible = false
    }

    fun hideOverlay() {
        styleableOverlayMapComponent?.isVisible = false
        styleableOverlayMapComponent?.isVisible = false
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
        when (geometry) {
            is ElementPolylinesGeometry -> {
                val lo = LineOptions()
                    // todo: this is only a single line
                    .withLatLngs(geometry.polylines.first().map { LatLng(it.latitude, it.longitude) })
                    .withLineColor("#ff0000")
                    .withLineWidth(4f)
                    .withLineOffset(5f)
                    .withLineOpacity(0.5f)
                lineManager?.create(lo)
            }
            is ElementPolygonsGeometry -> {
                fillManager?.create(FillOptions()
                    .withLatLngs(geometry.polygons.map { it.map { LatLng(it.latitude, it.longitude) } })
                    .withFillOutlineColor("#ff0000")
                    .withFillColor("#00ff00")
                    .withFillOpacity(0.3f)
                )
            }
            is ElementPointGeometry -> {
                circleManager?.create(CircleOptions()
                    .withCircleColor("#0000ff")
                    .withLatLng(LatLng(geometry.center.latitude, geometry.center.longitude))
                    .withCircleRadius(10f)
                    .withCircleOpacity(0.7f)
                )
            }
        }
    }

    /** Clear all highlighting */
    fun clearHighlighting() {
        pinsMapComponent?.isVisible = true
        styleableOverlayMapComponent?.isVisible = true
        selectedPinsMapComponent?.clear()
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
        lineManager?.deleteAll()
        circleManager?.deleteAll()
        fillManager?.deleteAll()
    }

    fun clearSelectedPins() {
        selectedPinsMapComponent?.clear()
    }

    /* ----------------------------  Markers for current highlighting --------------------------- */

    override fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    ) {
        geometryMarkersMapComponent?.put(geometry, drawableResId, title)
    }

    override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        geometryMarkersMapComponent?.delete(geometry)
    }

    override fun clearMarkersForCurrentHighlighting() {
        geometryMarkersMapComponent?.clear()
    }

    /* --------------------- Switching between quests and edit history pins --------------------- */

    private fun updatePinMode() {
        /* both managers use the same resource (PinsMapComponent), so the newly visible manager
           may only be activated after the old has been deactivated
         */
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

        // todo: this is bad, but very convenient for testing if we have access to everything from everywhere
        var mapView: MapView? = null
        var mapboxMap: MapboxMap? = null
        var symbolManager: SymbolManager? = null
        var lineManager: LineManager? = null
        var fillManager: FillManager? = null
        var circleManager: CircleManager? = null
        var style: Style? = null
    }
}
