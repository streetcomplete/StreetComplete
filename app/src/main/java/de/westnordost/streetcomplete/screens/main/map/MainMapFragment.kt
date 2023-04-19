package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.FillManager
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.layers.TransitionOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
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
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.FocusGeometryMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.GeometryMarkersMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.SelectedPinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.toElementKey
import de.westnordost.streetcomplete.util.ktx.createBitmap
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

        pinsMapComponent = PinsMapComponent(ctrl)
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

        /* ---------------------------- MapLibre stuff --------------------------- */

        // how to best hide layers?
        //   layer.setFilter(Expression.literal(false))
        //   maybe could also remove layer from style, but didn't try yet

        // todo
        //  performance test when updating pins (maybe CustomGeometrySource could be faster than GeoJsonSource)
        //   but looks like GeoJsonSource is the more usable one anyway, see below
        //  mapLibre always downloads "something" on startup: what is it, and why?
        //   looks like it's some icons (glyph / sprite urls)
        //    actually they are already present, does it really look whether there are new ones at every start?
        //    maybe just need to set different caching parameters... how?
        //    or maybe this is fixed when using offline stuff
        //   remove it by adjusting the style?
        //  hide pins of not-the-active quest (just set a filter)
        //  enable offline stuff
        //   needs some file at server because for absolutely no reason you can't supply a local style (or even just tile url) for offline stuff
        //   see MapTilesDownloader
        //  see CurrentLocationMapComponent

        // performance observations when displaying many icons (symbols)
        //  SymbolManager is not fast enough (though CircleManager is)
        //   -> use SymbolLayer and some source (GeoJson, or CustomGeometry)
        //  circle layer is much faster than symbol layer, when moving and especially when zooming
        //  enabling overlap makes things worse (probably because more symbols are displayed)
        //  smaller images help (shrink images already when adding to style, instead of using scale property)
        //  clustering helps noticeably, but still slower than circles
        //  not sorting symbols using symbolSortOrder (for priority) helps a lot
        //   using one order per quest type (instead of one per quest) considerably reduces performance impact of symbol sorting
        //   not setting sort order, but setting layer.symbolZOrder to SYMBOL_Z_ORDER_SOURCE is
        //    (almost?) as faster as not sorting (requires GeoJsonSource and sorting the list of pins)

        // add used images for quests pins and other icons
        // todo: here we should use pins, not round icons... simply create LayerDrawables?
        questTypeRegistry.forEach {
            val drawable = resources.getDrawable(it.icon)
            val bitmap = drawable.createBitmap(
                (drawable.intrinsicWidth*0.35).toInt(),
                (drawable.intrinsicHeight*0.35).toInt()
            ) // MapLibre converts everything to bitmap anyway, see https://github.com/maplibre/maplibre-gl-native/blob/c5992d58f1270f110960b326e2ae2d756d57d6ff/platform/android/MapboxGLAndroidSDK/src/main/java/com/mapbox/mapboxsdk/maps/Style.java#L341-L347
            style.addImage(resources.getResourceEntryName(it.icon), bitmap)
        }
        // use sdf here (the true in the end)
        // this is only recommended for monochrome icons, and allows using halo stuff for symbols (for mimicking tangram icons with outline)
        // but for some reason halo just does nothing, or creates a box around the icon, see https://github.com/mapbox/mapbox-gl-js/issues/7204
        TangramIconsSpriteSheet.ICONS.forEach { style.addImage(resources.getResourceEntryName(it), resources.getBitmapDrawable(it).bitmap, true) }
        // todo: here also the icons from pinIcons should be loaded

        // disable enablePlacementTransitions, so icons don't fade but (dis)appear immediately
        // this mimics tangram behavior, and noticeeably improves performance when there are many icons
        // defaults: 300, 0, true
        style.transition = TransitionOptions(style.transition.duration, style.transition.delay, false)

        // mapboxMap.uiSettings, has some potentially useful capabilities
        /* UiSettings for
         * compass (enable, position, image)
         * attribution
         * logo
         * focalPoint (for setting rotation center when adding a note)
         * enable / disable various gestures (roation, scroll, zoom, ...)
         * zoomRate
         * some velocity animations can be disabled... but that makes the map feel even worse compared to tangram
         *  actually we would need at least the scroll animation to be longer for more tangram-like feeling, but no setting for that
         *  better / more tangram-like animation is not implemented: https://github.com/maplibre/maplibre-gl-native/issues/25
         *   apparently it may be slow, but tangram is really fast here...
         * flingAnimation: time and threshold should be in next MapLibre version (now: 10.0.2),
         *  and should definitely be adjusted... sth like threshold 250 and time 500
         */

        // sources
        //  GeoJsonSource
        //   need to set all features for each change, though (visually) removing pins could be done via filter
        //  CustomGeometrySource
        //   map queries data directly (by tile, giving bbox and zoom), and caches returned data (probably unless set to volatile)
        //   queries happen at all zoom levels, which seems unnecessary... at least overscale can be used to ignore higher zoom levels (than the 16 we use as base)
        //   arbitrary bbox can be invalidated, but is not reloaded if already in view (thus useless for updating pins)
        //   thus we would need to use setTileData when updating pins (maybe / hopefully only for the currently displayed tile)
        //    would be less data to set than for GeoJsonSource, but still far from single pins
        //    if we need to set multiple tiles (zoom levels!) at once, data might be even more than for GetJsonSource
        //   -> just keep the tangram way of caching pins (or maybe features) and use GeoJsonSource
        //  clustering is a source option, not a layer option
        //   this is really bad for https://github.com/streetcomplete/StreetComplete/issues/124#issuecomment-1137061717
        //   because also the dots will be in the clusters (and thus not visible individually)
        //   if we use 2 sources, this could be done
        //   images (quest pins) could be used for clusters: https://github.com/mapbox/mapbox-gl-native/issues/16060

        // use a GeoJsonSource for quests, and one layer for pins and one for circles
        pinsSource = GeoJsonSource("pins-source", GeoJsonOptions().withBuffer(32)) // is the buffer relevant? default value is 128, so this should load less data fromm adjacent tiles
        style.addSource(pinsSource!!)

        // discarded attempt for CustomGeometrySource
