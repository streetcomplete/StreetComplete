package de.westnordost.streetcomplete.screens.main.map

import android.graphics.RectF
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.visiblequests.LevelFilter
import de.westnordost.streetcomplete.osm.ALL_ROADS
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.overlays.restriction.RestrictionOverlay
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyledElement
import de.westnordost.streetcomplete.screens.main.map.tangram.KtMapController
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext

/** Manages the layer of styled map data in the map view:
 *  Gets told by the MainMapFragment when a new area is in view and independently pulls the map
 *  data for the bbox surrounding the area from database and holds it in memory. */
class StyleableOverlayManager(
    private val ctrl: KtMapController,
    private val mapComponent: StyleableOverlayMapComponent,
    private val mapDataSource: MapDataWithEditsSource,
    private val selectedOverlaySource: SelectedOverlaySource,
    private val levelFilter: LevelFilter,
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var updateJob: Job? = null
    private val m = Mutex()

    // cache recent queries in some sort of crappy chaotic spatial cache
    // don't do it by tile, because this can be much slower to load in some cases
    private val cache = LinkedHashMap<TilesRect, HashMap<ElementKey, StyledElement>>(16, 0.9f, true)

    // return styledElements, and cache the ones from rects we don't have
    private fun getFromCache(tilesRect: TilesRect): Collection<StyledElement> {
        // get smallest cached rect that completely contains tileRect
        var smallestTile: TilesRect? = null
        cache.keys.forEach {
            if (it.contains(tilesRect) && it.size < (smallestTile?.size ?: 1000))
                smallestTile = it
        }
        if (smallestTile != null) return cache[smallestTile]!!.values
        // maybe we have it cached in multiple tiles
        val tiles = tilesRect.asTilePosSequence().toList()
        // info which cached tilesRect contains which tiles
        val a = cache.keys.mapNotNull { rect ->
            rect to tiles.filter { rect.contains(it.toTilesRect()) }.ifEmpty { return@mapNotNull null }
        }.sortedBy { it.first.size } // sort by rect size so we prefer small ones
        if (a.isEmpty()){
            return fetchAndCache(tilesRect)
        }

        val cachedTiles = hashSetOf<TilePos>()
        val fetchRects = mutableListOf<TilesRect>()
        for ((rect, list) in a) {
            if (list.any { it !in cachedTiles }) {
                cachedTiles.addAll(list)
                fetchRects.add(rect)
            }
        }
        // allow returning a larger area than wanted, this is slower when setting but uses cached data
        if (cachedTiles.containsAll(tiles) && fetchRects.flatMap { it.asTilePosSequence().toList() }.toHashSet().size <= tiles.size * 1.5) {
            val data = hashSetOf<StyledElement>()
            fetchRects.forEach { data.addAll(cache[it]!!.values) }
            return data
        }

        // still here -> just use the tiles that are fully contained in cache, and request the rest
        val fullyContainedTileRects = a.unzip().first.filter { tilesRect.contains(it) }
        val t = tiles.filterNot { tile -> fullyContainedTileRects.any { it.contains(tile.toTilesRect()) } }
        val minRect = t.minTileRect()
        return if (minRect == tilesRect || minRect == null) {
            fetchAndCache(tilesRect)
        } else {
            val data = hashSetOf<StyledElement>()
            fullyContainedTileRects.forEach { data.addAll(cache[it]!!.values) }
            data.addAll(fetchAndCache(minRect))
            data
        }
    }

    private fun fetchAndCache(tilesRect: TilesRect): Collection<StyledElement> {
        val mapData = mapDataSource.getMapDataWithGeometry(tilesRect.asBoundingBox(TILES_ZOOM))
        val overlay = overlay ?: return emptyList()
        val data = HashMap<ElementKey, StyledElement>(mapData.size / 20, 0.9f)
        createStyledElementsByKey(overlay, mapData).forEach { (key, styledElement) ->
            if (!levelFilter.levelAllowed(styledElement.element)) return@forEach
            data[key] = styledElement
        }

        cache[tilesRect] = data
        if (cache.size > 16) cache.keys.remove(cache.keys.first())
        return data.values
    }

    private var overlay: Overlay? = null
        set(value) {
            // always reload, even if the overlay is the same
            val wasNull = field == null
            val isNullNow = value == null
            field = value
            when {
                isNullNow -> hide()
                wasNull ->   show()
                else ->      switchOverlay()
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
                if (overlay is RestrictionOverlay
                        // reload all if relation is updated, because normal update doesn't change ways
                        // and reload if ways are updated, because without knowing the relation it will not be highlighted
                        && (updated.any { it is Relation || it.tags["highway"] in ALL_ROADS } || deleted.any { it.type == ElementType.RELATION })) {
                    lastDisplayedRect?.let {
                        cache.clear()
                        onNewTilesRect(it)
                    }
                }
            }
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
        overlay = null
        selectedOverlaySource.removeListener(overlayListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun show() {
        clear()
        onNewScreenPosition()
        mapDataSource.addListener(mapDataListener)
    }

    private fun switchOverlay() {
        clear()
        onNewScreenPosition()
    }

    private fun hide() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        clear()
        mapDataSource.removeListener(mapDataListener)
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
        updateJob?.cancel()
        updateJob = viewLifecycleScope.launch {
            while (m.isLocked) { delay(50) }
            if (!coroutineContext.isActive) return@launch
            val data = m.withLock { getFromCache(tilesRect) }
            if (!coroutineContext.isActive) return@launch
            mapComponent.set(data)
            ctrl.requestRender()
        }
    }

    private fun clear() {
        runBlocking { m.withLock { cache.clear() } }
        lastDisplayedRect = null
        viewLifecycleScope.launch { mapComponent.clear() }
    }

    private suspend fun updateStyledElements(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val overlay = overlay ?: return
        val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM)
        var changedAnything = false
        m.withLock {
            val bboxes = cache.keys.associateWith { it.asBoundingBox(TILES_ZOOM) }
            deleted.forEach { key ->
                cache.values.forEach {
                    if (it.remove(key) != null) changedAnything = true
                }
            }
            val styledElementsByKey = HashMap<ElementKey, StyledElement>(updated.size, 1f)
            createStyledElementsByKey(overlay, updated).forEach { styledElementsByKey[it.first] = it.second }
            // for elements that used to be displayed in the overlay but now not anymore
            updated.forEach { element ->
                val key = element.key
                if (!styledElementsByKey.containsKey(key)) {
                    cache.values.forEach { if (it.remove(key) != null) changedAnything = true }
                }
            }
            styledElementsByKey.forEach { (key, styledElement) ->
                if (!levelFilter.levelAllowed(styledElement.element)) return@forEach
                cache.forEach {
                    if (styledElement.geometry.getBounds().intersect(bboxes[it.key]!!))
                        it.value[key] = styledElement
                }
                if (!changedAnything && displayedBBox?.intersect(styledElement.geometry.getBounds()) != false) {
                    changedAnything = true
                }
            }

            if (changedAnything && coroutineContext.isActive) {
                mapComponent.set(lastDisplayedRect?.let { getFromCache(it) } ?: cache.values.flatMap { it.values }.toHashSet())
                ctrl.requestRender()
            }
        }
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
