package de.westnordost.streetcomplete.screens.main.map

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.DownloadedAreaMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.GeometryMarkersMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.SelectedPinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.toElementKey
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.presetIconIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory.visibility

/** This is the map shown in the main view. It manages a map that shows the quest pins, quest
 *  geometry, overlays... */
class MainMapFragment : LocationAwareMapFragment(), ShowsGeometryMarkers {

    private val questTypeOrderSource: QuestTypeOrderSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
    private val overlayRegistry: OverlayRegistry by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val editHistorySource: EditHistorySource by inject()
    private val mapDataSource: MapDataWithEditsSource by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()
    private val downloadedTilesSource: DownloadedTilesSource by inject()

    private var geometryMarkersMapComponent: GeometryMarkersMapComponent? = null
    private var pinsMapComponent: PinsMapComponent? = null
    private var selectedPinsMapComponent: SelectedPinsMapComponent? = null
    private var geometryMapComponent: FocusGeometryMapComponent? = null
    private var questPinsManager: QuestPinsManager? = null
    private var editHistoryPinsManager: EditHistoryPinsManager? = null
    private var styleableOverlayMapComponent: StyleableOverlayMapComponent? = null
    private var styleableOverlayManager: StyleableOverlayManager? = null
    private var downloadedAreaMapComponent: DownloadedAreaMapComponent? = null
    private var downloadedAreaManager: DownloadedAreaManager? = null

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