/*
        val options = CustomGeometrySourceOptions()
            .withMaxZoom(16) // avoids requesting data at zoom higher than 16 (thanks to overscale)
            .withMinZoom(16) // but this does not mean data from z16 is used on z15 -> how to do? or just have the same data at multiple zoom levels?
        val customGeometrySource = CustomGeometrySource("custom-source", options, object : GeometryTileProvider {
            override fun getFeaturesForBounds(bounds: LatLngBounds, zoomLevel: Int): FeatureCollection {
                // looks like it's actually requesting whole tiles (didn't verify though)
                return FeatureCollection.fromFeatures(emptyList())
            }
        })
        customGeometrySource.maxOverscaleFactorForParentTiles = 10 // use data at higher zoom levels
        style.addSource(customGeometrySource)

        style.addLayer(SymbolLayer("custom-geo", "custom-source"))
*/

        // use a symbol layer for the pins
        pinsLayer = SymbolLayer("pins-layer", "pins-source")
            // set icon from feature property
            .withProperties(PropertyFactory.iconImage("{icon-image}")) // take icon name from icon-image property of feature
            //.withProperties(PropertyFactory.iconImage(Expression.get("icon-image"))) // does the same, but feels slower (nothing conclusive though)

            // apply quest(pin) order
            //.withProperties(PropertyFactory.symbolSortKey(Expression.get("symbol-sort-key"))) // works, but is actually somewhat slow...
            .withProperties(PropertyFactory.symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE))
                // avoids sort key by setting order to the order the features are added to the source
                //  this is more performant than symbolSortKey, and appears to be working as intended (test some more?)

            // set icon size
            // no, better set icon size when creating the drawable: this is much faster when there are lots of pins
            //.withProperties(PropertyValue("icon-size", 0.35f))
        pinsLayer!!.setFilter(Expression.gte(Expression.zoom(), 14f))
        style.addLayer(pinsLayer!!)

        // add a circle layer using the pinsSource (could actually also be a symbol layer using the dot image, but circles are fast!)
        val pinsDotLayer = CircleLayer("pin-dot-layer", "pins-source")
            // set fixed properties, circles are all the same
            .withProperties(
                PropertyFactory.circleColor("white"),
                PropertyFactory.circleStrokeColor("black"),
                PropertyFactory.circleRadius(5f),
                PropertyFactory.circleStrokeWidth(1f)
            )

        // add layer below the pinsLayer
        // layers are kept in a list internally, and ordered by that list, so layers added later are above others by default
        style.addLayerBelow(pinsDotLayer, "pins-layer")

        super.onMapReady(mapView, mapboxMap, style) // arguemnts are a leftover from initial implementation, maybe change?

        // add click listeners
        mapboxMap.addOnMapClickListener { pos ->
            // check whether we clicked a feature
            val screenPoint: PointF = mapboxMap.projection.toScreenLocation(pos)
            // only query this specific layer(s), leave layerIds empty for querying all layers
            val features = mapboxMap.queryRenderedFeatures(screenPoint, "pins-layer", "overlay-symbols", "overlay-lines", "overlay-dashed-lines", "overlay-fills")
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
        MainMapFragment.mapView = mapView
        MainMapFragment.mapboxMap = mapboxMap
        MainMapFragment.style = style

        overlaySource = GeoJsonSource("overlay-source")
        style.addSource(overlaySource!!)

        // need more information on how to work with expressions...
        // or better use them in style json instead of here? probably easier
        val overlayDashedLineLayer = LineLayer("overlay-dashed-lines", "overlay-source")
            // separate layer for dashed lines
            .withFilter(Expression.has("dashed"))
            .withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_BUTT),
                PropertyFactory.lineColor(Expression.get("color")),
                PropertyFactory.lineOpacity(Expression.get("opacity")),
                PropertyFactory.lineOffset(changeDistanceWithZoom("offset")),
                PropertyFactory.lineWidth(changeDistanceWithZoom("width")),
                PropertyFactory.lineDasharray(arrayOf(1.5f, 1f)), // todo: dash length depends on zoom, but re-evaluated only at integer zoom borders and thus looks weird
