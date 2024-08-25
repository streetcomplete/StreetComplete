package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyledElement
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.android.maps.MapLibreMap

/** Manages the layer of styled map data in the map view:
 *  Gets told by the MainMapFragment when a new area is in view and independently pulls the map
 *  data for the bbox surrounding the area from database and holds it in memory. */
class StyleableOverlayManager(
    private val map: MapLibreMap,
    private val mapComponent: StyleableOverlayMapComponent,
    private val mapDataSource: MapDataWithEditsSource,
    private val selectedOverlaySource: SelectedOverlaySource
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()
    private val mapDataInViewMutex = Mutex()

    private val mapDataSourceMutex = Mutex()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private var updateJob: Job? = null

    private var overlay: Overlay? = null
        set(value) {
            if (field == value) return
            val wasNull = field == null
            val isNullNow = value == null
            field = value
            when {
                isNullNow -> hide()
                wasNull ->   show()
                else ->      {
                    clear()
                    invalidate()
                }
            }
        }

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            overlay = selectedOverlaySource.selectedOverlay
        }
    }

    private val mapDataListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            val oldUpdateJob = updateJob
            updateJob = viewLifecycleScope.launch {
                oldUpdateJob?.join() // don't cancel, as updateStyledElements only updates existing data
                updateStyledElements(updated, deleted)
            }
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            invalidate()
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
        overlay = null
        selectedOverlaySource.removeListener(overlayListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun show() {
        lastDisplayedRect = null
        onNewScreenPosition()
        mapDataSource.addListener(mapDataListener)
    }

    private fun invalidate() {
        lastDisplayedRect = null
        onNewScreenPosition()
    }

    private fun hide() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        clear()
        mapDataSource.removeListener(mapDataListener)
    }

    fun onNewScreenPosition() {
        if (overlay == null) return
        viewLifecycleScope.launch { updateCurrentScreenArea() }
    }

    private suspend fun updateCurrentScreenArea() {
        val zoom = map.cameraPosition.zoom
        if (zoom < TILES_ZOOM) return
        val displayedArea = withContext(Dispatchers.Main) { map.screenAreaToBoundingBox() }
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 32) return
        val isNewRect = lastDisplayedRect?.contains(tilesRect) != true
        if (!isNewRect) return

        lastDisplayedRect = tilesRect
        // Check QuestPinsManager::updateCurrentScreenArea for an explanation what this updateJob
        // stuff is about.
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            val bbox = tilesRect.asBoundingBox(TILES_ZOOM)
            setStyledElements(bbox)
        }
    }

    private fun clear() {
        lastDisplayedRect = null
        viewLifecycleScope.launch {
            mapDataInViewMutex.withLock { mapDataInView.clear() }
            withContext(Dispatchers.Main) { mapComponent.clear() }
        }
    }

    private suspend fun setStyledElements(bbox: BoundingBox) {
        val mapData = mapDataSourceMutex.withLock {
            withContext(Dispatchers.IO) { mapDataSource.getMapDataWithGeometry(bbox) }
        }
        val styledElements = mapDataInViewMutex.withLock {
            val overlay = overlay ?: return
            mapDataInView.clear()
            createStyledElementsByKey(overlay, mapData).forEach { (key, styledElement) ->
                mapDataInView[key] = styledElement
            }
            mapDataInView.values.toList()
        }
        mapComponent.set(styledElements)
    }

    private suspend fun updateStyledElements(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val styledElements = mapDataInViewMutex.withLock {
            val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM) ?: return
            var hasChanges = false
            val overlay = overlay ?: return

            deleted.forEach {
                if (mapDataInView.remove(it) != null) hasChanges = true
            }
            val styledElementsByKey = createStyledElementsByKey(overlay, updated).toMap()
            // elements that used to be displayed in the overlay but now not anymore
            updated.forEach {
                if (!styledElementsByKey.containsKey(it.key)) {
                    if (mapDataInView.remove(it.key) != null) hasChanges = true
                }
            }
            // elements that are either newly displayed or which were updated
            styledElementsByKey.forEach { (key, styledElement) ->
                if (displayedBBox.intersect(styledElement.geometry.getBounds())) {
                    mapDataInView[key] = styledElement
                    hasChanges = true
                } else {
                    if (mapDataInView.remove(key) != null) hasChanges = true
                }
            }

            if (!hasChanges) return

            mapDataInView.values.toList()
        }
        mapComponent.set(styledElements)
    }

    private fun createStyledElementsByKey(
        overlay: Overlay,
        mapData: MapDataWithGeometry
    ): Sequence<Pair<ElementKey, StyledElement>> =
        overlay.getStyledElements(mapData).mapNotNull { (element, style) ->
            val key = element.key
            val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
            key to StyledElement(element, geometry, style)
        }

    companion object {
        private const val TILES_ZOOM = 16
    }
}
