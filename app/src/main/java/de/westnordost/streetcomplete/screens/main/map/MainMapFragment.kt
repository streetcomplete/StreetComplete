package de.westnordost.streetcomplete.screens.main.map

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.toDrawable
import com.google.gson.JsonArray
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.Image
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions
import com.mapbox.mapboxsdk.plugins.annotation.ClusterOptions
import com.mapbox.mapboxsdk.plugins.annotation.FillManager
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.CircleLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyValue
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.CustomGeometrySource
import com.mapbox.mapboxsdk.style.sources.CustomGeometrySourceOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.GeometryTileProvider
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.edithistory.EditHistorySource
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.maptiles.toLatLng
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
import de.westnordost.streetcomplete.screens.main.map.components.toElementKey
import de.westnordost.streetcomplete.util.ktx.asBitmapDrawable
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

        // add used images for quests and icons
        // todo: here we should use pins, not round icons
        //  just create them as LayerDrawable?
        questTypeRegistry.forEach {
            val d = resources.getDrawable(it.icon)
            style.addImage(resources.getResourceEntryName(it.icon), d.createBitmap((d.intrinsicWidth*0.3).toInt(),(d.intrinsicHeight*0.3).toInt()).toDrawable(resources))
        }
        TangramIconsSpriteSheet.ICONS.forEach { style.addImage(resources.getResourceEntryName(it), resources.getBitmapDrawable(it)) }

        pinsMapComponent = PinsMapComponent(ctrl)
        selectedPinsMapComponent = SelectedPinsMapComponent(requireContext(), ctrl)
        geometryMapComponent = FocusGeometryMapComponent(ctrl, mapboxMap)
        // test: use layers instead of symbol manager
        //  layers can be added above / below a single other layer, or at specific index (probably just insert in the layers list at position)
        //  how to hide layers?
        //   they can be removed from the style, but is re-adding them fast?
        //   or a filter can be set
        //  the symbol layer seems to have only a single icon, is this true? then we would have to use a lot of layers, which might affect performance
        //  which source to use?
        //   GeoJsonSource? can have "features", need to check how they are displayed, and whether they can have icons
        //    features can be from json, which might work
        //    but can feature be removed? don't see a way...
        //   CustomGeometrySource? that might work pretty fast, especially if we store the pins (or pin jsons, or features) in yet another spatialCache
        //    but is there a way of reloading data? (after adding / removing quests)
        //    -> setTileData(z, x, y, featureCollection)
        //     but... then data would be valid only for a specific zoom level?
        //  how to actually check whether a feature in a layer was clicked?
        //   only map and mapView have click listeners (and the annotation managers which we want to avoid here)
        //  can add a filter that has access to feature.properties -> hope this is fast
        //  now find something about order...
        //   there are symbol/... sortKeys
        //   and symbolLayer also has a Z-order

        // what SymbolManager does:
        //  creating the manager (annotation/general)
        //   onMapClickListener is added, with a MapClickResolver (same for long click)
        //    -> check how we get the symbol from this, and what this resolver actually does
        //  adding a symbol
        //   create(json) -> create(FeatureCollection.fromJson(json)) -> just creates SymbolOptions
        //   interestingly, symbolOptions.build creates a json object... could do that in a simpler way i guess (json string -> features -> symbols -> JsonObject)
        //   annotation is put in the annotation LongSparseArray, map is updated
        //  updateSource
        //   creates features.fromGeometry(annotation.geometry, annotation.feature) and sets properties (what a waste if we already could store features)
        //   geoJsonSource.setGeoJson(FeatureCollection.fromFeatures)
        //   -> this helps a lot!
        //  initialize
        //   style.addSource(geoJsonSource)
        //   style.addLayer(below/at/above)
        //    actually which layer? SymbolElementProvider is created, and has a getLayer function -> simply generates a SymbolLayer with a unique id

        //  performance
        //   check if circle source is faster: simply invert the zoom filter
        //    -> MUCH faster
        //   check if enabling overlap (instead of hiding icons) helps
        //    -> actually worse, so displaying many images seems to be an issue (or maybe rather: having many in view)
        //   check if non-bitmap drawables help (but documentation explicitly mentions bitmap image, and actually creates one)
        //    -> no change
        //   check if smaller images help (size is reduced to 0.3 in pins component -> do it already when loading)
        //    -> no change
        //   check if showing only a single image for each quest helps
        //    a. set the same image for each quest type
        //     -> maybe(!) a little better, but not enough improvement
        //    b. provide the same(!) drawable for each quest type icon
        //     -> as above
        //   check if clustering helps: SymbolManager(mapView, mapboxMap, style, null, ClusterOptions().withClusterRadius(40))
        //    -> much faster, but still not close to displaying circles, and it displays a circle with number of icons which isn't really what we want
        //   use only one SymbolLayer with a single icon as layer property
        //    -> about as fast as a circleLayer, which would be acceptable (but all quests have the same icon here!)
        //     how to set multiple icons? annotationManager also does it, but how actually?
        //   use one layer per icon
        //    -> much worse than normal

        // todo next
        //  add quest pins (and dots, but maybe later) using a customGeometrySource (or geoJsonSource?) and symbolLayer
        //   should be done in QuestPinsManager
        //   first don't set volatile, as it should be faster, and test performance
        //  memory: in an area with a lot of pins, maplibre seems to use a lot of memory
        //   but better check properly, because maybe it's similar to tangram, and noticeable just because it's doubled now

        // mapLibre always downloads "something" on startup: what is it, and why?

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

        // using a geoJsonSource for quests, and one layer for pins and one for circles
        //  actually if we would put the circle layer behind pins, and always visible it would already be close to default sc, right?
        //  issue: how to set an icon per pin? this can be done in a single layer, judging from annotationmanager code
        pinsSource = GeoJsonSource("pins-source", GeoJsonOptions().withBuffer(32))
        style.addSource(pinsSource!!)
        pinsLayer = SymbolLayer("pins-layer", "pins-source").withSourceLayer("pins-source")