//                PropertyFactory.lineDasharray(Expression.array(Expression.literal(floatArrayOf(0.5f, 0.5f)))),
            )
        style.addLayerBelow(overlayDashedLineLayer, "pins-layer")
        val overlayLineLayer = LineLayer("overlay-lines", "overlay-source")
            .withFilter(Expression.not(Expression.has("dashed")))
            .withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_BUTT),
                PropertyFactory.lineColor(Expression.get("color")),
                PropertyFactory.lineOpacity(Expression.get("opacity")),
                // problem: click listener apparently only reacts to the underlying geometry, not the line at some offset
                PropertyFactory.lineOffset(changeDistanceWithZoom("offset")),
                PropertyFactory.lineWidth(changeDistanceWithZoom("width")),
                // there is no "lineOutlineColor", so how to properly copy the tangram overlay style?
            )
        style.addLayerBelow(overlayLineLayer, "pins-layer") // means: above the dashed layer

        // FillExtrusionLayer doesn't support outlines, only the normal FillLayer does...
        val overlayFillLayer = FillExtrusionLayer("overlay-fills", "overlay-source")
            .withFilter(Expression.has("outline-color")) // if a polygon has no outline-color, it's invisible anyway (actually this is to filter lines, maybe better filter by geometryType)
//            .withProperties(PropertyFactory.fillColor(Expression.get("color")))
//            .withProperties(PropertyFactory.fillOutlineColor(Expression.get("outline-color"))) // no outline color if extrusion?
//            .withProperties(PropertyFactory.fillOpacity(Expression.get("opacity")))
            .withProperties(PropertyFactory.fillExtrusionOpacity(Expression.get("opacity")))
            .withProperties(PropertyFactory.fillExtrusionColor(Expression.get("color")))
            .withProperties(PropertyFactory.fillExtrusionHeight(Expression.get("height"))) // need extrusion layer for height
        style.addLayerBelow(overlayFillLayer, "pins-layer")

        val overlaySymbolLayer = SymbolLayer("overlay-symbols", "overlay-source")
            .withProperties(
                PropertyFactory.iconImage("{icon}"),
                PropertyFactory.textField("{label}"),
                // or maybe read text properties from feature?
                PropertyFactory.textAnchor(Property.TEXT_ANCHOR_LEFT), // todo: only if icon -> use expression?
                PropertyFactory.textOffset(arrayOf(1.5f, 0f)), // todo: only if icon -> use expression?
                PropertyFactory.textMaxWidth(5f),
                PropertyFactory.textHaloColor("white"),
                PropertyFactory.textHaloWidth(1.5f), // works as expected, while for icons it doesn't
                PropertyFactory.iconColor("black"),
                PropertyFactory.iconHaloColor("white"),
                PropertyFactory.iconHaloWidth(1.5f), // size has almost no effect, halo stays tiny... (requires sdf icons, see above when adding to style)
//                PropertyFactory.iconHaloBlur(2f),
            )
        style.addLayerBelow(overlaySymbolLayer, "pins-layer")

        // for highlighted nearby
        // keep AnnotationManagers? or use layers? not enough icons to be relevant for performance
        geometrySymbolManager = SymbolManager(mapView, mapboxMap, style)
        geometryLineManager = LineManager(mapView, mapboxMap, style)
        geometryFillManager = FillManager(mapView, mapboxMap, style)
        geometryCircleManger = CircleManager(mapView, mapboxMap, style)

        // for quest geometry
        geometrySource = GeoJsonSource("geometry-source")
        style.addSource(geometrySource!!)

        val geometryLineLayer = LineLayer("geo-lines", "geometry-source")
            .withProperties(PropertyFactory.lineWidth(10f))
            .withProperties(PropertyFactory.lineColor("#D14000"))
            .withProperties(PropertyFactory.lineOpacity(0.5f))
            .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_ROUND)) // wow, this looks really ugly with opacity
        style.addLayerBelow(geometryLineLayer, "pins-layer")

        val geometryFillLayer = FillLayer("geo-fill", "geometry-source")
            .withProperties(PropertyFactory.fillColor("#D14000"))
            .withProperties(PropertyFactory.fillOpacity(0.3f))
        style.addLayerBelow(geometryFillLayer, "pins-layer")

        val geometryCircleLayer = CircleLayer("geo-circle", "geometry-source")
            .withProperties(PropertyFactory.circleColor("#D14000"))
            .withProperties(PropertyFactory.circleOpacity(0.7f))
            .withFilter(Expression.not(Expression.has("icon")))
        style.addLayerBelow(geometryCircleLayer, "pins-layer")

        val geometrySymbolLayer = SymbolLayer("geo-symbols", "geometry-source")
            .withFilter(Expression.has("icon"))
            .withProperties(PropertyFactory.iconImage("icon"))
        style.addLayerBelow(geometrySymbolLayer, "pins-layer")
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
        pinSymbolManager = null
        geometryCircleManger = null
        style = null
    }

    /* -------------------------------- Picking quest pins -------------------------------------- */

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

    fun hideNonHighlightedPins(questKey: QuestKey? = null) {
        pinsMapComponent?.isVisible = false
        if (questKey is OsmQuestKey)
            // set filter, so only pins of the highlighted quest are shown
            // currently just filtering by element id, and for OsmQuest, but at least it's clear how to do
            // and actually in MapLibre the properties can also be numbers, so no need to convert id to a string
            pinsLayer?.setFilter(Expression.eq(Expression.get("element_id"), questKey.elementId.toString()))
    }

    fun hideOverlay() {
        styleableOverlayMapComponent?.isVisible = false
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)

        // clear previous geometries as tangram does
        geometryLineManager?.deleteAll()
        geometryCircleManger?.deleteAll()
        geometryFillManager?.deleteAll()

        when (geometry) {
            is ElementPolylinesGeometry -> {
                val points = geometry.polylines.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points)
                geometrySource?.setGeoJson(Feature.fromGeometry(multilineString))
            }
            is ElementPolygonsGeometry -> {
                val points = geometry.polygons.map { it.map { com.mapbox.geojson.Point.fromLngLat(it.longitude, it.latitude) } }
                val polygon = com.mapbox.geojson.Polygon.fromLngLats(points) // todo: breaks for mulitpolygons when zooming in (weird...)
                // todo: actually the outline is displayed in the fill layer
                //  maybe this is what breaks multipolygon display
                //  just set some Expression.geometryType() filter on the fill layer
                val multilineString = com.mapbox.geojson.MultiLineString.fromLngLats(points) // outline
                geometrySource?.setGeoJson(FeatureCollection.fromFeatures(listOf(Feature.fromGeometry(multilineString), Feature.fromGeometry(polygon))))
            }
            is ElementPointGeometry -> {
                geometrySource?.setGeoJson(com.mapbox.geojson.Point.fromLngLat(geometry.center.longitude, geometry.center.latitude))
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
        geometrySymbolManager?.deleteAll()
        geometryLineManager?.deleteAll()
        geometryCircleManger?.deleteAll()
        geometryFillManager?.deleteAll()
//        val thisIsNoFeature: Feature? = null
//        geometrySource?.setGeoJson(thisIsNoFeature) // nullable, but crashes maplibre (native) if null. great.
        geometrySource?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        pinsLayer?.setFilter(Expression.literal(true)) // how to set "no filter"?
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
        var pinSymbolManager: SymbolManager? = null
        var overlaySource: GeoJsonSource? = null

        // keep AnnotationManagers for now, because they allow removing single annotations,
        // which is used in GeometryMarkersComponent
        var geometrySymbolManager: SymbolManager? = null
        var geometryLineManager: LineManager? = null
        var geometryFillManager: FillManager? = null
        var geometryCircleManger: CircleManager? = null

        var style: Style? = null
        var pinsSource: GeoJsonSource? = null
        var geometrySource: GeoJsonSource? = null
        var pinsLayer: SymbolLayer? = null
    }
}

