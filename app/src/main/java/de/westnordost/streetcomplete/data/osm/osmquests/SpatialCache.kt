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
// careful: put does not actually put key into cache if tile is not cached -> update db blocking if key was not put
// TODO: synchronized stuff? didn't get related crash so far...
// TODO: tile zoom as parameter? could smaller tiles help for mapDataController?
class SpatialCache<T>(
    private val maxSize: Int,
    private val fetch: (BoundingBox) -> Map<T, LatLon>,
    private val onEntriesRemoved: (Set<T>) -> Unit
) {
    private val byTile = LinkedHashMap<TilePos, HashSet<T>>((maxSize/0.75).toInt(), 0.75f, true)
    private val byKey = hashMapOf<T, LatLon>()

    fun remove(key: T) {
        val pos = byKey.remove(key) ?: return
        byTile[getTilePosFor(pos)]?.remove(key)
        onEntriesRemoved(setOf(key))
    }

    fun removeAll(keys: Collection<T>) {
        val removed = keys.filter {
            val pos = byKey.remove(it) ?: return@filter false
            byTile[getTilePosFor(pos)]?.remove(it)
            true
        }
        onEntriesRemoved(removed.toSet())
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

    fun replaceAllInBBox(entries: Map<T, LatLon>, bbox: BoundingBox) {
        replaceAllInTiles(entries, bbox.asListOfEnclosingTilePos())
        // trim only when bbox!
        trim()
    }

    // may add tiles, but does not call trim()
    private fun replaceAllInTiles(entries: Map<T, LatLon>, tiles: Collection<TilePos>) {
        // create / replace tiles
        tiles.forEach { tile ->
            byTile.remove(tile)?.let { byKey.keys.removeAll(it) }
            byTile[tile] = hashSetOf()
        }
        if (tiles.size == 1) {
            // shortcut: replace old tile
            byTile[tiles.single()] = entries.keys.toHashSet()
            byKey.putAll(entries)
        } else {
            // put only what is inside tiles
            tiles.forEach { tile ->
                val bbox = tile.asBoundingBox(TILE_ZOOM)
                entries.forEach { (key, pos) ->
                    if (bbox.contains(pos)) {
                        byTile[tile]!!.add(key)
                        byKey[key] = pos
                    }
                }
            }
        }
    }

    fun get(bbox: BoundingBox): List<T> {
        // TODO: shortcut for creation of tilePos list?
        val requiredTiles = bbox.asListOfEnclosingTilePos()

        val tilesToFetch = requiredTiles.filterNot { byTile.containsKey(it) }
        if (tilesToFetch.isNotEmpty()) {
            val newEntries = fetch(tilesToFetch.minTileRect()!!.asBoundingBox(TILE_ZOOM))
            replaceAllInTiles(newEntries, tilesToFetch)
        }

        val keys = requiredTiles.flatMap { tile ->
            if (tile.asBoundingBox(TILE_ZOOM).isCompletelyInside(bbox))
                byTile[tile]!!
            else
                byTile[tile]!!.filter { bbox.contains(byKey[it]!!) } // todo: why can't i use "in bbox"?
        }

        // resize cache now
        // don't do this right after putting new data, as this might remove tiles we need
        trim()

        return keys
    }

    fun trim(size: Int = maxSize) {
        if (byTile.size <= size) return

        val removedKeys = hashSetOf<T>()
        while (byTile.size > size) {
            byTile.remove(byTile.keys.first())?.let { removedKeys.addAll(it) }
        }
        if (removedKeys.isNotEmpty()) {
            byKey.keys.removeAll(removedKeys)
            onEntriesRemoved(removedKeys)
        }
    }

    fun clear() {
        byKey.clear()
        byTile.clear()
        Log.i("cachetest", "cache cleared")
    }

}

private const val TILE_ZOOM = 16

private fun getTilePosFor(pos: LatLon): TilePos = pos.enclosingTilePos(TILE_ZOOM)

private fun BoundingBox.asListOfEnclosingTilePos() = enclosingTilesRect(TILE_ZOOM).asTilePosSequence().toList()
