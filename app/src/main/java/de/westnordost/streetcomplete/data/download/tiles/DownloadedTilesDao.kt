package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.DATE
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.TYPE
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.X
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.Y
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.NAME
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Keeps info in which areas things have been downloaded already in a tile grid */
class DownloadedTilesDao(private val db: Database) {

    /** Persist that the given type has been downloaded in every tile in the given tile range  */
    fun put(tilesRect: TilesRect, typeName: String) {
        val time = nowAsEpochMilliseconds()
        db.replaceMany(NAME,
            arrayOf(X, Y, TYPE, DATE),
            tilesRect.asTilePosSequence().map { arrayOf<Any?>(
                it.x,
                it.y,
                typeName,
                time
            ) }.asIterable()
        )
    }

    /** Invalidate all types within the given tile. (consider them as not-downloaded) */
    fun remove(tile: TilePos): Int =
        db.delete(NAME, "$X = ? AND $Y = ?", arrayOf(tile.x.toString(), tile.y.toString()))

    fun removeAll() {
        db.exec("DELETE FROM $NAME")
    }

    /** @return a list of type names which have already been downloaded in every tile in the
     *  given tile range
     */
    fun get(tilesRect: TilesRect, ignoreOlderThan: Long): List<String> {
        val tileCount = tilesRect.size
        return db.query(NAME,
            columns = arrayOf(TYPE),
            where = "$X BETWEEN ? AND ? AND $Y BETWEEN ? AND ? AND $DATE > ?",
            args = arrayOf(
                tilesRect.left,
                tilesRect.right,
                tilesRect.top,
                tilesRect.bottom,
                ignoreOlderThan
            ),
            groupBy = TYPE,
            having = "COUNT(*) >= $tileCount"
        ) { it.getString(TYPE) }
    }
}