//            .withProperties(PropertyValue("icon-image", "ic_quest_notes"), PropertyValue("icon-size", 0.3f),)
//            .withProperties(PropertyFactory.iconImage("{icon-image}")) // this does the trick! is {} necessary? yes!
            .withProperties(PropertyValue("icon-image", "{icon-image}")) // works, any difference to using PropertyFactory?
            .withProperties(PropertyFactory.symbolZOrder(Property.SYMBOL_Z_ORDER_SOURCE))
            // maybe not 100% perfect, but best so far without performance loss (should be order as in source, so just need to provide sorted list)
            // is sorting really worse than using symbol-sort-key? for one element it's definitely fine,
            //  and currently can't find issues where it would be wrong for nearby elements -> keep it for npw, and switch to symbol-sort-key" if necessary
//            .withProperties(PropertyValue("symbol-sort-key", "{symbol-sort-key}")) // looks like this does nothing... why?
//            .withProperties(PropertyFactory.symbolSortKey("{symbol-sort-key}")) // this is not working, as it expects a float...
//            .withProperties(PropertyFactory.symbolSortKey(Expression.get("symbol-sort-key"))) // works, but is actually somewhat slow...
                // with a single importance value per quest type (not per quest), performance is ok... though still somewhat worse than without this order
            // how to avoid sort key?
            //  maybe the order of the featurecollection is sufficient?
            //  try sorting the pins by importance, and not setting sort order
            //  -> it works usually (and especially: sorts quest of a single element), but sometimes doesn't work (e.g. at low zoom the wrong quest remains)
            //   maybe we can use clustering to avoid those issues at low zoom? there should be a way of setting a symbol instead of circle with text
//            .withProperties(PropertyValue("icon-size", 0.3f)) // better set icon size when creating the drawable, this is clearly faster when setting a lot of pins
        // clustering actually might improve performance with a lot of pins, and allow to do more things like https://github.com/streetcomplete/StreetComplete/issues/124#issuecomment-1137061717
        //  but this is a source option, so we would need a second source just for the dots... which seems very unnecessary
        pinsLayer!!.setFilter(Expression.gte(Expression.zoom(), 14f))
        style.addLayer(pinsLayer!!)
        val pinsLayer2 = CircleLayer("pins-layer2", "pins-source").withSourceLayer("pins-source")
