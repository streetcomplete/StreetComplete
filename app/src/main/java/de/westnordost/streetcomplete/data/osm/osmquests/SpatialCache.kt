package de.westnordost.streetcomplete.data.osm.osmquests

import android.util.Log
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.isCompletelyInside

// inspired by Android LruCache
// fetch needs get data from db and fill other caches, like questKey -> Quest
// onRemoveTile needs to clean other caches, like questKey -> Quest
// the bbox queried in get() must fit in cache, i.e. may not be larger than maxTiles at tileZoom
class SpatialCache<T>(
    private val maxTiles: Int,
    private val tileZoom: Int,
    private val fetch: (BoundingBox) -> Collection<Pair<T, LatLon>>,
    private val onKeysRemoved: (Collection<T>) -> Unit
) {
    private val byTile = LinkedHashMap<TilePos, HashSet<T>>((maxTiles/0.75).toInt(), 0.75f, true)
    private val byKey = hashMapOf<T, LatLon>()

    fun remove(key: T) {
        val pos = byKey.remove(key) ?: return
        byTile[getTilePosFor(pos)]?.remove(key)
        onKeysRemoved(listOf(key))
    }

    fun removeAll(keys: Collection<T>) {
        val removed = keys.filter {
            val pos = byKey.remove(it) ?: return@filter false
            byTile[getTilePosFor(pos)]?.remove(it)
            true
        }
        onKeysRemoved(removed)
    }

    // returns whether the key was put in cache
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

    // returns keys that were not put to cache because they are not in fully contained tiles
    // contrary to get(bbox), here the bbox may be larger than what can be put in cache
    fun replaceAllInBBox(entries: Collection<Pair<T, LatLon>>, bbox: BoundingBox): List<T> {
        val tiles = bbox.asListOfEnclosingTilePos()
        val completelyContainedTiles = tiles.filter { it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        if (tiles != completelyContainedTiles)
            Log.i(TAG, "replacing tiles not completely contained in BBox")
        val tilesToFetch = if (completelyContainedTiles.size > maxTiles) {
            Log.i(TAG, "trying to replacing more tiles than fit in cache, ignoring some tiles")
            completelyContainedTiles.subList(0,maxTiles-1)
        } else
            completelyContainedTiles
        val ignoredKeys = replaceAllInTiles(entries, tilesToFetch)

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
            entries.forEach { (key, pos) ->
                for (tile in tiles) {
                    if (bboxByTile[tile]!!.contains(pos)) {
                        byTile[tile]!!.add(key)
                        byKey[key] = pos
                        return@forEach
                    }
                }
                ignoredKeys.add(key)
            }
            return ignoredKeys
        }
    }

    // bbox must fit in cache!
    fun get(bbox: BoundingBox): List<T> {
        val requiredTiles = bbox.asListOfEnclosingTilePos()
        if (requiredTiles.size > maxTiles)
            throw(IllegalArgumentException("trying to get more tiles than fit in cache"))

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

        // resize cache now
        // don't do this right after putting new data, as this might remove tiles we need
        trim()

        return keys
    }

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
