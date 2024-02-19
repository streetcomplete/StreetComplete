package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesSource
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

        downloadedAreaMapComponent = DownloadedAreaMapComponent(ctrl)
        downloadedAreaManager = DownloadedAreaManager(ctrl, downloadedAreaMapComponent!!, downloadedTilesSource)
        viewLifecycleOwner.lifecycle.addObserver(downloadedAreaManager!!)

        selectedOverlaySource.addListener(overlayListener)

        /* ---------------------------- MapLibre stuff --------------------------- */

        // how to best hide layers?
        //   layer.setFilter(Expression.literal(false))
        //   maybe could also remove layer from style, but didn't try yet
        //    possible, but need to be careful to re-insert it at correct position

        // todo
        //  performance test when updating pins (maybe CustomGeometrySource could be faster than GeoJsonSource)
        //   but looks like GeoJsonSource is the more usable one anyway, see below
        //   -> stay at geojson
        //  mapLibre always downloads "something" on startup: what is it, and why?
        //   looks like it's some icons (glyph / sprite urls)
        //    actually they are already present, does it really look whether there are new ones at every start?
        //    maybe just need to set different caching parameters... how?
        //    or maybe this is fixed when using offline stuff
        //   remove it by adjusting the style?
        //  enable offline stuff
        //   needs some file at server because for absolutely no reason you can't supply a local style (or even just tile url) for offline stuff
        //   see MapTilesDownloader
        //  zoom very often is choppy, far not as smooth as tangram
        //   more quests make it a little worse, but most of it seems to be "natural"
        //  overlay paths/roads are rather hard to select
        //   probably the pickRadius
        //  any way tp get useful stack traces from maplibre? often it just starts at Handler.dispatchMessage, so no idea which line of SC triggered it

        // todo now after removing tangram
        //  re-arrange things so things can be added via mapController instead of doing everything here and with MainActivity
        //  zoom-in for node quests is far too much (though it should not go to more than 20, what is wrong?)
        //  are overlays missing some zoom filter, iirc 16 is default SC limit
        //  overlays don't show nodes
        //   worked before removing tangram, what is wrong?
        //  text for highlighted (nearby) elements not shown
        //  camera does not unlock when panning (keeps following position)
        //  there is a way to get in a weird zoom-out state where the whole world is visible, and the zoom buttons don't work
        //  later
        //   make the location pointer work (currently rotates like crazy in top left corner -> there is also a degree / radians issue)
        //   open SC -> wait and press back -> open SC, wait more -> crash without SC code in stacktrace (great)
        //    possibly sth initializing twice
        //   no tilt or rotate in follow-mode
        //   quest pin looks awful, maybe layer drawable not suitable? or just need to properly calculate insets instead of guessing
        //   accuracy circle blocks quest pins, but not dots (sth with collision)
        //   accuracy circle disappears when center is off screen
        //   use the maplibre-internal position and accuracy stuff?
        //    but accuracy circle has really bad performance when zooming (that one could be fixed in the next release)
        //   gps and user tracks not working
        //   define pins/overlay/geometry/... layers in some json instead of in code? for easier change of attributes

        // performance observations when displaying many icons (symbols)
        //  SymbolManager is not fast enough (though CircleManager is)
        //   -> use SymbolLayer and GeoJsonSource
        //  circle layer is much faster than symbol layer, when moving and especially when zooming
        //  enabling overlap makes things worse (probably because more symbols are displayed)
        //  smaller images help (shrink images already when adding to style, instead of using scale property)
        //  clustering helps noticeably, but still slower than circles
        //  not sorting symbols using symbolSortOrder (for priority) helps a lot
        //   using one order per quest type (instead of one per quest) considerably reduces performance impact of symbol sorting
        //   not setting sort order, but setting layer.symbolZOrder to SYMBOL_Z_ORDER_SOURCE is same as not sorting

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
            .withProperties(PropertyFactory.iconOffset(listOf(-iconSize / 12f, -iconSize / 4f).toTypedArray()))

            // apply quest(pin) order
            // setting layer.symbolZOrder to SYMBOL_Z_ORDER_SOURCE is (almost?) as fast as not sorting
            // but it requires sorting the list of pins in the GeoJsonSource
            // using symbolSortKey instead of this is much slower
            .withProperties(PropertyFactory.symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE))

        pinsLayer!!.setFilter(Expression.gte(Expression.zoom(), 14f))
        style.addLayer(pinsLayer!!)

        // add a circle layer using the pinsSource (could actually also be a symbol layer using the dot image, but circles are fast!)
        pinsDotLayer = CircleLayer("pin-dot-layer", "pins-source")
            // set fixed properties, circles are all the same
            .withProperties(
                PropertyFactory.circleColor("white"),
                PropertyFactory.circleStrokeColor("grey"),
                PropertyFactory.circleRadius(5f),
                PropertyFactory.circleStrokeWidth(1f)
            )

        // add layer below the pinsLayer
        // layers are kept in a list internally, and ordered by that list, so layers added later are above others by default
        pinsDotLayer!!.setFilter(Expression.gte(Expression.zoom(), 14f))
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
        MainMapFragment.mapView = mapView
        MainMapFragment.mapboxMap = mapboxMap
        MainMapFragment.style = style

        overlaySource = GeoJsonSource("overlay-source")
        style.addSource(overlaySource!!)

        // need more information on how to work with expressions...
        // or better use them in style json instead of here? probably easier
        overlayDashedLineLayer = LineLayer("overlay-dashed-lines", "overlay-source")
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
        style.addLayerBelow(overlayDashedLineLayer!!, "pins-layer")
        overlayLineLayer = LineLayer("overlay-lines", "overlay-source")
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
        style.addLayerBelow(overlayLineLayer!!, "pins-layer") // means: above the dashed layer

        // FillExtrusionLayer doesn't support outlines, only the normal FillLayer does...
        overlayFillLayer = FillExtrusionLayer("overlay-fills", "overlay-source")
            .withFilter(Expression.has("outline-color")) // if a polygon has no outline-color, it's invisible anyway (actually this is to filter lines, maybe better filter by geometryType)
