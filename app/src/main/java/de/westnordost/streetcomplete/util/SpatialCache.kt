package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside

/**
 * Spatial cache containing items of type T that each must have an id of type K and a position
 * (LatLon). See [getKey] and [getPosition].
 *
 * @fetch needs to get data from db and fill other caches, e.g. questKey -> Quest
 *
 * the bbox queried in get() must fit in cache, i.e. may not be larger than maxTiles at tileZoom.
 */
class SpatialCache<K, T>(
    private val tileZoom: Int,
    private val maxTiles: Int,
    initialCapacity: Int?,
    private val fetch: (BoundingBox) -> Collection<T>,
    private val getKey: T.() -> K,
    private val getPosition: T.() -> LatLon,
) {
    private val byTile = LinkedHashMap<TilePos, HashSet<T>>((maxTiles / 0.75).toInt(), 0.75f, true)
    private val byKey = initialCapacity?.let { HashMap<K, T>(it) } ?: HashMap<K, T>()

    /** Number of tiles containing at least one item. Empty tiles are disregarded, as
     *  they barely use memory, and keeping them may avoid unnecessary fetches from database */
    val size get() = byTile.count { it.value.isNotEmpty() }

    /** @return a new list of all keys in the cache */
    fun getKeys(): List<K> = synchronized(this) { byKey.keys.toList() }

    /** @return a new set of all tilePos in the cache */
    fun getTiles(): Set<TilePos> = synchronized(this) { byTile.keys.toSet() }

    /** @return the item with the given [key] if in cache */
    fun get(key: K): T? = synchronized(this) {
        byKey[key]
    }

    /** @return the items with the given [keys] that are in the cache */
    fun getAll(keys: Collection<K>): List<T> = synchronized(this) {
        keys.mapNotNull { byKey[it] }
    }

    /**
     * Removes the keys in [deleted] from cache
     * Puts the [updatedOrAdded] items into cache only if the containing tile is already cached
     */
    fun update(updatedOrAdded: Iterable<T> = emptyList(), deleted: Iterable<K> = emptyList()) { synchronized(this) {
        for (key in deleted) {
            val item = byKey.remove(key) ?: continue
            byTile[item.getTilePos()]?.remove(item)
        }

        for (item in updatedOrAdded) {
            // items must be removed before they are re-added because e.g. position may have changed
            val oldItem = byKey.remove(item.getKey())
            if (oldItem != null) {
                byTile[oldItem.getTilePos()]?.remove(oldItem)
            }

            // items are only added if the tile they would be sorted into is already cached,
            // because all tiles that are in cache must be complete
            val tile = byTile[item.getTilePos()] ?: continue
            tile.add(item)
            byKey[item.getKey()] = item
        }
    } }

    /**
     * Replaces all tiles fully contained in the [bbox] with empty tiles.
     * Any tile only partially contained in the [bbox] is removed (because all tiles cached must be
     * complete).
     * The given [items] are added to cache if the containing tile is cached. Note that for putting
     * and item, it does not have to be inside the [bbox].
     * If the number of tiles exceeds maxTiles, only maxSize tiles are cached.
     */
    fun replaceAllInBBox(items: Collection<T>, bbox: BoundingBox) { synchronized(this) {
        val tiles = bbox.asListOfEnclosingTilePos()
        val (completelyContainedTiles, incompleteTiles) = tiles.partition { it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        if (incompleteTiles.isNotEmpty()) {
            Log.w(TAG, "bbox does not align with tiles, clearing incomplete tiles from cache")
            for (tile in incompleteTiles) {
                removeTile(tile)
            }
        }
        replaceAllInTiles(items, completelyContainedTiles)

        trim()
    } }

    /** replaces [tiles] with empty tiles and calls [update] for [items] */
    private fun replaceAllInTiles(items: Collection<T>, tiles: Collection<TilePos>) {
        // create / replace tiles
        for (tile in tiles) {
            removeTile(tile)
            byTile[tile] = HashSet()
        }

        // put items if tile is cached (not limited to collection of tiles to replace)
        update(updatedOrAdded = items)
    }

    /** @return all items inside [bbox], items will be loaded if necessary via [fetch] */
    fun get(bbox: BoundingBox): List<T> = synchronized(this) {
        val requiredTiles = bbox.asListOfEnclosingTilePos()

        val tilesToFetch = requiredTiles.filterNot { byTile.containsKey(it) }
        val tilesRectToFetch = tilesToFetch.minTileRect()
        if (tilesRectToFetch != null) {
            val newItems = fetch(tilesRectToFetch.asBoundingBox(tileZoom))
            replaceAllInTiles(newItems, tilesToFetch)
        }

        val items = requiredTiles.flatMap { tile ->
            if (tile.asBoundingBox(tileZoom).isCompletelyInside(bbox)) {
                byTile[tile]!!
            } else {
                byTile[tile]!!.filter { it.getPosition() in bbox }
            }
        }

        trim()

        return items
    }

    /** Reduces cache size to the given number of non-empty [tiles]. */
    fun trim(tiles: Int = maxTiles) { synchronized(this) {
        while (size > tiles) {
            val firstNonEmptyTile = byTile.entries.firstOrNull { it.value.isNotEmpty() }?.key ?: return
            removeTile(firstNonEmptyTile)
        }
    } }

    private fun removeTile(tilePos: TilePos) {
        val removedItems = byTile.remove(tilePos)
        if (removedItems != null) {
            for (item in removedItems) {
                byKey.remove(item.getKey())
            }
        }
    }

    /** Clears the cache */
    fun clear() { synchronized(this) {
        byKey.clear()
        byTile.clear()
    } }

    private fun T.getTilePos(): TilePos =
        getPosition().enclosingTilePos(tileZoom)

    private fun BoundingBox.asListOfEnclosingTilePos() =
        enclosingTilesRect(tileZoom).asTilePosSequence().toList()

    companion object {
        private const val TAG = "SpatialCache"
    }
}
