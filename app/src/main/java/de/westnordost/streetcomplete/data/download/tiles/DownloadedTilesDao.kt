package de.westnordost.streetcomplete.data.download.tiles

import android.database.sqlite.SQLiteOpenHelper
import androidx.core.content.contentValuesOf
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.X
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.Y
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.TYPE
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.Columns.DATE
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesTable.NAME
import de.westnordost.streetcomplete.ktx.query
import de.westnordost.streetcomplete.util.Tile
import de.westnordost.streetcomplete.util.TilesRect

import javax.inject.Inject

/** Keeps info in which areas things have been downloaded already in a tile grid */
class DownloadedTilesDao @Inject constructor(private val dbHelper: SQLiteOpenHelper) {

    private val db get() = dbHelper.writableDatabase

    /** Persist that the given type has been downloaded in every tile in the given tile range  */
    fun put(tilesRect: TilesRect, typeName: String) {
        val time = System.currentTimeMillis()
        for (tile in tilesRect.asTileSequence()) {
            val values = contentValuesOf(
                X to tile.x,
                Y to tile.y,
                TYPE to typeName,
                DATE to time
            )
            db.replaceOrThrow(NAME, null, values)
        }
    }

    /** Invalidate all types within the given tile. (consider them as not-downloaded) */
    fun remove(tile: Tile): Int {
        return db.delete(NAME, "$X = ? AND $Y = ?", arrayOf(tile.x.toString(), tile.y.toString()))
    }

    fun removeAll() {
        db.execSQL("DELETE FROM $NAME")
    }

    /** @return a list of type names which have already been downloaded in every tile in the
     *  given tile range
     */
    fun get(tilesRect: TilesRect, ignoreOlderThan: Long): List<String> {
        val tileCount = tilesRect.size
        return db.query(NAME,
            columns = arrayOf(TYPE),
            selection = "$X BETWEEN ? AND ? AND $Y BETWEEN ? AND ? AND $DATE > ?",
            selectionArgs = arrayOf(
                tilesRect.left.toString(),
                tilesRect.right.toString(),
                tilesRect.top.toString(),
                tilesRect.bottom.toString(),
                ignoreOlderThan.toString()
            ),
            groupBy = TYPE,
            having = "COUNT(*) >= $tileCount") { it.getString(0) }
    }
}
