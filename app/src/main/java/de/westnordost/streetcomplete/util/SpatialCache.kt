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
 * <Key, LatLon> cache based on tiles, inspired by Android LruCache.
 * @fetch needs get data from db and fill other caches, e.g. questKey -> Quest.
 * @onKeysRemoved needs to clean other caches, e.g. questKey -> Quest.
 * trim() needs to be called after get() and getting data from other caches.
 * the bbox queried in get() must fit in cache, i.e. may not be larger than maxTiles at tileZoom.
 */
class SpatialCache<T>(
    private val maxTiles: Int,
    private val tileZoom: Int,
    private val fetch: (BoundingBox) -> Collection<Pair<T, LatLon>>,
    private val onKeysRemoved: (Collection<T>) -> Unit
) {
    private val byTile = LinkedHashMap<TilePos, HashSet<T>>((maxTiles/0.75).toInt(), 0.75f, true)
    private val byKey = hashMapOf<T, LatLon>()

    /**
     * Removes the given key from cache.
     * If a key was removed, onKeysRemoved is called.
     */
    fun remove(key: T) {
        val pos = byKey.remove(key) ?: return
        byTile[getTilePosFor(pos)]?.remove(key)
        onKeysRemoved(listOf(key))
    }

    /**
     * Removes the given keys from cache.
     * If any keys were removed, onKeysRemoved is called.
     */
    fun removeAll(keys: Collection<T>) {
        val removed = keys.filter {
            val pos = byKey.remove(it) ?: return@filter false
            byTile[getTilePosFor(pos)]?.remove(it)
            true
        }
        onKeysRemoved(removed)
    }

    /**
     * Puts the entry to cache only if the containing tile is already cached.
     * @return whether the key was put in cache
     */
    fun putIfTileExists(key: T, position: LatLon): Boolean {
        val previousPosition = byKey[key]
        // in cache already
        if (previousPosition != null) {
            // but moved -> remove
            if (previousPosition != position) remove(key)
            else return true
        }
        val tileInCache = byTile[getTilePosFor(position)]
        if (tileInCache  != null) {
            tileInCache.add(key)
            byKey[key] = position
            return true
        }
        return false
    }

    /**
     * Replaces all tiles fully contained in the bounding box.
     * If the number of tiles exceeds maxTiles, only the first maxSize tiles are cached.
     * @return all keys not put to cache, either because the tile was not fully contained
     * in the given bbox, or because the bbox contained too many tiles at tileZoom.
     */
    fun replaceAllInBBox(entries: Collection<Pair<T, LatLon>>, bbox: BoundingBox): List<T> {
        val tiles = bbox.asListOfEnclosingTilePos()
        val completelyContainedTiles = tiles.filter { it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        val incompleteTiles = tiles.filter { !it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        if (incompleteTiles.isNotEmpty()) {
            Log.w(TAG, "bbox does not align with tile, clearing incomplete tiles from cache")
            val removedKeys = hashSetOf<T>()
            incompleteTiles.forEach { tile ->
                byTile.remove(tile)?.let { removedKeys.addAll(it) }
            }
            byKey.keys.removeAll(removedKeys)
            onKeysRemoved(removedKeys)
        }
        val tilesToReplace = if (completelyContainedTiles.size > maxTiles) {
            Log.w(TAG, "trying to replacing more tiles than fit in cache, ignoring some tiles")
            completelyContainedTiles.subList(0,maxTiles-1)
        } else
            completelyContainedTiles
        val ignoredKeys = replaceAllInTiles(entries, tilesToReplace)

        // trim only when bbox!
        trim()
        return ignoredKeys
    }

    // may add tiles, but does not call trim()
    private fun replaceAllInTiles(entries: Collection<Pair<T, LatLon>>, tiles: Collection<TilePos>): List<T> {
        // create / replace tiles
        tiles.forEach { tile ->
            byTile.remove(tile)?.let { byKey.keys.removeAll(it) }
            byTile[tile] = hashSetOf()
        }
        if (tiles.size == 1) {
            // shortcut: replace old tile
            byTile[tiles.single()] = entries.unzip().first.toHashSet()
            byKey.putAll(entries)
            return emptyList()
        } else {
            // put only what is inside tiles
            // and return what wasn't put
            val ignoredKeys = mutableListOf<T>() // could also use sth like filter or mapNotNull instead of mutable list
            val bboxByTile = tiles.associateWith { it.asBoundingBox(tileZoom) }
            entries@for ((key, pos) in entries) {
                for (tile in tiles) {
                    if (bboxByTile[tile]!!.contains(pos)) {
                        byTile[tile]!!.add(key)
                        byKey[key] = pos
                        continue@entries
                    }
                }
                ignoredKeys.add(key)
            }
            return ignoredKeys
        }
    }

    /**
     * @return all keys inside bbox, will be loaded from db if necessary using fetch.
     * Call trim() after using get and getting data from other caches, as cache size may hav increased
     */
    fun get(bbox: BoundingBox): List<T> {
        val requiredTiles = bbox.asListOfEnclosingTilePos()

        val tilesToFetch = requiredTiles.filterNot { byTile.containsKey(it) }
        if (tilesToFetch.isNotEmpty()) {
            val newEntries = fetch(tilesToFetch.minTileRect()!!.asBoundingBox(tileZoom))
            replaceAllInTiles(newEntries, tilesToFetch)
        }

        val keys = requiredTiles.flatMap { tile ->
            if (tile.asBoundingBox(tileZoom).isCompletelyInside(bbox))
                byTile[tile]!!
            else
                byTile[tile]!!.filter { byKey[it]!! in bbox }
        }

        return keys
    }

    /**
     * Reduces cache size to the given number of tiles.
     * If any keys were removed, onKeysRemoved is called.
     */
    fun trim(size: Int = maxTiles) {
        if (byTile.size <= size) return

        val removedKeys = hashSetOf<T>()
        while (byTile.size > size) {
            byTile.remove(byTile.keys.first())?.let { removedKeys.addAll(it) }
        }
        if (removedKeys.isNotEmpty()) {
            byKey.keys.removeAll(removedKeys)
            onKeysRemoved(removedKeys)
        }
    }

    /**
     * clears the caches without calling onKeysRemoved
     */
    fun clear() {
        byKey.clear()
        byTile.clear()
    }

    private fun getTilePosFor(pos: LatLon): TilePos = pos.enclosingTilePos(tileZoom)

    private fun BoundingBox.asListOfEnclosingTilePos() = enclosingTilesRect(tileZoom).asTilePosSequence().toList()

    companion object {
        private const val TAG = "SpatialCache"
    }
}
