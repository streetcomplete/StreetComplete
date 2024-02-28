package de.westnordost.streetcomplete.screens.main.map

import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
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
import de.westnordost.streetcomplete.util.ktx.asBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getBitmapDrawable
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.view.presetIconIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/** This is the map shown in the main view. It manages a map that shows the quest pins, quest
 *  geometry, overlays... */
class MainMapFragment : LocationAwareMapFragment(), ShowsGeometryMarkers {

    private val questTypeOrderSource: QuestTypeOrderSource by inject()
    private val questTypeRegistry: QuestTypeRegistry by inject()
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
//            questPinsSpriteSheet.sceneUpdates + iconsSpriteSheet.sceneUpdates
        }
//        sceneMapComponent?.addSceneUpdates(sceneUpdates)

        overlaySceneUpdates = selectedOverlaySource.selectedOverlay?.sceneUpdates
        overlaySceneUpdates?.let { sceneMapComponent?.addSceneUpdates(it) }
    }

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    override suspend fun onMapReady(mapView: MapView, mapboxMap: MapboxMap, style: Style) {
        val ctrl = controller ?: return
//        ctrl.setPickRadius(8f)
        geometryMarkersMapComponent = GeometryMarkersMapComponent(resources, ctrl)

        pinsMapComponent = PinsMapComponent(ctrl)
        selectedPinsMapComponent = SelectedPinsMapComponent(requireContext(), ctrl)
        geometryMapComponent = FocusGeometryMapComponent(ctrl)

        questPinsManager = QuestPinsManager(ctrl, pinsMapComponent!!, questTypeOrderSource, questTypeRegistry, resources, visibleQuestsSource)
        viewLifecycleOwner.lifecycle.addObserver(questPinsManager!!)
        questPinsManager!!.isVisible = pinMode == PinMode.QUESTS

        editHistoryPinsManager = EditHistoryPinsManager(pinsMapComponent!!, editHistorySource, resources)
        viewLifecycleOwner.lifecycle.addObserver(editHistoryPinsManager!!)
        editHistoryPinsManager!!.isVisible = pinMode == PinMode.EDITS

        styleableOverlayMapComponent = StyleableOverlayMapComponent(resources, ctrl)
        styleableOverlayManager = StyleableOverlayManager(ctrl, styleableOverlayMapComponent!!, mapDataSource, selectedOverlaySource)
        viewLifecycleOwner.lifecycle.addObserver(styleableOverlayManager!!)

        downloadedAreaMapComponent = DownloadedAreaMapComponent(ctrl)
        downloadedAreaManager = DownloadedAreaManager(downloadedAreaMapComponent!!, downloadedTilesSource)
        viewLifecycleOwner.lifecycle.addObserver(downloadedAreaManager!!)

        selectedOverlaySource.addListener(overlayListener)

        /* ---------------------------- MapLibre stuff --------------------------- */

        // add used images for quests pins and other icons
        val pin = ContextCompat.getDrawable(requireContext(), R.drawable.pin)!! // why nullable? instead of resource not found?
        val iconSize = pin.intrinsicWidth
        questTypeRegistry.forEach {
            val iconDrawable = ContextCompat.getDrawable(requireContext(), it.icon)!!
            val iconBitmap = iconDrawable.asBitmapDrawable(
                resources,
                (iconDrawable.intrinsicWidth*0.35).toInt(),
                (iconDrawable.intrinsicHeight*0.35).toInt()
            ) // MapLibre converts everything to bitmap anyway, see https://github.com/maplibre/maplibre-gl-native/blob/c5992d58f1270f110960b326e2ae2d756d57d6ff/platform/android/MapboxGLAndroidSDK/src/main/java/com/mapbox/mapboxsdk/maps/Style.java#L341-L347
            val drawable = LayerDrawable(arrayOf(pin, iconBitmap)) // behaves really weird with inset...
            drawable.setLayerInset(1, (iconDrawable.intrinsicWidth * 0.35 / 2).toInt(), pin.intrinsicHeight / 10, (iconDrawable.intrinsicWidth * 0.35 / 8).toInt(), pin.intrinsicHeight / 4) // not perfect, but not the right way to do it anyway
            style.addImage(resources.getResourceEntryName(it.icon), drawable)
        }
        // use sdf here
        // this is only recommended for monochrome icons, and allows using halo stuff for symbols
        // but for some reason halo just does nothing, or creates a box around the icon, see https://github.com/mapbox/mapbox-gl-js/issues/7204
        presetIconIndex.values.forEach {
            style.addImage(resources.getResourceEntryName(it), resources.getBitmapDrawable(it).bitmap, true)
        } // getBitmapDrawable gives a lot of log warnings

        // disable enablePlacementTransitions, so icons don't fade but (dis)appear immediately
        // this mimics tangram behavior, and noticeably improves performance when there are many icons
        // defaults: 300, 0, true
        style.transition = TransitionOptions(style.transition.duration, style.transition.delay, false)

        mapboxMap.uiSettings.setCompassFadeFacingNorth(true)
        // todo: set correct top margin, should depend on where bottom of menu button is
        mapboxMap.uiSettings.setCompassMargins(0, requireContext().dpToPx(80).toInt(), 6, 0)
        val compass = LayerDrawable(arrayOf(
            ContextCompat.getDrawable(requireContext(), R.drawable.round_white_button)!!,
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_compass_needle_48dp)!!
        ))
        // todo: image too large, but when creating a correctly sized bitmap, it's grey and not white (wtf?)
        mapboxMap.uiSettings.setCompassImage(compass)

        mapboxMap.uiSettings.isLogoEnabled = false
        mapboxMap.uiSettings.isAttributionEnabled = false

        // use a symbol layer for the pins
        pinsLayer = SymbolLayer("pins-layer", "pins-source")
            // set icon from feature property
            .withProperties(
                iconImage("{icon-image}"), // take icon name from icon-image property of feature
                //iconImage(get("icon-image")), // does the same, but feels slower (nothing conclusive though)
                iconOffset(listOf(-iconSize / 12f, -iconSize / 4f).toTypedArray()),
                // apply quest(pin) order
                // setting layer.symbolZOrder to SYMBOL_Z_ORDER_SOURCE is (almost?) as fast as not sorting
                // but it requires sorting the list of pins in the GeoJsonSource
                // using symbolSortKey instead of this is much slower
                symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE),
            )

        pinsLayer!!.setFilter(gte(zoom(), 14f))
        style.addLayer(pinsLayer!!)

        // add a circle layer using the pinsSource (could actually also be a symbol layer using the dot image, but circles are fast!)
        pinsDotLayer = CircleLayer("pin-dot-layer", "pins-source")
            // set fixed properties, circles are all the same
            .withProperties(
                circleColor("white"),
                circleStrokeColor("grey"),
                circleRadius(5f),
                circleStrokeWidth(1f)
            )

        // add layer below the pinsLayer
        // layers are kept in a list internally, and ordered by that list, so layers added later are above others by default
        pinsDotLayer!!.setFilter(gte(zoom(), 14f))
        style.addLayerBelow(pinsDotLayer!!, "pins-layer")

        super.onMapReady(mapView, mapboxMap, style) // leftover from initial implementation, maybe change?

        // add click listeners
        val radius = requireContext().dpToPx(8).toInt()
        mapboxMap.addOnMapClickListener { pos ->
            // check whether we clicked a feature
            val screenPoint: PointF = mapboxMap.projection.toScreenLocation(pos)
            val searchArea = RectF(screenPoint.x - radius, screenPoint.y - radius, screenPoint.x + radius, screenPoint.y + radius)
            // only query this specific layer(s), leave layerIds empty for querying all layers
//            val features = mapboxMap.queryRenderedFeatures(screenPoint, "pins-layer", "overlay-symbols", "overlay-lines", "overlay-dashed-lines", "overlay-fills")
            val features = mapboxMap.queryRenderedFeatures(searchArea, "pins-layer", "overlay-symbols", "overlay-lines", "overlay-dashed-lines", "overlay-fills")
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
        mapboxMap.addOnMapLongClickListener { pos ->
            val screenPoint: PointF = mapboxMap.projection.toScreenLocation(pos)
            onLongPress(screenPoint.x, screenPoint.y)
            true
        }

        // set map stuff available everywhere in the app (for simplified testing)
        MainMapFragment.mapboxMap = mapboxMap
        MainMapFragment.style = style

        // need more information on how to work with expressions...
        // or better use them in style json instead of here? probably easier
        overlayDashedLineLayer = LineLayer("overlay-dashed-lines", "overlay-source")
            // separate layer for dashed lines
            .withFilter(all(has("dashed"), gte(zoom(), 16f)))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                lineOffset(changeDistanceWithZoom("offset")),
                lineWidth(changeDistanceWithZoom("width")),
                lineDasharray(arrayOf(1.5f, 1f)), // todo: dash length depends on zoom, but re-evaluated only at integer zoom borders and thus looks weird
                // lineDasharray(array(literal(floatArrayOf(0.5f, 0.5f)))),
            )
        style.addLayerBelow(overlayDashedLineLayer!!, "pins-layer")
        overlayLineLayer = LineLayer("overlay-lines", "overlay-source")
            .withFilter(all(not(has("dashed")), gte(zoom(), 16f)))
            .withProperties(
                lineCap(Property.LINE_CAP_BUTT),
                lineColor(get("color")),
                lineOpacity(get("opacity")),
                // problem: click listener apparently only reacts to the underlying geometry, not the line at some offset
                lineOffset(changeDistanceWithZoom("offset")),
                lineWidth(changeDistanceWithZoom("width")),
                // there is no "lineOutlineColor", so how to properly copy the tangram overlay style?
            )
        style.addLayerBelow(overlayLineLayer!!, "pins-layer") // means: above the dashed layer

        // FillExtrusionLayer doesn't support outlines, only the normal FillLayer does...
        overlayFillLayer = FillExtrusionLayer("overlay-fills", "overlay-source")
            .withFilter(all(has("outline-color"), gte(zoom(), 16f))) // if a polygon has no outline-color, it's invisible anyway (actually this is to filter lines, maybe better filter by geometryType)
            .withProperties(
                //fillColor(get("color")),
                //fillOutlineColor(get("outline-color")), // no outline color if extrusion?
                //fillOpacity(get("opacity")),
                fillExtrusionOpacity(get("opacity")),
                fillExtrusionColor(get("color")),
                fillExtrusionHeight(get("height")) // need extrusion layer for height
            )
        style.addLayerBelow(overlayFillLayer!!, "pins-layer")

        overlaySymbolLayer = SymbolLayer("overlay-symbols", "overlay-source")
            .withProperties(
                iconImage("{icon}"),
                textField("{label}"),
                // or maybe read text properties from feature?
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
                textHaloColor("white"),
                textHaloWidth(1.5f), // works as expected, while for icons it doesn't
                iconColor("black"),
                // iconHaloColor("white"), // not needed any more but still why doesn't it work?
                // iconHaloWidth(1.5f), // size has almost no effect, halo stays tiny... (requires sdf icons, see above when adding to style)
                // iconHaloBlur(2f),
                // both overlaps are required
                iconAllowOverlap(step(zoom(), literal(false), stop(18, true))),
                textAllowOverlap(step(zoom(), literal(false), stop(18, true))),
            )
            .withFilter(gte(zoom(), 16f))
        style.addLayerBelow(overlaySymbolLayer!!, "pins-layer")

        val geometryLineLayer = LineLayer("geo-lines", "geometry-source")
            .withProperties(
                lineWidth(10f),
                lineColor("#D140D0"),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND) // wow, this looks really ugly with opacity
            )
        style.addLayerBelow(geometryLineLayer, "pins-layer")

        val geometryFillLayer = FillLayer("geo-fill", "geometry-source")
            .withProperties(
                fillColor("#D140D0"),
                fillOpacity(0.3f)
            )
        style.addLayerBelow(geometryFillLayer, "pins-layer")

        val geometryCircleLayer = CircleLayer("geo-circle", "geometry-source")
            .withProperties(
                circleColor("#D140D0"),
                circleOpacity(0.7f),
                textField("{label}"),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
            )
            .withFilter(not(has("icon")))
        style.addLayerBelow(geometryCircleLayer, "pins-layer")

        val geometrySymbolLayer = SymbolLayer("geo-symbols", "geometry-source")
            .withFilter(has("icon"))
            .withProperties(
                iconColor("#D140D0"),
                iconImage("{icon}"),
                textField("{label}"),
                textAnchor(Property.TEXT_ANCHOR_LEFT),
                textOffset(arrayOf(1.5f, 0f)),
                textMaxWidth(5f),
            )
        style.addLayerBelow(geometrySymbolLayer, "pins-layer")

        // TODO low prio: animation of width+alpha (breathing selection effect). From shader:
        //  opacity = min(max(sin(u_time * 3.0) / 2.0 + 0.5, 0.125), 0.875) * 0.5 + 0.125;
        //  width *= min(max(-sin(u_time * 3.0) / 2.0 + 0.5, 0.125), 0.875) + 0.625;
        val focusGeometryLineLayer = LineLayer("focus-geo-lines", "focus-geometry-source")
            .withProperties(
                lineWidth(10f),
                lineColor("#D14000"),
                lineOpacity(0.5f),
                lineCap(Property.LINE_CAP_ROUND) // wow, this looks really ugly with opacity
            )
        style.addLayerBelow(focusGeometryLineLayer, "pins-layer")

        val focusGeometryFillLayer = FillLayer("focus-geo-fill", "focus-geometry-source")
            .withProperties(
                fillColor("#D14000"),
                fillOpacity(0.3f)
            )
        focusGeometryFillLayer.setFilter(not(has("way")))
        style.addLayerBelow(focusGeometryFillLayer, "pins-layer")

        val focusGeometryCircleLayer = CircleLayer("focus-geo-circle", "focus-geometry-source")
            .withProperties(
                circleColor("#D14000"),
                circleOpacity(0.7f)
            )
            .withFilter(not(has("icon")))
        style.addLayerBelow(focusGeometryCircleLayer, "pins-layer")

        // something is not working here
        val trackLayer = LineLayer("track", "track-source")
            .withProperties(
                lineWidth(10f),
                lineColor("#536dfe"),
                lineOpacity(0.3f),
                lineCap(Property.LINE_CAP_ROUND)
            )
        style.addLayerBelow(trackLayer, "pins-layer")
        val oldTrackLayer = LineLayer("old-track", "old-track-source")
            .withProperties(
                lineWidth(10f),
                lineColor("#536dfe"),
                lineOpacity(0.15f),
                lineCap(Property.LINE_CAP_ROUND)
            )
        style.addLayerBelow(oldTrackLayer, "pins-layer")

        val downloadedAreaLayer = FillLayer("downloaded-area", "downloaded-area-source")
            .withProperties(fillColor(Color.BLACK), fillOpacity(0.3f))
        style.addLayerBelow(downloadedAreaLayer, "pins-layer")
    }

    override fun onMapIsChanging(position: LatLon, rotation: Double, tilt: Double, zoom: Double) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
        styleableOverlayManager?.onNewScreenPosition()
        downloadedAreaManager?.onNewScreenPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedOverlaySource.removeListener(overlayListener)
        mapboxMap = null
        style = null
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
//        selectedPinsMapComponent?.set(iconResId, pinPositions)
    }

    fun hideNonHighlightedPins(questKey: QuestKey? = null) {
        pinsMapComponent?.isVisible = false
        if (questKey is OsmQuestKey) {
            // set filter, so only pins of the highlighted quest are shown
            // currently just filtering by element id, and for OsmQuest, but at least it's clear how to do
            // and actually in MapLibre the properties can also be numbers, so no need to convert id to a string
            // todo: really use filter here? or better use source? or different layer instead of filtering?
            pinsLayer?.setFilter(eq(get("element_id"), questKey.elementId.toString()))
            pinsDotLayer?.setFilter(eq(get("element_id"), questKey.elementId.toString()))
        }
    }

    fun hideOverlay() {
        overlayFillLayer?.setFilter(literal(false))
        overlayLineLayer?.setFilter(literal(false))
        overlayDashedLineLayer?.setFilter(literal(false))
        overlaySymbolLayer?.setFilter(literal(false))
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
    }

    /** Clear all highlighting */
    fun clearHighlighting() {
        pinsMapComponent?.isVisible = true
        overlayFillLayer?.setFilter(all(has("outline-color"), gte(zoom(), 16f)))
        overlayLineLayer?.setFilter(all(not(has("dashed")), gte(zoom(), 16f)))
        overlayDashedLineLayer?.setFilter(all(has("dashed"), gte(zoom(), 16f)))
        overlaySymbolLayer?.setFilter(gte(zoom(), 16f))
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
        pinsLayer?.setFilter(gte(zoom(), 14f))
        pinsDotLayer?.setFilter(gte(zoom(), 14f))
    }

    fun clearSelectedPins() {
//        selectedPinsMapComponent?.clear()
    }

    /* ----------------------------  Markers for current highlighting --------------------------- */

    @UiThread override fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    ) {
        geometryMarkersMapComponent?.put(geometry, drawableResId, title)
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

        // todo: this is bad, but very convenient for testing if we have access to everything from everywhere
        var mapboxMap: MapboxMap? = null

        var style: Style? = null
        var pinsLayer: SymbolLayer? = null
        var pinsDotLayer: CircleLayer? = null

        var overlayDashedLineLayer: LineLayer? = null
        var overlayLineLayer: LineLayer? = null
        var overlayFillLayer: FillExtrusionLayer? = null
        var overlaySymbolLayer: SymbolLayer? = null
    }
}

// expression for line width dependent on zoom (line width in property in meters)
// this seems to work reasonably well, but probably should be done in the style json (plus consider which stops to use)
fun changeDistanceWithZoom(lineWidthProperty: String): Expression =
    interpolate(exponential(2), zoom(),
        stop(10, division(get(lineWidthProperty), literal(128))),
        stop(24, product(get(lineWidthProperty), literal(128)))
    )
