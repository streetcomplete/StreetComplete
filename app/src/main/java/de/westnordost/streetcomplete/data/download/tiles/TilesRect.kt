package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.splitAt180thMeridian
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sinh
import kotlin.math.tan

/** X and Y position of a tile */
@Serializable
data class TilePos(val x: Int, val y: Int) {
    /** Returns this tile rect as a bounding box.
     *
     *  In order that bounding boxes of neighbouring tiles do not overlap, a precision in number of
     *  digits the resulting bounding box is snapped to must be specified. The default is not
     *  incidentally the precision of coordinates in OSM */
    fun asBoundingBox(zoom: Int, precision: Int = 7) = BoundingBox(
        ceil(tile2lat(y + 1, zoom), precision),
        ceil(tile2lon(x, zoom), precision),
        floor(tile2lat(y, zoom), precision),
        floor(tile2lon(x + 1, zoom), precision)
    )

    fun toTilesRect() = TilesRect(x, y, x, y)
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
fun LatLon.enclosingTilePos(zoom: Int) = TilePos(
    lon2tile(((longitude + 180) % 360) - 180, zoom),
    lat2tile(latitude, zoom)
)

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

    /** Returns this tile rect as a bounding box.
     *
     *  In order that bounding boxes of neighbouring tiles do not overlap, a precision in number of
     *  digits the resulting bounding box is snapped to must be specified. The default is not
     *  incidentally the precision of coordinates in OSM */
    fun asBoundingBox(zoom: Int, precision: Int = 7) = BoundingBox(
        ceil(tile2lat(bottom + 1, zoom), precision),
        ceil(tile2lon(left, zoom), precision),
        floor(tile2lat(top, zoom), precision),
        floor(tile2lon(right + 1, zoom), precision)
    )

    fun contains(other: TilesRect): Boolean =
        left <= other.left && right >= other.right && top <= other.top && bottom >= other.bottom
}

/** Returns the bounding box of the tile rect at the given zoom level that encloses this bounding box.
 *  In other words, it expands this bounding box to fit to the tile boundaries.
 *  If this bounding box crosses the 180th meridian, it'll take only the first half of the bounding
 *  box*/
fun BoundingBox.asBoundingBoxOfEnclosingTiles(zoom: Int, precision: Int = 7): BoundingBox {
    return enclosingTilesRect(zoom).asBoundingBox(zoom, precision)
}

/** Returns the tile rect that enclose this bounding box at the given zoom level. If this bounding
 *  box crosses the 180th meridian, it'll take only the first half of the bounding box */
fun BoundingBox.enclosingTilesRect(zoom: Int): TilesRect {
    return if (crosses180thMeridian) {
        splitAt180thMeridian().first().enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
    } else {
        enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
    }
}

private fun BoundingBox.enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom: Int): TilesRect {
    val min = LatLon(min.latitude, min.longitude)
    val max = LatLon(max.latitude, max.longitude)
    val minTile = min.enclosingTilePos(zoom)
    val maxTile = max.enclosingTilePos(zoom)
    return TilesRect(minTile.x, maxTile.y, maxTile.x, minTile.y)
}

private fun tile2lon(x: Int, zoom: Int): Double =
    360.0 * x / numTiles(zoom).toDouble() - 180.0

private fun tile2lat(y: Int, zoom: Int): Double =
    180.0 * atan(sinh(PI * (1.0 - 2.0 * y / numTiles(zoom)))) / PI

private fun lon2tile(lon: Double, zoom: Int): Int =
    (numTiles(zoom) * (lon + 180.0) / 360.0).toInt()

private fun lat2tile(lat: Double, zoom: Int): Int =
    (numTiles(zoom) * (1.0 - asinh(tan(PI * lat / 180.0)) / PI) / 2.0).toInt()

private fun numTiles(zoom: Int): Int = 1 shl zoom

private fun ceil(value: Double, digits: Int): Double {
    val pow = 10.0.pow(digits)
    return kotlin.math.ceil(value * pow) / pow
}

private fun floor(value: Double, digits: Int): Double {
    val pow = 10.0.pow(digits)
    return kotlin.math.floor(value * pow) / pow
}
