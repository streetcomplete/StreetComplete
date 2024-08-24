package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
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
    private val selectedOverlaySource: SelectedOverlaySource,
    private val levelFilter: LevelFilter,
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()
    private val mapDataInViewMutex = Mutex()

    private val mapDataSourceMutex = Mutex()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private var updateJob: Job? = null

    /* todo: either re-introduce this cache (if clear performance benefit), or kick it (if noticeable improvement, maybe do sth like that for SC?)
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
*/
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
                if (overlay is RestrictionOverlay
                        // reload all if relation is updated, because normal update doesn't change ways
                        // and reload if ways are updated, because without knowing the relation it will not be highlighted
                        && (updated.any { it is Relation || it.tags["highway"] in ALL_ROADS } || deleted.any { it.type == ElementType.RELATION })) {
                    lastDisplayedRect?.let {
                        //cache.clear() todo: remove when removing cache
                        setStyledElements(it.asBoundingBox(TILES_ZOOM))
                    }
                }
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
        //runBlocking { m.withLock { cache.clear() } }
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
                if (!levelFilter.levelAllowed(styledElement.element)) return@forEach
                mapDataInView[key] = styledElement
            }
            mapDataInView.values.toList()
        }
        mapComponent.set(styledElements)
    }

    // todo: when using cache, this here was not called iirc
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
                if (displayedBBox.intersect(styledElement.geometry.getBounds()) && levelFilter.levelAllowed(styledElement.element)) {
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