//        pinsLayer2.setFilter(Expression.lt(Expression.zoom(), 14f))
//        style.addLayer(pinsLayer2)
        style.addLayerBelow(pinsLayer2, "pins-layer") // always show dots, but only below symbols

        // consider customGeometrySource
        //  map queries data directly, and caches returned data (probably unless set to volatile)
        //  but: query happens at every zoom level, which is unnecessary
        //   overscale can be used to ignore higher zoom levels, but when setting minZoom, getFeaturesForBounds is not called on lower zoom
        //   so there will be getFeaturesForBounds e.g. at z15, and we will need to supply data that the map actually might already have, but only for z16
        //  adding / removing pins would be easier, as arbitrary bbox can be invalidated
        //   but then data is not immediately reloaded...
        //   so better use setTileData when updating pins
        //    but then we need to adjust all available zoom levels (at least the ones we know that are loaded, so we would need to keep track of this, but we don't know what is still cached internally...)
        //    this means we need to set a lot of data (at many zoom levels!) when pins are updated
        // -> most likely not feasible, just keep the tangram way of caching pins (or maybe features)
/*        val options = CustomGeometrySourceOptions().withMaxZoom(16).withMinZoom(16) // request data only at z16 (and higher thanks to overscale) -> how to do it at 15?
        customGeometrySource = CustomGeometrySource("custom-source", options, object : GeometryTileProvider {
            override fun getFeaturesForBounds(bounds: LatLngBounds, zoomLevel: Int): FeatureCollection {
                // looks like it's actually requesting whole tiles (didn't verify though)
                return FeatureCollection.fromFeatures(emptyList())
            }
        })
        customGeometrySource!!.maxOverscaleFactorForParentTiles = 10
        style.addSource(customGeometrySource!!)
        style.addLayer(SymbolLayer("custom-geo", "custom-source"))
*/
        super.onMapReady(mapView, mapboxMap, style)

        mapboxMap.addOnMapClickListener { pos ->

            // check whether we clicked a feature
            val screenPoint: PointF = mapboxMap.projection.toScreenLocation(pos)
            val features = mapboxMap.queryRenderedFeatures(screenPoint, "pins-layer")
            if (features.isNotEmpty()) { // yes, there is a feature
                // is the first feature always the correct one?
                // maybe need to check order in maplibre sources, but in a quick test it always was the one rendered on top
                viewLifecycleScope.launch {
                    when (pinMode) {
                        PinMode.QUESTS -> {
                            val questKey = features.firstOrNull()?.properties()?.toQuestKey()
                            if (questKey != null) {
                                listener?.onClickedQuest(questKey)
                                return@launch
                            }
                        }
                        PinMode.EDITS -> {
                            val editKey = features.firstOrNull()?.properties()?.toEditKey()
                            if (editKey != null) {
                                listener?.onClickedEdit(editKey)
                                return@launch
                            }
                        }
                        PinMode.NONE -> {}
                    }
                }
                return@addOnMapClickListener true
            }

            // just click the map
            listener?.onClickedMapAt(LatLon(pos.latitude, pos.longitude), 1.0)
            false
        }
        mapboxMap.addOnMapLongClickListener { pos ->
            val pixel: PointF = mapboxMap.projection.toScreenLocation(pos)
            onLongPress(pixel.x, pixel.y)
            true
        }

        MainMapFragment.mapView = mapView
        MainMapFragment.mapboxMap = mapboxMap
        MainMapFragment.style = style