    private var previouslyHiddenLayers: List<String> = emptyList()

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() { hideLayersAsPerOverlaySelected() }
    }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override suspend fun onMapReady(map: MapLibreMap, style: Style) {
        super.onMapReady(map, style)

        geometryMarkersMapComponent = GeometryMarkersMapComponent(requireContext(), map)

        pinsMapComponent = PinsMapComponent(requireContext(), questTypeRegistry, overlayRegistry, map)
        geometryMapComponent = FocusGeometryMapComponent(requireContext().contentResolver, map)
        viewLifecycleOwner.lifecycle.addObserver(geometryMapComponent!!)

        questPinsManager = QuestPinsManager(map, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, resources, visibleQuestsSource)
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)
        questPinsManager!!.isVisible = pinMode == PinMode.QUESTS

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource, resources)
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)
        editHistoryPinsManager!!.isVisible = pinMode == PinMode.EDITS

        styleableOverlayMapComponent = StyleableOverlayMapComponent(requireContext(), map)
        styleableOverlayManager = StyleableOverlayManager(map, styleableOverlayMapComponent!!, mapDataSource, selectedOverlaySource)
        viewLifecycleOwner.lifecycle.addObserver(styleableOverlayManager!!)

        downloadedAreaMapComponent = DownloadedAreaMapComponent(requireContext(), map)
        downloadedAreaManager = DownloadedAreaManager(downloadedAreaMapComponent!!, downloadedTilesSource)
        viewLifecycleOwner.lifecycle.addObserver(downloadedAreaManager!!)

        selectedPinsMapComponent = SelectedPinsMapComponent(requireContext(), map)
        viewLifecycleOwner.lifecycle.addObserver(selectedPinsMapComponent!!)

        hideLayersAsPerOverlaySelected()
        selectedOverlaySource.addListener(overlayListener)

        /* ---------------------------- MapLibre stuff --------------------------- */

        val presetIcons = HashMap<String, Bitmap>(presetIconIndex.values.size)
        presetIconIndex.values.forEach {
            val name = resources.getResourceEntryName(it)
            val bitmap = requireContext().getDrawable(it)!!.createBitmap()
            presetIcons[name] = bitmap
        }
        style.addImagesAsync(presetIcons, true)

        map.uiSettings.isCompassEnabled = false
        map.uiSettings.isLogoEnabled = false
        map.uiSettings.isAttributionEnabled = false


        // add click listeners
        val pickRadius = requireContext().dpToPx(8).toInt()
        map.addOnMapClickListener { pos ->
            // check whether we clicked a feature
            val screenPoint: PointF = map.projection.toScreenLocation(pos)
            val searchArea = RectF(screenPoint.x - pickRadius, screenPoint.y - pickRadius, screenPoint.x + pickRadius, screenPoint.y + pickRadius)
            // only query specific layer(s), leave layerIds empty for querying all layers
            val features = map.queryRenderedFeatures(searchArea, "pins-layer", "overlay-symbols", "overlay-lines", "overlay-lines-dashed", "overlay-fills")
            if (features.isNotEmpty()) { // found a feature
                // is the first feature always the correct one? looks like yes in a quick test
                viewLifecycleScope.launch {
                    when (pinMode) {
                        PinMode.QUESTS -> {
                            val questKey = features.first().properties()?.toQuestKey()
                            if (questKey != null) {
                                listener?.onClickedQuest(questKey)
                                return@launch
                            }
                        }
                        PinMode.EDITS -> {
                            val editKey = features.first().properties()?.toEditKey()
                            if (editKey != null) {
                                listener?.onClickedEdit(editKey)
                                return@launch
                            }
                        }
                        PinMode.NONE -> {}
                    }

                    // maybe it was an overlay?
                    val elementKey = features.first().properties()?.toElementKey()
                    if (elementKey != null) {
                        listener?.onClickedElement(elementKey)
                    }
                }
                return@addOnMapClickListener true
            }

            // no feature: just click the map
            listener?.onClickedMapAt(LatLon(pos.latitude, pos.longitude), 1.0)
            false
        }


        // names etc. should still be readable behind hatching
        downloadedAreaMapComponent?.layers?.forEach { style.addLayerAbove(it, "labels-country") }
        // left-and-right lines should be rendered behind the actual road
        styleableOverlayMapComponent?.sideLayers?.forEach { style.addLayerAbove(it, "pedestrian-tunnel-casing") }
        styleableOverlayMapComponent?.sideLayersBridge?.forEach { style.addLayerAbove(it, "pedestrian-bridge-casing") }

        // layers added first appear behind other layers
        styleableOverlayMapComponent?.layers?.forEach { style.addLayer(it) }
        geometryMarkersMapComponent?.layers?.forEach { style.addLayer(it) }
        geometryMapComponent?.layers?.forEach { style.addLayer(it) }
        pinsMapComponent?.layers?.forEach { style.addLayer(it) }
        selectedPinsMapComponent?.layers?.forEach { style.addLayer(it) }
    }

    override fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
        styleableOverlayManager?.onNewScreenPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedOverlaySource.removeListener(overlayListener)
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */
/*
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
*/

    private fun hideLayersAsPerOverlaySelected() {
        val new = selectedOverlaySource.selectedOverlay?.hidesLayers.orEmpty()
        val old = previouslyHiddenLayers
        if (old == new) return

        old.forEach { layer ->
            map?.style?.getLayer(layer)?.setProperties(visibility(Property.VISIBLE))
        }
        new.forEach { layer ->
            map?.style?.getLayer(layer)?.setProperties(visibility(Property.NONE))
        }

        previouslyHiddenLayers = new
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
        geometryMapComponent?.endFocusGeometry()
        centerCurrentPositionIfFollowing()
    }

    fun highlightPins(@DrawableRes iconResId: Int, pinPositions: Collection<LatLon>) {
        selectedPinsMapComponent?.set(iconResId, pinPositions)
    }

    fun hideNonHighlightedPins(questKey: QuestKey? = null) {
        pinsMapComponent?.isVisible = false
    }

    fun hideOverlay() {
        styleableOverlayMapComponent?.isVisible = false
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
    }

    /** Clear all highlighting */
    fun clearHighlighting() {
        pinsMapComponent?.isVisible = true
        styleableOverlayMapComponent?.isVisible = true
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
        selectedPinsMapComponent?.clear()
    }

    fun clearSelectedPins() {
        selectedPinsMapComponent?.clear()
    }

    /* ----------------------------  Markers for current highlighting --------------------------- */


    @UiThread override fun putMarkersForCurrentHighlighting(markers: Iterable<Marker>) {
        geometryMarkersMapComponent?.putAll(markers)
    }

    @UiThread override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        geometryMarkersMapComponent?.delete(geometry)
    }

    @UiThread override fun clearMarkersForCurrentHighlighting() {
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
    }
}