// expression stuff is horrible... available examples are almost all in json
// is there a way of converting json string to expression? would be much more readable
// first attempt of modifying example expression from https://docs.mapbox.com/archive/android/maps/api/7.1.2/com/mapbox/mapboxsdk/style/expressions/Expression.html
/*
    Expression.interpolate(Expression.exponential(2), Expression.zoom(),
        Expression.stop(15,
            Expression.division( // gah, there must be sth like Expression.multiplication?
                Expression.get(lineWidthProperty),
                Expression.division( // but ok, if we divide that thing again we have a multiplication
                    Expression.pow(1, 1), // great and simple way to write 1
                    Expression.pow(2, -2)
                )
            ),
        ),
        Expression.stop(18,
            Expression.division(
                Expression.get(lineWidthProperty),
                Expression.division(
                    Expression.pow(1, 1),
                    Expression.pow(2, 1)
                )
            ),
        ),
    )
 */

fun changeDistanceWithZoom(lineWidth: Float): Expression =
    Expression.interpolate(Expression.exponential(2), Expression.zoom(),
        Expression.stop(10, lineWidth / 128f), // * 2^-7
        Expression.stop(25, lineWidth * 256f) // * 2^8 -> 8 - (-7) = 15, which is the zoom range for this interpolation
    )

// expression for line width dependent on zoom (if we want width in meters)
// this seems to work reasonably well, but probably should be done in the style json
fun changeDistanceWithZoom(lineWidthProperty: String): Expression =
    // todo: actually the style json uses 1.5 and 1.3 as base (depends for what), so overlay lines change size compared to roads
    //  also tangram seems to use something like 1.5
    Expression.interpolate(Expression.exponential(BASE), Expression.zoom(),
        // why didn't I use BASE.pow(7)?
        Expression.stop(10, Expression.division(Expression.get(lineWidthProperty), Expression.literal(BASE*BASE*BASE*BASE*BASE*BASE*BASE / FACTOR))), // width / base^7
        Expression.stop(25, Expression.division(Expression.get(lineWidthProperty), Expression.literal(1 / (BASE*BASE*BASE*BASE*BASE*BASE*BASE*BASE * FACTOR)))) // width / base^-8
    )

private const val BASE = 1.5f
private const val FACTOR = 2f // to get width / distance similar to tangram
