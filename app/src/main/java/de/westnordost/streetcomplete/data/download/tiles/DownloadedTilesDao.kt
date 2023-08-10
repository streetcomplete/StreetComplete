package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.DATE
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.X
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.Y
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Keeps info in which areas things have been downloaded already in a tile grid */
class DownloadedTilesDao(private val db: Database) {

    /** Persist that the given tile range has been downloaded (now) */
    fun put(tilesRect: TilesRect) {
        val time = nowAsEpochMilliseconds()
        val tiles = tilesRect.asTilePosSequence()
        db.replaceMany(NAME,
            columnNames = arrayOf(X, Y, DATE),
            valuesList = tiles.map { arrayOf<Any?>(it.x, it.y, time) }.asIterable()
        )
    }

    /** Remove that given tile has been downloaded */
    fun delete(tile: TilePos): Int =
        db.delete(NAME,
            where = "$X = ? AND $Y = ?",
            args = arrayOf(tile.x.toString(), tile.y.toString())
        )

    fun deleteAll() {
        db.exec("DELETE FROM $NAME")
    }

    fun updateTimeNewerThan(tile: TilePos, time: Long) {
        db.update(NAME,
            values = listOf(DATE to time),
            where = "$X = ? AND $Y = ? AND $DATE > ?",
            args = arrayOf(tile.x.toString(), tile.y.toString(), time.toString()),
        )
    }

    fun updateAllTimesNewerThan(time: Long) {
        db.update(NAME,
            values = listOf(DATE to time),
            where = "$DATE > ?",
            args = arrayOf(time.toString()),
        )
    }

    fun deleteOlderThan(time: Long): Int =
        db.delete(NAME, where = "$DATE < $time")

    /** @return whether the given tiles range has been completely downloaded */
    fun contains(tilesRect: TilesRect, ignoreOlderThan: Long): Boolean {
         val tileCount = db.queryOne(NAME,
            columns = arrayOf("COUNT(*) as c"),
            where = "$X BETWEEN ? AND ? AND $Y BETWEEN ? AND ? AND $DATE > ?",
            args = arrayOf(
                tilesRect.left,
                tilesRect.right,
                tilesRect.top,
                tilesRect.bottom,
                ignoreOlderThan
            )
        ) { it.getInt("c") } ?: 0
        return tilesRect.size <= tileCount
    }

    /** @return all tiles that have been downloaded */
    fun getAll(ignoreOlderThan: Long): List<TilePos> =
        db.query(NAME,
            columns = arrayOf(X, Y),
            where = "$DATE > ?",
            args = arrayOf(ignoreOlderThan),
        ) { TilePos(it.getInt(X), it.getInt(Y)) }
}
