package de.westnordost.streetcomplete.screens.main.map

import android.graphics.RectF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
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
class StyleableOverlayManager(
    private val ctrl: KtMapController,
    private val mapComponent: StyleableOverlayMapComponent,
    private val mapDataSource: MapDataWithEditsSource,
    private val selectedOverlaySource: SelectedOverlaySource
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null

    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private var overlay: Overlay? = null
    set(value) {
        if (field == value) return
        field = value
        if (value != null) show() else hide()
    }

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            overlay = selectedOverlaySource.selectedOverlay
        }
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

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        overlay = selectedOverlaySource.selectedOverlay
        selectedOverlaySource.addListener(overlayListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        selectedOverlaySource.removeListener(overlayListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        hide()
        viewLifecycleScope.cancel()
    }

    private fun show() {
        clear()
        onNewScreenPosition()
        mapDataSource.addListener(mapDataListener)
    }

    private fun hide() {
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
        if (overlay == null) return
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
        val layer = overlay ?: return
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
        val layer = overlay ?: return
        synchronized(mapDataInView) {
            createStyledElementsByKey(layer, updated).forEach { (key, styledElement) ->
                if (styledElement != null) mapDataInView[key] = styledElement
                else                       mapDataInView.remove(key)
            }
            deleted.forEach { mapDataInView.remove(it) }
            mapComponent.set(mapDataInView.values)
        }
    }

    private fun createStyledElementsByKey(overlay: Overlay, mapData: MapDataWithGeometry): Sequence<Pair<ElementKey, StyledElement?>> =
        overlay.getStyledElements(mapData).map { (element, style) ->
            val key = ElementKey(element.type, element.id)
            val geometry = mapData.getGeometry(element.type, element.id)
            key to geometry?.let { StyledElement(element, geometry, style) }
        }

    companion object {
        private const val TILES_ZOOM = 16
    }
}
