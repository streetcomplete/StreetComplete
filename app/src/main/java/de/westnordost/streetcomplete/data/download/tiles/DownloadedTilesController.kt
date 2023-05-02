package de.westnordost.streetcomplete.data.download.tiles

import java.util.concurrent.CopyOnWriteArrayList

class DownloadedTilesController(
    private val dao: DownloadedTilesDao
): DownloadedTilesSource {

    private val listeners = CopyOnWriteArrayList<DownloadedTilesSource.Listener>()

    fun put(tilesRect: TilesRect) {
        dao.put(tilesRect, DownloadedTilesType.ALL)
        listeners.forEach { it.onUpdated() }
    }

    override fun contains(tilesRect: TilesRect, ignoreOlderThan: Long): Boolean =
        dao.get(tilesRect, ignoreOlderThan).contains(DownloadedTilesType.ALL)

    override fun getAll(ignoreOlderThan: Long): List<TilePos> =
        dao.getAll(DownloadedTilesType.ALL, ignoreOlderThan)

    fun remove(tile: TilePos) {
        dao.remove(tile)
        listeners.forEach { it.onUpdated() }
    }

    fun removeAll() {
        dao.removeAll()
        listeners.forEach { it.onUpdated() }
    }

    override fun addListener(listener: DownloadedTilesSource.Listener) {
        listeners.add(listener)
    }
    override fun removeListener(listener: DownloadedTilesSource.Listener) {
        listeners.remove(listener)
    }
}
