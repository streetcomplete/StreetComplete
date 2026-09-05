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
    private val mapDataWithEditsSource: MapDataWithEditsSource,
    workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)
    private val _styledElements = MutableStateFlow<List<StyledElement>>(emptyList())
    val styledElements: StateFlow<List<StyledElement>> = _styledElements.asStateFlow()

    private val selectedOverlay = MutableStateFlow<Overlay?>(null)
    private val stateLock = ReentrantLock()
    private var lastViewport: Viewport? = null
    private var lastDisplayedRect: TilesRect? = null
    private var mapDataInView = mutableMapOf<ElementKey, StyledElement>()
    private val mapDataInViewMutex = Mutex()
    private val mapDataSourceMutex = Mutex()
    private var updateJob: Job? = null
    private var viewportGeneration = 0L
    private var isActive = false
    private var isClosed = false

    private val listenerLock = ReentrantLock()
    private var isSelectedOverlayListenerAttached = false
    private var isMapDataListenerAttached = false

    private val selectedOverlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            updateSelectedOverlay()
        }
    }

    private val mapDataWithEditsListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            stateLock.withLock {
                if (isClosed || !isActive) return
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

    fun onViewportChanged(zoom: Double, displayedArea: BoundingBox?) {
        stateLock.withLock {
            if (isClosed) return
            lastViewport = Viewport(zoom, displayedArea)
            if (!isActive) return
            processViewport(zoom, displayedArea)
        }
    }

    /** Starts or stops overlay loading with the map's presentation lifecycle. */
    fun setActive(active: Boolean) {
        stateLock.withLock {
            if (isClosed || isActive == active) return
            isActive = active
            if (!active) {
                lastDisplayedRect = null
                ++viewportGeneration
                updateJob?.cancel()
            }
        }
        setSelectedOverlayListenerAttached(active)
        if (active) {
            updateSelectedOverlay(forceReload = true)
        } else {
            setMapDataListenerAttached(false)
        }
    }

    fun close() {
        stateLock.withLock {
            if (isClosed) return
            isClosed = true
            isActive = false
            updateJob?.cancel()
        }
        setSelectedOverlayListenerAttached(false)
        setMapDataListenerAttached(false)
        scope.cancel()
    }

    private fun processViewport(zoom: Double, displayedArea: BoundingBox?) {
        if (zoom < MIN_ZOOM || displayedArea == null) return

        val tilesRect = displayedArea.enclosingTilesRect(TILES_ZOOM)
        if (tilesRect.size > MAX_TILES_IN_VIEW) return
        stateLock.withLock {
            if (
                isClosed || !isActive || selectedOverlay.value == null ||
                lastDisplayedRect?.contains(tilesRect) == true
            ) return
            lastDisplayedRect = tilesRect
            loadViewport(tilesRect)
        }
    }

    private fun loadViewport(tilesRect: TilesRect) {
        stateLock.withLock {
            if (isClosed || !isActive) return
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
            mapDataWithEditsSource.getMapDataWithGeometry(bbox)
        }
        coroutineContext.ensureActive()
        if (selectedOverlay.value !== overlay) return

        // Overlay styling walks all loaded map elements and can be expensive in dense areas.
        // Prepare it before taking either source-state lock so camera callbacks remain cheap.
        val prepared = createStyledElementsByKey(overlay, mapData).toMap().toMutableMap()
        val styledElements = prepared.values.toList()
        coroutineContext.ensureActive()

        mapDataInViewMutex.withLock {
            coroutineContext.ensureActive()
            stateLock.withLock {
                // Swap the complete generation atomically so a superseded load cannot seed a
                // later delta with stale elements.
                coroutineContext.ensureActive()
                if (
                    !isClosed && isActive && generation == viewportGeneration &&
                    selectedOverlay.value === overlay
                ) {
                    mapDataInView = prepared
                    _styledElements.value = styledElements
                }
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
            if (isClosed || !isActive || generation != viewportGeneration) return
            val bbox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM) ?: return
            val currentOverlay = selectedOverlay.value ?: return
            bbox to currentOverlay
        }
        val styledElements = mapDataInViewMutex.withLock {
            coroutineContext.ensureActive()
            var hasChanges = false
            deleted.forEach { if (mapDataInView.remove(it) != null) hasChanges = true }

            val updatedStyles = createStyledElementsByKey(overlay, updated).toMap()
            updated.forEach { element ->
                if (element.key !in updatedStyles && mapDataInView.remove(element.key) != null) {
                    hasChanges = true
                }
            }
            updatedStyles.forEach { (key, element) ->
                if (displayedBBox.intersect(element.geometry.bounds)) {
                    mapDataInView[key] = element
                    hasChanges = true
                } else if (mapDataInView.remove(key) != null) {
                    hasChanges = true
                }
            }

            if (!hasChanges) return
            mapDataInView.values.toList()
        }
        stateLock.withLock {
            coroutineContext.ensureActive()
            if (
                isClosed || !isActive || generation != viewportGeneration ||
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
            val key = element.key
            val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
            key to StyledElement(element, geometry, style)
        }

    private fun updateSelectedOverlay(forceReload: Boolean = false) {
        val newOverlay = selectedOverlaySource.selectedOverlay
        val changed = stateLock.withLock {
            if (isClosed || !isActive) return
            if (selectedOverlay.value === newOverlay) return@withLock false
            selectedOverlay.value = newOverlay
            true
        }
        setMapDataListenerAttached(newOverlay != null)
        if (changed) clear(resetViewport = false)
        if (newOverlay != null && (changed || forceReload)) reloadCurrentViewport()
    }

    private fun reloadCurrentViewport() {
        val viewport = stateLock.withLock {
            if (isClosed || !isActive) return
            lastDisplayedRect = null
            lastViewport
        }
        viewport?.let { processViewport(it.zoom, it.displayedArea) }
    }

    private fun clear(resetViewport: Boolean) {
        stateLock.withLock {
            if (isClosed || !isActive) return
            lastDisplayedRect = null
            if (resetViewport) lastViewport = null
            ++viewportGeneration
            _styledElements.value = emptyList()
            updateJob?.cancel()
            updateJob = scope.launch {
                mapDataInViewMutex.withLock { mapDataInView.clear() }
            }
        }
    }

    private fun setMapDataListenerAttached(attached: Boolean) {
        listenerLock.withLock {
            if (isMapDataListenerAttached == attached) return
            isMapDataListenerAttached = attached
            if (attached) mapDataWithEditsSource.addListener(mapDataWithEditsListener)
            else mapDataWithEditsSource.removeListener(mapDataWithEditsListener)
        }
    }

    private fun setSelectedOverlayListenerAttached(attached: Boolean) {
        listenerLock.withLock {
            if (isSelectedOverlayListenerAttached == attached) return
            isSelectedOverlayListenerAttached = attached
            if (attached) selectedOverlaySource.addListener(selectedOverlayListener)
            else selectedOverlaySource.removeListener(selectedOverlayListener)
        }
    }

    private data class Viewport(val zoom: Double, val displayedArea: BoundingBox?)

    private companion object {
        const val TILES_ZOOM = 16
        const val MIN_ZOOM = 14.0
        const val MAX_TILES_IN_VIEW = 32
    }
}
