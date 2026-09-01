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
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads and styles the selected overlay's OSM elements around a renderer-independent viewport.
 */
class StyleableOverlaySource(
    private val selectedOverlaySource: SelectedOverlaySource,
    private val mapDataSource: MapDataWithEditsSource,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _styledElements = MutableStateFlow<List<StyledElement>>(emptyList())
    val styledElements: StateFlow<List<StyledElement>> = _styledElements.asStateFlow()

    private val selectedOverlay = MutableStateFlow<Overlay?>(null)
    private val stateLock = ReentrantLock()
    private var lastViewport: Viewport? = null
    private var lastDisplayedRect: TilesRect? = null
    private val elementsInView = mutableMapOf<ElementKey, StyledElement>()
    private val elementsInViewMutex = Mutex()
    private val mapDataSourceMutex = Mutex()
    private var updateJob: Job? = null
    private var viewportGeneration = 0L
    private var isClosed = false

    private val listenerLock = ReentrantLock()
    private var isMapDataListenerAttached = false

    private val selectedOverlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            updateSelectedOverlay()
        }
    }

    private val mapDataListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            stateLock.withLock {
                if (isClosed) return
                val precedingUpdate = updateJob
                val generation = viewportGeneration
                updateJob = scope.launch {
                    precedingUpdate?.join()
                    updateStyledElements(updated, deleted, generation)
                }
            }
        }

        override fun onReplacedForBBox(
            bbox: BoundingBox,
            mapDataWithGeometry: MapDataWithGeometry,
        ) {
            reloadCurrentViewport()
        }

        override fun onCleared() {
            clear(resetViewport = false)
        }
    }

    init {
        selectedOverlaySource.addListener(selectedOverlayListener)
        updateSelectedOverlay()
    }

    fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) {
        stateLock.withLock {
            if (isClosed) return
            lastViewport = Viewport(zoom, displayedArea)
            processViewport(zoom, displayedArea)
        }
    }

    fun close() {
        stateLock.withLock {
            if (isClosed) return
            isClosed = true
            updateJob?.cancel()
        }
        selectedOverlaySource.removeListener(selectedOverlayListener)
        setMapDataListenerAttached(false)
        scope.cancel()
    }

    private fun processViewport(zoom: Double, displayedArea: BoundingBox?) {
        if (zoom < MIN_ZOOM || displayedArea == null) return

        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (tilesRect.size > MAX_TILES_IN_VIEW) return
        stateLock.withLock {
            if (
                isClosed || selectedOverlay.value == null ||
                lastDisplayedRect?.contains(tilesRect) == true
            ) return
            lastDisplayedRect = tilesRect
            loadViewport(tilesRect)
        }
    }

    private fun loadViewport(tilesRect: TilesRect) {
        stateLock.withLock {
            if (isClosed) return
            updateJob?.cancel()
            val generation = ++viewportGeneration
            updateJob = scope.launch {
                setStyledElements(tilesRect.asBoundingBox(TILES_ZOOM), generation)
            }
        }
    }

    private suspend fun setStyledElements(bbox: BoundingBox, generation: Long) {
        val coroutineContext = currentCoroutineContext()
        val overlay = selectedOverlay.value ?: return
        val mapData = mapDataSourceMutex.withLock {
            mapDataSource.getMapDataWithGeometry(bbox)
        }
        coroutineContext.ensureActive()
        if (selectedOverlay.value !== overlay) return

        elementsInViewMutex.withLock {
            coroutineContext.ensureActive()
            stateLock.withLock {
                // Reentrant listeners may clear the source while this load waits for state.
                coroutineContext.ensureActive()
                if (
                    isClosed || generation != viewportGeneration ||
                    selectedOverlay.value !== overlay
                ) return@withLock
                elementsInView.clear()
                createStyledElementsByKey(overlay, mapData).forEach { (key, element) ->
                    elementsInView[key] = element
                }
                _styledElements.value = elementsInView.values.toList()
            }
        }
    }

    private suspend fun updateStyledElements(
        updated: MapDataWithGeometry,
        deleted: Collection<ElementKey>,
        generation: Long,
    ) {
        val coroutineContext = currentCoroutineContext()
        val (displayedBBox, overlay) = stateLock.withLock {
            if (isClosed || generation != viewportGeneration) return
            val bbox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM) ?: return
            val currentOverlay = selectedOverlay.value ?: return
            bbox to currentOverlay
        }
        val styledElements = elementsInViewMutex.withLock {
            coroutineContext.ensureActive()
            var hasChanges = false
            deleted.forEach { if (elementsInView.remove(it) != null) hasChanges = true }

            val updatedStyles = createStyledElementsByKey(overlay, updated).toMap()
            updated.forEach { element ->
                if (element.key !in updatedStyles && elementsInView.remove(element.key) != null) {
                    hasChanges = true
                }
            }
            updatedStyles.forEach { (key, element) ->
                if (displayedBBox.intersect(element.geometry.bounds)) {
                    elementsInView[key] = element
                    hasChanges = true
                } else if (elementsInView.remove(key) != null) {
                    hasChanges = true
                }
            }

            if (!hasChanges) return
            elementsInView.values.toList()
        }
        stateLock.withLock {
            coroutineContext.ensureActive()
            if (
                isClosed || generation != viewportGeneration ||
                selectedOverlay.value !== overlay
            ) return
            _styledElements.value = styledElements
        }
    }

    private fun createStyledElementsByKey(
        overlay: Overlay,
        mapData: MapDataWithGeometry,
    ): Sequence<Pair<ElementKey, StyledElement>> =
        overlay.getStyledElements(mapData).mapNotNull { (element, style) ->
            val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
            element.key to StyledElement(element, geometry, style)
        }

    private fun updateSelectedOverlay() {
        val newOverlay = selectedOverlaySource.selectedOverlay
        stateLock.withLock {
            if (isClosed || selectedOverlay.value === newOverlay) return
            selectedOverlay.value = newOverlay
        }
        setMapDataListenerAttached(newOverlay != null)
        clear(resetViewport = false)
        if (newOverlay != null) reloadCurrentViewport()
    }

    private fun reloadCurrentViewport() {
        val viewport = stateLock.withLock {
            if (isClosed) return
            lastDisplayedRect = null
            lastViewport
        }
        viewport?.let { processViewport(it.zoom, it.displayedArea) }
    }

    private fun clear(resetViewport: Boolean) {
        stateLock.withLock {
            if (isClosed) return
            lastDisplayedRect = null
            if (resetViewport) lastViewport = null
            ++viewportGeneration
            _styledElements.value = emptyList()
            updateJob?.cancel()
            updateJob = scope.launch {
                elementsInViewMutex.withLock { elementsInView.clear() }
            }
        }
    }

    private fun setMapDataListenerAttached(attached: Boolean) {
        listenerLock.withLock {
            if (isMapDataListenerAttached == attached) return
            isMapDataListenerAttached = attached
            if (attached) mapDataSource.addListener(mapDataListener)
            else mapDataSource.removeListener(mapDataListener)
        }
    }

    private data class Viewport(val zoom: Double, val displayedArea: BoundingBox?)

    private companion object {
        const val TILES_ZOOM = 16
        const val MIN_ZOOM = 14.0
        const val MAX_TILES_IN_VIEW = 32
    }
}
