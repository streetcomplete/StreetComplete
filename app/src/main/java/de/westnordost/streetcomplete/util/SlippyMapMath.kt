package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.splitAt180thMeridian
import kotlinx.serialization.Serializable
import kotlin.math.*

/** X and Y position of a tile */
@Serializable
data class TilePos(val x: Int, val y:Int) {
    /** Returns this tile rect as a bounding box */
    fun asBoundingBox(zoom: Int): BoundingBox {
        return BoundingBox(
            tile2lat(y + 1, zoom),
            tile2lon(x, zoom),
            tile2lat(y, zoom),
            tile2lon(x + 1, zoom)
        )
    }

    fun toTilesRect() = TilesRect(x,y,x,y)
}

/** Returns the minimum rectangle of tiles that encloses all the tiles */
fun Collection<TilePos>.minTileRect(): TilesRect? {
    if (isEmpty()) return null
    val right = maxByOrNull { it.x }!!.x
    val left = minByOrNull { it.x }!!.x
    val bottom = maxByOrNull { it.y }!!.y
    val top = minByOrNull { it.y }!!.y
    return TilesRect(left, top, right, bottom)
}

/** Returns the tile that encloses the position at the given zoom level */
fun LatLon.enclosingTilePos(zoom: Int): TilePos {
    return TilePos(
        lon2tile(((longitude + 180) % 360) - 180, zoom),
        lat2tile(latitude, zoom)
    )
}

/** A rectangle that represents containing all tiles from left bottom to top right */
@Serializable
data class TilesRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {

    init {
        require(left <= right && top <= bottom)
    }

    /** size of the tiles rect */
    val size: Int get() = (bottom - top + 1) * (right - left + 1)

    /** Returns all the individual tiles contained in this tile rect as an iterable sequence */
    fun asTilePosSequence(): Sequence<TilePos> = sequence {
        for (y in top..bottom) {
            for (x in left..right) {
                yield(TilePos(x, y))
            }
        }
    }

    /** Returns this tile rect as a bounding box */
    fun asBoundingBox(zoom: Int): BoundingBox {
        return BoundingBox(
            tile2lat(bottom + 1, zoom),
            tile2lon(left, zoom),
            tile2lat(top, zoom),
            tile2lon(right + 1, zoom)
        )
    }
}

/** Returns the bounding box of the tile rect at the given zoom level that encloses this bounding box.
 *  In other words, it expands this bounding box to fit to the tile boundaries.
 *  If this bounding box crosses the 180th meridian, it'll take only the first half of the bounding
 *  box*/
fun BoundingBox.asBoundingBoxOfEnclosingTiles(zoom: Int): BoundingBox {
    return enclosingTilesRect(zoom).asBoundingBox(zoom)
}

/** Returns the tile rect that enclose this bounding box at the given zoom level. If this bounding
 *  box crosses the 180th meridian, it'll take only the first half of the bounding box */
fun BoundingBox.enclosingTilesRect(zoom: Int): TilesRect {
    return if (crosses180thMeridian) {
        splitAt180thMeridian().first().enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
    }
    else {
        enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
    }

}

private fun BoundingBox.enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom: Int): TilesRect {
    /* TilesRect.asBoundingBox returns a bounding box that intersects in line with the neighbouring
     *  tiles to ensure that there is no space between the tiles. So when converting a bounding box
     *  that exactly fits a tiles rect back to a tiles rect, it must be made smaller by the tiniest
     *  amount */
    val notTheNextTile = 1e-7
    val min = LatLon(min.latitude + notTheNextTile, min.longitude + notTheNextTile)
    val max = LatLon(max.latitude - notTheNextTile, max.longitude - notTheNextTile)
    val minTile = min.enclosingTilePos(zoom)
    val maxTile = max.enclosingTilePos(zoom)
    return TilesRect(minTile.x, maxTile.y, maxTile.x, minTile.y)
}

private fun tile2lon(x: Int, zoom: Int): Double =
    x / numTiles(zoom).toDouble() * 360.0 - 180.0

private fun tile2lat(y: Int, zoom: Int): Double =
    atan(sinh(PI - 2.0 * PI * y / numTiles(zoom))).toDegrees()

private fun lon2tile(lon: Double, zoom: Int): Int =
    (numTiles(zoom) * (lon + 180.0) / 360.0).toInt()

private fun lat2tile(lat: Double, zoom: Int): Int =
    (numTiles(zoom) * (1.0 - asinh(tan(lat.toRadians())) / PI) / 2.0).toInt()

private fun numTiles(zoom: Int): Int = 1 shl zoom

private fun Double.toDegrees() = this / PI * 180.0
private fun Double.toRadians() = this / 180.0 * PI
