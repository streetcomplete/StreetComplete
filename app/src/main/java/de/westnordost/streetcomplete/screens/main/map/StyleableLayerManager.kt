package de.westnordost.streetcomplete.screens.main.map

import android.graphics.RectF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.layers.Color
import de.westnordost.streetcomplete.layers.Layer
import de.westnordost.streetcomplete.layers.PointStyle
import de.westnordost.streetcomplete.layers.PolygonStyle
import de.westnordost.streetcomplete.layers.PolylineStyle
import de.westnordost.streetcomplete.layers.Style
import de.westnordost.streetcomplete.screens.main.map.components.StyleableLayerMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyledElement
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Manages the layer of styled map data in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the map
 *  data for the bbox surrounding the area from database and holds it in memory. */
class StyleableLayerManager(
    private val ctrl: KtMapController,
    private val mapComponent: StyleableLayerMapComponent,
    private val mapDataSource: MapDataWithEditsSource
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val isPrivateFilter by lazy { """
        nodes, ways, relations with
        access ~ private|no
        and (!foot or foot ~ private|no)
    """.toElementFilterExpression() }

    /** The layer to display */
    var layer: Layer? = null
        set(value) {
            if (field == value) return
            field = value
            if (value != null) start() else stop()
        }

    private val mapDataListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            updateStyledElements(updated, deleted)
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            clear()
            onNewScreenPosition()
        }

        override fun onCleared() {
            clear()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        stop()
        viewLifecycleScope.cancel()
    }

    private fun start() {
        onNewScreenPosition()
        mapDataSource.addListener(mapDataListener)
    }

    private fun stop() {
        clear()
        viewLifecycleScope.coroutineContext.cancelChildren()
        mapDataSource.removeListener(mapDataListener)
    }

    private fun clear() {
        synchronized(mapDataInView) { mapDataInView.clear() }
        lastDisplayedRect = null
        mapComponent.clear()
    }

    fun onNewScreenPosition() {
        if (layer == null) return
        val zoom = ctrl.cameraPosition.zoom
        if (zoom < TILES_ZOOM) return
        val displayedArea = ctrl.screenAreaToBoundingBox(RectF()) ?: return
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 16) return
        if (lastDisplayedRect?.contains(tilesRect) != true) {
            lastDisplayedRect = tilesRect
            onNewTilesRect(tilesRect)
        }
    }

    private fun onNewTilesRect(tilesRect: TilesRect) {
        val bbox = tilesRect.asBoundingBox(TILES_ZOOM)
        viewLifecycleScope.launch {
            val mapData = withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }
            setStyledElements(mapData)
        }
    }

    private fun setStyledElements(mapData: MapDataWithGeometry) {
        val layer = layer ?: return
        synchronized(mapDataInView) {
            mapDataInView.clear()
            createStyledElementsByKey(layer, mapData).forEach { (key, styledElement) ->
                if (styledElement != null) {
                    mapDataInView[key] = styledElement
                }
            }
            mapComponent.set(mapDataInView.values)
        }
    }

    private fun updateStyledElements(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val layer = layer ?: return
        synchronized(mapDataInView) {
            createStyledElementsByKey(layer, updated).forEach { (key, styledElement) ->
                if (styledElement != null) mapDataInView[key] = styledElement
                else                       mapDataInView.remove(key)
            }
            deleted.forEach { mapDataInView.remove(it) }
            mapComponent.set(mapDataInView.values)
        }
    }

    private fun createStyledElementsByKey(layer: Layer, mapData: MapDataWithGeometry): Sequence<Pair<ElementKey, StyledElement?>> =
        layer.getStyledElements(mapData).map { (element, style) ->
            val key = ElementKey(element.type, element.id)
            val geometry = mapData.getGeometry(element.type, element.id)
            key to geometry?.let { StyledElement(element, geometry, overrideStyle(style, element)) }
        }

    // TODO LAYERS "show last checked older X as not set" slider? -> controller simply modifies colors -> needs standard colors

    // TODO LAYERS may be cleaner after all to move this override-stuff to each layer; decide after introducing re-coloring based on tag age
    private fun overrideStyle(style: Style, element: Element): Style {
        return when (style) {
            is PointStyle -> style
            is PolygonStyle -> {
                val color = overrideColor(style.color, element)
                if (color !== style.color) style.copy(color) else style
            }
            is PolylineStyle -> {
                val colorLeft = style.colorLeft?.let { overrideColor(it, element) }
                val colorRight = style.colorRight?.let { overrideColor(it, element) }
                val color = style.color?.let { overrideColor(it, element) }
                if (colorLeft !== style.colorLeft || colorRight !== style.colorRight || color !== style.color) {
                    style.copy(color, colorLeft, colorRight)
                } else {
                    style
                }
            }
        }
    }

    private fun overrideColor(color: String, element: Element): String {
        if ((color == Color.UNSPECIFIED || color == Color.UNSUPPORTED) && isPrivateFilter.matches(element)) {
            return Color.INVISIBLE
        }
        return color
    }

    companion object {
        private const val TILES_ZOOM = 16
    }
}
