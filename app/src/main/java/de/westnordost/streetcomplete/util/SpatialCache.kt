package de.westnordost.streetcomplete.util

import android.util.Log
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside

/**
 * Spatial cache containing items of type T that each must have an id of type K and a position
 * (LatLon). See [getKey] and [getPosition].
 *
 * @fetch needs get data from db and fill other caches, e.g. questKey -> Quest
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
    private val byTile = LinkedHashMap<TilePos, HashSet<T>>((maxTiles/0.75).toInt(), 0.75f, true)
    private val byKey = initialCapacity?.let { HashMap<K, T>(it) } ?: HashMap<K, T>()
    val size get() = byTile.size

    /** @return a new list of all keys in the cache */
    fun getKeys(): List<K> = synchronized(this) { byKey.keys.toList() }

    /** @return a new list of all tilePos in the cache */
    fun getTiles(): List<TilePos> = synchronized(this) { byTile.keys.toList() }

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
    fun update(updatedOrAdded: Iterable<T> = emptyList(), deleted: Iterable<K> = emptyList()) = synchronized(this) {
        for (key in deleted) {
            val item = byKey.remove(key) ?: continue
            byTile[item.getTilePos()]?.remove(item)
        }
        for (item in updatedOrAdded) {
            val key = item.getKey()
            // remove item if it exists
            byKey[item.getKey()]?.let {
                byTile[it.getTilePos()]!!.remove(it)
            }
            val tile = byTile[item.getTilePos()]
            if (tile != null) {
                tile.add(item)
                byKey[key] = item
            } else {
                byKey.remove(key)
            }
        }
    }

    /**
     * Replaces all tiles fully contained in the bounding box.
     * If the number of tiles exceeds maxTiles, only the first maxSize tiles are cached.
     */
    fun replaceAllInBBox(items: Collection<T>, bbox: BoundingBox) = synchronized(this) {
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
    }

    // may add tiles, but does not call trim()
    private fun replaceAllInTiles(items: Collection<T>, tiles: Collection<TilePos>) {
        // create / replace tiles
        for (tile in tiles) {
            removeTile(tile)
            byTile[tile] = HashSet()
        }
/*        for (item in items) {
            val tile = byTile[item.getTilePos()] ?: continue
            tile.add(item)
            byKey[item.getKey()] = item
        }*/
        val bboxByTile = byTile.keys.associateWith { it.asBoundingBox(tileZoom) }
        items@for (item in items) {
            val pos = item.getPosition()
            for (tile in tiles) {
                if (pos in bboxByTile[tile]!!) {
                    byTile[tile]!!.add(item)
                    byKey[item.getKey()] = item
                    continue@items
                }
            }
        }
    }

    /** @return all items inside [bbox], items will be loaded if necessary via [fetch] */
    fun get(bbox: BoundingBox): List<T> = synchronized(this) {
        val requiredTiles = bbox.asListOfEnclosingTilePos()

        val tilesToFetch = requiredTiles.filterNot { byTile.containsKey(it) }
        if (tilesToFetch.isNotEmpty()) {
            val newItems = fetch(tilesToFetch.minTileRect()!!.asBoundingBox(tileZoom))
            replaceAllInTiles(newItems, tilesToFetch)
        }

        val items = requiredTiles.flatMap { tile ->
            if (tile.asBoundingBox(tileZoom).isCompletelyInside(bbox))
                byTile[tile]!!
            else
                byTile[tile]!!.filter { it.getPosition() in bbox }
        }

        trim()

        return items
    }

    /** Reduces cache size to the given number of non-empty [tiles].
     *  Empty tiles are kept, as the barely use memory. This avoids empty tiles pushing other
     *  tiles out of the cache and thus may avoid database fetches. */
    fun trim(tiles: Int = maxTiles) = synchronized(this) {
        if (byTile.count { it.value.isNotEmpty() } <= tiles) return

        while (byTile.count { it.value.isNotEmpty() } > tiles) {
            removeTile(byTile.entries.firstOrNull { it.value.isNotEmpty() }?.key ?: byTile.keys.first())
        }
    }

    private fun removeTile(tilePos: TilePos) {
        val removedItems = byTile.remove(tilePos)
        if (removedItems != null) {
            for (item in removedItems) {
                byKey.remove(item.getKey())
            }
        }
    }

    /** Clears the cache */
    fun clear() = synchronized(this) {
        byKey.clear()
        byTile.clear()
    }

    private fun T.getTilePos(): TilePos =
        getPosition().enclosingTilePos(tileZoom)

    private fun BoundingBox.asListOfEnclosingTilePos() =
        enclosingTilesRect(tileZoom).asTilePosSequence().toList()

    companion object {
        private const val TAG = "SpatialCache"
    }
}