//            .withProperties(PropertyFactory.fillColor(Expression.get("color")))
//            .withProperties(PropertyFactory.fillOutlineColor(Expression.get("outline-color"))) // no outline color if extrusion?
//            .withProperties(PropertyFactory.fillOpacity(Expression.get("opacity")))
            .withProperties(PropertyFactory.fillExtrusionOpacity(Expression.get("opacity")))
            .withProperties(PropertyFactory.fillExtrusionColor(Expression.get("color")))
            .withProperties(PropertyFactory.fillExtrusionHeight(Expression.get("height"))) // need extrusion layer for height
        style.addLayerBelow(overlayFillLayer!!, "pins-layer")

        overlaySymbolLayer = SymbolLayer("overlay-symbols", "overlay-source")
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
        style.addLayerBelow(overlaySymbolLayer!!, "pins-layer")

        // for nearby element geometry
        geometrySource = GeoJsonSource("geometry-source")
        style.addSource(geometrySource!!)

        val geometryLineLayer = LineLayer("geo-lines", "geometry-source")
            .withProperties(PropertyFactory.lineWidth(10f))
            .withProperties(PropertyFactory.lineColor("#D140D0"))
            .withProperties(PropertyFactory.lineOpacity(0.5f))
            .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_ROUND)) // wow, this looks really ugly with opacity
        style.addLayerBelow(geometryLineLayer, "pins-layer")

        val geometryFillLayer = FillLayer("geo-fill", "geometry-source")
            .withProperties(PropertyFactory.fillColor("#D140D0"))
            .withProperties(PropertyFactory.fillOpacity(0.3f))
        style.addLayerBelow(geometryFillLayer, "pins-layer")

        val geometryCircleLayer = CircleLayer("geo-circle", "geometry-source")
            .withProperties(PropertyFactory.circleColor("#D140D0"))
            .withProperties(PropertyFactory.circleOpacity(0.7f))
            .withProperties(PropertyFactory.textField("{label}")) // todo: not showing, maybe need anchor, offset, ...
            .withFilter(Expression.not(Expression.has("icon")))
        style.addLayerBelow(geometryCircleLayer, "pins-layer")

        val geometrySymbolLayer = SymbolLayer("geo-symbols", "geometry-source")
            .withFilter(Expression.has("icon"))
            .withProperties(PropertyFactory.iconColor("#D140D0"))
            .withProperties(PropertyFactory.iconImage("{icon}"))
            .withProperties(PropertyFactory.textField("{label}")) // todo: this is never set, title has its own circle geometry thing
        style.addLayerBelow(geometrySymbolLayer, "pins-layer")

        // for focused element geometry
        focusedGeometrySource = GeoJsonSource("focus-geometry-source")
        style.addSource(focusedGeometrySource!!)

        val focusGeometryLineLayer = LineLayer("focus-geo-lines", "focus-geometry-source")
            .withProperties(PropertyFactory.lineWidth(10f))
            .withProperties(PropertyFactory.lineColor("#D14000"))
            .withProperties(PropertyFactory.lineOpacity(0.5f))
            .withProperties(PropertyFactory.lineCap(Property.LINE_CAP_ROUND)) // wow, this looks really ugly with opacity
        style.addLayerBelow(focusGeometryLineLayer, "pins-layer")

        val focusGeometryFillLayer = FillLayer("focus-geo-fill", "focus-geometry-source")
            .withProperties(PropertyFactory.fillColor("#D14000"))
            .withProperties(PropertyFactory.fillOpacity(0.3f))
        focusGeometryFillLayer.setFilter(Expression.not(Expression.has("way")))
        style.addLayerBelow(focusGeometryFillLayer, "pins-layer")

        val focusGeometryCircleLayer = CircleLayer("focus-geo-circle", "focus-geometry-source")
            .withProperties(PropertyFactory.circleColor("#D14000"))
            .withProperties(PropertyFactory.circleOpacity(0.7f))
            .withFilter(Expression.not(Expression.has("icon")))
        style.addLayerBelow(focusGeometryCircleLayer, "pins-layer")
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        super.onMapIsChanging(position, rotation, tilt, zoom)
        questPinsManager?.onNewScreenPosition()
        styleableOverlayManager?.onNewScreenPosition()
        downloadedAreaManager?.onNewScreenPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedOverlaySource.removeListener(overlayListener)
        mapboxMap = null
        mapView = null
        geometryCircleManger = null
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
            pinsLayer?.setFilter(Expression.eq(Expression.get("element_id"), questKey.elementId.toString()))
            pinsDotLayer?.setFilter(Expression.eq(Expression.get("element_id"), questKey.elementId.toString()))
        }
    }

    fun hideOverlay() {
        overlayFillLayer?.setFilter(Expression.literal(false))
        overlayLineLayer?.setFilter(Expression.literal(false))
        overlayDashedLineLayer?.setFilter(Expression.literal(false))
        overlaySymbolLayer?.setFilter(Expression.literal(false))
    }

    fun highlightGeometry(geometry: ElementGeometry) {
        geometryMapComponent?.showGeometry(geometry)
    }

    /** Clear all highlighting */
    fun clearHighlighting() {
        pinsMapComponent?.isVisible = true
        overlayFillLayer?.setFilter(Expression.has("outline-color"))
        overlayLineLayer?.setFilter(Expression.not(Expression.has("dashed")))
        overlayDashedLineLayer?.setFilter(Expression.has("dashed"))
        overlaySymbolLayer?.setFilter(Expression.literal(true)) // no default filter
//        selectedPinsMapComponent?.clear()
        geometryMapComponent?.clearGeometry()
        geometryMarkersMapComponent?.clear()
//        val thisIsNoFeature: Feature? = null
//        geometrySource?.setGeoJson(thisIsNoFeature) // nullable, but crashes maplibre (native) if null. great.
        geometrySource?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        focusedGeometrySource?.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
        pinsLayer?.setFilter(Expression.gte(Expression.zoom(), 14f))
        pinsDotLayer?.setFilter(Expression.gte(Expression.zoom(), 14f))
    }

    fun clearSelectedPins() {
//        selectedPinsMapComponent?.clear()
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
        var focusedGeometrySource: GeoJsonSource? = null
        var pinsLayer: SymbolLayer? = null
        var pinsDotLayer: CircleLayer? = null

        var overlayDashedLineLayer: LineLayer? = null
        var overlayLineLayer: LineLayer? = null
        var overlayFillLayer: FillExtrusionLayer? = null
        var overlaySymbolLayer: SymbolLayer? = null
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
