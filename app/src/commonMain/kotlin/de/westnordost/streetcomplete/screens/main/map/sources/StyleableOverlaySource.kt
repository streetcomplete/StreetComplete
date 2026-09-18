package de.westnordost.streetcomplete.screens.main.map.sources

import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.screens.main.map.layers.StyledElement
import de.westnordost.streetcomplete.screens.main.map.layers.isDisabled
import de.westnordost.streetcomplete.screens.main.map.layers.toElementKey
import de.westnordost.streetcomplete.screens.main.map.toBoundingBox
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.maplibre.compose.camera.CameraState

class StyleableOverlaySource(
    private val selectedOverlaySource: SelectedOverlaySource,
    private val mapDataWithEditsSource: MapDataWithEditsSource,
) {
    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    val styledElements: StateFlow<Collection<StyledElement>> get() = _styledElements
    private val _styledElements = MutableStateFlow<Collection<StyledElement>>(emptyList())

    private val selectedOverlay = MutableStateFlow<Overlay?>(null)

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()
    private val mapDataInViewMutex = Mutex()

    private val mapDataSourceMutex = Mutex()

    private var updateJob: Job? = null

    private val selectedOverlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() { updateSelectedOverlay() }
    }
    private val mapDataWithEditsListener = object : MapDataWithEditsSource.Listener {
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

    init {
        updateSelectedOverlay()
        selectedOverlaySource.addListener(selectedOverlayListener)
    }

    fun onDestroy() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        selectedOverlaySource.removeListener(selectedOverlayListener)
    }

    fun onMapMoved(cameraState: CameraState) {
        // require zoom >= 14, which is the lowest zoom level where quests are shown
        val zoom = cameraState.position.zoom
        if (zoom < 14) return
        val displayedArea = cameraState.viewport
            ?.visibleBoundingBox
            ?.toBoundingBox()
            ?: return
        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        // area too big -> skip (performance)
        if (tilesRect.size > 32) return
        val isNewRect = lastDisplayedRect?.contains(tilesRect) != true
        if (!isNewRect) return
        setStyledElements(tilesRect)
        lastDisplayedRect = tilesRect
    }

    private fun setStyledElements(tilesRect: TilesRect) {
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            val bbox = tilesRect.asBoundingBox(TILES_ZOOM)
            setStyledElements(bbox)
        }
    }

    private suspend fun setStyledElements(bbox: BoundingBox) {
        val overlay = selectedOverlay.value
        if (overlay == null) {
            mapDataInViewMutex.withLock { mapDataInView.clear() }
            _styledElements.value = emptyList()
        } else {
            val mapData = mapDataSourceMutex.withLock {
                withContext(Dispatchers.IO) { mapDataWithEditsSource.getMapDataWithGeometry(bbox) }
            }
            val styledElements = mapDataInViewMutex.withLock {
                mapDataInView.clear()
                createStyledElementsByKey(overlay, mapData).forEach { (key, styledElement) ->
                    mapDataInView[key] = styledElement
                }
                mapDataInView.values.toList()
            }
            _styledElements.value = styledElements
        }
    }

    private suspend fun updateStyledElements(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val styledElements = mapDataInViewMutex.withLock {
            val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM) ?: return
            var hasChanges = false
            val overlay = selectedOverlay.value ?: return

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
                if (displayedBBox.intersect(styledElement.geometry.bounds)) {
                    mapDataInView[key] = styledElement
                    hasChanges = true
                } else {
                    if (mapDataInView.remove(key) != null) hasChanges = true
                }
            }

            if (!hasChanges) return

            mapDataInView.values.toList()
        }
        _styledElements.value = styledElements
    }

    private fun updateSelectedOverlay() {
        selectedOverlay.value = selectedOverlaySource.selectedOverlay
        invalidate()
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

    private fun invalidate() {
        val rect = lastDisplayedRect
        if (rect != null) {
            setStyledElements(rect)
        } else {
            clear()
        }
    }

    private fun clear() {
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            mapDataInViewMutex.withLock { mapDataInView.clear() }
            _styledElements.value = emptyList()
        }
    }

    fun getElementKey(properties: JsonObject): ElementKey? =
        if (!properties.isDisabled()) properties.toElementKey() else null

    companion object {
        private const val TILES_ZOOM = 16
        private const val MIN_ZOOM = 14
    }
}
