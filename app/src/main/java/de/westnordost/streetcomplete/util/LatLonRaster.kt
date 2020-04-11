package de.westnordost.streetcomplete.util

import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/** A spatial index implemented as a grid, based on points  */
class LatLonRaster(bounds: BoundingBox, private val cellSize: Double) {
    private val raster: Array<ArrayList<LatLon>?>
    private val rasterWidth: Int
    private val rasterHeight: Int
    private val bbox: BoundingBox
    var size = 0
        private set

    init {
        val lonDiff = normalizeLongitude(bounds.maxLongitude - bounds.minLongitude)
        val latDiff = bounds.maxLatitude - bounds.minLatitude
        rasterWidth = ceil(lonDiff / cellSize).toInt()
        rasterHeight = ceil(latDiff / cellSize).toInt()
        raster = arrayOfNulls(rasterWidth * rasterHeight)
        val maxLon = normalizeLongitude(bounds.minLongitude + rasterWidth * cellSize)
        val maxLat = bounds.minLatitude + rasterHeight * cellSize
        bbox = BoundingBox(bounds.min, OsmLatLon(maxLat, maxLon))
    }

    fun insert(p: LatLon) {
        val x = longitudeToCellX(p.longitude)
        val y = latitudeToCellY(p.latitude)
        checkBounds(x, y)
        var list = raster[y * rasterWidth + x]
        if (list == null) {
            list = ArrayList()
            raster[y * rasterWidth + x] = list
        }
        list.add(p)
        size++
    }

    fun getAll(bounds: BoundingBox): Iterable<LatLon> {
        val startX = max(0, min(longitudeToCellX(bounds.minLongitude), rasterWidth - 1))
        val startY = max(0, min(latitudeToCellY(bounds.minLatitude), rasterHeight - 1))
        val endX = max(0, min(longitudeToCellX(bounds.maxLongitude), rasterWidth - 1))
        val endY = max(0, min(latitudeToCellY(bounds.maxLatitude), rasterHeight - 1))
        val result = MultiIterable<LatLon>()
        for (y in startY..endY) {
            for (x in startX..endX) {
                val list = raster[y * rasterWidth + x]
                if (list != null) result.add(list)
            }
        }
        return result
    }

    fun remove(p: LatLon): Boolean {
        val x = longitudeToCellX(p.longitude)
        val y = latitudeToCellY(p.latitude)
        if (x < 0 || x >= rasterWidth || y < 0 || y >= rasterHeight) return false
        val list = raster[y * rasterWidth + x] ?: return false
        val result = list.remove(p)
        if (result) --size
        return result
    }

    private fun checkBounds(x: Int, y: Int) {
        require(x in 0 until rasterWidth) { "Longitude is out of bounds" }
        require(y in 0 until rasterHeight) { "Latitude is out of bounds" }
    }

    private fun longitudeToCellX(longitude: Double) =
        floor(normalizeLongitude(longitude - bbox.minLongitude) / cellSize).toInt()

    private fun latitudeToCellY(latitude: Double) =
        floor((latitude - bbox.minLatitude) / cellSize).toInt()
}