//        pinSymbolManager = SymbolManager(mapView, mapboxMap, style)
        pinSymbolManager?.addClickListener { symbol ->
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
//        pinSymbolManager?.setFilter(Expression.gte(Expression.zoom(), 14f)) // hide quests at low zoom
        // todo: 2 separate managers mean that we have to load the data twice
        //  is there a way of providing data only once?
        //  maybe Style.layers can be used here?
//        pinDotManager = CircleManager(mapView, mapboxMap, style)
        // hmm... this looks ugly. icons would be possible, and a filter depending on icon name
        // actually this is only useful if there is a separate collide/no collide thing
//        pinDotManager?.setFilter(Expression.lt(Expression.zoom(), 14f))

        overlaySymbolManager = SymbolManager(mapView, mapboxMap, style)
        overlaySymbolManager?.addClickListener {
            val key = it.data?.toElementKey()
            if (key != null) {
                viewLifecycleScope.launch {
                    listener?.onClickedElement(key)
                }
                true
            } else
                false
        }
        overlayLineManager = LineManager(mapView, mapboxMap, style)
        overlayLineManager?.addClickListener { line ->
            val key = line.data?.toElementKey()
            if (key != null) {
                viewLifecycleScope.launch {
                    listener?.onClickedElement(key)
                }
                true
            } else
                false
        }
        overlayFillManager = FillManager(mapView, mapboxMap, style)
        overlayFillManager?.addClickListener { fill ->
            val key = fill.data?.toElementKey()
            if (key != null) {
                viewLifecycleScope.launch {
                    listener?.onClickedElement(key)
                }
                true
            } else
                false
        }
        geometrySymbolManager = SymbolManager(mapView, mapboxMap, style)
        geometryLineManager = LineManager(mapView, mapboxMap, style)
        geometryFillManager = FillManager(mapView, mapboxMap, style)
        geometryCircleManger = CircleManager(mapView, mapboxMap, style)
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
        overlayLineManager = null
        overlayFillManager = null
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

    fun hideNonHighlightedPins() {
        pinsMapComponent?.isVisible = false
        // todo: what to do in mapLibre?
        //  ideally we would just set a filter on the symbolManager so it shows only pins of the current quest key
        //  but need to find some nice guide first
        //  and actually this would also hide icons set by geometryMapComponent
        //   simply use a second symbolManager? should work
    }

    fun hideOverlay() {
        styleableOverlayMapComponent?.isVisible = false
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
                val options = geometry.polylines.map { line ->
                    LineOptions()
                        .withLatLngs(line.map { it.toLatLng() })
                        .withLineColor("#D14000")
                        .withLineWidth(6f)
                        .withLineOpacity(0.5f)
                }
                geometryLineManager?.create(options)
            }
            is ElementPolygonsGeometry -> {
                geometryFillManager?.create(FillOptions()
                    .withLatLngs(geometry.polygons.map { it.map { it.toLatLng() } })
                    .withFillColor("#D14000")
                    .withFillOpacity(0.3f)
                )
                // create outline
                val options = geometry.polygons.map { line ->
                    LineOptions()
                        .withLatLngs(line.map { it.toLatLng() })
                        .withLineColor("#D14000")
                        .withLineWidth(6f)
                        .withLineOpacity(0.5f)
                }
                geometryLineManager?.create(options)
            }
            is ElementPointGeometry -> {
                geometryCircleManger?.create(CircleOptions()
                    .withCircleColor("#D14000")
                    .withLatLng(LatLng(geometry.center.latitude, geometry.center.longitude))
                    .withCircleRadius(14f)
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
        geometrySymbolManager?.deleteAll()
        geometryLineManager?.deleteAll()
        geometryCircleManger?.deleteAll()
        geometryFillManager?.deleteAll()
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
        var pinDotManager: CircleManager? = null
        var overlaySymbolManager: SymbolManager? = null
        var overlayLineManager: LineManager? = null
        var overlayFillManager: FillManager? = null
        var geometrySymbolManager: SymbolManager? = null
        var geometryLineManager: LineManager? = null
        var geometryFillManager: FillManager? = null
        var geometryCircleManger: CircleManager? = null
        var style: Style? = null
        var pinsSource: GeoJsonSource? = null
        var pinsLayer: SymbolLayer? = null
        var customGeometrySource: CustomGeometrySource? = null
    }
}
