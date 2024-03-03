package de.westnordost.streetcomplete.data.download.tiles

import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.splitAt180thMeridian
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.math.sinh
import kotlin.math.tan

/** X and Y position of a tile */
@Serializable
data class TilePos(val x: Int, val y: Int) {
    /** Returns this tile rect as a bounding box.
     *
     *  Note that the edges of a `BoundingBox`es are inclusive (a bounding box of 0,0 to 1,1 contains
     *  both position 0,0 and 1,1) while the edges of `TilePos`itions are inclusive only on the
     *  lower two edges but not on the upper two edges (a tile pos covering the area of 0,0 to 1,1
     *  contains 0,0 and only 0.999...,0.999... but not 1,1.
     *  So, a bounding box is always marginally smaller than a `TilePos` */
    fun asBoundingBox(zoom: Int) = BoundingBox(
        tile2lat(y + 1, zoom) + 1e-12,
        tile2lon(x, zoom),
        tile2lat(y, zoom),
        tile2lon(x + 1, zoom) - 1e-12
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

/** Returns up to two TileRects that together enclose all the tiles.
 *  These TilesRects together should contain fewer TilePos than [minTileRect].
 *  This method is specifically aimed at the SpatialCache to avoid loading the whole bbox after
 *  scrolling diagonally, and may not produce good results in other situations.
 *  If this function can't divide the Collection into two smaller TileRects, [minTileRect] is
 *  returned.
 */
fun Collection<TilePos>.upToTwoMinTileRects(): List<TilesRect>? {
    val minTileRect = minTileRect() ?: return null
    if (minTileRect.size == size)
        return listOf(minTileRect)

    val grouped = groupBy { it.y } // sort tiles in lines
    val lines = grouped.keys.sorted()
    if (lines != (lines.min()..lines.max()).toList())
        return listOf(minTileRect) // can't deal with missing lines

    val minXTop = grouped[lines.first()]!!.minOf { it.x }
    val maxXTop = grouped[lines.first()]!!.maxOf { it.x }
    val minXBottom = grouped[lines.last()]!!.minOf { it.x }
    val maxXBottom = grouped[lines.last()]!!.maxOf { it.x }

    // Find the line where minX or maxX change. Typically there should be such a line, because
    // otherwise minTileRect.size == size would return true. Exception is when there are holes,
    // e.g. from zooming out with the center cached and thus loading a "ring".
    val changeLine = lines.firstOrNull { line ->
        grouped[line]!!.minOf { it.x } != minXTop || grouped[line]!!.maxOf { it.x } != maxXTop
    } ?: return listOf(minTileRect)

    // Fall back to minTileRect if there is a second change in line width. We could deal with this,
    // but this is not a situation expected in spatialCache.
    if ((changeLine..lines.last()).any { line ->
            grouped[line]!!.minOf { it.x } != minXBottom || grouped[line]!!.maxOf { it.x } != maxXBottom
        })
        return listOf(minTileRect)

    return listOf(
        TilesRect(minXTop, lines.first(), maxXTop, changeLine-1),
        TilesRect(minXBottom, changeLine, maxXBottom, lines.last())
    )
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
     *  Note that the edges of a `BoundingBox`es are inclusive (a bounding box of 0,0 to 1,1 contains
     *  both position 0,0 and 1,1) while the edges of `TilesRect`s are inclusive only on the
     *  lower two edges but not on the upper two edges (a tile pos covering the area of 0,0 to 1,1
     *  contains 0,0 and only 0.999...,0.999... but not 1,1
     *  So, a bounding box is always marginally smaller than a `TileRect` */
    fun asBoundingBox(zoom: Int) = BoundingBox(
        tile2lat(bottom + 1, zoom) + 1e-12,
        tile2lon(left, zoom),
        tile2lat(top, zoom),
        tile2lon(right + 1, zoom) - 1e-12
    )

    fun contains(other: TilesRect): Boolean =
        left <= other.left && right >= other.right && top <= other.top && bottom >= other.bottom
}

/** Returns the bounding box of the tile rect at the given zoom level that encloses this bounding box.
 *  In other words, it expands this bounding box to fit to the tile boundaries.
 *  If this bounding box crosses the 180th meridian, it'll take only the first half of the bounding
 *  box*/
fun BoundingBox.asBoundingBoxOfEnclosingTiles(zoom: Int): BoundingBox =
    enclosingTilesRect(zoom).asBoundingBox(zoom)

/** Returns the tile rect that enclose this bounding box at the given zoom level. If this bounding
 *  box crosses the 180th meridian, it'll take only the first half of the bounding box */
fun BoundingBox.enclosingTilesRect(zoom: Int): TilesRect =
    if (crosses180thMeridian) {
        splitAt180thMeridian().first().enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
    } else {
        enclosingTilesRectOfBBoxNotCrossing180thMeridian(zoom)
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
    (180.0 / PI * atan(sinh(PI * (1.0 - 2.0 * y / numTiles(zoom))))).nextDown()

private fun lon2tile(lon: Double, zoom: Int): Int =
    (numTiles(zoom) * (lon + 180.0) / 360.0).toInt()

private fun lat2tile(lat: Double, zoom: Int): Int =
    (numTiles(zoom) * (1.0 - asinh(tan(lat * (PI / 180.0))) / PI) / 2.0).nextUp().toInt()

private fun numTiles(zoom: Int): Int = 1 shl zoom
